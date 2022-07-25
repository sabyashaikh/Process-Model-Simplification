package org.processmining.logfiltering.visualizerPlugin;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.visualizerModel.FilteringVisualizerModel;

@Plugin(name = "Create Filtering visualizer model!", level = PluginLevel.PeerReviewed, parameterLabels = { "Log" }, returnLabels = { "FilteringVisualizerModel" }, returnTypes = { FilteringVisualizerModel.class }, help = "Create a Filtering visualizer model that can be shown by a visualizer or exported", quality = PluginQuality.Fair, categories = {
		PluginCategory.Discovery })
public class CreateFiltVisModel {
	@UITopiaVariant(affiliation = "PADS", author = "Mohammedreza, Berti", email = "fanisani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public FilteringVisualizerModel createVisModel(PluginContext context, XLog log) {
		// plugin context is mandatory! and contains referral to other prom objects etc.
		FilteringVisualizerModel visModel = new FilteringVisualizerModel(context, log);
		return visModel;
	}
}
