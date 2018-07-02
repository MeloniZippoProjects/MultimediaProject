package melonizippo.org.facerecognition.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Identity implements Serializable
{
    public String label;
    public boolean authorized;
    public List<FaceData> identityDataset;

    public void filterDuplicatesFromDataset()
    {
        for(int i = 0; i < identityDataset.size(); i++)
        {
            FaceData referenceFD = identityDataset.get(i);
            List<FaceData> toRemove = new ArrayList<>();
            for(FaceData fd : identityDataset.subList(i+1, identityDataset.size()))
            {
                if(referenceFD.getSimilarity(fd) == 1)
                    toRemove.add(fd);
            }
            identityDataset.removeAll(toRemove);
        }
    }
}
