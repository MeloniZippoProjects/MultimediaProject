package melonizippo.org.facerecognition.facerecognition;

import android.support.annotation.NonNull;

import java.text.DecimalFormat;

import melonizippo.org.facerecognition.database.FaceData;
import melonizippo.org.facerecognition.database.Identity;
import melonizippo.org.facerecognition.Parameters;

public class PredictedClass implements Comparable<PredictedClass>
{
	private static DecimalFormat confidenceFormatter = new DecimalFormat("#0.000");

	private Identity identity;
	private double confidence;
	
	public PredictedClass(Identity identity, double confidence)
	{
		this.identity = identity;
		this.confidence = confidence;
	}

	public boolean isClassified()
	{
		return identity != null;
	}

	public boolean getAuthorized()
	{
		if(identity == null)
			return false;
		else
			return identity.authorized;
	}

	public String getLabel()
	{
		if(identity == null)
		{
			if(confidence < Parameters.MIN_CONFIDENCE)
				return "Unknown";
			else
				return "Unclassified";
		}
		else
			return identity.label;
	}

	public double getConfidence() {
		return confidence;
	}

	@Override
	public int compareTo(@NonNull PredictedClass o)
	{
		return -Double.compare(confidence, o.confidence);
	}

	@Override
	public String toString()
	{
		return getLabel() +
				": confidence " + confidenceFormatter.format(confidence) +
				" ; " + (getAuthorized() ? "Authorized" : "Not authorized");
	}
}
