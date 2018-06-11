package melonizippo.org.facerecognition.facerecognition;

import android.support.annotation.NonNull;

public class PredictedClass implements Comparable<PredictedClass> {
	
	private String label;
	private double conf;
	
	public PredictedClass(String label, double conf) {
		this.label = label;
		this.conf = conf;
	}
	
	public String getLabel() {
		return label;
	}
	public double getConfidence() {
		return conf;
	}

	@Override
	public int compareTo(@NonNull PredictedClass o) {
		return -Double.compare(conf, o.conf);
	}
	
	@Override
	public String toString() {
		return label + "=" + conf;
	}
	
}
