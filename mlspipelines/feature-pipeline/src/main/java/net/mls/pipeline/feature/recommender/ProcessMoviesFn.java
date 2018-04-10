package net.mls.pipeline.feature.recommender;

import java.io.Serializable;
import java.util.function.Function;

public class ProcessMoviesFn implements Function<String, String>, Serializable {
    @Override
    public String apply(String s) {
        String[] line = s.split("\\|");
        // movie:: id,name
        return String.format("%s,%s", line[0], line[1].replace(",", ""));
    }
}
