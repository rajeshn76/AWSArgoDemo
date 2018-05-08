package net.mls.modelserving;

import java.util.List;

public class PredictionsRequest {
    List<String> texts;

    public List<String> getTexts() {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }
}
