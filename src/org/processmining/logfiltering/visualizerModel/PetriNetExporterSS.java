package org.processmining.logfiltering.visualizerModel;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

@Plugin(name = "Filtered Petri net exporting", returnLabels = { "Filtered Petri Net" }, returnTypes = { Petrinet.class }, parameterLabels = {
"Petri Net" }, help = "Filtered Petri net exporting", userAccessible = false)
public class PetriNetExporterSS {
	@UITopiaVariant(author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de", affiliation = "PADS")
	@PluginVariant(requiredParameterLabels = { 0 })
	public Petrinet returnFilteredLogSS(PluginContext context, Petrinet filteredNet) {
		return filteredNet;
	}
}
