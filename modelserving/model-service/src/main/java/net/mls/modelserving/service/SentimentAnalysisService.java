package net.mls.modelserving.service;

import net.mls.modelserving.operation.LogisticRegressionOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentimentAnalysisService {

    @Autowired
    private LogisticRegressionOperation op;

    @RequestMapping(value = "lreg", method = RequestMethod.POST)
    public String getPrediction(@RequestBody String review) {

        return this.op.apply(review);
    }
}
