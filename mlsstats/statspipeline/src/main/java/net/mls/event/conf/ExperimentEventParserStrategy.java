package net.mls.event.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mls.event.ExperimentEvent;

import java.io.IOException;

public enum ExperimentEventParserStrategy implements  IExperimentEventParser{
    SIMPLE_EVENT {
        @Override
        public ExperimentEvent parse(String payload) {
            try {
                return mapper.readValue(payload, ExperimentEvent.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }, WEB_EVENT{
        @Override
        public ExperimentEvent parse(String payload) {
            throw new RuntimeException("Incomplete parser implementation");
        }
    };

    private final static ObjectMapper mapper = new ObjectMapper();
}
