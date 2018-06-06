package melonizippo.org.facerecognition.facerecognition;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetector {

	public static final int CV_HAAR_DO_CANNY_PRUNING = 1;

	private CascadeClassifier face_cascade;

	//TODO
	public FaceDetector(String haarcascadePath) {
		//init detector
		face_cascade = new CascadeClassifier();
		face_cascade.load(haarcascadePath);
	}

	//TODO
	public MatOfRect detect(Mat img, Size minSize, Size maxSize) {
		//detect faces
		MatOfRect rectVector = new MatOfRect();
		face_cascade.detectMultiScale(img, rectVector, 1.2, 3, CV_HAAR_DO_CANNY_PRUNING, minSize, maxSize);
		return rectVector;
	}
}
