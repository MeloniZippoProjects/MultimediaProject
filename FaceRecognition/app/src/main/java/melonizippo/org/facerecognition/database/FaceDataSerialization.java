package melonizippo.org.facerecognition.database;

import org.opencv.core.Mat;

import java.io.Serializable;

public class FaceDataSerialization implements Serializable
{
    public int columns;
    public int rows;
    public int type;
    public byte[] bytes;

    public float[] features;

    public void serializeMat(Mat mat)
    {
        columns = mat.cols();
        rows = mat.rows();
        type = mat.type();
        bytes = new byte[(int) (mat.total() * mat.elemSize())];
        mat.get(0,0, bytes);
    }

    public Mat deserializeMat()
    {
        Mat mat = new Mat(rows, columns, type);
        mat.put(0,0, bytes);
        return mat;
    }
}
