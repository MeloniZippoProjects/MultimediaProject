package melonizippo.org.facerecognition.database;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class FaceData implements Serializable
{
    public Mat FaceMat;
    public float[] Features;

    //Bitmap conversion
    public Bitmap toBitmap()
    {
        Bitmap bitmap = Bitmap.createBitmap( FaceMat.cols(), FaceMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(FaceMat, bitmap);
        return bitmap;
    }

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
