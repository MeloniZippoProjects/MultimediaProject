package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FaceDatabase implements Serializable
{
    public List<IdentityEntry> knownIdentities = new ArrayList<>();
    public List<FaceData> uncategorizedData = new ArrayList<>();

    //todo: still discussed about
    //public List<IdentityEntry> SuggestedIdentities;

    //todo: implement this
    public List<IdentityEntry> buildSuggestedIdentities()
    {
        return new ArrayList<>();
    }
}
