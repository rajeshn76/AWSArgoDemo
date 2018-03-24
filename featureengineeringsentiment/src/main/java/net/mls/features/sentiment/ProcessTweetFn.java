package net.mls.features.sentiment;



import net.mls.features.sentiment.domain.BasicTweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ProcessTweetFn implements Function<String, String>, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessTweetFn.class);
    @Override
    public String apply(String line) {
        LOG.info("Input: "+ line);
        BasicTweet tweet = parse(line);
        String str = tweet.getDate().toString();

        LocalDateTime dateTime = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy"));

        String dataModel = String.format("%s,%b,%d,%s", tweet.getTweet(), SentimentAnalysisCommon.isAfterRelease(dateTime.toLocalDate()),
                dateTime.getHour(), mapSentimentFromStr(tweet.getSentiment().toString()));
        LOG.info("Output: "+ dataModel);

        return dataModel;
    }

    private Integer mapSentimentFromStr(String str) {
        if (str.equalsIgnoreCase("neutral") || str.equalsIgnoreCase("positive")) {
            return 1;
        } else if (str.equalsIgnoreCase("negative")) {
            return 0;
        } else throw new UnsupportedOperationException("Unable to map string '" + str + "' to 0 or 1");
    }

    private BasicTweet parse(String s) {
        String[] line = s.split(",");
        return new BasicTweet(line[0].trim(),line[1].trim(),line[2].trim(),line[4].trim());
    }
}
