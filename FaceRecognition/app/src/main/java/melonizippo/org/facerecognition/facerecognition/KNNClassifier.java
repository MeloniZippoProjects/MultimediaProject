package melonizippo.org.facerecognition.facerecognition;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.FaceDatabase;
import melonizippo.org.facerecognition.database.FaceDatabaseStorage;
import melonizippo.org.facerecognition.database.Identity;
import melonizippo.org.facerecognition.database.LabeledFaceData;
import melonizippo.org.facerecognition.deep.Parameters;

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
		int k = Parameters.K;
		for(Map.Entry<Double,LabeledFaceData> faceData : sortedFaceData.descendingMap().entrySet())
		{
			if(nearestNeighbours.size() < k)
				nearestNeighbours.add(faceData);
			else
				break;
		}

        PredictedClass predictedClass = getBestClass(nearestNeighbours);
		predictedClass.setFaceData(queryFaceData);

        double minConfidence = Parameters.MIN_CONFIDENCE;
        if(predictedClass.getConfidence() >= minConfidence)
        {
            return predictedClass;
        }
        else
        {
            return new PredictedClass(null, predictedClass.getConfidence(), queryFaceData);
        }
	}


	//todo: refactor "label" names to "identity" ones
	private PredictedClass getBestClass(List<Map.Entry<Double,LabeledFaceData>> results)
    {
		//Loop in the results list and retrieve the best label

		HashMap<Identity, Double> identityScores = new HashMap<>();
		HashMap<Identity, Double> bestIdentityScore = new HashMap<>();

		for(Map.Entry<Double,LabeledFaceData> descriptor : results)
		{
			Identity identityLabel = descriptor.getValue().getIdentityLabel();
			Double currentIdentityScore = identityScores.getOrDefault(identityLabel, 0d);
			identityScores.put(identityLabel, currentIdentityScore + descriptor.getKey());

			Double currentBestIdentityScore = bestIdentityScore.getOrDefault(identityLabel, 0d);
			if(descriptor.getKey() > currentBestIdentityScore)
				bestIdentityScore.put(identityLabel, descriptor.getKey());
		}

		Optional<Map.Entry<Identity,Double>> bestIdentityLabel =
                identityScores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));

		if(bestIdentityLabel.isPresent())
		{
			Identity bestLabel = bestIdentityLabel.get().getKey();
			Double confidence = bestIdentityScore.get(bestLabel);

			return new PredictedClass(bestLabel, confidence, null);
		}
		else
		    throw new IllegalStateException("Best label should exist");
	}
}
