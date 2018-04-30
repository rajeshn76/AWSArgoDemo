package net.mls.scala

import net.mls.stats.domain.ConfidenceIntervalMapper
import org.apache.commons.math3.distribution.{ChiSquaredDistribution, NormalDistribution}

import scala.math._

case class RateInfo(trials: Int, successes: Int) {
  val failures: Double = trials - successes
  val rate: Double = successes.toDouble / trials.toDouble

  def rate_adj(z: Double, adjType: AdjustmentType): Double = {
    val z2 = pow(z, 2)
    (successes + z2 / (2 * adjType.base)) / (trials + z2 / adjType.base)
  }

  def trials_adj(z: Double, adjType: AdjustmentType): Double = {
    val z2 = pow(z, 2)
    trials + z2 / adjType.base
  }

  override def toString = s"$successes/$trials"
}

sealed trait AdjustmentType {
  val base: Int
}

case object One extends AdjustmentType {
  override val base = 1
}

case object Two extends AdjustmentType {
  override val base = 2
}

trait PValue {
  val pValue: Double
  lazy val confidence = (1 - pValue) * 100
}

case class PValueFromZ(z: Double) extends PValue {
  lazy val pValue = 2 * (1 - new NormalDistribution().cumulativeProbability(z))

  override def toString = s"p-value=$pValue, confidence=$confidence, z=$z"
}

case class PValueFromChi(chi2: Double) extends PValue {
  lazy val pValue = 1 - new ChiSquaredDistribution(1).cumulativeProbability(chi2)

  override def toString = s"p-value=$pValue, confidence=$confidence, chi2=$chi2"
}
object RateStats {

  def pValueFromZ(t1: RateInfo, t2: RateInfo): PValue = {
    val p: Double = (t1.successes.toDouble + t2.successes.toDouble) / (t1.trials.toDouble + t2.trials.toDouble)
    val trials: Double = t1.trials + t2.trials
    val z: Double = (t1.rate - t2.rate) * math.sqrt((trials-1) / trials) /
      math.sqrt(p*(1-p)*(1/t1.trials.toDouble + 1 / t2.trials.toDouble))
    PValueFromZ(z)
  }

  def pValueFromChi(t1: RateInfo, t2: RateInfo): PValue = {
    val chi2: Double = pow((t1.successes*t2.failures - t1.failures*t2.successes), 2) * (t1.trials + t2.trials - 1) /
      (t1.trials * t2.trials *(t1.successes + t2.successes)*(t1.failures+t2.failures))
    PValueFromChi(chi2)
  }

//  def differenceConfidenceInterval(t1: RateInfo, t2: RateInfo, level: Double = .95, model: String, controlModel: String): ConfidenceIntervalMapper = {
//    val z: Double = new NormalDistribution().inverseCumulativeProbability(1.0 - (1-level) / 2)
//
//    val p_adj1: Double = t1.rate_adj(z, Two)
//    val p_adj2: Double = t2.rate_adj(z, Two)
//    val n_adj1: Double = t1.trials_adj(z, Two)
//    val n_adj2: Double = t2.trials_adj(z, Two)
//
//    val e: Double = z * math.sqrt(p_adj1 * (1-p_adj1) / n_adj1 + p_adj2 * (1-p_adj2) / n_adj2)
//    val p_diff: Double = p_adj1 - p_adj2
//    new ConfidenceIntervalMapper(p_diff, e, model, controlModel, t1)
//  }


  def confidenceInterval(t: RateInfo, level: Double = .95, model: String, controlModel: String): ConfidenceIntervalMapper = {
    val z: Double = new NormalDistribution().inverseCumulativeProbability(1.0 - (1-level) / 2)

    val p_adj: Double = t.rate_adj(z, One)
    val n_adj: Double = t.trials_adj(z, One)

    val e: Double = z * math.sqrt(p_adj * (1-p_adj) / n_adj)
    new ConfidenceIntervalMapper(p_adj, e, model, controlModel, t)
  }
}