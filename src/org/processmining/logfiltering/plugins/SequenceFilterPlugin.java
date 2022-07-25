package org.processmining.logfiltering.plugins;

import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.algorithms.FilterBasedOnSequence;
import org.processmining.logfiltering.parameters.SequenceFilterParameter;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Sequence Filter", parameterLabels = { "Event Log", "Parameter Object" }, returnLabels = { "Filtered Event Log" }, returnTypes = { XLog.class})
public class SequenceFilterPlugin {
	@SuppressWarnings("unchecked")
	String [] EventAttributes;
	public class SequenceFilterWizardPanel extends JPanel  {
				
		private static final long serialVersionUID = 8572008504104999027L;
		public XEventClassifier[] EventAttribute;
		
		private final NiceDoubleSlider HighSupportPatternSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Select the Minimum Support of Odd Patterns Threshold", 0, 1, 1, Orientation.HORIZONTAL);
		
		private final NiceDoubleSlider ConfHighConfSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Select the Minimum Confidence of High Confidence Rules Threshold", 0, 1, 0.9, Orientation.HORIZONTAL);
		private final NiceDoubleSlider SupportHighConfSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Select the Minimum Support of High Confidence Rules Threshold", 0, 1, 0.1, Orientation.HORIZONTAL);
		private final NiceDoubleSlider SupportOridnaryRulesSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Select the Minimum Support of Ordinary Pattenrs", 0, 1, 0.05, Orientation.HORIZONTAL);
		private final NiceDoubleSlider ConfOridnaryRulesSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Select the Minimum Confidance of Ordinary Rules Threshold", 0, 1, 0.01, Orientation.HORIZONTAL);
		private NiceIntegerSlider OddDistance= SlickerFactory.instance().createNiceIntegerSlider("Please Select the Odd Length", 1, 8, 2, Orientation.HORIZONTAL);
		final JLabel comboAttlabel = new JLabel("Select the Event column");
		
		
		JComboBox<XEventClassifier> comboAtt;
		  
		public SequenceFilterWizardPanel(XLog log) {
			
			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			
			
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);
			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);			
			add(comboAtt);
			//add(comboFilterMethodolabel);
			//add(comboFilterMethod);
			add(HighSupportPatternSlider);
			add(ConfHighConfSlider);
			add(SupportHighConfSlider);
			add(SupportOridnaryRulesSlider);
			add(ConfOridnaryRulesSlider);
			add(OddDistance);
			add(comboAttlabel);
			if (EventAttribute.length!=0)
				add(comboAtt);
			
		}
		
		public SequenceFilterParameter getParameters() {
			 
			return new SequenceFilterParameter(HighSupportPatternSlider.getValue(),ConfHighConfSlider.getValue(),SupportHighConfSlider.getValue(),
					SupportOridnaryRulesSlider.getValue(),ConfOridnaryRulesSlider.getValue(),OddDistance.getValue(),(XEventClassifier) comboAtt.getSelectedItem());
			
		}
		
	}

	@UITopiaVariant(affiliation = "RWTH-Aachen University", author = "Mohammadreza", email = "fanisani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog run(UIPluginContext context, XLog log) throws IOException {
		
	//	 LogProperties LogProp = new LogProperties(log);
	//	 Map<String, String> EvntsAttributes= LogProp.getEventAttributeTypeMap();
	//	//int temp=0;
		//final String [] Attributes = new String [EvntsAttributes.size()] ;
		//for(String Att : EvntsAttributes.keySet()){
		//	Attributes[temp]=Att;
		//	temp ++;
		//}
		//EventAttributes=Attributes;
		SequenceFilterWizardPanel configPanel = new SequenceFilterWizardPanel(log);
		
		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			return run(context, log, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "fanisani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = {0})
	public static XLog run(PluginContext context, XLog log) throws IOException {
		return run(context, log, new SequenceFilterParameter());
	}
	
	@PluginVariant(requiredParameterLabels = {0,1})
	public static XLog run(PluginContext context, XLog log, SequenceFilterParameter parameters) throws IOException {
		return FilterBasedOnSequence.apply(log, parameters);
	}
	
}
