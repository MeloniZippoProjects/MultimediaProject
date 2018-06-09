package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.List;

public class IdentityEntry implements Serializable
{
    public String label;
    public boolean authorized;
    public List<FaceData> identityDataset;
}
