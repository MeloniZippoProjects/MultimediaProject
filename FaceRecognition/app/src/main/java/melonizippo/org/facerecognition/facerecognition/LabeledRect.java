package melonizippo.org.facerecognition.facerecognition;

import org.opencv.core.Rect;

public class LabeledRect {

    private Rect rect;
    private String label;
    private Double confidence;

    public LabeledRect(Rect rect, String label, Double confidence)
    {
        this.rect = rect;
        this.label = label;
        this.confidence = confidence;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
