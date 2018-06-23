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

	private static final PredictedClass unknownPerson = new PredictedClass("unknown", 1d);

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
		int k = Parameters.K;
		for(Map.Entry<Double,LabeledFaceData> faceData : sortedFaceData.descendingMap().entrySet())
		{
			if(nearestNeighbours.size() < k)
				nearestNeighbours.add(faceData);
			else
				break;
		}

		if(nearestNeighbours.size() != k)
		{
			return unknownPerson;
		}
		else {
			PredictedClass predictedClass = getBestLabel(nearestNeighbours);
			if(predictedClass == null)
				return unknownPerson;

			double minConfidence = Parameters.MIN_CONFIDENCE;
			if(predictedClass.getConfidence() >= minConfidence)
			{
				return predictedClass;
			}
			else
			{
				return unknownPerson;
			}
		}
	}


	//TODO
	private PredictedClass getBestLabel(List<Map.Entry<Double,LabeledFaceData>> results) {
		//Loop in the results list and retrieve the best label

		HashMap<String, Double> labelScores = new HashMap<>();
		HashMap<String, Double> bestLabelsScore = new HashMap<>();

		for(Map.Entry<Double,LabeledFaceData> descriptor : results)
		{
			String label = descriptor.getValue().getLabel();
			Double currentLabelScore = labelScores.getOrDefault(label, 0d);
			labelScores.put(label, currentLabelScore + descriptor.getKey());

			Double currentBestLabelScore = bestLabelsScore.getOrDefault(label, 0d);
			if(descriptor.getKey() > currentBestLabelScore)
				bestLabelsScore.put(label, descriptor.getKey());
		}

		Optional<Map.Entry<String,Double>> bestLabelOptional = labelScores.entrySet().stream().
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
