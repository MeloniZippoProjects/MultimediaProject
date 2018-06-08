package melonizippo.org.facerecognition.deep;


import static org.opencv.dnn.Dnn.blobFromImage;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.dnn.Dnn.readNetFromCaffe;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.dnn.Net;

public class DNNExtractor {

	private Net net;
	private Size imgSize;
	
	public DNNExtractor(
		File protoTxt,
		File caffeModel
	)
	{
		net = readNetFromCaffe(protoTxt.getPath(), caffeModel.getPath());
        imgSize = new Size(Parameters.IMG_WIDTH, Parameters.IMG_HEIGHT);
	}

	public float[] extract(File image, String layer) {
		Mat img = imread(image.getPath());
		return extract(img, layer);
	}

    //todo: verify correctness of this implementation porting
    public float[] extract(Mat img, String layer)
	{
		//temp workaround to avoid runtime crashes
		return new float[]{0.0f, 1.1f, 3.5f};

//		Mat inputBlob = blobFromImage(img, 1.0, imgSize, null, false, false); // Convert Mat to dnn::Blob image batch
//
//		net.setInput(inputBlob, "data"); // set the network input
//
//		Mat prob = net.forward(layer); // compute output
//
//		float[] features = new float[(int) prob.total()];
//		prob.get(0, 0, features);
//		return features;
	}
}
