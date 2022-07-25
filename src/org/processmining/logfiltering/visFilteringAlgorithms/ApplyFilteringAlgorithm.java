package org.processmining.logfiltering.visFilteringAlgorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

public class ApplyFilteringAlgorithm {
	public XLog getFilteredLog(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		FilteringAlgorithmParameters parameters = new FilteringAlgorithmParameters();
		return getFilteredLog(context, log, parameters);
	}
	
	public XLog getFilteredLog(PluginContext context, XLog log, FilteringAlgorithmParameters parameters) {
		return null;
	}
}
