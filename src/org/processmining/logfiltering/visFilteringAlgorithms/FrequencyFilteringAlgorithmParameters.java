package org.processmining.logfiltering.visFilteringAlgorithms;



import org.processmining.logfiltering.parameters.AbsteractionType;

public class FrequencyFilteringAlgorithmParameters extends FilteringAlgorithmParameters {
	Integer SubsequenceThreshold;
	Double ProbablitytThreshold;
	AbsteractionType AbsteractionUsed;

	public FrequencyFilteringAlgorithmParameters() {
		super();
		
		// sets the default value for subsequence length
		SubsequenceThreshold = 2;
		ProbablitytThreshold= 0.15;
		AbsteractionUsed=AbsteractionType.SET;

	}
	public Integer getSubsequenceThreshold() {
		return SubsequenceThreshold;
	}
	public void setSubsequenceThreshold(Integer subsequenceThreshold) {
		SubsequenceThreshold = subsequenceThreshold;
	}
	public Double getProbablitytThreshold() {
		return ProbablitytThreshold;
	}
	public void setProbablitytThreshold(Double probablitytThreshold) {
		ProbablitytThreshold = probablitytThreshold;
	}
	public AbsteractionType getAbsteractionUsed() {
		return AbsteractionUsed;
	}
	public void setAbsteractionUsed(AbsteractionType absteractionUsed) {
		AbsteractionUsed = absteractionUsed;
	}
}
