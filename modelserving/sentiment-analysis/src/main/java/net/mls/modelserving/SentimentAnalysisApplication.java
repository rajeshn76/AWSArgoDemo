package net.mls.modelserving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//import javax.servlet.*;

@SpringBootApplication
@ComponentScan("net.mls.modelserving")
public class SentimentAnalysisApplication {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(SentimentAnalysisApplication.class, args);
    }
}

//@Order
//@Component
//class ApplicationCORSFilter implements Filter {
//    public void doFilter()
//}
