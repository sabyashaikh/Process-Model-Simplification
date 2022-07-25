package org.processmining.logfiltering.plugins.Sabya;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "SimplifiedProcessTreePlugin", parameterLabels = { "Event Log", "Parameters", "Process Tree"  }, 
returnLabels = {"Simplied Process Tree", " Simplified PetriNet" }, returnTypes = { ProcessTree.class, Petrinet.class })
public class SimplifiedProcessTreePlugin {
	public class SimplifiedProcessTreeWizardPanel extends JPanel {
		public XEventClassifier[] EventClassifier;
		private final NiceDoubleSlider doubleSlider = SlickerFactory.instance().createNiceDoubleSlider("Please Select the Threshold", 0, 1, 0.2, Orientation.HORIZONTAL);
		final JLabel comboEventClassifierLabel = new JLabel("Select the event classifier");
		JComboBox<XEventClassifier> comboEventClassifier;
		JLabel frequencyTypeSelectionLabel = new JLabel("Select Frequency type");
		JComboBox<String> frequencyTypeSelectionOptions = new JComboBox();
		
		@SuppressWarnings("unchecked")
		public SimplifiedProcessTreeWizardPanel(XLog log) {
			EventClassifier = log.getClassifiers().toArray(new XEventClassifier[log.getClassifiers().size()]);
			comboEventClassifier = SlickerFactory.instance().createComboBox(EventClassifier);		
			frequencyTypeSelectionOptions.addItem("Activity Frequency");
	        frequencyTypeSelectionOptions.addItem("Case Frequency");
	        frequencyTypeSelectionOptions.setSelectedItem("Case Frequency");
	        
			setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
		    constraints.anchor = GridBagConstraints.WEST;
		    constraints.insets = new Insets(10, 10, 10, 10);
			
			constraints.gridx = 0;
	        constraints.gridy = 0;     
	        add(comboEventClassifierLabel, constraints);
	 
	        constraints.gridx = 1;
	        constraints.fill= GridBagConstraints.HORIZONTAL;
	        add(comboEventClassifier, constraints);
	         
	        constraints.gridx = 0;
	        constraints.gridy = 1;    
	        constraints.gridwidth = 2;
	        add(doubleSlider, constraints);
	        
	        constraints.gridx = 0;
	        constraints.gridy = 2;    
	        add(frequencyTypeSelectionLabel, constraints);
	        
	        constraints.gridx = 1;
	        constraints.gridy = 2;    
	        add(frequencyTypeSelectionOptions, constraints);
	        
		}

		public Parameters getParameters() {
			return new Parameters((XEventClassifier) comboEventClassifier.getSelectedItem(), doubleSlider.getValue(), (String)frequencyTypeSelectionOptions.getSelectedItem(),"SimplifiedProcessTree");
		}

	}

	@UITopiaVariant(uiLabel = "Simplified Process Tree Generation", affiliation = "RWTH Aachen University", author = "Sabya", email = "sabya.shaikh@rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public Object[] run(UIPluginContext context, XLog log) {
		SimplifiedProcessTreeWizardPanel configPanel = new SimplifiedProcessTreeWizardPanel(log);
		if (!(context.showWizard("Choose Parameters", true, true, configPanel).equals(InteractionResult.FINISHED))) {
			context.getFutureResult(0).cancel(true);
			return null;
		} else {
			return run(context, log, configPanel.getParameters());
		}
	}
	 
	@PluginVariant(requiredParameterLabels = { 0, 1})
	public static Object[] run(PluginContext context, XLog log, Parameters parameters) throws UnknownTreeNodeException {
		System.out.println("Running main app");
		XEventClassifier eventClassifier = parameters.getEventClassifier();
		MiningParametersIMf miningParameters = new MiningParametersIMf();
		miningParameters.setNoiseThreshold(0);
		miningParameters.setClassifier(eventClassifier);
		System.out.println("EventClassifier: "+ eventClassifier);
		ProcessTree processTree = IMProcessTree.mineProcessTree(log, miningParameters);
		System.out.println("Created process tree");
		SimplifiedProcessTree simplifiedProcessTree = new SimplifiedProcessTree(log, processTree, parameters);
		return simplifiedProcessTree.apply();
	}

}