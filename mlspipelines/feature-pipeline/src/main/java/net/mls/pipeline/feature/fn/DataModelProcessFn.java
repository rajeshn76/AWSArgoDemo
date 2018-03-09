package net.mls.pipeline.feature.fn;

import net.mls.pipeline.common.avro.BasicData;
import net.mls.pipeline.feature.avro.DataModel;
import net.mls.pipeline.feature.avro.IOSReview;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataModelProcessFn implements Function<IOSReview, DataModel> {

    private static Map<Integer, List<LocalDate>> releaseDates = new HashMap<>();
    static {
        try {
            InputStream is = DataModelProcessFn.class.getClassLoader().getResourceAsStream("releases.txt");

            Stream<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines();
            lines.map(line -> LocalDate.parse(line, DateTimeFormatter.ofPattern("M/d/yyyy")))
                    .forEach(DataModelProcessFn::addDate);
        } catch (Exception e) {

        }
    }
    @Override
    public DataModel apply(IOSReview review) {
        String str = review.getDate().toString();
        String upper = str.substring(0,1).toUpperCase() + str.substring(1);
        LocalDate date = LocalDate.parse(upper, DateTimeFormatter.ofPattern("MMM dd yyyy"));

        return new DataModel(review.getBody(), isAfterRelease(date), review.getVersion().toString(), mapSentiment(review.getRating().toString()));
    }

    private Boolean isAfterRelease(LocalDate date) {
        List<Boolean> withReleases = releaseDates.get(date.getYear()).stream()
                .map(release -> (date.isAfter(release) || date.isEqual(release)) &&
                        (date.isBefore(release.plusWeeks(1)) || date.isEqual(release.plusWeeks(1))))
                .filter(inRange -> inRange)
                .collect(Collectors.toList());
        return withReleases.size() > 0;
    }
    private Integer mapSentiment(String str) {
       Integer rating = Optional.ofNullable(Integer.parseInt(str)).orElse(0);
       if (rating >= 3) {
           return 1;
       } else return 0;

//        if(str.equalsIgnoreCase("neutral") || str.equalsIgnoreCase("positive")) {
//            return 1;
//        } else return 0;
    }
    private static void addDate(LocalDate date) {
        if(releaseDates.containsKey(date.getYear())) {
            releaseDates.get(date.getYear()).add(date);
        } else {
            releaseDates.put(date.getYear(), new ArrayList<>(Collections.singletonList(date)));
        }
    }
}
