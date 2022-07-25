package org.processmining.logfiltering.plugins;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.algorithms.SplitMinerinProM;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Apply SplitMiner ", parameterLabels = { "Event Log" , "Parameter Object"},
returnLabels = {"Petrinet" },
returnTypes =  {Petrinet.class })
public class SplitMinerinProMPlugin {
/////////////Here We use Matrix Filter Parameter to fast develop the code
	///They are not relate to each other
	//// We did not develo the Split Miner
	///// We just call it
	@SuppressWarnings("unchecked")
	public class MatrixFilterWizardPanel extends JPanel {

		private static final long serialVersionUID = 8572008504104999027L;
		public XEventClassifier[] EventAttribute;
		private final NiceDoubleSlider doubleSlider1 = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Select the Epsilon", 0, 1, 0.1, Orientation.HORIZONTAL);
		private final NiceDoubleSlider doubleSlider2 = SlickerFactory.instance()
				.createNiceDoubleSlider("Please Selecct Frequency Threshold", 0, 1, 0.4, Orientation.HORIZONTAL);
		
		
		JComboBox<XEventClassifier> comboAtt;

		public MatrixFilterWizardPanel(XLog log) {

			EventAttribute = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboAtt = SlickerFactory.instance().createComboBox(EventAttribute);

			GridLayout layout = new GridLayout(0, 1);
			setLayout(layout);
			//add(comboFilterMethodolabel);
			//add(comboFilterMethod);
			add(doubleSlider1);
			add(doubleSlider2);
		}

		public MatrixFilterParameter getParameters() {

			return new MatrixFilterParameter(doubleSlider1.getValue(),doubleSlider2.getValue(),1, FilterSelection.SELECT ,	(XEventClassifier) comboAtt.getSelectedItem());

		}
	}
	
	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public Petrinet run(UIPluginContext context, XLog log) {
		MatrixFilterWizardPanel configPanel = new MatrixFilterWizardPanel(log);

		if (context.showWizard("Choose Filtering Parameters", true, true, configPanel)
				.equals(InteractionResult.FINISHED)) {
			return run(context, log, configPanel.getParameters());
		} else {
			context.getFutureResult(0).cancel(true);
			return null;
		}
	}
	@UITopiaVariant(affiliation = "RWTH Aachen Universityy", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = {0})
	public  static Petrinet run(PluginContext context, XLog log) {
		return run(context, log, new MatrixFilterParameter());
	}
	
	@PluginVariant(requiredParameterLabels = {0, 1 })
	public  static Petrinet run(PluginContext context, XLog log, MatrixFilterParameter parameters) {
		System.out.println("org.processmining.logfiltering.plugins.SplitMinerinProMPlugin.run(PluginContext, XLog, MatrixFilterParameter)");
		Object[] sss = SplitMinerinProM.apply2(log,parameters);
		return SplitMinerinProM.apply(log,parameters);
	
				
	}
	
	

}