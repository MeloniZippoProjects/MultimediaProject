package melonizippo.org.facerecognition.deep;

import java.io.File;

import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class Parameters {
	
	//DEEP parameters for lightened

	/*
	public static final String DEEP_LAYER = "fc1";

	public static final int IMG_WIDTH = 128;
	public static final int IMG_HEIGHT = 128;

	public static final Scalar MEAN = new Scalar(0);
	*/


	//DEEP parameters for resnet50

	public static final String DEEP_LAYER = "pool5/7x7_s1";

	public static final Scalar MEAN = new Scalar(91.4953, 103.8827, 131.0912);

	public static final int IMG_WIDTH = 224;
	public static final int IMG_HEIGHT = 224;


	//Parameters for face detector

	public static final double SCALE_FACTOR = 1.2;
	public static final int CANNY_PRUNING = 1;
	public static final int MIN_NEIGHBORS = 3;
	public static final Size FACE_MIN_SIZE = new Size(120, 120);
	public static final Size FACE_MAX_SIZE = new Size(500, 500);


	//k-Nearest Neighbors
	public static final int K = 11;
	public static final double MIN_CONFIDENCE = 0.6;
	
	//Face Detection Parameters

	public static final float KNN_CONF_THRESHOLD = 0.6f;

	public static final int MAX_DIMENSION = 1349;
	public static final long GC_INTERVAL = 60;
	public static final int MIN_IDENTITY_SAMPLES = 10;
	public static final long VIDEO_FRAMES_TO_EXTRACT = MIN_IDENTITY_SAMPLES * 2;
}
