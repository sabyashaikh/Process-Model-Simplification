package org.processmining.logfiltering.plugins.Sabya;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginQuality;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "ProcessTree Based Filter Visualizer", level = PluginLevel.PeerReviewed,  parameterLabels = { "Event Log", "Parameters", "Process Tree" }, returnLabels = { "ProcessTreeFilterVisualizerModel2" }, returnTypes = { ProcessTreeBasedFilterVisualizerModel2.class }, help = "Create a Filtering visualizer model based on process trees, that can be shown by a visualizer or exported", quality = PluginQuality.Fair, categories = {
		PluginCategory.Discovery })
public class ProcessTreeBasedFilterVisualizerPlugin2 {
	@UITopiaVariant(uiLabel = "Process Model Simplification and log filtering", affiliation = "RWTH Aachen University", author = "Sabya and Mohammadreza", email = "sabya.shaikh@rwth-aachen.de, fanisani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public ProcessTreeBasedFilterVisualizerModel2 run(UIPluginContext context, XLog log) {
		System.out.println("Running application with log only");
		ProcessTreeBasedFilterVisualizerModel2 visualizerModel = new ProcessTreeBasedFilterVisualizerModel2(context, log);
		visualizerModel.computePTwithProbability();
		return visualizerModel;
	}
}