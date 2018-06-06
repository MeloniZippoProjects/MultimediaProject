package melonizippo.org.facerecognition.algorithms.opencv.facerecognition;

import java.io.File;

import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.Parameters;
import melonizippo.org.facerecognition.algorithms.opencv.facerecognition.VideoFaceRecognizer;

public class VideoFaceRecognitionMain {
	
	public static void main(String[] args) throws Exception {
		VideoFaceRecognizer main = new VideoFaceRecognizer(Parameters.HAAR_CASCADE_FRONTALFACE, Parameters.STORAGE_FILE);
		main.initVideo(new File(Parameters.VIDEO_PATH), Parameters.DEST_VIDEO);
		main.analyzeVideo();
		main.closeRecorder();
	}

}