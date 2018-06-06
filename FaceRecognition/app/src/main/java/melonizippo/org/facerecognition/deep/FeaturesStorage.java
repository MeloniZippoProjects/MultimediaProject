package melonizippo.org.facerecognition.deep;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class FeaturesStorage {
	
	public static void store(List<ImgDescriptor> ids, File storageFile) throws IOException {
		if (ids != null){
			storageFile.getParentFile().mkdir();
	        FileOutputStream fos =  new FileOutputStream(storageFile);
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        try {
	        	oos.writeObject(ids);
	        } finally {
	        	oos.close();
	        	fos.close();
	        }
		}
	}
	
	public static List<ImgDescriptor> load(File storageFile) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(storageFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<ImgDescriptor> ids = null;
        try {
        	ids = (List<ImgDescriptor>) ois.readObject();
        } finally {
        	ois.close();
        	fis.close();
        }
        return ids;
	}	
}
