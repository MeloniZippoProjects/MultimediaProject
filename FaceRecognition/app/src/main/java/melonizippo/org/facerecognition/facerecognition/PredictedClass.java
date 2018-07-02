package melonizippo.org.facerecognition.facerecognition;

import android.support.annotation.NonNull;
import android.text.SpannableString;

import melonizippo.org.facerecognition.database.Identity;

public class PredictedClass implements Comparable<PredictedClass> {
	
	private Identity identity;
	private double confidence;
	
	public PredictedClass(Identity identity, double confidence) {
		this.identity = identity;
		this.confidence = confidence;
	}

	public boolean getAuthorized()
	{
		return identity.authorized;
	}

	public String getLabel() {
		if(identity == null)
			return "unknown";
		else
			return identity.label;
	}

	public double getConfidence() {
		return confidence;
	}

	@Override
	public int compareTo(@NonNull PredictedClass o) {
		return -Double.compare(confidence, o.confidence);
	}

	public SpannableString toStyledString()
	{
		//todo: return styled string (red if not authorized)
		return null;
	}

	@Override
	public String toString()
	{
		//todo: do pretty print here
		return identity.label + "=" + confidence;
	}
}
