package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FaceDatabase implements Serializable
{
    public List<Identity> knownIdentities = new ArrayList<>();
    public List<FaceData> uncategorizedData = new ArrayList<>();

    //todo: still discussed about
    //public List<Identity> SuggestedIdentities;

    //todo: implement this
    public List<Identity> buildSuggestedIdentities()
    {
        return new ArrayList<>();
    }

    public int getSampleCount()
    {
        int sum = 0;
        for (Identity identity : knownIdentities)
        {
            sum += identity.identityDataset.size();
        }
        return sum;
    }
}
