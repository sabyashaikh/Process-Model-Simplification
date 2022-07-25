package org.processmining.logfiltering.visFilteringAlgorithms;

public class MatrixFilteringAlgorithmParameters extends FilteringAlgorithmParameters {
	Integer SubsequenceThreshold;
	Double ProbablitytThreshold;
	public MatrixFilteringAlgorithmParameters() {
		super();
		
		// sets the default value for subsequence length
		SubsequenceThreshold = 2;
		ProbablitytThreshold= 0.15;
	}

	public Integer getSubsequenceThreshold() {
		return this.SubsequenceThreshold;
	}
	
	public void setSubsequenceThreshold(Integer subsequenceLength) {
		this.SubsequenceThreshold = subsequenceLength;
	}
	public Double getProbabilityThreshold() {
		return this.ProbablitytThreshold;
	}
	
	public void setProbabilityThreshold(Double probability) {
		this.ProbablitytThreshold = probability;
	}
}