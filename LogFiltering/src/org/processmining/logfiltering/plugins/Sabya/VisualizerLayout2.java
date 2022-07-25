package org.processmining.logfiltering.plugins.Sabya;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

@Plugin(name = "Visualizer For Process tree based filter model 2", parameterLabels = { "ProcessTreeBasedFilterVisualizerModel2" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class VisualizerLayout2 {
	@PluginVariant(requiredParameterLabels = { 0 })
	public static JPanel visualize(UIPluginContext context, ProcessTreeBasedFilterVisualizerModel2 model) throws Exception {
		MainView2 view = new MainView2(context, model);
		return view;
	}
}

class MainView2 extends JPanel {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	XLog inputLog;
	
	// store the left panel child object
	LeftPanel2 leftPanel;
	// store the right panel child object
	RightPanel2 rightPanel;
	
	// keep objects separated for the left and the right panel
	// (but after that make aware each other)
	
	public MainView2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model2) throws NotYetImplementedException, InvalidProcessTreeException {
		this.context = context;
		this.model = model2;

		// sets the relative layout (the occupation of components is specified by a percentage)
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		
		this.leftPanel = new LeftPanel2(context, model2);
		this.rightPanel = new RightPanel2(context, model2);

		// add the two elements to the view specifiying the percentage (e.g. 75% and 25%)
		this.add(this.leftPanel, new Float(75));
		this.add(this.rightPanel, new Float(25));
		
		// set the two panels aware of each other
		System.out.println("set aware 1");
		this.leftPanel.setAware(this.rightPanel);
		System.out.println("set aware 2");
		this.rightPanel.setAware(this.leftPanel);
		System.out.println("visualizing");
		this.leftPanel.visualize();
		System.out.println("visualized");
	}
}

class LeftPanel2 extends JPanel {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	
	// store the right panel object
	RightPanel2 rightPanel;
	
	// objects needed for Petri net visualization
	ScalableComponent scalable;
	ProMJGraph graph;
	JComponent graphComponent;
	
	public Double currentScale;
	
	public LeftPanel2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
		
		// sets the relative layout (the occupation of components is specified by a percentage)
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
	}
	
	public void setAware(RightPanel2 rightPanel) {
		this.rightPanel = rightPanel;
	}
	
	public void visualize() throws NotYetImplementedException, InvalidProcessTreeException {
		if (this.graphComponent != null) {
			// step to remove the graph from the view when it's updated
			this.remove(this.graphComponent);
			this.graphComponent = null;
		}
		this.graphComponent = graphVisualize();
		// in relative layout, this is occupying all the left panel
		this.add(graphComponent, new Float(100));
				
        this.currentScale = graph.getScale();
        this.addMouseWheelListener(new ZoomListenerOnLeftPanel2(this));
	}
	
	public void constructVisualization(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		graph.getModel().beginUpdate();
		graph.getModel().endUpdate();
		graph.refresh();
		graph.revalidate();
		graph.repaint();
	}
	
    public void setScale(Double scale) {
        this.graph.setScale(scale);
        this.currentScale = scale;
    }
	
	public static ProMJGraph buildJGraph(
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> net,
			GraphLayoutConnection layoutConnection) {
		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();

		ProMGraphModel model = new ProMGraphModel(net);
		ProMJGraph jGraph = null;

		if (layoutConnection == null || !layoutConnection.isLayedOut()) {
			if (layoutConnection == null) {
				layoutConnection = new GraphLayoutConnection(net);
			}
			jGraph = new ProMJGraph(model, map, layoutConnection);

			JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
			layout.setDeterministic(false);
			layout.setCompactLayout(false);
			layout.setFineTuning(true);
			layout.setParallelEdgeSpacing(15);
			layout.setFixRoots(false);

			layout.setOrientation(map.get(net, AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

			JGraphFacade facade = new JGraphFacade(jGraph);

			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();

			facade.run(layout, true);

			java.util.Map<?, ?> nested = facade.createNestedMap(true, true);
			jGraph.getGraphLayoutCache().edit(nested);

			jGraph.setUpdateLayout(layout);

		} else {
			jGraph = new ProMJGraph(model, map, layoutConnection);

		}
		return jGraph;
	}
	
	JComponent graphVisualize() throws NotYetImplementedException, InvalidProcessTreeException {
		scalable = buildJGraph(this.model.getPetriNet(), null);
		graph = (ProMJGraph) scalable;
		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		return graph.getComponent();
	}
}

class RightPanel2 extends JPanel {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	XLog inputLog;
	
	// store the left panel object
	LeftPanel2 leftPanel;
	
	//object to store event classifier
	JLabel EventClassifierSelectionLabel;
	JComboBox<XEventClassifier> EventClassifierSelectionOptions;
	EventClassifierSelectionActionListener2 EventClassifierSelectionListener;
	XEventClassifier EventClassifierSelectionValue;
	
	//object to store threshold
	//NiceDoubleSlider thresholdSlider;
	//ThresholdChangeListener thresholdChangeListener;
	JLabel ThresholdLabel;
	Double thresholdValue;
	ThresholdActionListener2 thresholdActionListener;
	JFormattedTextField thresholdfield;
	
	//object to store what kind of frequency to consider for removal
	JLabel FrequencyTypeSelectionLabel;
	JComboBox<String> FrequencyTypeSelectionOptions;
	FrequencyTypeSelectionActionListerner2 FrequencyTypeSelectionListerner;
	String FrequencyTypeSelectionValue;
	
	//object to store what kind of probability to consider for removal
	JLabel 	ProbabilityTypeSelectionLabel;
	JComboBox<String> ProbabilityTypeSelectionOptions;
	ProbabilityTypeSelectionActionListerner2 ProbabilityTypeSelectionListerner;
	String ProbabilityTypeSelectionValue;
	
	//object to store what kind of modification to consider for variants
	JLabel ModificationTypeSelectionLabel;
	JComboBox<String> ModificationTypeSelectionOption;
	ModificationTypeSelectionActionListener2 ModificationTypeSelectionListener;
	String ModificationTypeSelectionValue;
		
	
	//button to modify tree based on threshold
	JButton visualiseButton;
	VisualiseButtonMouseListener2 visualiseButtonMouseListener;
	
	JLabel EmptyLabel;
	JLabel ExportOptionsLabel;
	
	
	//button to update the log with process tree changed
	JButton petriNetExportButton;
	PetriNetExportButtonMouseListener2 petriNetExportButtonMouseListener;
	
	//button to export log
	JButton logExportButton;
	LogExportButtonMouseListener2 logExportButtonMouseListener;
	
	
	JButton processTreeExportButton;
	ProcessTreeExportButtonMouseListener2 processTreeExportButtonMouseListener;
	
	@SuppressWarnings("unchecked")
	public RightPanel2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
		this.inputLog = this.model.getInputLog();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	/**
	 * Method that is called to recalculate the model
	 * and update the visualization
	 * @throws InvalidProcessTreeException 
	 * @throws NotYetImplementedException 
	 * 
	 */
	public void updateModel(boolean EventClassifierChanged) throws NotYetImplementedException, InvalidProcessTreeException {
		System.out.println("updateModel");
		if(EventClassifierChanged) {
			System.out.println("computePTwithProbability called");
			this.model.computePTwithProbability();
		}
		else {
			System.out.println("modifyPTBasedOnThreshold called");
			this.model.modifyPT();
		}
		// updates the visualization
		this.leftPanel.visualize();
		this.leftPanel.updateUI();
	}
	
	public void setAware(LeftPanel2 leftPanel) {
		this.leftPanel = leftPanel;
		GridBagConstraints lastConstraints = null;
	    GridBagConstraints buttonConstraints = null;
	    GridBagConstraints labelConstraints = null;

	 
		// weightx is 1.0 for fields, 0.0 for labels
        // gridwidth is REMAINDER for fields, 1 for labels
        lastConstraints = new GridBagConstraints();
        // Stretch components horizontally (but not vertically)
        lastConstraints.fill = GridBagConstraints.HORIZONTAL;
        // Components that are too short or narrow for their space
        // Should be pinned to the northwest (upper left) corner
        lastConstraints.anchor = GridBagConstraints.NORTHWEST;
        // Give the "last" component as much space as possible
        lastConstraints.weightx = 1.0;
        // Give the "last" component the remainder of the row
        lastConstraints.gridwidth = GridBagConstraints.REMAINDER;
        // Add a little padding
        lastConstraints.insets = new Insets(1, 1, 1, 1);
        
        // Now for the "middle" field components
        buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(1, 1, 1, 1);
        buttonConstraints.anchor= GridBagConstraints.CENTER;
        buttonConstraints.gridwidth= GridBagConstraints.CENTER;
        
        // And finally the "label" constrains, typically to be
        // used for the first component on each row
        labelConstraints = (GridBagConstraints) lastConstraints.clone();
        // Give these as little space as necessary
        labelConstraints.weightx = 0.0;
        labelConstraints.gridwidth = 1;
        
        GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		
		// assign all interface elements AFTER it is being aware of the presence of the left panel :)
        this.EventClassifierSelectionLabel = new JLabel("Select classifier");
        this.EventClassifierSelectionOptions = new JComboBox(); 
        this.EventClassifierSelectionListener = new EventClassifierSelectionActionListener2(context, model, leftPanel, this, this.EventClassifierSelectionOptions);
        XEventClassifier[] selectionOptions = this.inputLog.getClassifiers().toArray(new XEventClassifier[this.inputLog.getClassifiers().size()]);
        
        for(XEventClassifier option:selectionOptions) {
        	EventClassifierSelectionOptions.addItem(option);
        }
        this.EventClassifierSelectionOptions.setSelectedItem(selectionOptions[0]);
        this.EventClassifierSelectionOptions.addActionListener(this.EventClassifierSelectionListener);
        System.out.println("selectionOptions[0]: "+ selectionOptions[0]);
        
        gbl.setConstraints(this.EventClassifierSelectionLabel, labelConstraints);
        gbl.setConstraints(this.EventClassifierSelectionOptions, lastConstraints);
        this.add(this.EventClassifierSelectionLabel);
        this.add(this.EventClassifierSelectionOptions);
        
        
        this.FrequencyTypeSelectionLabel= new JLabel("Select Frequency type");
        this.FrequencyTypeSelectionOptions = new JComboBox(); 
        this.FrequencyTypeSelectionListerner = new FrequencyTypeSelectionActionListerner2(context, model, leftPanel, this, this.FrequencyTypeSelectionOptions);
        this.FrequencyTypeSelectionOptions.addItem("Activity Frequency");
        this.FrequencyTypeSelectionOptions.addItem("Case Frequency");
        
        this.FrequencyTypeSelectionOptions.setSelectedItem("Case Frequency");
        this.FrequencyTypeSelectionOptions.addActionListener(this.FrequencyTypeSelectionListerner);
        System.out.println("selectionOptions[0]: "+ selectionOptions[0]);
        
        gbl.setConstraints(this.FrequencyTypeSelectionLabel, labelConstraints);
        gbl.setConstraints(this.FrequencyTypeSelectionOptions, lastConstraints);
        this.add(this.FrequencyTypeSelectionLabel);
        this.add(this.FrequencyTypeSelectionOptions);
        
        
        this.ProbabilityTypeSelectionLabel= new JLabel("Select Probability type");
        this.ProbabilityTypeSelectionOptions = new JComboBox(); 
        this.ProbabilityTypeSelectionListerner = new ProbabilityTypeSelectionActionListerner2(context, model, leftPanel, this, this.ProbabilityTypeSelectionOptions);
        this.ProbabilityTypeSelectionOptions.addItem("Probability based on parent");
        this.ProbabilityTypeSelectionOptions.addItem("Probability based on root");
        
        this.ProbabilityTypeSelectionOptions.setSelectedItem("Probability based on parent");
        this.ProbabilityTypeSelectionOptions.addActionListener(this.ProbabilityTypeSelectionListerner);
        System.out.println("selectionOptions[0]: "+ selectionOptions[0]);

        gbl.setConstraints(this.ProbabilityTypeSelectionLabel, labelConstraints);
        gbl.setConstraints(this.ProbabilityTypeSelectionOptions, lastConstraints);
        this.add(this.ProbabilityTypeSelectionLabel);
        this.add(this.ProbabilityTypeSelectionOptions);
        
        this.ModificationTypeSelectionLabel= new JLabel("Select Variant Modification type");
        this.ModificationTypeSelectionOption = new JComboBox(); 
        this.ModificationTypeSelectionListener = new ModificationTypeSelectionActionListener2(context, model, leftPanel, this, this.ModificationTypeSelectionOption);
        this.ModificationTypeSelectionOption.addItem("Convert");
        this.ModificationTypeSelectionOption.addItem("Remove");
        
        this.ModificationTypeSelectionOption.setSelectedItem("Convert");
        this.ModificationTypeSelectionOption.addActionListener(this.ModificationTypeSelectionListener);
        System.out.println("selectionOptions[0]: "+ selectionOptions[0]);

        gbl.setConstraints(this.ModificationTypeSelectionLabel, labelConstraints);
        gbl.setConstraints(this.ModificationTypeSelectionOption, lastConstraints);
        this.add(this.ModificationTypeSelectionLabel);
        this.add(this.ModificationTypeSelectionOption);
        
        //this.thresholdSlider = SlickerFactory.instance().createNiceDoubleSlider("Please Select the Threshold", 0, 1, 0.0, Orientation.HORIZONTAL);        
        //this.thresholdChangeListener = new ThresholdChangeListener(context, model, leftPanel, this, this.thresholdSlider);
        //this.thresholdSlider.addChangeListener(this.thresholdChangeListener);
        //this.add(this.thresholdSlider);
        
        this.ThresholdLabel = new JLabel("Choose Threshold");
        gbl.setConstraints(this.ThresholdLabel, labelConstraints);
        this.add(this.ThresholdLabel);
       
        NumberFormat format = DecimalFormat.getInstance();
        format.setMinimumFractionDigits(2);
        NumberFormatter formatter = new NumberFormatter(format);
        //formatter.setValueClass(Double.class);
        formatter.setMinimum(0.00);
        formatter.setMaximum(1.00);
        formatter.setAllowsInvalid(false);
        // If you want the value to be committed on each keystroke instead of focus lost
        //formatter.setCommitsOnValidEdit(true);
    	this.thresholdfield = new JFormattedTextField(formatter);
    	this.thresholdActionListener = new ThresholdActionListener2(context, model, leftPanel, this, this.thresholdfield);
        this.thresholdfield.setValue(0.00);
    	this.thresholdfield.getDocument().addDocumentListener(this.thresholdActionListener);
        gbl.setConstraints(this.thresholdfield, lastConstraints);
        this.add(this.thresholdfield);   
        
        
        this.visualiseButton = new JButton("Apply settings and visualise");
        this.visualiseButtonMouseListener = new VisualiseButtonMouseListener2(context, this, model);
        this.visualiseButton.addMouseListener(this.visualiseButtonMouseListener);
        buttonConstraints.gridy = 5;
        gbl.setConstraints(this.visualiseButton, buttonConstraints);
        this.add(this.visualiseButton);
        
        
        this.EmptyLabel= new JLabel("");
        labelConstraints.gridy = 7;
        gbl.setConstraints(this.EmptyLabel, labelConstraints);
        this.add(this.EmptyLabel);
        
        this.ExportOptionsLabel= new JLabel("Export Options");
        labelConstraints.gridy++;
        gbl.setConstraints(this.ExportOptionsLabel, labelConstraints);
        this.add(this.ExportOptionsLabel);
        
        buttonConstraints.gridy = 9;
        this.petriNetExportButton = new JButton("Export Petrinet");
        this.petriNetExportButtonMouseListener = new PetriNetExportButtonMouseListener2(context,this, model);
        this.petriNetExportButton.addMouseListener(this.petriNetExportButtonMouseListener);
        buttonConstraints.gridy++;
        gbl.setConstraints(this.petriNetExportButton, buttonConstraints);
        this.add(this.petriNetExportButton);
        
        this.processTreeExportButton = new JButton("Export Process Tree");
        this.processTreeExportButtonMouseListener = new ProcessTreeExportButtonMouseListener2(context, this, model);
        this.processTreeExportButton.addMouseListener(this.processTreeExportButtonMouseListener);
        buttonConstraints.gridy++;
        gbl.setConstraints(this.processTreeExportButton, buttonConstraints);
        this.add(this.processTreeExportButton);
        
        this.logExportButton = new JButton("Export filtered event log");
        this.logExportButtonMouseListener = new LogExportButtonMouseListener2(context, this, model);
        this.logExportButton.addMouseListener(this.logExportButtonMouseListener);
        buttonConstraints.gridy++;
        gbl.setConstraints(this.logExportButton, buttonConstraints);
        this.add(this.logExportButton);
        
	}
}


class EventClassifierSelectionActionListener2 implements ActionListener {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	// store the left panel object
	LeftPanel2 leftPanel;
	// store the right panel (father) object
	RightPanel2 rightPanel;
	// store the JComboBox object
	JComboBox<XEventClassifier> eventClassifierOptions;
	
	
	public EventClassifierSelectionActionListener2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, LeftPanel2 leftPanel, RightPanel2 rightPanel, JComboBox<XEventClassifier> xEventClassifierOptions) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.eventClassifierOptions = xEventClassifierOptions;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("XEvent classifier changed actionPerformed ");
		XEventClassifier selectedItemInCombo = (XEventClassifier) eventClassifierOptions.getSelectedItem();
		this.rightPanel.EventClassifierSelectionValue = selectedItemInCombo;
		//System.out.println("XEvent classifier changed from  "+this.model.parameters.getEventClassifier()+" to " +selectedItemInCombo);
		this.model.parameters.setEventClassifier(selectedItemInCombo);
		System.out.println(this.model.parameters.getEventClassifier());
			
		//this.rightPanel.logExportButton.setEnabled(false);
		//this.rightPanel.petriNetExportButton.setEnabled(false);
		//this.rightPanel.visualiseButton.setEnabled(false);
		//this.rightPanel.processTreeExportButton.setEnabled(false);
		try {
			this.rightPanel.updateModel(true);
		} catch (NotYetImplementedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidProcessTreeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//this.rightPanel.logExportButton.setEnabled(true);
		//this.rightPanel.petriNetExportButton.setEnabled(true);
		//this.rightPanel.visualiseButton.setEnabled(true);
		//this.rightPanel.processTreeExportButton.setEnabled(true);
			
	}
}

class ProbabilityTypeSelectionActionListerner2 implements ActionListener {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	// store the left panel object
	LeftPanel2 leftPanel;
	// store the right panel (father) object
	RightPanel2 rightPanel;
	// store the JComboBox object
	JComboBox<String> probabilityTypeSelectionOptions;
	
	
	public ProbabilityTypeSelectionActionListerner2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, LeftPanel2 leftPanel, RightPanel2 rightPanel, JComboBox<String> ProbabilityTypeSelectionOptions) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.probabilityTypeSelectionOptions = ProbabilityTypeSelectionOptions;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Probability type changed actionPerformed ");
		String selectedItemInCombo = (String) probabilityTypeSelectionOptions.getSelectedItem();
		this.rightPanel.ProbabilityTypeSelectionValue = selectedItemInCombo;
		this.model.parameters.setProbabilityType(selectedItemInCombo);
		System.out.println(this.model.parameters.getProbabilityType());
			//try {
			//	this.rightPanel.logExportButton.setEnabled(false);
			//	this.rightPanel.petriNetExportButton.setEnabled(false);
			//	this.rightPanel.visualiseButton.setEnabled(false);
			//	this.rightPanel.processTreeExportButton.setEnabled(false);
			//	this.rightPanel.updateModel(false);
			//	this.rightPanel.logExportButton.setEnabled(true);
			//	this.rightPanel.petriNetExportButton.setEnabled(true);
			//	this.rightPanel.visualiseButton.setEnabled(true);
			//	this.rightPanel.processTreeExportButton.setEnabled(true);
			//} catch (NotYetImplementedException | InvalidProcessTreeException e1) {
				// TODO Auto-generated catch block
			//	e1.printStackTrace();
			//}
	}
}

class FrequencyTypeSelectionActionListerner2 implements ActionListener {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	// store the left panel object
	LeftPanel2 leftPanel;
	// store the right panel (father) object
	RightPanel2 rightPanel;
	// store the JComboBox object
	JComboBox<String> frequencyTypeSelectionOptions;
	
	
	public FrequencyTypeSelectionActionListerner2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, LeftPanel2 leftPanel, RightPanel2 rightPanel, JComboBox<String> FrequencyTypeSelectionOptions) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.frequencyTypeSelectionOptions = FrequencyTypeSelectionOptions;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Frequency type changed actionPerformed ");
		String selectedItemInCombo = (String) frequencyTypeSelectionOptions.getSelectedItem();
		this.rightPanel.FrequencyTypeSelectionValue = selectedItemInCombo;
		//System.out.println("XEvent classifier changed from  "+this.model.parameters.getEventClassifier()+" to " +selectedItemInCombo);
		this.model.parameters.setFrequencyType(selectedItemInCombo);
		System.out.println(this.model.parameters.getFrequencyType());
	
		if(selectedItemInCombo.contentEquals("Activity Frequency")) {
			this.rightPanel.ProbabilityTypeSelectionOptions.setSelectedItem("Probability based on parent");
			this.rightPanel.ProbabilityTypeSelectionOptions.setEnabled(false);
		}
		else {
			this.rightPanel.ProbabilityTypeSelectionOptions.setEnabled(true);
		}
	}
}

class ModificationTypeSelectionActionListener2 implements ActionListener {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	// store the left panel object
	LeftPanel2 leftPanel;
	// store the right panel (father) object
	RightPanel2 rightPanel;
	// store the JComboBox object
	JComboBox<String> modificationTypeSelectionOption;
	
	
	public ModificationTypeSelectionActionListener2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, LeftPanel2 leftPanel, RightPanel2 rightPanel, JComboBox<String> ModificationTypeSelectionOptions) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.modificationTypeSelectionOption = ModificationTypeSelectionOptions;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Modification type changed actionPerformed ");
		String selectedItemInCombo = (String) modificationTypeSelectionOption.getSelectedItem();
		this.rightPanel.ModificationTypeSelectionValue = selectedItemInCombo;
		if(selectedItemInCombo.contentEquals("Convert")) {
			this.model.modificationTypeIsConvert = true;
		}
		else {
			this.model.modificationTypeIsConvert = false;
		}
	}
}

class ThresholdActionListener2 implements DocumentListener {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	// store the left panel object
	LeftPanel2 leftPanel;
	// store the right panel (father) object
	RightPanel2 rightPanel;
	// store the JComboBox object
	JFormattedTextField thresholdField;
	
	
	public ThresholdActionListener2(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, LeftPanel2 leftPanel, RightPanel2 rightPanel, JFormattedTextField thresholdfield) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.thresholdField = thresholdfield;
	}

	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Threshold changedUpdate");
		
	}

	public void insertUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		Double selectedValue = (Double) this.thresholdField.getValue();
		this.rightPanel.thresholdValue = selectedValue;
		System.out.println("Threshold changed to :"+ selectedValue);
		this.model.parameters.setThreshold(selectedValue);
	}

	public void removeUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		Double selectedValue = (Double) this.thresholdField.getValue();
		this.rightPanel.thresholdValue = selectedValue;
		System.out.println("Threshold changed to :"+ selectedValue);
		this.model.parameters.setThreshold(selectedValue);
	}
}


/*class ThresholdChangeListener implements ChangeListener {
	// store always the context and the model
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel2 rightPanel;
	
	// store the NiceDoubleSlider object
	NiceDoubleSlider thresholdSlider;
	
	
	public ThresholdChangeListener(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, LeftPanel leftPanel, RightPanel2 rightPanel, JFormattedTextField thresholdfield) {
		System.out.println("Threshold listener constructor called");
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.thresholdField = thresholdfield;
	}

	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("Threshold changed");
		Double selectedValue = (Double) this.thresholdField.getValue();
		this.rightPanel.thresholdValue = selectedValue;
		this.model.parameters.setThreshold(selectedValue);
		try {
			this.rightPanel.updateModel(false);
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidProcessTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}
*/

class ZoomListenerOnLeftPanel2 implements MouseWheelListener {
    LeftPanel2 leftPanel;
    
    public ZoomListenerOnLeftPanel2(LeftPanel2 leftPanel) {
           this.leftPanel = leftPanel;
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
           // TODO Auto-generated method stub
           int stps = e.getWheelRotation();
           
           Double newScale = this.leftPanel.currentScale;
           
           if (stps == -1) {
                 newScale *= 1.2;
           }
           else if (stps == 1) {
                 newScale *= 0.9;
           }
           
           this.leftPanel.setScale(newScale);

           System.out.println(stps);
    }
}
class PetriNetExportButtonMouseListener2 implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	RightPanel2 rightPanel;
	
	public PetriNetExportButtonMouseListener2(PluginContext context, RightPanel2 rightPanel, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
		this.rightPanel = rightPanel;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println(this.model.processing);
		//if(!this.model.processing) {
			try {
				this.rightPanel.logExportButton.setEnabled(false);
				this.rightPanel.petriNetExportButton.setEnabled(false);
				this.rightPanel.visualiseButton.setEnabled(false);
				this.rightPanel.processTreeExportButton.setEnabled(false);
				this.model.exportPetrinet();
				this.rightPanel.logExportButton.setEnabled(true);
				this.rightPanel.petriNetExportButton.setEnabled(true);
				this.rightPanel.visualiseButton.setEnabled(true);
				this.rightPanel.processTreeExportButton.setEnabled(true);
			} catch (NotYetImplementedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidProcessTreeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		/*}
		else {
			JOptionPane.showMessageDialog(new JFrame(), "Please wait! Still processing...", "Dialog",
		            JOptionPane.ERROR_MESSAGE);
		}*/
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class VisualiseButtonMouseListener2 implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	RightPanel2 rightPanel;
	
	public VisualiseButtonMouseListener2(PluginContext context, RightPanel2 rightPanel, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
		this.rightPanel = rightPanel;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println(this.model.processing);
		//if(!this.model.processing) {
			try {
				this.rightPanel.logExportButton.setEnabled(false);
				this.rightPanel.petriNetExportButton.setEnabled(false);
				//this.rightPanel.visualiseButton.setEnabled(false);
				this.rightPanel.processTreeExportButton.setEnabled(false);
				this.rightPanel.updateModel(false);
				this.rightPanel.logExportButton.setEnabled(true);
				this.rightPanel.petriNetExportButton.setEnabled(true);
				//this.rightPanel.visualiseButton.setEnabled(true);
				this.rightPanel.processTreeExportButton.setEnabled(true);
			} catch (NotYetImplementedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidProcessTreeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		/*}
		else {
			JOptionPane.showMessageDialog(new JFrame(), "Please wait! Still processing...", "Dialog",
		            JOptionPane.ERROR_MESSAGE);
		}*/
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class ProcessTreeExportButtonMouseListener2 implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	RightPanel2 rightPanel;
	
	public ProcessTreeExportButtonMouseListener2(PluginContext context, RightPanel2 rightPanel, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
		this.rightPanel = rightPanel;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println(this.model.processing);
		//if(!this.model.processing) {
			try {
				this.rightPanel.logExportButton.setEnabled(false);
				this.rightPanel.petriNetExportButton.setEnabled(false);
				this.rightPanel.visualiseButton.setEnabled(false);
				this.rightPanel.processTreeExportButton.setEnabled(false);
				this.model.exportProcessTree();
				this.rightPanel.logExportButton.setEnabled(true);
				this.rightPanel.petriNetExportButton.setEnabled(true);
				this.rightPanel.visualiseButton.setEnabled(true);
				this.rightPanel.processTreeExportButton.setEnabled(true);
			} catch (NotYetImplementedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidProcessTreeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		/*}
		else {
			JOptionPane.showMessageDialog(new JFrame(), "Please wait! Still processing...", "Dialog",
		            JOptionPane.ERROR_MESSAGE);
		}*/
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

class LogExportButtonMouseListener2 implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	RightPanel2 rightPanel;
	
	public LogExportButtonMouseListener2(PluginContext context, RightPanel2 rightPanel, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
		this.rightPanel = rightPanel;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println(this.model.processing);
		
			this.rightPanel.logExportButton.setEnabled(false);
			this.rightPanel.petriNetExportButton.setEnabled(false);
			this.rightPanel.visualiseButton.setEnabled(false);
			this.rightPanel.processTreeExportButton.setEnabled(false);
			this.model.updateLog();
			this.model.exportLog();
			this.rightPanel.logExportButton.setEnabled(true);
			this.rightPanel.petriNetExportButton.setEnabled(true);
			this.rightPanel.visualiseButton.setEnabled(true);
			this.rightPanel.processTreeExportButton.setEnabled(true);
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}

/*class PetriNetExportMouseListener implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	
	public PetriNetExportMouseListener(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model) {
		this.context = context;
		this.model = model;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		this.model.exportPetrinet();
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

/*class RestoreParametersMouseListener implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	RightPanel2 rightPanel;
	
	public RestoreParametersMouseListener(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, RightPanel2 rightPanel) {
		this.context = context;
		this.model = model;
		this.rightPanel = rightPanel;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		this.rightPanel.restoreFirstParameters();
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

/*class SaveParametersMouseListener implements MouseListener {
	PluginContext context;
	ProcessTreeBasedFilterVisualizerModel2 model;
	RightPanel2 rightPanel;
	
	public SaveParametersMouseListener(PluginContext context, ProcessTreeBasedFilterVisualizerModel2 model, RightPanel2 rightPanel) {
		this.context = context;
		this.model = model;
		this.rightPanel = rightPanel;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		this.rightPanel.saveParameters();
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}*/