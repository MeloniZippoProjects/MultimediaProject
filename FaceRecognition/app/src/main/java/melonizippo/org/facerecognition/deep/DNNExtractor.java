package melonizippo.org.facerecognition.deep;


import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

public class DNNExtractor {

	private Net net;
	
	public DNNExtractor(
		File protoTxt,
		File caffeModel
	)
	{
		net = readNetFromCaffe(protoTxt.getPath(), caffeModel.getPath());
	}

    //todo: verify correctness of this implementation porting


	private Mat converted = new Mat();
    public float[] extract(Mat img)
	{
		//when using resnet the mat must be rgb
		Imgproc.cvtColor(img, converted, Imgproc.COLOR_RGBA2RGB);

		Mat inputBlob = blobFromImage(converted, 1.0, Parameters.IMG_SIZE, Parameters.MEAN, false, false); // Convert Mat to dnn::Blob image batch

		net.setInput(inputBlob); // set the network input

		List<Mat> outputBlobs = new ArrayList<>();
		net.forward(outputBlobs, Parameters.DEEP_LAYER); // compute output

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
