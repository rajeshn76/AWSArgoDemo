package net.mls.argo.template.structure;


public final class S3 {
    public String endpoint;
    public String bucket;
    public String key;
    public Secret accessKeySecret;
    public Secret secretKeySecret;

    public S3(String endpoint, String bucket, String key, Secret accessKeySecret, Secret secretKeySecret) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.key = key;
        this.accessKeySecret = accessKeySecret;
        this.secretKeySecret = secretKeySecret;
    }
}
