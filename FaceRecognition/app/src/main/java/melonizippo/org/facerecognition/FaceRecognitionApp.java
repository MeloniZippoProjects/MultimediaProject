package melonizippo.org.facerecognition;


import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FaceRecognitionApp extends Application {

    private final static String TAG = "FaceRecognitionApp";

    private final static Integer[] filesToCopy = {
            InternalStorageFiles.HAARCASCADE_FRONTALFACE,
            InternalStorageFiles.VGG_PROTOTXT,
            InternalStorageFiles.VGG_MODEL
    };

    @Override
    public void onCreate() {
        super.onCreate();
        /* even if it is called initDebug it is not actually for debug
         * It just calls loadLibrary("opencv_java3") and some other needed stuff
         */
        OpenCVLoader.initDebug();

        //initialize internal storage with assets and internal storage dir
        initInternalStorage(getAssets(), getFilesDir());

        //let's test with one file
        try {
            //String filepath = InternalStorageFiles.getAssetPath(InternalStorageFiles.HAARCASCADE_FRONTALFACE);
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

    private static void initInternalStorage(AssetManager assetManager, File internalStorage)
    {
        InternalStorageFiles.setAssetManager(assetManager);
        InternalStorageFiles.setInternalStorage(internalStorage);
    }
}
