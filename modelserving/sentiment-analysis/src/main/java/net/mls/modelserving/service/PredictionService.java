package net.mls.modelserving.service;

import net.mls.modelserving.operation.LogisticRegressionOperation;
import net.mls.modelserving.operation.TensorFlowOperation;
import net.mls.modelserving.util.WordVocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tensorflow.Tensor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by char on 2/26/18.
 */
@RestController
public class PredictionService {

    public static final Float DROPOUT_KEEP_PROB_VALUE = new Float(1.0);
    public static final String DATA_IN = "data_in";
    public static final String DROPOUT_KEEP_PROB = "dropout_keep_prob";

    @Autowired
    private LogisticRegressionOperation op;

    @Autowired
    private WordVocabulary wordVocabulary;

    @Autowired
    private TensorFlowOperation tfop;

    @RequestMapping(value = "lreg", method = RequestMethod.POST)
    public String getLinearPrediction(@RequestBody String review) {
        return this.op.apply(review);
    }


    @RequestMapping(value = "tf", method = RequestMethod.POST)
    public String getTensorflowPrediction(@RequestBody String review) {
        int[][] tweetVector = wordVocabulary.vectorizeSentence(review);

        Map<String, Object> feeds = new HashMap<>();
        feeds.put(DATA_IN, tweetVector);
        feeds.put(DROPOUT_KEEP_PROB, DROPOUT_KEEP_PROB_VALUE);

        Tensor tensor = tfop.apply(feeds);

        float[][] resultMatrix = new float[12][2];
        tensor.copyTo(resultMatrix);

        return "Sentiment is " + (resultMatrix[0][1] > 0.5 ? "positive" : "negative");
    }
}
