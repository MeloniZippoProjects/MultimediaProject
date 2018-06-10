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

    private static FaceDatabase faceDatabase;
    public static FaceDatabase getFaceDatabase()
    {
        if(faceDatabase == null)
            load();
        return faceDatabase;
    }

    public static void load()
    {
        File dbFile = new File(internalStorage, IMAGE_DATABASE_PATH);

        try(
                FileInputStream fis = new FileInputStream(dbFile);
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
        File dbFile = new File(internalStorage, IMAGE_DATABASE_PATH);

        try(
            FileOutputStream fos = new FileOutputStream(dbFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos)
        )
        {
            if(!dbFile.exists())
                dbFile.createNewFile();

            oos.writeObject(faceDatabase);
            Log.d(TAG, "Database stored");
        }
        catch (Exception ex)
        {
            Log.d(TAG, "Database store failed");
        }
    }
}
