package org.processmining.logfiltering.visFilteringAlgorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.plugins.MatrixFilterPlugin;

public class MatrixFilteringAlgorithm extends ApplyFilteringAlgorithm {
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		MatrixFilteringAlgorithmParameters parameters = new MatrixFilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters0) {
		// cast it to NoFilteringAlgorithmParameters (this because we must override a method with the exact signature of the father)
		MatrixFilteringAlgorithmParameters parameters = new MatrixFilteringAlgorithmParameters();
		
		System.out.println(parameters0.getClass());
		
		if (parameters0 instanceof MatrixFilteringAlgorithmParameters) {
			parameters = (MatrixFilteringAlgorithmParameters) parameters0;
		}
		
		parameters.setFilteringSelection(parameters0.getFilteringSelection());
		
		MatrixFilterPlugin variantCounterPlugin = new MatrixFilterPlugin();
		MatrixFilterParameter internalParameters = new MatrixFilterParameter();
		
		internalParameters.setProbabilityOfRemoval(parameters.getProbabilityThreshold());
		internalParameters.setSubsequenceLength(parameters.getSubsequenceThreshold());
		internalParameters.setFilteringSelection(parameters.getFilteringSelection());
		
		System.out.println("filteringSelection = "+parameters.getFilteringSelection());

		//internalParameters.set
		
		return MatrixFilterPlugin.run(context, log, internalParameters);
	}
}
