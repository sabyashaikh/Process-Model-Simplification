package org.processmining.logfiltering.visualizerModel;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Filtered log export", returnLabels = { "Filtered log" }, returnTypes = { XLog.class }, parameterLabels = {
		"XLog" }, help = "Filtered log export", userAccessible = false)
public class FilteredLogExporterSS {
	@UITopiaVariant(author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de", affiliation = "PADS")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog returnFilteredLogSS(PluginContext context, XLog filteredLog) {
		return filteredLog;
	}
}