package melonizippo.org.facerecognition.database;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import melonizippo.org.facerecognition.FaceRecognitionApp;

public class FaceDatabaseStorage
{
    public static final String IMAGE_DATABASE_NAME = "image_database.dat";
    private static final String TAG = "FaceDabaseSerializer";

    private static File internalStorage;

    public static void setFileStorage(File storage)
    {
        internalStorage = internalStorage;
    }

    private static FaceDatabase faceDatabase;
    public static FaceDatabase getFaceDatabase()
    {
        if(faceDatabase == null)
            load();
        return faceDatabase;
    }

    public static void load()
    {
        try(
                FileInputStream fis = FaceRecognitionApp.getAppContext().openFileInput(IMAGE_DATABASE_NAME);
                ObjectInputStream ois = new ObjectInputStream(fis))
        {
            FaceDatabase db = (FaceDatabase) ois.readObject();
            Log.i(TAG, "Database loaded");
            faceDatabase = db;
        }
        catch (Exception ex)
        {
            Log.i(TAG, "Database loading failed");
            faceDatabase = new FaceDatabase();
        }
    }

    public static void store()
    {
        try(
                FileOutputStream fos = FaceRecognitionApp.getAppContext().openFileOutput(IMAGE_DATABASE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        )
        {
            oos.writeObject(faceDatabase);
            Log.d(TAG, "Database stored");
        }
        catch (Exception ex)
        {
            Log.d(TAG, "Database store failed");
        }
    }
}
