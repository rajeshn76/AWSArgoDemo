package net.mls.stats

import java.net.InetAddress

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.spark.connector.cql.{AuthConf, NoAuthConf, PasswordAuthConf}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.log4j.{Level, Logger}

import scala.util.Try

final class StatsPipelineSettings(conf: Option[Config] = None) extends Serializable {

  Logger.getRootLogger setLevel(Level.ERROR)
  Logger.getLogger("org") setLevel(Level.WARN)
  Logger.getLogger("akka") setLevel(Level.WARN)

  val rootConf = conf match {
    case Some(c) => c.withFallback(ConfigFactory.load())
    case _ => ConfigFactory.load()
  }

  val localAddress: String = Try(InetAddress.getLocalHost.getHostAddress) getOrElse "localhost"

  protected val spark = rootConf.getConfig("spark")
  protected val cassandra = rootConf.getConfig("cassandra")
  protected val kafka = rootConf.getConfig("kafka")
  protected val statsPipeline = rootConf.getConfig("stats-pipeline")

  val sparkMaster = withFallback[String](Try(spark.getString("master")),
    "spark.master") getOrElse "local[2]"

  val sparkCleanerTtl = withFallback[Int](Try(spark.getInt("cleaner.ttl")),
    "spark.cleaner.ttl") getOrElse (3600 * 2)

  val sparkStreamingBatchInterval = withFallback[Int](Try(spark.getInt("streaming.batch.interval")),
    "spark.streaming.batch.interval") getOrElse 1000

  val sparkCheckpointDir = statsPipeline.getString("spark.checkpoint.dir")

  val cassandraHosts = withFallback[String](Try(cassandra.getString("connection.host")),
    "spark.cassandra.connection.host") getOrElse localAddress

  val cassandraAuthUsername = Try(cassandra.getString("auth.username")).toOption
      .orElse(sys.props.get("spark.cassandra.auth.username"))

  val cassandraAuthPassword = Try(cassandra.getString("auth.password")).toOption
    .orElse(sys.props.get("spark.cassandra.auth.password"))

  val cassandraAuth: AuthConf = {
    val credentials = for (
      username <- cassandraAuthUsername;
      password <- cassandraAuthPassword
    ) yield (username, password)
    credentials match {
      case Some((user, password)) => PasswordAuthConf(user, password)
      case None => NoAuthConf
    }
  }

  val cassandraRpcPort = withFallback[Int](Try(cassandra.getInt("connection.rpc.port")),
    "spark.cassandra.connection.rpc.port") getOrElse 9160

  val cassandraNativePort = withFallback[Int](Try(cassandra.getInt("connection.native.port")),
    "spark.cassandra.connection.native.port") getOrElse 9042

  val cassandraKeepAlive = withFallback[Int](Try(cassandra.getInt("connection.keep-alive")),
    "spark.cassandra.connection.keep_alive_ms") getOrElse 1000

  val cassandraRetryCount = withFallback[Int](Try(cassandra.getInt("connection.query.retry-count")),
    "spark.cassandra.query.retry.count") getOrElse 10

  val cassandraConnectionReconnectDelayMin = withFallback[Int](Try(cassandra.getInt("connection.reconnect-delay.min")),
    "spark.cassandra.connection.reconnection_delay_ms.min") getOrElse 1000

  val cassandraConnectionReconnectDelayMax = withFallback[Int](Try(cassandra.getInt("reconnect-delay.max")),
    "spark.cassandra.connection.reconnection_delay_ms.max") getOrElse 60000


  val cassandraReadPageRowSize = withFallback[Int](Try(cassandra.getInt("read.page.row.size")),
    "spark.cassandra.input.page.row.size") getOrElse 1000

  val cassandraReadConsistencyLevel: ConsistencyLevel = ConsistencyLevel.valueOf(
    withFallback[String](Try(cassandra.getString("read.consistency.level")),
    "spark.cassandra.input.consistency.level") getOrElse ConsistencyLevel.LOCAL_ONE.name)

  val cassandraReadSplitSize = withFallback[Long](Try(cassandra.getLong("read.split.size")),
    "spark.cassandra.input.split.size") getOrElse 100000


  val cassandraWriteParallelismLevel = withFallback[Int](Try(cassandra.getInt("write.concurrent.writes")),
    "spark.cassandra.output.concurrent.writes") getOrElse 5

  val cassandraWriteBatchSizeBytes = withFallback[Int](Try(cassandra.getInt("write.batch.size.bytes")),
    "spark.cassandra.output.batch.size.bytes") getOrElse 64 * 1024

  private val cassandraWriteBatchSizeRows = withFallback[String](Try(cassandra.getString("write.batch.size.rows")),
    "spark.cassandra.output.batch.size.rows") getOrElse "auto"

  val cassandraWriteBatchRowSize: Option[Int] = {
    val NumberPattern = "([0-9]+)".r
    cassandraWriteBatchSizeRows match {
      case "auto" => None
      case NumberPattern(x) => Some(x.toInt)
      case other =>
          throw new IllegalArgumentException(
            s"Invalid value for 'cassandra.output.batch.size.rows': $other. Number or 'auto' expected")
    }
  }

  val cassandraWriteConsistencyLevel: ConsistencyLevel = ConsistencyLevel.valueOf(
    withFallback[String](Try(cassandra.getString("write.consistency.level")),
      "spark.cassandra.output.consistency.level") getOrElse ConsistencyLevel.LOCAL_ONE.name)

  val cassandraDefaultMeasuredInsertsCount: Int = 128

  val kafkaHosts = kafka.getString("hosts")
  val kafkaZKQuorum = kafka.getString("zookeeper.connection")
  val kafkaGroupId = kafka.getString("group.id")
  val kafkaTopicRaw = kafka.getString("topic.raw")
  val kafkaEncoderFqcn = kafka.getString("encoder.fqcn")
  val kafkaDecoderFqcn = kafka.getString("decoder.fqcn")
  val kafkaPartitioner = kafka.getString("partitioner.fqcn")
  val kafkaBatchSendSize = kafka.getString("batch.send.size")

  val appName = statsPipeline.getString("app-name")
  val cassandraKeyspace = statsPipeline.getString("cassandra.keyspace")
  val cassandraTableRaw = statsPipeline.getString("cassandra.table.raw")
  val experimentEventParser = statsPipeline.getString("experiment-event-parser")

  def withFallback[T](env: Try[T], key: String): Option[T] = env match {
    case null => None
    case value => value.toOption
  }
}
