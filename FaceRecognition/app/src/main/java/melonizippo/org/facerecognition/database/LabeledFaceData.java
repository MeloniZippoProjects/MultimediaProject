package melonizippo.org.facerecognition.database;

public class LabeledFaceData extends FaceData {

    private Identity label;

    public LabeledFaceData(FaceData faceData, Identity label)
    {
        this.features = faceData.features;
        this.faceMat = faceData.faceMat;
        this.label = label;
    }

    public Identity getLabel() {
        return label;
    }

    public void setLabel(Identity label) {
        this.label = label;
    }
}
