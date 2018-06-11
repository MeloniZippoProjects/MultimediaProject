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
    protected Mat faceMat;
    protected float[] features;

    //Bitmap conversion
    public Bitmap toBitmap()
    {
        Bitmap bitmap = Bitmap.createBitmap( faceMat.cols(), faceMat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(faceMat, bitmap);
        return bitmap;
    }

    //Custom serialization code
    private void writeObject(ObjectOutputStream oos) throws IOException
    {
        FaceDataSerialization fds = new FaceDataSerialization();
        fds.features = features;
        fds.serializeMat(faceMat);

        oos.writeObject(fds);
    }

    public Mat getFaceMat() {
        return faceMat;
    }

    public void setFaceMat(Mat faceMat) {
        this.faceMat = faceMat;
    }

    public float[] getFeatures() {
        return features;
    }

    public void setFeatures(float[] features) {
        this.features = features;
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException
    {
        FaceDataSerialization fds = (FaceDataSerialization) ois.readObject();
        features = fds.features;
        faceMat = fds.deserializeMat();
    }
}
