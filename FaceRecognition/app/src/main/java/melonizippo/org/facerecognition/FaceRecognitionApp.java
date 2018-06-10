package melonizippo.org.facerecognition;


import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;

import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.deep.DNNExtractor;
import melonizippo.org.facerecognition.facerecognition.FaceDetector;
import melonizippo.org.facerecognition.facerecognition.KNNClassifier;

public class FaceRecognitionApp extends Application {

    private final static String TAG = "FaceRecognitionApp";

    public FaceDetector faceDetector;
    public DNNExtractor extractor;
    public KNNClassifier knnClassifier;

    private final static Integer[] filesToCopy = {
            InternalStorageFiles.HAARCASCADE_FRONTALFACE,
            InternalStorageFiles.VGG_PROTOTXT,
            InternalStorageFiles.VGG_CAFFE_MODEL
    };

    @Override
    public void onCreate() {
        super.onCreate();
        /* even if it is called initDebug it is not actually for debug
         * It just calls loadLibrary("opencv_java3") and some other needed stuff
         */
        OpenCVLoader.initDebug();

        //initialize internal storage with assets and internal storage dir
        initInternalStorage(getAssets(), getCacheDir());
        FaceDatabaseStorage.setFileStorage(getFilesDir());

        copyFiles();

        initFaceRecognition();
    }

    private void copyFiles()
    {
        try
        {
            for(int fileId : filesToCopy)
            {
                InternalStorageFiles.copyToInternalStorage(fileId);
            }
        }
        catch(IOException ex)
        {
            Log.e(TAG, "Cannot copy to internal storage");
        }
    }

    private void initFaceRecognition()
    {
        extractor = new DNNExtractor(
                InternalStorageFiles.getFile(InternalStorageFiles.VGG_PROTOTXT),
                InternalStorageFiles.getFile(InternalStorageFiles.VGG_CAFFE_MODEL)
        );

        faceDetector = new FaceDetector(
                InternalStorageFiles.getFile(
                        InternalStorageFiles.HAARCASCADE_FRONTALFACE)
                        .getPath());

//        try {
//            knnClassifier = new KNNClassifier(File.createTempFile("place", "holder")); //todo: implement internal storage
//        } catch(Exception e) {}
    }

    private static void initInternalStorage(AssetManager assetManager, File internalStorage)
    {
        InternalStorageFiles.setAssetManager(assetManager);
        InternalStorageFiles.setInternalStorage(internalStorage);
    }
}
