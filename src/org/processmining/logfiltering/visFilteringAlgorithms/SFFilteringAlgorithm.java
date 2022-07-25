package org.processmining.logfiltering.visFilteringAlgorithms;

import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.parameters.SequenceFilterParameter;
import org.processmining.logfiltering.plugins.SequenceFilterPlugin;

public class SFFilteringAlgorithm extends ApplyFilteringAlgorithm {
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		SFFilteringAlgorithmParameters parameters = new SFFilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters0) {
		// cast it to NoFilteringAlgorithmParameters (this because we must override a method with the exact signature of the father)
		SFFilteringAlgorithmParameters parameters = new SFFilteringAlgorithmParameters();
		
		if (parameters0 instanceof SFFilteringAlgorithmParameters) {
			parameters = (SFFilteringAlgorithmParameters) parameters0;
		}
		
		SequenceFilterPlugin variantCounterPlugin = new SequenceFilterPlugin();
		SequenceFilterParameter internalParameters = new SequenceFilterParameter();
		
		internalParameters.setHighSupportPattern(parameters.getMaximumSupportOddPatters());
		internalParameters.setSuppHighConfRules(parameters.getMinimumSupportHighRules());
		internalParameters.setConfHighConfRules(parameters.getMinimumConfidenceHighRules());
		
		System.out.println("filteringSelection = "+parameters.getFilteringSelection());
		
		//internalParameters.set
		
		try {
			return SequenceFilterPlugin.run(context, log, internalParameters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
