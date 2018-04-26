package net.mls;

import net.mls.performance.config.CassandraConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan({"net.mls.performance.collector", "net.mls.performance"})
@Import(CassandraConfig.class)
public class PerformanceCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceCollectorApplication.class, args);
    }
}

