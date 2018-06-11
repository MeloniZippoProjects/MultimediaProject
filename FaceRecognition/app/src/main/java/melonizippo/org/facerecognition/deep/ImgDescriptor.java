package melonizippo.org.facerecognition.deep;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class ImgDescriptor implements Serializable, Comparable<ImgDescriptor> {

	private static final long serialVersionUID = 1L;
	
	private float[] normalizedVector; // image feature
	
	private String id; // unique id of the image (usually file name)
	
	private String label;
	
	private double dist; // used for sorting purposes
	
	public ImgDescriptor(float[] features, String id, String label) {
		if (features != null) {
			float norm2 = evaluateNorm2(features);
			this.normalizedVector = getNormalizedVector(features, norm2);
		}
		this.id = id;
		this.label = label;
	}
	
	public float[] getFeatures() {
		return normalizedVector;
	}
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
    public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	// compare with other friends using distances
	@Override
	public int compareTo(@NonNull ImgDescriptor arg0) {

		return Double.compare(dist, arg0.dist);
	}

	public double getSimilarity(@NonNull ImgDescriptor queryDescriptor)
	{
		float[] queryVector = queryDescriptor.getFeatures();
		double scalarProduct = 0;

		for(int i = 0; i < queryVector.length; ++i)
			scalarProduct += getFeatures()[i] * queryVector[i];

		return scalarProduct;
	}
	
	//Normalize the vector values 
	private float[] getNormalizedVector(float[] vector, float norm) {
		if (norm != 0) {
			for (int i = 0; i < vector.length; i++) {
				vector[i] = vector[i]/norm;
			}
		}
		return vector;
	}
	
	//Norm 2
	private float evaluateNorm2(float[] vector) {
		float norm2 = 0;
		for (float elem : vector) {
			norm2 += (elem) * (elem);
		}
		norm2 = (float) Math.sqrt(norm2);
		
		return norm2;
	}
    
}
