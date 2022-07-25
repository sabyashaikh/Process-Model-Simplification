package org.processmining.logfiltering.visFilteringAlgorithms;

public class SFFilteringAlgorithmParameters extends FilteringAlgorithmParameters {

	Double MaximumSupportOddPatters;
	Double MinimumSupportHighRules;
	Double MinimumConfidenceHighRules;  
	public SFFilteringAlgorithmParameters() {
		super();
		
		// sets the default value for subsequence length
		MaximumSupportOddPatters = 0.01;
		MinimumSupportHighRules= 0.4;
		MinimumConfidenceHighRules= 0.15 ;
	}
	public Double getMaximumSupportOddPatters() {
		return MaximumSupportOddPatters;
	}
	public void setMaximumSupportOddPatters(Double maximumSupportOddPatters) {
		MaximumSupportOddPatters = maximumSupportOddPatters;
	}
	public Double getMinimumSupportHighRules() {
		return MinimumSupportHighRules;
	}
	public void setMinimumSupportHighRules(Double minimumSupportHighRules) {
		MinimumSupportHighRules = minimumSupportHighRules;
	}
	public Double getMinimumConfidenceHighRules() {
		return MinimumConfidenceHighRules;
	}
	public void setMinimumConfidenceHighRules(Double minimumConfidenceHighRules) {
		MinimumConfidenceHighRules = minimumConfidenceHighRules;
	}
}
