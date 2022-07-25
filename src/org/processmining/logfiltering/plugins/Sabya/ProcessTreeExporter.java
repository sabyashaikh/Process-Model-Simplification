package org.processmining.logfiltering.plugins.Sabya;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.processtree.ProcessTree;


@Plugin(name = "Process Tree exporting", returnLabels = { "Process Tree" }, returnTypes = { ProcessTree.class }, parameterLabels = {
"Process Tree" }, help = "Process Tree exporting", userAccessible = false)
public class ProcessTreeExporter {
	@UITopiaVariant(author = "Alessandro Berti", email = "a.berti@pads.rwth-aachen.de", affiliation = "PADS")
	@PluginVariant(requiredParameterLabels = { 0 })
	public ProcessTree returnFilteredLogSS(PluginContext context, ProcessTree processTree) {
		return processTree;
	}
}

