package melonizippo.org.facerecognition;


import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstracts the assets path in the internal memory storage
 */
public class InternalStorageFiles {
    public static final String TAG = "Internal Storage";

    public static final int HAARCASCADE_FRONTALFACE = 0;
    public static final int VGG_PROTOTXT = 1;
    public static final int VGG_CAFFE_MODEL = 2;

    public static AssetManager assetManager;
    public static File internalStorage;

    public static void setAssetManager(AssetManager assetMgr)
    {
        assetManager = assetMgr;
    }

    public static void setInternalStorage(File intStorage)
    {
        internalStorage = intStorage;
    }

    public static File getFile(int fileId)
    {
        String path = internalStorage.getPath() + "/" + getAssetPath(fileId);
        File ret = new File(path);
        Log.d(TAG, "File" + fileId + " opened");
        return ret;
    }

    public static String getAssetPath(int fileId)
    {
        String path;
        switch(fileId)
        {
            case HAARCASCADE_FRONTALFACE:
                path = "haarcascades/haarcascade_frontalface_alt.xml";
                break;
            case VGG_PROTOTXT:
                //path = "caffe_model/senet50_ft_caffe/senet50_ft.prototxt";
                path = "caffe_model/resnet50_ft_caffe/resnet50_ft.prototxt";
                //path = "caffe_model/light/LightenedCNN_A.prototxt";
                break;
            case VGG_CAFFE_MODEL:
                //path = "caffe_model/senet50_ft_caffe/senet50_ft.caffemodel";
                path = "caffe_model/resnet50_ft_caffe/resnet50_ft.caffemodel";
                //path = "caffe_model/light/LightenedCNN_A.caffemodel";
                break;
            default:
                path = "";
        }

        return path;
    }

    public static void copyToInternalStorage(int fileId) throws IOException
    {
        File targetFile = InternalStorageFiles.getFile(fileId);

        //create the parent directories if they do not exist yet
        File parentDirectory = targetFile.getParentFile();
        if(!parentDirectory.exists())
        {
            if(!parentDirectory.mkdirs())
            {
                Log.e(TAG, "Cannot create parent directories");
                throw new IOException();
            }
            else
                Log.i(TAG, "Parent directories of " + parentDirectory.getName() +  " created");
        }

        //copy the asset in the target file
        try(InputStream inputStream = assetManager.open(InternalStorageFiles.getAssetPath(fileId)))
        {
            try(FileOutputStream outputStream = new FileOutputStream(targetFile))
            {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while((bytesRead = inputStream.read(buffer)) != -1)
                {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            Log.i(TAG, "File " + fileId + "copied");
        }
    }
}
