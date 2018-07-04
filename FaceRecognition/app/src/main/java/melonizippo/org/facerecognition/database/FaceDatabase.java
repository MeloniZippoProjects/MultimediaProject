package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class FaceDatabase implements Serializable
{
    public Set<Identity> knownIdentities = new ConcurrentSkipListSet<>();
    public Map<Integer, FaceData> unclassifiedFaces = new ConcurrentHashMap<>();
    public AtomicInteger nextMapId = new AtomicInteger(0);

    public int getSampleCount()
    {
        int sum = 0;
        for (Identity identity : knownIdentities)
        {
            sum += identity.identityDataset.size();
        }
        sum += unclassifiedFaces.size();
        return sum;
    }
}
