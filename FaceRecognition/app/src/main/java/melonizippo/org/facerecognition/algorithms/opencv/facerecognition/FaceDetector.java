package melonizippo.org.facerecognition.algorithms.opencv.facerecognition;

import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

public class FaceDetector {

	private CascadeClassifier face_cascade;

	//TODO
	public FaceDetector(String haarcascadePath) {
		//init detector
		face_cascade = new CascadeClassifier();
		face_cascade.load(haarcascadePath);
	}

	//TODO
	public RectVector detect(Mat img, Size minSize, Size maxSize) {
		//detect faces
		RectVector rectVector = new RectVector();
		face_cascade.detectMultiScale(img, rectVector, 1.2, 3, CV_HAAR_DO_CANNY_PRUNING, minSize, maxSize);
		return rectVector;
	}
}
