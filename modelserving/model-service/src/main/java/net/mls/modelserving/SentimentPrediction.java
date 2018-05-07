package net.mls.modelserving;

/**
 * Created by char on 5/3/18.
 */
public class SentimentPrediction {
    String text;
    String prediction;

    public SentimentPrediction(String text, String prediction) {
        this.text = text;
        this.prediction = prediction;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SentimentPrediction that = (SentimentPrediction) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;
        return prediction != null ? prediction.equals(that.prediction) : that.prediction == null;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (prediction != null ? prediction.hashCode() : 0);
        return result;
    }
}
