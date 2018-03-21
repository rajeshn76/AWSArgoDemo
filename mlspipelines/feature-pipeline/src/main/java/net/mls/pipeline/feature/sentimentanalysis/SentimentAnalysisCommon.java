package net.mls.pipeline.feature.sentimentanalysis;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SentimentAnalysisCommon {
    private final static Map<Integer, List<LocalDate>> dates = new HashMap<>();

    static {
        try {
            InputStream is = SentimentAnalysisCommon.class.getClassLoader().getResourceAsStream("releases.txt");

            Stream<String> lines = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines();
            lines.map(line -> LocalDate.parse(line, DateTimeFormatter.ofPattern("M/d/yyyy")))
                    .forEach(SentimentAnalysisCommon::addDate);
        } catch (Exception e) {

        }
    }

    private static void addDate(LocalDate date) {
        if (dates.containsKey(date.getYear())) {
            dates.get(date.getYear()).add(date);
        } else {
            dates.put(date.getYear(), new ArrayList<>(Collections.singletonList(date)));

        }

    }

    public static Boolean isAfterRelease(LocalDate date) {
        List<Boolean> withReleases = dates.get(date.getYear()).stream()
                .map(release -> (date.isAfter(release) || date.isEqual(release)) &&
                        (date.isBefore(release.plusWeeks(1)) || date.isEqual(release.plusWeeks(1))))
                .filter(inRange -> inRange)
                .collect(Collectors.toList());
        return withReleases.size() > 0;
    }






}