package org.processmining.logfiltering.plugins;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;

@Plugin(name = "Petri Net To DFG ", parameterLabels = { "Petrinet" },
returnLabels = {"Petrinet" },
returnTypes =  {Petrinet.class })
public class PetriNetToDFG {
/////////////Here We use Matrix Filter Parameter to fast develop the code
	///They are not relate to each other
	//// We did not develo the Split Miner
	///// We just call it
	
	
	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public Petrinet run(UIPluginContext context, BPMNDiagram bpmn) {
		Object[] resultedModel =BPMNToPetriNetConverter.convert(bpmn);
		
		
		return run(bpmn);
	}
	
	
	@PluginVariant(requiredParameterLabels = {0})
	public  static Petrinet run(BPMNDiagram bpmn) {
		Object[] resultedModel =BPMNToPetriNetConverter.convert(bpmn);
		return  (Petrinet)  resultedModel[0];
	
				
	}
	
	

}