package melonizippo.org.facerecognition.algorithms.opencv.facerecognition;

public class PredictedClass implements Comparable<PredictedClass> {
	
	private String label;
	private float conf;
	
	public PredictedClass(String label, float conf) {
		this.label = label;
		this.conf = conf;
	}
	
	public String getLabel() {
		return label;
	}
	public float getConfidence() {
		return conf;
	}

	@Override
	public int compareTo(PredictedClass o) {
		return - new Float(conf).compareTo(o.conf);
	}
	
	@Override
	public String toString() {
		return label + "=" + conf;
	}
	
}
