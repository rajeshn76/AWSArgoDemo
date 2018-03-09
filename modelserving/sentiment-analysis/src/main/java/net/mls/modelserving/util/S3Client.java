package net.mls.modelserving.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;

import java.io.File;

/**
 * Created by char on 2/26/18.
 */
public final class S3Client {
    private AmazonS3Client client = null;

    public S3Client(String accessKey, String secretKey, String endpoint) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        client = new AmazonS3Client(credentials, clientConfiguration);
        clientConfiguration.setProtocol(Protocol.HTTP);
        client.setEndpoint(endpoint);
    }

    public File download(String bucket, String output) {
        String path = System.getProperty("java.io.tmpdir") + output.substring(output.lastIndexOf('/'));
        File temp = new File(path);
        client.getObject(new GetObjectRequest(bucket, output), temp);
        return temp;
    }

}
