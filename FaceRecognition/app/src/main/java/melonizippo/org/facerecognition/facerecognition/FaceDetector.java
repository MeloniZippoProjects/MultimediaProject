package melonizippo.org.facerecognition.facerecognition;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import melonizippo.org.facerecognition.deep.Parameters;

public class FaceDetector {

	private CascadeClassifier face_cascade;

	private static Size minSize = Parameters.FACE_MIN_SIZE;
	private static Size maxSize = Parameters.FACE_MAX_SIZE;


	//TODO
	public FaceDetector(String haarcascadePath) {
		//init detector
		face_cascade = new CascadeClassifier();
		face_cascade.load(haarcascadePath);
	}

	//TODO
	private MatOfRect rectVector = new MatOfRect();
	public MatOfRect detect(Mat img)
	{
		//detect faces
		double scaleFactor = Parameters.SCALE_FACTOR;
		int minNeighbors = Parameters.MIN_NEIGHBORS;
		int cannyPruning = Parameters.CANNY_PRUNING;
		face_cascade.detectMultiScale(img, rectVector, scaleFactor, minNeighbors, cannyPruning, minSize, maxSize);
		return rectVector;
	}
}
