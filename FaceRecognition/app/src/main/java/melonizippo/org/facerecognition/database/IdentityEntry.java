package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.List;

public class IdentityEntry implements Serializable
{
    public String Label;
    public boolean Authorized;
    public List<FaceData> IdentityDataset;
}
