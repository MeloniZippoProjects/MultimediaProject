package melonizippo.org.facerecognition.deep;


import android.util.Log;

import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

public class DNNExtractor {

	private Net net;
	private Size imgSize;
	private Scalar mean;
	private String layer;
	
	public DNNExtractor(
		File protoTxt,
		File caffeModel
	)
	{
		net = readNetFromCaffe(protoTxt.getPath(), caffeModel.getPath());
        imgSize = new Size(Parameters.IMG_WIDTH, Parameters.IMG_HEIGHT);
		mean = Parameters.MEAN;
		layer = Parameters.DEEP_LAYER;
	}

    //todo: verify correctness of this implementation porting


	private Mat converted = new Mat();
    public float[] extract(Mat img)
	{
		//temp workaround to avoid runtime crashes
		//return new float[]{0.0f, 1.1f, 3.5f};

		//when using resnet the mat must be bgr
		Imgproc.cvtColor(img, converted, Imgproc.COLOR_RGBA2RGB);
		converted.copyTo(img);

		//when using lightened the mat can be rgba (or bgra?)
		//img.copyTo(converted);

		Mat inputBlob = blobFromImage(converted, 1.0, imgSize, mean, false, false); // Convert Mat to dnn::Blob image batch

		net.setInput(inputBlob); // set the network input

		List<Mat> outputBlobs = new ArrayList<>();
		net.forward(outputBlobs, layer); // compute output

		Mat prob = outputBlobs.get(0);

		//this way we force rows and cols to 1x2048
		//also official tutorials do this (???)
		Mat featuresMat = prob.reshape(0, 1);
		float[] features = new float[(int) featuresMat.total()];

		featuresMat.get(0, 0, features);

		return normalizeVector(features);
	}


	private float[] normalizeVector(float[] features)
	{
		float norm = 0;
		for(float elem : features)
		{
			norm += elem*elem;
		}

		norm = (float)Math.sqrt(norm);

		float[] normalizedVector = new float[features.length];

		for(int i = 0; i < features.length; ++i)
		{
			normalizedVector[i] = features[i]/norm;
		}

		norm = 0;
		for(float elem : normalizedVector)
		{
			norm += elem*elem;
		}

		return normalizedVector;
	}
}
