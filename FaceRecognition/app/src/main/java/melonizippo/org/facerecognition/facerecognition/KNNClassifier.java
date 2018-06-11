package melonizippo.org.facerecognition.facerecognition;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.IdentityEntry;
import melonizippo.org.facerecognition.database.LabeledFaceData;
import melonizippo.org.facerecognition.deep.Parameters;

import java.util.*;

public class KNNClassifier {

	private FaceDatabase faceDatabase;

	public KNNClassifier()
	{
		faceDatabase = FaceDatabaseStorage.getFaceDatabase();
	}

	//TODO
	public PredictedClass predict(FaceData query) {
		//perform a kNN similarity search and call getBestLabel to retrieve the best label

		TreeMap<Double, LabeledFaceData> sortedFaceData = new TreeMap<>();

		//check on known identities
		for(IdentityEntry identityEntry : faceDatabase.knownIdentities)
		{
			for(FaceData faceData : identityEntry.identityDataset)
			{
				LabeledFaceData labeledFaceData = new LabeledFaceData(faceData, identityEntry.label);
				double similarity = labeledFaceData.getSimilarity(query);
				sortedFaceData.put(similarity, labeledFaceData);
			}
		}

		//check in uncategorized data
		for(FaceData faceData : faceDatabase.uncategorizedData)
		{
			LabeledFaceData labeledFaceData = new LabeledFaceData(faceData, "unknown");
			double similarity = labeledFaceData.getSimilarity(query);
			sortedFaceData.put(similarity, labeledFaceData);
		}

		//take first K
		List<Map.Entry<Double,LabeledFaceData>> nearestNeighbours = new LinkedList<>();

		for(Map.Entry<Double,LabeledFaceData> faceData : sortedFaceData.descendingMap().entrySet())
		{
			int k = Parameters.K;
			if(nearestNeighbours.size() < k)
				nearestNeighbours.add(faceData);
			else
				break;
		}

		return getBestLabel(nearestNeighbours);
	}


	//TODO
	private PredictedClass getBestLabel(List<Map.Entry<Double,LabeledFaceData>> results) {
		//Loop in the results list and retrieve the best label

		HashMap<String, Integer> labelCounts = new HashMap<>();
		HashMap<String, Double> bestLabelsScore = new HashMap<>();

		for(Map.Entry<Double,LabeledFaceData> descriptor : results)
		{
			String label = descriptor.getValue().getLabel();
			Integer labelCount = labelCounts.getOrDefault(label, 0);
			labelCounts.put(label, labelCount + 1);

			Double currentLabelScore = bestLabelsScore.getOrDefault(label, 0d);
			if(descriptor.getKey() > currentLabelScore)
				bestLabelsScore.put(label, descriptor.getKey());
		}

		Optional<Map.Entry<String,Integer>> bestLabelOptional = labelCounts.entrySet().stream().
				max(Comparator.comparing(Map.Entry::getValue));

		if(bestLabelOptional.isPresent())
		{
			String bestLabel = bestLabelOptional.get().getKey();
			Double confidence = bestLabelsScore.get(bestLabel);

			return new PredictedClass(bestLabel, confidence);
		}

		return null;
	}
}
