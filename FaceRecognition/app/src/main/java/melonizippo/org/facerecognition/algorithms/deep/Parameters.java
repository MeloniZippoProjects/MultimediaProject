package melonizippo.org.facerecognition.algorithms.deep;

import java.io.File;

import org.bytedeco.javacpp.opencv_core.Size;

public class Parameters {
	
	//DEEP parameters
	public static final String DEEP_PROTO = "data/caffe/LightenedCNN_A_deploy.prototxt";
	public static final String DEEP_MODEL = "data/caffe/LightenedCNN_A.caffemodel";
	
	public static final String DEEP_LAYER = "fc1";
	public static final int IMG_WIDTH = 128;
	public static final int IMG_HEIGHT = 128;
	
	//Image Source Folder
	public static final File SRC_FOLDER = new File("data/classes");
	
	//Features Storage File
	public static final File STORAGE_FILE = new File("data/features.dat");
	
	//k-Nearest Neighbors
	public static final int K = 10;
	
	//HTML Output Parameters
	public static final  String BASE_URI = "file:///" + Parameters.SRC_FOLDER.getAbsolutePath() + "/";
	public static final File RESULTS_HTML = new File("out/deep.seq.html");
	public static final File RESULTS_HTML_LUCENE = new File("out/deep.lucene.html");
	public static final File RESULTS_HTML_REORDERED = new File("out/deep.reordered.html");
	
	//Face Detection Parameters 
	public static final Size FACE_MIN_SIZE = new Size(120, 120);
	public static final Size FACE_MAX_SIZE = new Size(500, 500);
	public static final Size EYE_MIN_SIZE = new Size(40, 40);
	public static final Size EYE_MAX_SIZE = new Size(80, 80);
	public static final Size MOUTH_MIN_SIZE = new Size(40, 40);
	public static final Size MOUTH_MAX_SIZE = new Size(80, 50);
	public static final Size NOSE_MIN_SIZE = new Size(20, 20);
	public static final Size NOSE_MAX_SIZE = new Size(60, 60);
	public static final String HAAR_CASCADE_FRONTALFACE = "data/haarcascades/haarcascade_frontalface_alt.xml";
	public static final String HAAR_CASCADE_EYES = "data/haarcascades/haarcascade_eye.xml";
	public static final String HAAR_CASCADE_LEFTEYE = "data/haarcascades/haarcascade_mcs_lefteye.xml";
	public static final String HAAR_CASCADE_RIGHTEYE = "data/haarcascades/haarcascade_mcs_righteye.xml";
	public static final String HAAR_CASCADE_MOUTH = "data/haarcascades/haarcascade_mcs_mouth.xml";
	public static final String HAAR_CASCADE_NOSE = "data/haarcascades/haarcascade_mcs_nose.xml";
	
	public static final String VIDEO_PATH = "data/videos/dimartedi.mp4";
	
	public static final int VIDEO_WIDTH = 720;
	public static final int VIDEO_HEIGHT = 576;
	
	public static final String DATASET_PATH = "data/classes";
		
	public static final File DEST_VIDEO = new File("out/out.avi");
	
	public static final float KNN_CONF_THRESHOLD = 0.6f;

}
