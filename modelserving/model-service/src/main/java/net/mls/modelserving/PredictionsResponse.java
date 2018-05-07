package net.mls.modelserving;

import java.util.List;


public class PredictionsResponse {
    List<SentimentPrediction> predictions;

    public List<SentimentPrediction> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<SentimentPrediction> predictions) {
        this.predictions = predictions;
    }
}
