package org.processmining.logfiltering.visFilteringAlgorithms;

public class AFAFilteringAlgorithmParameters extends FilteringAlgorithmParameters {
	Double threshold;
	
	public AFAFilteringAlgorithmParameters() {
		super();
		
		// sets the default value for subsequence length
		threshold = 0.050;
		
	}
	
	public Double getThreshold() {
		return this.threshold;
	}
	
	public void setThreshold(Double AFAthreshold) {
		this.threshold = AFAthreshold;
	}
}
