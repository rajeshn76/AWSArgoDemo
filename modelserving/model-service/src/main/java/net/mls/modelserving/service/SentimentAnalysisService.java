package net.mls.modelserving.service;

import net.mls.modelserving.PredictionsRequest;
import net.mls.modelserving.PredictionsResponse;
import net.mls.modelserving.SentimentPrediction;
import net.mls.modelserving.operation.LogisticRegressionOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SentimentAnalysisService {

    @Autowired
    private LogisticRegressionOperation op;


    @RequestMapping(value = "lreg", method = RequestMethod.POST)
    public String getPrediction(@RequestBody String review) {

        return "Sentiment is " + this.op.apply(review).getPrediction();
    }


    @RequestMapping(value = "lreg/list", method = RequestMethod.POST)
    public PredictionsResponse getPredictionOfList(@RequestBody PredictionsRequest reviews) {
        List<SentimentPrediction> predictions = reviews.getTexts().stream()
                .map(review -> this.op.apply(review))
                .collect(Collectors.toList());

        PredictionsResponse resp = new PredictionsResponse();
        resp.setPredictions(predictions);
        return resp;
    }

}
