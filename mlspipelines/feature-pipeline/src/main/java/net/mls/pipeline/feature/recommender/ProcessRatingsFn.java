package net.mls.pipeline.feature.recommender;

import java.io.Serializable;
import java.util.function.Function;

public class ProcessRatingsFn implements Function<String, String>, Serializable {
    @Override
    public String apply(String s) {
        String[] rawRating = s.split("\t");
        // user, product, rating
        return String.format("%s,%s,%s", rawRating[0], rawRating[1], rawRating[2]);
    }
}
