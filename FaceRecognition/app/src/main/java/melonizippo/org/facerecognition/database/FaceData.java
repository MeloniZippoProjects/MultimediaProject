package melonizippo.org.facerecognition.database;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class FaceData implements Serializable, Clusterable
{
    protected Mat faceMat = new Mat();
    protected float[] features;

    public FaceData() {}

    public FaceData(Mat faceMat, float[] features)
    {
        faceMat.copyTo(this.faceMat);
        this.features = features;
    }

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
        faceMat.copyTo(this.faceMat);
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

    public double getSimilarity(@NonNull FaceData queryData)
    {
        float[] queryVector = queryData.getFeatures();

        return scalarProduct(queryVector, getFeatures());
    }

    private static double scalarProduct(float[] a, float[] b)
    {
        double scalarProduct = 0;

        for(int i = 0; i < a.length; ++i)
            scalarProduct += a[i] * b[i];

        return scalarProduct;
    }

    private static double scalarProduct(double[] a, double[] b)
    {
        double scalarProduct = 0;

        for(int i = 0; i < a.length; ++i)
            scalarProduct += a[i] * b[i];

        return scalarProduct;
    }

    public static DistanceMeasure distanceMeasure = new DistanceMeasure() {
        @Override
        public double compute(double[] a, double[] b) throws DimensionMismatchException {
            return 1 - scalarProduct(a, b);
        }
    };

    @Override
    public double[] getPoint() {
        double[] point = new double[features.length];
        for(int i = 0; i < features.length; ++i)
            point[i] = features[i];

        return point;
    }
}
