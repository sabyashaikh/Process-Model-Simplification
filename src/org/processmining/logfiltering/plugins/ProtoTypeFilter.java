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
import org.processmining.logfiltering.algorithms.ProtoTypeSelectionAlgo2;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.ClusteringType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.SamplingReturnType;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;



@Plugin(name = "Super Instance Selection", parameterLabels = { "Event Log", "Parameter Object" }, returnLabels = {
		"Super Instances" }, returnTypes = { XLog.class })
public class ProtoTypeFilter {

	@SuppressWarnings("unchecked")
public class MatrixFilterWizardPanel extends JPanel {
		
		
		private static final long serialVersionUID = 8572008504104999027L;
		private  SamplingReturnType[] ReturnOptions = SamplingReturnType.values();
		private PrototypeType[] PrototypeTypes = PrototypeType.values();
		public XEventClassifier[] EventAttribute;
		private FilterLevel[] chooseName = FilterLevel.values();
		private  SimilarityMeasure[] SimilarityMeasures = SimilarityMeasure.values();
		private  ClusteringType[] ClusteringMethod = ClusteringType.values();
		
		private final NiceDoubleSlider doubleSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Selecct the Threshold", 0, 1, 0.25, Orientation.HORIZONTAL);
		private NiceIntegerSlider SubsequenceLength= SlickerFactory.instance().createNiceIntegerSlider("Please Select the number of clusters", 0, 200, 5, Orientation.HORIZONTAL);
		private final JComboBox<FilterLevel> comboReturnMethod = SlickerFactory.instance().createComboBox(ReturnOptions);
		private final JComboBox<SimilarityMeasure> comboSamplingMethod = SlickerFactory.instance().createComboBox(SimilarityMeasures);
		final JLabel comboFilterMethodolabel = new JLabel("Please make a selection...");
		private final JComboBox<FilterSelection> comboProtoTypeSelectionType = SlickerFactory.instance().createComboBox(PrototypeTypes);
		final JLabel comboProtoTypeSelectionTypelabel = new JLabel("Please Select How increment the alignment");
		final JLabel comboAttlabel = new JLabel("Select the Event column");
		private final JComboBox<SimilarityMeasure> comboClusteringMethod = SlickerFactory.instance().createComboBox(ClusteringMethod);
		
		JComboBox<XEventClassifier> comboAtt;

		public MatrixFilterWizardPanel(XLog log) {

			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);

			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);
			//add(comboProtoTypeSelectionTypelabel);
			//add(comboProtoTypeSelectionType);
			
			//add(comboReturnMethod);
			//add(comboSamplingMethod);
			//add(doubleSlider);
			add(SubsequenceLength);
			add(comboAttlabel);
			add(comboAtt);
			//add(comboProtoTypeSelectionType);
			//add(comboProtoTypeSelectionTypelabel);
			//add(comboSamplingMethod);
			add(comboClusteringMethod);
		}

		public MatrixFilterParameter getParameters() {
			return new MatrixFilterParameter(SubsequenceLength.getValue(),
					(XEventClassifier) comboAtt.getSelectedItem(), (ClusteringType)comboClusteringMethod.getSelectedItem());
		}
		
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public XLog run(UIPluginContext context, XLog log) {
//
	//	TransEvClassMapping mapping = constructTransEvMapping(context, log, net);
		
		MatrixFilterWizardPanel configPanel = new MatrixFilterWizardPanel(log);

		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			
			return run(context, log, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}

	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = {0})
	public static XLog run(PluginContext context, XLog log, MatrixFilterParameter matrixFilterParameter) {
		return ProtoTypeSelectionAlgo2.apply(log, matrixFilterParameter);
	}

	


	
	
	




}