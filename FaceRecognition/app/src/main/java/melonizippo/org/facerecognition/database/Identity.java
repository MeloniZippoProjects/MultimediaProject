package melonizippo.org.facerecognition.database;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Identity implements Serializable, Comparable<Identity>
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

    @Override
    public boolean equals(Object other)
    {
        if(other == null) return false;
        if(other == this) return true;
        if (!(other instanceof Identity))return false;
        Identity otherIdentity = (Identity) other;
        return otherIdentity.label.matches(this.label);
    }

    @Override
    public int compareTo(@NonNull Identity o)
    {
        return this.label.compareTo(o.label);
    }
}
