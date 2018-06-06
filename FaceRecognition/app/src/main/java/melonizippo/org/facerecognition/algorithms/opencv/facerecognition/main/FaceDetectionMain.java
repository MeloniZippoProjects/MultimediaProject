package melonizippo.org.facerecognition.algorithms.opencv.facerecognition.main;

import static org.bytedeco.javacpp.opencv_highgui.destroyAllWindows;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.Parameters;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.tools.BoundingBox;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.RectVector;

public class FaceDetectionMain {

	public static void main(String[] args) {
		File image = new File("data/girl.jpg");
		Mat img = imread(image.getAbsolutePath());

		// Face
		FaceDetector faceDetection = new FaceDetector(Parameters.HAAR_CASCADE_FRONTALFACE);
		RectVector faces = faceDetection.detect(img, Parameters.FACE_MIN_SIZE, Parameters.FACE_MAX_SIZE);
		for (int i = 0; i < faces.size(); i++) {
			BoundingBox.highlight(img, faces.get(i));
		}

		/*FaceDetector eyesDetection = new FaceDetector(Parameters.HAAR_CASCADE_EYES);
		RectVector eyes = eyesDetection.detect(img, Parameters.EYE_MIN_SIZE, Parameters.EYE_MAX_SIZE);
		for (int i = 0; i < eyes.size(); i++) {
			BoundingBox.highlight(img, eyes.get(i));
		}


		FaceDetector mouthDetection = new FaceDetector(Parameters.HAAR_CASCADE_MOUTH);
		RectVector mouth = mouthDetection.detect(img, Parameters.MOUTH_MIN_SIZE, Parameters.MOUTH_MAX_SIZE);
		for (int i = 0; i < mouth.size(); i++) {
			BoundingBox.highlight(img, mouth.get(i));
		}

		/*FaceDetector noseDetection = new FaceDetector(Parameters.HAAR_CASCADE_NOSE);
		RectVector nose = noseDetection.detect(img, Parameters.NOSE_MIN_SIZE, Parameters.NOSE_MAX_SIZE);
		for (int i = 0; i < nose.size(); i++) {
			BoundingBox.highlight(img, nose.get(i));
		}

		*/

		BoundingBox.imshow("Face Detection", img);
		waitKey();
		destroyAllWindows();
	}

}