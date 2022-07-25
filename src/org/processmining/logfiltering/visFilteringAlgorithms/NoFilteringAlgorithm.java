package org.processmining.logfiltering.visFilteringAlgorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

public class NoFilteringAlgorithm extends ApplyFilteringAlgorithm {
	public NoFilteringAlgorithm() {
		super();
	}
	
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		NoFilteringAlgorithmParameters parameters = new NoFilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	
	@Override
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters0) {
		// cast it to NoFilteringAlgorithmParameters (this because we must override a method with the exact signature of the father)
		if (parameters0 instanceof NoFilteringAlgorithmParameters) {
			NoFilteringAlgorithmParameters parameters = (NoFilteringAlgorithmParameters) parameters0;
		}
		
		
		return log;
	}
}
