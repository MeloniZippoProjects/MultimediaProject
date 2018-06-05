package melonizippo.org.facerecognition.algorithms.opencv.facerecognition;

import melonizippo.org.facerecognition.algorithms.deep.ImgDescriptor;
import melonizippo.org.facerecognition.algorithms.deep.seq.SeqImageSearch;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KNNClassifier {

	private SeqImageSearch sequentialScan;

	public KNNClassifier(File storageFile) throws IOException, ClassNotFoundException {
		sequentialScan = new SeqImageSearch();
		sequentialScan.open(storageFile);
	}

	//TODO
	public PredictedClass predict(ImgDescriptor query) {
		//perform a kNN similarity search and call getBestLabel to retrieve the best label
		return getBestLabel(sequentialScan.search(query, Parameters.K));

	}

	//TODO
	private PredictedClass getBestLabel(List<ImgDescriptor> results) {
		//Loop in the results list and retrieve the best label

		HashMap<String, Integer> labelScores = new HashMap<>();

		for(ImgDescriptor descriptor : results)
		{
			String label = descriptor.getLabel();

			Integer score = labelScores.getOrDefault(label, 0);

			labelScores.put(label, score + 1);
		}
		Optional<Map.Entry<String,Integer>> bestLabel = labelScores.entrySet().stream().
				max(Comparator.comparing(Map.Entry::getValue));

		if(bestLabel.isPresent())
		{
			String label = bestLabel.get().getKey();
			Float confidence = bestLabel.get().getValue().floatValue()/Parameters.K;
			return new PredictedClass(label, confidence);
		}

		return null;
	}
}
