package net.mls.pipeline.common.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class S3Client {


    private static final Logger LOG = LoggerFactory.getLogger(S3Client.class);
    private static Config conf = ConfigFactory.load();

    private static AWSCredentials credentials = new BasicAWSCredentials(conf.getString("s3Config.accessKey"), conf.getString("s3Config.secretKey"));

    private static ClientConfiguration clientConfig = new ClientConfiguration();

    private static AmazonS3Client client = new AmazonS3Client(credentials, clientConfig);

    static {
        clientConfig.setProtocol(Protocol.HTTP);
        client.setEndpoint(conf.getString("s3Config.endpoint"));
    }

    public static void uploadFromString(String bucket, String path, String str) throws IOException {
        InputStream is = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8.name()));
        client.putObject(bucket, path, is, new ObjectMetadata());
    }

    public static InputStream readFromString(String bucket, String file) throws IOException {
        S3Object obj = client.getObject(new GetObjectRequest(bucket, file));
        return obj.getObjectContent();
    }

    public static void upload(String bucket, String path, File file) throws IOException {
        InputStream is = new FileInputStream(file);

        client.putObject(bucket, path, is, new ObjectMetadata());

        AccessControlList acl = client.getObjectAcl(bucket, path);
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
        client.setObjectAcl(bucket, path, acl);
    }
}
