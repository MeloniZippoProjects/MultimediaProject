package melonizippo.org.facerecognition;


import android.content.res.AssetManager;

import java.io.File;

/**
 * Abstracts the assets path in the internal memory storage
 */
public class InternalStorageFiles {

    public static final int HAARCASCADE_FRONTALFACE = 0;
    public static final int VGG_PROTOTXT = 1;
    public static final int VGG_MODEL = 2;

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
        return new File(path);
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
                path = "caffe_model/senet50_ft_caffe/senet50_ft.prototxt";
                break;
            case VGG_MODEL:
                path = "caffe_model/senet50_ft_caffe/senet50_ft.caffemodel";
                break;
            default:
                path = "";
        }

        return path;
    }
}
