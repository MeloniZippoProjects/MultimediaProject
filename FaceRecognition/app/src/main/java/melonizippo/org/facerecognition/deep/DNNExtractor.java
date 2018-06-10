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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

public class DNNExtractor {

	private Net net;
	private Size imgSize;
	private Scalar mean;
	
	public DNNExtractor(
		File protoTxt,
		File caffeModel
	)
	{
		net = readNetFromCaffe(protoTxt.getPath(), caffeModel.getPath());
        imgSize = new Size(Parameters.IMG_WIDTH, Parameters.IMG_HEIGHT);
		mean = new Scalar(91.4953, 103.8827, 131.0912);
	}

	public float[] extract(File image, String layer) {
		Mat img = imread(image.getPath());
		return extract(img, layer);
	}

    //todo: verify correctness of this implementation porting
    public float[] extract(Mat img, String layer)
	{
		//temp workaround to avoid runtime crashes
		//return new float[]{0.0f, 1.1f, 3.5f};

		//test
		Mat converted = new Mat();
		Imgproc.cvtColor(img, converted, Imgproc.COLOR_RGBA2BGR);

		//Size imgSize = new Size(224, 224);
		//Scalar mean = new Scalar(0);
		Mat inputBlob = blobFromImage(converted, 1.0, imgSize, mean, false, false); // Convert Mat to dnn::Blob image batch

		net.setInput(inputBlob, "data"); // set the network input

		List<Mat> outputBlobs = new ArrayList<>();
		net.forward(outputBlobs, layer); // compute output

		Mat prob = outputBlobs.get(0);

		float[] features = new float[(int) prob.total()];
		prob.get(0, 0, features);
		return features;
	}
}
