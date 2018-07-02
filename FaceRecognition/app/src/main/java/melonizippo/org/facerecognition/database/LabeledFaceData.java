package melonizippo.org.facerecognition.database;

public class LabeledFaceData extends FaceData {

    private Identity identityLabel;

    public LabeledFaceData(FaceData faceData, Identity identityLabel)
    {
        this.features = faceData.features;
        this.faceMat = faceData.faceMat;
        this.identityLabel = identityLabel;
    }

    public Identity getIdentityLabel() {
        return identityLabel;
    }

    public void setIdentityLabel(Identity identityLabel) {
        this.identityLabel = identityLabel;
    }
}
