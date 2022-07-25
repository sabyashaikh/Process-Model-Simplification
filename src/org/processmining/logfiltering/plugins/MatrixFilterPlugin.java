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
import org.processmining.logfiltering.algorithms.FilterBasedOnRelationMatrixK;
import org.processmining.logfiltering.parameters.AdjustingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.ProbabilityType;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Matrix Filter", parameterLabels = { "Event Log", "Parameter Object" }, returnLabels = {
		"Filtered Event Log" }, returnTypes = { XLog.class })
public class MatrixFilterPlugin {

	@SuppressWarnings("unchecked")
	public class MatrixFilterWizardPanel extends JPanel {

		private static final long serialVersionUID = 8572008504104999027L;
		public XEventClassifier[] EventAttribute;
		private FilterLevel[] chooseName = FilterLevel.values();
		private FilterSelection[] KeepOrRemove = FilterSelection.values();
		private final NiceDoubleSlider doubleSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.25, Orientation.HORIZONTAL);
		private  AdjustingType[] adjustingType = AdjustingType.values();
		private ProbabilityType[] probabilityTypes= ProbabilityType.values();
		private NiceIntegerSlider SubsequenceLength= SlickerFactory.instance().createNiceIntegerSlider("Please Select the Subsequence Length", 0, 10, 2, Orientation.HORIZONTAL);
		private final JComboBox<FilterLevel> comboFilterMethod = SlickerFactory.instance().createComboBox(chooseName);
		private final JComboBox<FilterLevel> comboThresholdAdjustingMethod = SlickerFactory.instance().createComboBox(adjustingType);
		private final JComboBox<FilterLevel> comboProbabilityComputingMethod = SlickerFactory.instance().createComboBox(probabilityTypes);
		final JLabel comboFilterMethodolabel = new JLabel("Please make a selection...");
		private final JComboBox<FilterSelection> comboFilterSelectionType = SlickerFactory.instance().createComboBox(KeepOrRemove);
		final JLabel comboFilterSelectionTypelabel = new JLabel("Keep/Remove Outliers");
		final JLabel comboAttlabel = new JLabel("Select the Event column");
		final JLabel probabilityTypelabel = new JLabel("How Computing Probabilities");
		final JLabel adjustingMethodlabel = new JLabel("Select the Threshold adjusting Method");
		
		JComboBox<XEventClassifier> comboAtt;

		public MatrixFilterWizardPanel(XLog log) {

			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);

			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);
			add(comboFilterMethodolabel);
			add(comboFilterMethod);
			add(doubleSlider);
			add(adjustingMethodlabel);
			add (comboThresholdAdjustingMethod);
			add(SubsequenceLength);
			add(comboAttlabel);
			add(comboAtt);
			add(probabilityTypelabel);
			add(comboProbabilityComputingMethod);
			add(comboFilterSelectionTypelabel);
			add(comboFilterSelectionType);
		}

		public MatrixFilterParameter getParameters() {

			return new MatrixFilterParameter(doubleSlider.getValue(),(AdjustingType) comboThresholdAdjustingMethod.getSelectedItem(),SubsequenceLength.getValue() ,(FilterLevel) comboFilterMethod.getSelectedItem(), (FilterSelection) comboFilterSelectionType.getSelectedItem(),
				(ProbabilityType)comboProbabilityComputingMethod.getSelectedItem()	,(XEventClassifier) comboAtt.getSelectedItem());

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
		MatrixFilterWizardPanel configPanel = new MatrixFilterWizardPanel(log);

		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			return run(context, log, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

	@UITopiaVariant(affiliation = "Eindhoven University of Technology", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = {0})
	public static XLog run(PluginContext context, XLog log) {
		return run(context, log, new MatrixFilterParameter());
	}

	@PluginVariant(requiredParameterLabels = {0, 1 })
	public static XLog run(PluginContext context, XLog log, MatrixFilterParameter parameters) {
		return FilterBasedOnRelationMatrixK.apply(log, parameters);
	}

}
