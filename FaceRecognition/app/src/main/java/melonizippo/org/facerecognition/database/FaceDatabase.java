package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class FaceDatabase implements Serializable
{
    public Set<Identity> knownIdentities = new ConcurrentSkipListSet<>();
    public Set<FaceData> uncategorizedData = new ConcurrentSkipListSet<>();

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
