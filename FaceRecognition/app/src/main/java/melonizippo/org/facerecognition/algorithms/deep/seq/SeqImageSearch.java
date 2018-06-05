package melonizippo.org.facerecognition.algorithms.deep.seq;

import it.unipi.ing.mim.deep.DNNExtractor;
import it.unipi.ing.mim.deep.ImgDescriptor;
import it.unipi.ing.mim.deep.tools.FeaturesStorage;
import it.unipi.ing.mim.opencv.facerecognition.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SeqImageSearch {

	private List<ImgDescriptor> descriptors;
		
	public static void main(String[] args) throws Exception {

		SeqImageSearch searcher = new SeqImageSearch();
		
		searcher.open(Parameters.STORAGE_FILE);
		
		//Image Query File
		File img = new File(Parameters.SRC_FOLDER, "b402c97071eea022f2d8fd700eed04ad.jpg");
		
		DNNExtractor extractor = new DNNExtractor();
		
		float[] features = extractor.extract(img, Parameters.DEEP_LAYER);
		ImgDescriptor query = new ImgDescriptor(features, img.getName(), null);
				
		long time = -System.currentTimeMillis();
		List<ImgDescriptor> res = searcher.search(query, Parameters.K);
		time += System.currentTimeMillis();
		System.out.println("Sequential search time: " + time + " ms");
	}
		
	public void open(File storageFile) throws ClassNotFoundException, IOException {
		descriptors = FeaturesStorage.load(storageFile );
	}
	
	public List<ImgDescriptor> search(ImgDescriptor queryF, int k) {
		for (int i=0;i<descriptors.size();i++){
			descriptors.get(i).distance(queryF);
		}
		Collections.sort(descriptors);
		
		return descriptors.subList(0, k);
	}

}
