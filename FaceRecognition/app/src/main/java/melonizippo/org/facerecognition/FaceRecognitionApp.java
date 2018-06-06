package melonizippo.org.facerecognition;

import android.app.Application;

public class FaceRecognitionApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("opencv_java3");
    }
}
