package net.mls.features.sentiment;

import net.mls.features.sentiment.domain.IOSReview;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ProcessReviewFn implements Function<String, String>, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessReviewFn.class);
    @Override
    public String apply(String s) {
        LOG.info("Input: "+ s);
        IOSReview review = parse(s);
        String str = review.getDate().toString();

        String upper = str.substring(0, 1).toUpperCase() + str.substring(1);
        LocalDate date = LocalDate.parse(upper, DateTimeFormatter.ofPattern("MMM dd yyyy"));

        String dataModel = String.format("%s,%b,%s,%s", review.getBody(), SentimentAnalysisCommon.isAfterRelease(date),
                review.getVersion().toString(), mapSentimentFromNum(review.getRating().toString()));
        LOG.info("Output: "+ dataModel);
        return dataModel;
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
