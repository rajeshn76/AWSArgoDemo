package net.mls.pipeline.feature.sentimentanalysis;

import net.mls.pipeline.feature.avro.BasicTweet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ProcessTweetFn implements Function<String, String>, Serializable {

    @Override
    public String apply(String line) {

        BasicTweet tweet = parse(line);
        String str = tweet.getDate().toString();

        LocalDateTime dateTime = LocalDateTime.parse(str, DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss z yyyy"));


        return String.format("%s,%b,%d,%s", tweet.getTweet(), SentimentAnalysisCommon.isAfterRelease(dateTime.toLocalDate()),
                dateTime.getHour(), mapSentimentFromStr(tweet.getSentiment().toString()));
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
