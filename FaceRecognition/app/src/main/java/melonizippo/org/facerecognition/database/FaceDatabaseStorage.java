package melonizippo.org.facerecognition.database;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

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
            loadFromInternalStorage();
        return faceDatabase;
    }

    public static void setFaceDatabase(FaceDatabase newFaceDatabase)
    {
        faceDatabase = newFaceDatabase;
    }

    public static void loadFromInternalStorage()
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

    public static void storeToInternalStorage()
    {
        try(
                FileOutputStream fos = FaceRecognitionApp.getAppContext().openFileOutput(IMAGE_DATABASE_NAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        )
        {
            FileChannel fileChannel = fos.getChannel();
            FileLock fileLock = fileChannel.lock();

            oos.writeObject(faceDatabase);
            Log.d(TAG, "Database stored");

            fileLock.release();
        }
        catch (Exception ex)
        {
            Log.d(TAG, "Database store failed");
        }
    }

    public static void clear()
    {
        faceDatabase.clear();
        Log.i(TAG, "Database reset");
        storeToInternalStorage();
    }
}
