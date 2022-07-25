package org.processmining.logfiltering.visFilteringAlgorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.plugins.VariantCounterPlugin;

public class VariantFilteringAlgorithm extends ApplyFilteringAlgorithm {
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		VariantFilteringAlgorithmParameters parameters = new VariantFilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters0) {
		// cast it to NoFilteringAlgorithmParameters (this because we must override a method with the exact signature of the father)
		VariantFilteringAlgorithmParameters parameters = new VariantFilteringAlgorithmParameters();
		if (parameters0 instanceof VariantFilteringAlgorithmParameters) {
			parameters = (VariantFilteringAlgorithmParameters) parameters0;
		}
		VariantCounterPlugin variantCounterPlugin = new VariantCounterPlugin();
		MatrixFilterParameter internalParameters = new MatrixFilterParameter();
		
		internalParameters.setSubsequenceLength(parameters.getThreshold());
		internalParameters.setFilteringSelection(parameters.getFilteringSelection());
		
		System.out.println("filteringSelection = "+parameters.getFilteringSelection());
		
		//internalParameters.set
		
		return variantCounterPlugin.run(context, log, internalParameters);
	}
}
