package net.mls.pipeline.feature.sentimentanalysis;

import net.mls.pipeline.feature.avro.IOSReview;
import org.apache.beam.sdk.repackaged.org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ProcessReviewFn implements Function<String, String>, Serializable {


    @Override
    public String apply(String s) {
        IOSReview review = parse(s);
        String str = review.getDate().toString();

        String upper = str.substring(0, 1).toUpperCase() + str.substring(1);
        LocalDate date = LocalDate.parse(upper, DateTimeFormatter.ofPattern("MMM dd yyyy"));
        return String.format("%s,%b,%s,%s", review.getBody(), SentimentAnalysisCommon.isAfterRelease(date),
                review.getVersion().toString(), mapSentimentFromNum(review.getRating().toString()));
    }


    private Integer mapSentimentFromNum(String str) {
        if (StringUtils.isNumeric(str)) {
            Integer rating = Integer.parseInt(str);
            if (rating >= 3) {
                return 1;
            } else return 0;
        } else throw new UnsupportedOperationException("Unable to map string '" + str + "' to 0 or 1");
    }

    private IOSReview parse(String s) {
        String[] line = s.split(",");
        return new IOSReview(line[0].trim(), line[3].trim(), line[2].trim(), line[1].trim());
    }
}
