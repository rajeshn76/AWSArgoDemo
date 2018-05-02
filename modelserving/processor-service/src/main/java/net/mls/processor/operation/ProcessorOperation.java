package net.mls.processor.operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.function.Function;

@Service("processor-op")
public class ProcessorOperation implements Function<String, String> {

    private String baseUrl;


    @Override
    public String apply(String str) {

        return str+"!!";
    }
}
