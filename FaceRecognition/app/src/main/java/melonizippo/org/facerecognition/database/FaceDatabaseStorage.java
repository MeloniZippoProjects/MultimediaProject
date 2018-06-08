package melonizippo.org.facerecognition.database;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FaceDatabaseStorage
{
    public static final String IMAGE_DATABASE_PATH = "image_database.dat";
    private static final String TAG = "FaceDabaseSerializer";

    private static File internalStorage;

    public static void setFileStorage(File storage)
    {
        internalStorage = internalStorage;
    }

    public static FaceDatabase load()
    {
        File dbFile = new File(internalStorage, IMAGE_DATABASE_PATH);

        try(FileInputStream fis = new FileInputStream(dbFile))
        {
            try(ObjectInputStream ois = new ObjectInputStream(fis))
            {
                FaceDatabase db = (FaceDatabase) ois.readObject();
                Log.d(TAG, "Database loaded");
                return db;
            }
        }
        catch (Exception ex)
        {
            Log.d(TAG, "Database loading failed");
            return new FaceDatabase();
        }
    }

    public static void store(FaceDatabase database)
    {
        File dbFile = new File(internalStorage, IMAGE_DATABASE_PATH);

        try(FileOutputStream fos = new FileOutputStream(dbFile))
        {
            try(ObjectOutputStream oos = new ObjectOutputStream(fos))
            {
                oos.writeObject(database);
                Log.d(TAG, "Database stored");
            }
        }
        catch (Exception ex)
        {
            Log.d(TAG, "Database store failed");
        }
    }
}
