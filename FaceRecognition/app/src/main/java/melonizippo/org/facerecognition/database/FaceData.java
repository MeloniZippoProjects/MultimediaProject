package melonizippo.org.facerecognition.database;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.opencv.core.Mat;

class FaceData implements Serializable
{
    public Mat FaceMat;
    public float[] Features;

    //Custom serialization code

    private void writeObject(ObjectOutputStream oos) throws IOException
    {
        FaceDataSerialization fds = new FaceDataSerialization();
        fds.Features = Features;
        fds.serializeMat(FaceMat);

        oos.writeObject(fds);
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException
    {
        FaceDataSerialization fds = (FaceDataSerialization) ois.readObject();
        Features = fds.Features;
        FaceMat = fds.deserializeMat();
    }
}
