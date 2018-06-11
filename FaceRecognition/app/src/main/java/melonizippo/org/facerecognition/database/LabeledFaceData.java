package melonizippo.org.facerecognition.database;

import android.media.FaceDetector;

public class LabeledFaceData extends FaceData {

    private String label;

    public LabeledFaceData(FaceData faceData, String label)
    {
        this.features = faceData.features;
        this.faceMat = faceData.faceMat;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
