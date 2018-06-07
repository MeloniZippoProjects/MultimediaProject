package melonizippo.org.facerecognition;

import android.app.Application;

import org.opencv.android.OpenCVLoader;

public class FaceRecognitionApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /* even if it is called initDebug it is not actually for debug
         * It just calls loadLibrary("opencv_java3") and some other needed stuff
         */
        OpenCVLoader.initDebug();
    }
}
