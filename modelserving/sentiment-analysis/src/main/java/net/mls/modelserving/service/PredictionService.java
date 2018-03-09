package net.mls.modelserving.service;

import net.mls.modelserving.operation.LogisticRegressionOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by char on 2/26/18.
 */
@RestController
public class PredictionService {

    @Autowired
    private LogisticRegressionOperation op;

    @RequestMapping(value = "lreg", method = RequestMethod.POST)
    public String getPrediction(@RequestBody String review) {

        return this.op.apply(review);
    }
}
