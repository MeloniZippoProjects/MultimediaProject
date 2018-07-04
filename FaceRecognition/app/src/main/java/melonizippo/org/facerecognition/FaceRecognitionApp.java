package melonizippo.org.facerecognition;


import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private static FaceRecognitionApp appInstance = null;
    private static FaceRecognitionApp getAppInstance()
    {
        return appInstance;
    }

    private Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        FaceRecognitionApp.appInstance = this;
        context = getApplicationContext();

        /* even if it is called initDebug it is not actually for debug
         * It just calls loadLibrary("opencv_java3") and some other needed stuff
         */
        OpenCVLoader.initDebug();

        //initialize internal storage with assets and internal storage dir
        initInternalStorage(getAssets(), getCacheDir());
        FaceDatabaseStorage.setFileStorage(getDataDir());

        copyFiles();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.gc();
            System.runFinalization();
        },
                Parameters.GC_INTERVAL,
                Parameters.GC_INTERVAL,
                TimeUnit.SECONDS);

        initFaceRecognition();
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

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


        knnClassifier = new KNNClassifier();

    }

    private static void initInternalStorage(AssetManager assetManager, File internalStorage)
    {
        InternalStorageFiles.setAssetManager(assetManager);
        InternalStorageFiles.setInternalStorage(internalStorage);
    }

    public static Context getAppContext()
    {
        return appInstance.context;
    }
}
