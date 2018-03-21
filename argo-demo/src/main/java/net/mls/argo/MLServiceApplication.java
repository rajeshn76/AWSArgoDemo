package net.mls.argo;

import net.mls.argo.util.WorkflowConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("net.mls.argo")
public class MLServiceApplication {

    @Bean
    public WorkflowConfig config() {
        return new WorkflowConfig();
    }

    public static void main(String[] args) {
        SpringApplication.run(MLServiceApplication.class, args);
    }
}
