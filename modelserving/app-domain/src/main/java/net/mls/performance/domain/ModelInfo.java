package net.mls.performance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;

@Table(value = "model")
@Builder
@Value
@AllArgsConstructor
public class ModelInfo {
    @PrimaryKey("model_id")
    private String model;

    @Column("type")
    private String modelType;

    @Column("dt_created")
    private Date time;

    @Column("data_path")
    private String dataPath;

    @Column("features_path")
    private String featuresPath;

    @Column("model_path")
    private String modelPath;
}