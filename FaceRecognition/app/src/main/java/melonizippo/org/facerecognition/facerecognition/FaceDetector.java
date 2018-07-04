package melonizippo.org.facerecognition.facerecognition;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import melonizippo.org.facerecognition.Parameters;

public class FaceDetector {

	private CascadeClassifier face_cascade;

	public FaceDetector(String haarcascadePath) {
		//init detector
		face_cascade = new CascadeClassifier();
		face_cascade.load(haarcascadePath);
	}

	private MatOfRect rectVector = new MatOfRect();
	public MatOfRect detect(Mat img)
	{
		//detect faces
		face_cascade.detectMultiScale(img, rectVector, Parameters.SCALE_FACTOR,
				Parameters.MIN_NEIGHBORS, Parameters.CANNY_PRUNING,
				Parameters.FACE_MIN_SIZE, Parameters.FACE_MAX_SIZE);
		return rectVector;
	}
}
