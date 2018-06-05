package melonizippo.org.facerecognition.algorithms.deep.seq;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;
import it.unipi.ing.mim.opencv.facerecognition.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SeqImageStorage {
	
	private	DNNExtractor extractor;

	public SeqImageStorage() {
		extractor = new DNNExtractor();
	}

	public static void main(String[] args) throws Exception {
				
		SeqImageStorage indexing = new SeqImageStorage();
				
		List<ImgDescriptor> descriptors = indexing.extractFeatures(Parameters.SRC_FOLDER);
		
		FeaturesStorage.store(descriptors, Parameters.STORAGE_FILE);
	}
	
	private List<ImgDescriptor> extractFeatures(File srcFolder){
		List<ImgDescriptor>  descs = new ArrayList<ImgDescriptor>();
		
		File[] folders = srcFolder.listFiles();

		int counter = 1;
		for (File imgFolder: folders) {
			File[] imgFiles = imgFolder.listFiles();
			
			for (File imgFile: imgFiles) {
				System.out.println(counter++ + " - extracting " + imgFile.getName());
				try {
					long time = -System.currentTimeMillis();
					float[] features = extractor.extract(imgFile, Parameters.DEEP_LAYER);
					time += System.currentTimeMillis();
					System.out.println(time);
					descs.add(new ImgDescriptor(features, imgFile.getName(), imgFile.getParentFile().getName()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return descs;	
	}		
}
