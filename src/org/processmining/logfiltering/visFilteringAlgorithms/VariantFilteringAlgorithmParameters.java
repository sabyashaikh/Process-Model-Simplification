package org.processmining.logfiltering.visFilteringAlgorithms;

public class VariantFilteringAlgorithmParameters extends FilteringAlgorithmParameters {
	Integer threshold;
	
	public VariantFilteringAlgorithmParameters() {
		super();
		
		// sets the default value for subsequence length
		threshold = 80;
	}
	
	public Integer getThreshold() {
		return this.threshold;
	}
	
	public void setThreshold(Integer subsequenceLength) {
		this.threshold = subsequenceLength;
	}
}
