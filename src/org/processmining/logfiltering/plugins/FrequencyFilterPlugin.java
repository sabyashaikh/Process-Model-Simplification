package org.processmining.logfiltering.plugins;

import java.awt.GridLayout;

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
import org.processmining.logfiltering.algorithms.FilterBasedOnFrequencyAbstraction;
import org.processmining.logfiltering.parameters.AbsteractionType;
import org.processmining.logfiltering.parameters.AdjustingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.FrequencyFilterParameter;
import org.processmining.logfiltering.parameters.ProbabilityType;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Frequency based Filtering ", parameterLabels = { "Event Log", "Parameter Object" }, returnLabels = {
		"Filtered Event Log" }, returnTypes = { XLog.class })
public class FrequencyFilterPlugin {

	@SuppressWarnings("unchecked")
	public class FrequencyFilterWizardPanel extends JPanel {

		private static final long serialVersionUID = 8572008504104999027L;
		public XEventClassifier[] EventAttribute;
		private AbsteractionType[] chooseName = AbsteractionType.values();
		private FilterSelection[] KeepOrRemove = FilterSelection.values();
		private final NiceDoubleSlider doubleSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.25, Orientation.HORIZONTAL);
		private NiceIntegerSlider SubsequenceLength= SlickerFactory.instance().createNiceIntegerSlider("Please Select the Subsequence Length", 0, 10, 2, Orientation.HORIZONTAL);
		private final JComboBox<FilterLevel> comboAbsteractionMethod = SlickerFactory.instance().createComboBox(chooseName);
		final JLabel comboFilterMethodolabel = new JLabel("Please make a selection...");
		private final JComboBox<FilterSelection> comboFilterSelectionType = SlickerFactory.instance().createComboBox(KeepOrRemove);
		final JLabel comboFilterSelectionTypelabel = new JLabel("Keep/Remove Outliers");
		final JLabel comboAttlabel = new JLabel("Select the Event column");
		
		JComboBox<XEventClassifier> comboAtt;

		public FrequencyFilterWizardPanel(XLog log) {

			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);

			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);
			add(comboFilterMethodolabel);
			add(comboAbsteractionMethod);
			add(doubleSlider);
			add(SubsequenceLength);
			add(comboAttlabel);
			add(comboAtt);
			add(comboFilterSelectionTypelabel);
			add(comboFilterSelectionType);
		}

		public FrequencyFilterParameter getParameters() {

			return new FrequencyFilterParameter(doubleSlider.getValue(), AdjustingType.None,SubsequenceLength.getValue() ,(AbsteractionType) comboAbsteractionMethod.getSelectedItem(), (FilterSelection) comboFilterSelectionType.getSelectedItem(),
					ProbabilityType.DIRECT,(XEventClassifier) comboAtt.getSelectedItem(), FilterLevel.TRACE );

		}

	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog run(UIPluginContext context, XLog log) {

		//LogProperties LogProp = new LogProperties(log);
		//Map<String, String> EvntsAttributes = LogProp.getEventAttributeTypeMap();
		int temp = 0;
		//		final String[] Attributes = new String[EvntsAttributes.size()];
		//		for (String Att : EvntsAttributes.keySet()) {
		//			Attributes[temp] = Att;
		//			temp++;
		//		}
		//		EventAttributes = Attributes;
		FrequencyFilterWizardPanel configPanel = new FrequencyFilterWizardPanel(log);

		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			return run(context, log, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "M.Fani.Sani@tue.nl")
	@PluginVariant(requiredParameterLabels = {0})
	public static XLog run(PluginContext context, XLog log) {
		return run(context, log, new FrequencyFilterParameter());
	}

	@PluginVariant(requiredParameterLabels = {0, 1 })
	public static XLog run(PluginContext context, XLog log, FrequencyFilterParameter parameters) {
		return FilterBasedOnFrequencyAbstraction.apply(log, parameters);
	}

}
