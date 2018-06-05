package melonizippo.org.facerecognition.algorithms.deep;

import static org.bytedeco.javacpp.opencv_dnn.blobFromImage;
import static org.bytedeco.javacpp.opencv_dnn.readNetFromCaffe;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.io.File;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_dnn.Net;

public class DNNExtractor {

	private Net net;
	private Size imgSize;
	
	public DNNExtractor() {		
		net = readNetFromCaffe(new File(Parameters.DEEP_PROTO).getPath(), new File(Parameters.DEEP_MODEL).getPath()); 
        imgSize = new Size(Parameters.IMG_WIDTH, Parameters.IMG_HEIGHT);
	}

	public float[] extract(File image, String layer) {
		Mat img = imread(image.getPath());
		return extract(img, layer);
	}

	public float[] extract(Mat img, String layer) {
		Mat inputBlob = blobFromImage(img, 1.0, imgSize, null, false, false); // Convert Mat to dnn::Blob image batch

		net.setInput(inputBlob, "data"); // set the network input
		
		Mat prob = net.forward(layer); // compute output
		
		float[] features = new float[(int) prob.total()];
		try (FloatPointer fp = new FloatPointer(prob.data())) {
			fp.get(features);
			return features;
		}
	}
}
