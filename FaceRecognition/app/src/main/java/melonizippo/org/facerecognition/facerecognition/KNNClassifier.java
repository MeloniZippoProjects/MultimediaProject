package melonizippo.org.facerecognition.facerecognition;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.Identity;
import melonizippo.org.facerecognition.database.LabeledFaceData;
import melonizippo.org.facerecognition.Parameters;

import java.util.*;

public class KNNClassifier {

	private FaceDatabase faceDatabase;

	public KNNClassifier()
	{
		faceDatabase = FaceDatabaseStorage.getFaceDatabase();
	}

	public PredictedClass classify(FaceData queryFaceData) {
		//perform a kNN similarity search and call getBestClass to retrieve the best label

		TreeMap<Double, LabeledFaceData> sortedFaceData = new TreeMap<>();

		//check on known identities
		for(Identity identity : faceDatabase.knownIdentities)
		{
			for(FaceData faceData : identity.identityDataset)
			{
			    LabeledFaceData labeledFaceData = new LabeledFaceData(faceData, identity);
				double similarity = labeledFaceData.getSimilarity(queryFaceData);
				sortedFaceData.put(similarity, labeledFaceData);
			}
		}

		//check in unclassified data
		for(FaceData faceData : faceDatabase.unclassifiedFaces.values())
		{
			LabeledFaceData labeledFaceData = new LabeledFaceData(faceData, null);
			double similarity = labeledFaceData.getSimilarity(queryFaceData);
			sortedFaceData.put(similarity, labeledFaceData);
		}

		//take first K
		List<Map.Entry<Double,LabeledFaceData>> nearestNeighbours = new LinkedList<>();

		for(Map.Entry<Double,LabeledFaceData> faceData : sortedFaceData.descendingMap().entrySet())
		{
			if(nearestNeighbours.size() < Parameters.K)
				nearestNeighbours.add(faceData);
			else
				break;
		}

		PredictedClass predictedClass = getBestClass(nearestNeighbours);

        if(predictedClass.getConfidence() >= Parameters.MIN_CONFIDENCE)
        {
            return predictedClass;
        }
        else
        {
            return new PredictedClass(null, predictedClass.getConfidence());
        }
	}

	private PredictedClass getBestClass(List<Map.Entry<Double,LabeledFaceData>> kNearestNeighbours)
    {
		//Loop in the results list and retrieve the best label


		//Total accumulated score for each identity
		HashMap<Identity, Double> identityTotalScores = new HashMap<>();

		//Best score so far for each identity
		HashMap<Identity, Double> identityBestScores = new HashMap<>();

		for(Map.Entry<Double,LabeledFaceData> descriptor : kNearestNeighbours)
		{
			Identity identityLabel = descriptor.getValue().getIdentityLabel();
			Double currentIdentityTotalScore = identityTotalScores.getOrDefault(identityLabel, 0d);
			identityTotalScores.put(identityLabel, currentIdentityTotalScore + descriptor.getKey());

			Double currentBestIdentityScore = identityBestScores.getOrDefault(identityLabel, 0d);
			if(descriptor.getKey() > currentBestIdentityScore)
				identityBestScores.put(identityLabel, descriptor.getKey());
		}

		Optional<Map.Entry<Identity,Double>> bestIdentityLabelOptional =
                identityTotalScores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));

		if(bestIdentityLabelOptional.isPresent())
		{
			Identity bestIdentityLabel = bestIdentityLabelOptional.get().getKey();
			Double confidence = identityBestScores.get(bestIdentityLabel);

			return new PredictedClass(bestIdentityLabel, confidence);
		}
		else
		    throw new IllegalStateException("Best label should exist");
	}
}
