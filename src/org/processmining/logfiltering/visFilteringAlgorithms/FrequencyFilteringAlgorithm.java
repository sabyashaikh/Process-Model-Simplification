package org.processmining.logfiltering.visFilteringAlgorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.parameters.FrequencyFilterParameter;
import org.processmining.logfiltering.plugins.FrequencyFilterPlugin;

public class FrequencyFilteringAlgorithm extends ApplyFilteringAlgorithm {
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		FrequencyFilteringAlgorithmParameters parameters = new FrequencyFilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters0) {
		// cast it to NoFilteringAlgorithmParameters (this because we must override a method with the exact signature of the father)
		FrequencyFilteringAlgorithmParameters parameters = new FrequencyFilteringAlgorithmParameters();
		
		if (parameters0 instanceof FrequencyFilteringAlgorithmParameters) {
			parameters = (FrequencyFilteringAlgorithmParameters) parameters0;
		}
		
		parameters.setFilteringSelection(parameters0.getFilteringSelection());
		
		FrequencyFilterPlugin variantCounterPlugin = new FrequencyFilterPlugin();
		FrequencyFilterParameter internalParameters = new FrequencyFilterParameter();
		
		internalParameters.setProbabilityOfRemoval(parameters.getProbablitytThreshold());
		internalParameters.setAbsteractionLength(parameters.getSubsequenceThreshold());
		
		System.out.println("filteringSelection = "+parameters.getFilteringSelection());
		
		internalParameters.setFilteringSelection(parameters.getFilteringSelection());
		internalParameters.setAbstractionUsed(parameters.getAbsteractionUsed());
		
		return FrequencyFilterPlugin.run(context, log, internalParameters);
	}
}
