package net.mls.performance.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;

@Table(value = "model_count")
@Builder
@Value
@AllArgsConstructor
public class ModelCount {
    @PrimaryKeyColumn(name = "model", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String model;

    @PrimaryKeyColumn(name = "converted", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String converted;

    @PrimaryKeyColumn(name = "time", ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date time;

    @Column("count")
    private Float count;

    @Column("sum")
    private Float sum;
}
