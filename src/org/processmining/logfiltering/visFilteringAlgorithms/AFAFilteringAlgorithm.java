package org.processmining.logfiltering.visFilteringAlgorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.noisefiltering.plugins.RProMNoiseFilterPlugin;

public class AFAFilteringAlgorithm extends ApplyFilteringAlgorithm {
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		AFAFilteringAlgorithmParameters parameters = new AFAFilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters0) {
		// cast it to NoFilteringAlgorithmParameters (this because we must override a method with the exact signature of the father)
		AFAFilteringAlgorithmParameters parameters = new AFAFilteringAlgorithmParameters();
		if (parameters0 instanceof AFAFilteringAlgorithmParameters) {
			parameters = (AFAFilteringAlgorithmParameters) parameters0;
		}
		
		parameters.setFilteringSelection(parameters0.getFilteringSelection());
		
		RProMNoiseFilterPlugin AFAfilterPlugin = new RProMNoiseFilterPlugin();
		MatrixFilterParameter internalParameters = new MatrixFilterParameter();
		
		System.out.println("filteringSelection = "+parameters.getFilteringSelection());
		
		internalParameters.setFilteringSelection(parameters.getFilteringSelection());

		internalParameters.setProbabilityOfRemoval(parameters.getThreshold());
				
		//parameters.setThreshold(parameters.getThreshold());
		
		//internalParameters.set
		
		XLog filteredLog = AFAfilterPlugin.apply(log, internalParameters.getProbabilityOfRemoval());
		return filteredLog;
	}
}
