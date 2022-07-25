package org.processmining.logfiltering.visualizerPlugin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.logfiltering.parameters.AbsteractionType;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.visConstants.FilteringConstants;
import org.processmining.logfiltering.visConstants.ModelConstants;
import org.processmining.logfiltering.visFilteringAlgorithms.AFAFilteringAlgorithmParameters;
import org.processmining.logfiltering.visFilteringAlgorithms.ApplyFilteringAlgorithm;
import org.processmining.logfiltering.visFilteringAlgorithms.FilteringAlgorithmFactory;
import org.processmining.logfiltering.visFilteringAlgorithms.FilteringAlgorithmParameters;
import org.processmining.logfiltering.visFilteringAlgorithms.FrequencyFilteringAlgorithmParameters;
import org.processmining.logfiltering.visFilteringAlgorithms.MatrixFilteringAlgorithmParameters;
import org.processmining.logfiltering.visFilteringAlgorithms.NoFilteringAlgorithmParameters;
import org.processmining.logfiltering.visFilteringAlgorithms.SFFilteringAlgorithmParameters;
import org.processmining.logfiltering.visFilteringAlgorithms.VariantFilteringAlgorithmParameters;
import org.processmining.logfiltering.visMiningAlgorithms.AlphaAlgorithmParameters;
import org.processmining.logfiltering.visMiningAlgorithms.ApplyAlphaAlgorithm;
import org.processmining.logfiltering.visMiningAlgorithms.ApplyInductiveMiner;
import org.processmining.logfiltering.visMiningAlgorithms.ApplyMiningAlgorithm;
import org.processmining.logfiltering.visMiningAlgorithms.InductiveMinerParameters;
import org.processmining.logfiltering.visMiningAlgorithms.MiningAlgorithmParameters;
import org.processmining.logfiltering.visMiningAlgorithms.MiningAlgorithmsFactory;
import org.processmining.logfiltering.visualizerModel.FilteringVisualizerModel;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

@Plugin(name = "Visualize Filtering model", parameterLabels = { "FilteringVisualizerModel" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class FiltVisModelVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public static JPanel visualize(UIPluginContext context, FilteringVisualizerModel model) throws Exception {
		MainView view = new MainView(context, model);
		
		return view;
	}
}

class MainView extends JPanel {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	
	// store the left panel child object
	LeftPanel leftPanel;
	// store the right panel child object
	RightPanel rightPanel;
	
	// keep objects separated for the left and the right panel
	// (but after that make aware each other)
	
	public MainView(PluginContext context, FilteringVisualizerModel model) {
		this.context = context;
		this.model = model;

		// sets the relative layout (the occupation of components is specified by a percentage)
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
		
		this.leftPanel = new LeftPanel(context, model);
		this.rightPanel = new RightPanel(context, model);

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

class LeftPanel extends JPanel {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	
	// store the right panel object
	RightPanel rightPanel;
	
	// objects needed for Petri net visualization
	ScalableComponent scalable;
	ProMJGraph graph;
	JComponent graphComponent;
	
	public Double currentScale;
	
	public LeftPanel(PluginContext context, FilteringVisualizerModel model) {
		this.context = context;
		this.model = model;
		
		// sets the relative layout (the occupation of components is specified by a percentage)
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill( true );
		this.setLayout(rl);
	}
	
	public void setAware(RightPanel rightPanel) {
		this.rightPanel = rightPanel;
	}
	
	public void visualize() {
		if (this.graphComponent != null) {
			// step to remove the graph from the view when it's updated
			this.remove(this.graphComponent);
			this.graphComponent = null;
		}
		this.graphComponent = graphVisualize();
		// in relative layout, this is occupying all the left panel
		this.add(graphComponent, new Float(100));
		
		this.rightPanel.etConformanceVisualizer.update();
				
        this.currentScale = graph.getScale();
        this.addMouseWheelListener(new ZoomListenerOnLeftPanel(this));
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
	
	JComponent graphVisualize() {
		scalable = buildJGraph(this.model.getNet(), null);
		graph = (ProMJGraph) scalable;
		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		return graph.getComponent();
	}
}

class RightPanel extends JPanel {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	
	// store the left panel object
	LeftPanel leftPanel;
	
	// store the saved parameters
	public Map<String, Map<String, Object>> savedParameters;
	public Map<String, FilteringAlgorithmFactory.algorithms> filteringType;
	
	// variables to store the current selection of mining and filtering algorithms
	// (we set the visibility to Protected as they should be accessible from child objects)
	protected MiningAlgorithmsFactory.algorithms selectedMiningAlgorithm;
	protected FilteringAlgorithmFactory.algorithms selectedFilteringAlgorithm;
	// variables to store possibly the selection of the user regarding parameters
	protected MiningAlgorithmParameters miningAlgorithmParameters;
	protected FilteringAlgorithmParameters filteringAlgorithmParameters;
	
	
	// label and checkbox for mining algorithm selection
	JLabel miningAlgorithmLabel;
	JComboBox<String> miningAlgorithmSelection;
	MiningAlgorithmSelectionActionListener miningAlgorithmSelectionActionListener;
	
	// label and checkbox for filtering algorithm selection
	JLabel filteringAlgorithmLabel;
	JComboBox<String> filteringAlgorithmSelection;
	FilteringAlgorithmSelectionActionListener filteringAlgorithmSelectionActionListener;
	
	ETConformanceVisualizer etConformanceVisualizer;
	
	JLabel precisionMeasurementLabel;
	JComboBox<String> precisionMeasurementSelection;
	PrecisionSelectionActionListener precisionSelectionActionListener;
	String precisionMeasurementSelectedValue;
	
	JLabel filteringSelectionLabel;
	JComboBox<String> filteringSelectionOptions;
	FilteringSelectionActionListener filteringSelectionActionListener;
	String filteringSelectionSelectedValue;
	
	JButton logExportButton;
	LogExportButtonMouseListener logExportButtonMouseListener;
	
	JButton petriNetExportButton;
	PetriNetExportMouseListener petriNetExportButtonMouseListener;
	
	JButton saveParameters;
	SaveParametersMouseListener saveParametersMouseListener;
	
	JButton restoreParameters;
	RestoreParametersMouseListener restoreParametersMouseListener;
	
	public RightPanel(PluginContext context, FilteringVisualizerModel model) {
		this.context = context;
		this.model = model;
		
		this.selectedMiningAlgorithm = ModelConstants.defaultMiningAlgorithm;
		this.selectedFilteringAlgorithm = ModelConstants.defaultFilteringMethod;
		// sets algorithms parameters to null (default options are taken)
		this.miningAlgorithmParameters = null;
		this.filteringAlgorithmParameters = null;
		
		this.precisionMeasurementSelectedValue = "etconformance";
		
		this.savedParameters = new HashMap<String, Map<String, Object>>();
		this.filteringType = new HashMap<String, FilteringAlgorithmFactory.algorithms>();
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}
	
	public void saveParameters() {
		if (this.filteringAlgorithmSelectionActionListener != null) {
			if (this.filteringAlgorithmSelectionActionListener.optionsPanel != null) {
				Map<String, Object> parameters = new HashMap<String, Object>(this.filteringAlgorithmSelectionActionListener.optionsPanel.getParameters());
				
				String desc = this.filteringAlgorithmSelectionActionListener.optionsPanel.getParametersDescription();
				FilteringAlgorithmFactory.algorithms filteringType = this.filteringAlgorithmSelectionActionListener.optionsPanel.getFilteringType();
				
				this.savedParameters = new HashMap<String, Map<String, Object>>();
				this.filteringType = new HashMap<String, FilteringAlgorithmFactory.algorithms>();
				this.savedParameters.put(desc, parameters);
				this.filteringType.put(desc, filteringType);
				
				System.out.println("this.savedParameters");
				System.out.println(this.savedParameters);
			}
		}
	}
	
	public void restoreFirstParameters() {
		if (this.filteringAlgorithmSelectionActionListener != null) {
			if (this.filteringAlgorithmSelectionActionListener.optionsPanel != null) {
				//deleteAndUpdateSeletion
				List<String> savedParametersKeys = new ArrayList<String>(this.savedParameters.keySet());
				if (savedParametersKeys.size() > 0) {
					String chosenKey = savedParametersKeys.get(0);
					FilteringAlgorithmFactory.algorithms userSelection = this.filteringType.get(chosenKey);
					Map<String, Object> parameters = this.savedParameters.get(chosenKey);
					FilteringAlgorithmFactory factory = new FilteringAlgorithmFactory();
					String userSelectionString = factory.invAlgorithmLabels.get(userSelection);
					
					System.out.println(userSelectionString);
					
					this.filteringAlgorithmSelection.setSelectedItem(userSelectionString);
					
					this.filteringAlgorithmSelectionActionListener.deleteAndUpdateSeletion(userSelection);
					this.filteringAlgorithmSelectionActionListener.optionsPanel.setParameters(parameters);
					
					System.out.println("Restored parameters for "+chosenKey);
				}
				
				//FilteringAlgorithmFactory.algorithms userSelection = this.filteringType.get
			}
		}
	}
	
	/**
	 * Method that is called to recalculate the model
	 * and update the visualization
	 * 
	 */
	public void updateModel(boolean openDialogs) {
		System.out.println("updateModel");
		
		// call the factory to get the implementation of the selected mining algorithm
		MiningAlgorithmsFactory miningAlgorithmsFactory = new MiningAlgorithmsFactory();
		ApplyMiningAlgorithm miningAlgorithm = miningAlgorithmsFactory.getMiningAlgorithm(selectedMiningAlgorithm);
		
		if (miningAlgorithm instanceof ApplyInductiveMiner) {
			if (miningAlgorithmParameters == null || !(miningAlgorithmParameters instanceof InductiveMinerParameters)) {
				miningAlgorithmParameters = new InductiveMinerParameters();
			}
			if (openDialogs) {
				Double noiseThreshold = Double.parseDouble(JOptionPane.showInputDialog(null, "Inserts the noise threshold for the Inductive Miner process discovery algorithm (0.0 ensures perfect fitness)", 0.0));
				miningAlgorithmParameters.setParameter("noiseThreshold", noiseThreshold);
			}
		}
		else if (miningAlgorithm instanceof ApplyAlphaAlgorithm) {
			if (miningAlgorithmParameters == null || !(miningAlgorithmParameters instanceof AlphaAlgorithmParameters)) {
				miningAlgorithmParameters = new AlphaAlgorithmParameters();
			}
		}
		
		// call the factory to get the implementation of the selected filtering algorithm
		FilteringAlgorithmFactory filteringAlgorithmFactory = new FilteringAlgorithmFactory();
		ApplyFilteringAlgorithm filteringAlgorithm = filteringAlgorithmFactory.getFilteringAlgorithm(selectedFilteringAlgorithm);
		
		if (this.filteringAlgorithmParameters != null) {
			if (this.filteringSelectionSelectedValue.equals("SELECT")) {
				System.out.println("XXXXXX SELECT");
				filteringAlgorithmParameters.setFilteringSelection(FilterSelection.SELECT);
			}
			else {
				System.out.println("YYYYYY REMOVE");
				filteringAlgorithmParameters.setFilteringSelection(FilterSelection.REMOVE);
			}
		}
		
		// calls the method to do the filtering and obtaining the new Petri net
		this.model.filterLogAndGetPetrinet(miningAlgorithm, filteringAlgorithm, miningAlgorithmParameters, filteringAlgorithmParameters, this.precisionMeasurementSelectedValue);
		System.out.println("`filtering parameter"+ filteringAlgorithmParameters);
		// updates the visualization
		this.leftPanel.visualize();
		this.leftPanel.updateUI();
		
		System.out.println("updating conformance results");
		this.leftPanel.rightPanel.etConformanceVisualizer.update();
		System.out.println("updated conformance results");
	}
	
	public void setAware(LeftPanel leftPanel) {
		this.leftPanel = leftPanel;
		
		this.filteringSelectionSelectedValue = "REMOVE";
		
        this.etConformanceVisualizer = new ETConformanceVisualizer(context, model, leftPanel, this);
        this.add(this.etConformanceVisualizer);
		
		// assign all interface elements AFTER it is being aware of the presence of the left panel :)
		
		this.miningAlgorithmLabel = new JLabel("Please select the process discovery algorithm");
		
		this.miningAlgorithmSelection = new JComboBox() {
            /** 
             * @inherited <p>
             */
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                //max.height = getPreferredSize().height;
                max.height = 50;
                return max;
            }
        };
        
        this.miningAlgorithmSelection.setSize(new Dimension(100, 50));
        this.miningAlgorithmSelection.setPreferredSize(new Dimension(100, 50));
        
        MiningAlgorithmsFactory algorithmsFactory = new MiningAlgorithmsFactory();
        
        for (String miningAlgorithm : algorithmsFactory.algorithmsLabels.keySet()) {
        	this.miningAlgorithmSelection.addItem(miningAlgorithm);
        }
        this.miningAlgorithmSelection.setSelectedIndex(1);
        this.miningAlgorithmSelection.setSelectedIndex(1);
        
        this.miningAlgorithmSelectionActionListener = new MiningAlgorithmSelectionActionListener(context, model, leftPanel, this, this.miningAlgorithmSelection);
        this.miningAlgorithmSelection.addActionListener(this.miningAlgorithmSelectionActionListener);
        
		this.add(miningAlgorithmLabel);
		this.add(miningAlgorithmSelection);
        
        this.filteringAlgorithmLabel = new JLabel("Please select the filtering algorithm");
        
        this.filteringAlgorithmSelection = new JComboBox() {
            /** 
             * @inherited <p>
             */
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                //max.height = getPreferredSize().height;
                max.height = 50;
                return max;
            }
        };
        
        this.filteringAlgorithmSelection.setSize(new Dimension(100, 50));
        this.filteringAlgorithmSelection.setPreferredSize(new Dimension(100, 50));
        
        this.filteringAlgorithmSelectionActionListener = new FilteringAlgorithmSelectionActionListener(context, model, leftPanel, this, this.filteringAlgorithmSelection);
        this.filteringAlgorithmSelection.addActionListener(this.filteringAlgorithmSelectionActionListener);
        
        FilteringAlgorithmFactory filteringFactory = new FilteringAlgorithmFactory();
        
        for (String filteringAlgorithm : filteringFactory.algorithmsLabels.keySet()) {
        	this.filteringAlgorithmSelection.addItem(filteringAlgorithm);
        }
        
        this.filteringAlgorithmSelection.setSelectedItem("No filtering");
        
        this.add(filteringAlgorithmLabel);
        this.add(filteringAlgorithmSelection);
        
        this.precisionMeasurementLabel = new JLabel("Fitness measurement method");

        this.precisionMeasurementSelection = new JComboBox() {
            /** 
             * @inherited <p>
             */
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                //max.height = getPreferredSize().height;
                max.height = 50;
                return max;
            }
        };
        
        this.precisionMeasurementSelection.setSize(new Dimension(100, 50));
        this.precisionMeasurementSelection.setPreferredSize(new Dimension(100, 50));
        
        this.precisionMeasurementSelection.setSize(new Dimension(100, 50));
        this.precisionMeasurementSelection.setPreferredSize(new Dimension(100, 50));
        
        this.precisionMeasurementSelection.addItem("ETConformance");
        this.precisionMeasurementSelection.addItem("Alignments");
        this.precisionMeasurementSelection.setSelectedItem("ETConformance");
        
        this.precisionSelectionActionListener = new PrecisionSelectionActionListener(context, model, leftPanel, this, this.precisionMeasurementSelection);
        this.precisionMeasurementSelection.addActionListener(this.precisionSelectionActionListener);
        
        this.add(this.precisionMeasurementLabel);
        this.add(this.precisionMeasurementSelection);
        
        this.filteringSelectionLabel = new JLabel("Filtering selection");

        this.filteringSelectionOptions = new JComboBox() {
            /** 
             * @inherited <p>
             */
            @Override
            public Dimension getMaximumSize() {
                Dimension max = super.getMaximumSize();
                //max.height = getPreferredSize().height;
                max.height = 50;
                return max;
            }
        };
        
        this.filteringSelectionOptions.setSize(new Dimension(100, 50));
        this.filteringSelectionOptions.setPreferredSize(new Dimension(100, 50));
        
        this.filteringSelectionOptions.setSize(new Dimension(100, 50));
        this.filteringSelectionOptions.setPreferredSize(new Dimension(100, 50));
        
        this.filteringSelectionOptions.addItem("REMOVE");
        this.filteringSelectionOptions.addItem("SELECT");
        this.filteringSelectionOptions.setSelectedItem("REMOVE");
        
        this.filteringSelectionActionListener = new FilteringSelectionActionListener(context, model, leftPanel, this, this.filteringSelectionOptions);
        this.filteringSelectionOptions.addActionListener(this.filteringSelectionActionListener);
        
        this.add(this.filteringSelectionLabel);
        this.add(this.filteringSelectionOptions);
        
        this.logExportButton = new JButton("Export log");
        
        this.logExportButtonMouseListener = new LogExportButtonMouseListener(context, model);
        this.logExportButton.addMouseListener(this.logExportButtonMouseListener);
        
        this.add(this.logExportButton);
        
        this.petriNetExportButton = new JButton("Export Petri net");
        
        this.petriNetExportButtonMouseListener = new PetriNetExportMouseListener(context, model);
        this.petriNetExportButton.addMouseListener(this.petriNetExportButtonMouseListener);
        
        this.add(this.petriNetExportButton);
        
        /*
         * 	JButton saveParameters;
	SaveParametersMouseListener saveParametersMouseListener;
	
	JButton restoreParameters;
	RestoreParametersMouseListener restoreParametersMouseListener;
         */
        
        this.saveParameters = new JButton("Save parameters");
        this.saveParametersMouseListener = new SaveParametersMouseListener(context, model, this);
        this.saveParameters.addMouseListener(this.saveParametersMouseListener);
        
        this.restoreParameters = new JButton("Restore parameters");
        this.restoreParametersMouseListener = new RestoreParametersMouseListener(context, model, this);
        this.restoreParameters.addMouseListener(this.restoreParametersMouseListener);
        
        this.add(this.saveParameters);
        this.add(this.restoreParameters);
	}
}

class MiningAlgorithmSelectionActionListener implements ActionListener {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	// store the JComboBox object
	JComboBox<String> miningAlgorithmSelection;
	
	public MiningAlgorithmSelectionActionListener(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel, JComboBox<String> miningAlgorithmSelection) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.miningAlgorithmSelection = miningAlgorithmSelection;
	}

	public void actionPerformed(ActionEvent e) {
		// in this case the algorithm was changed
		// gets the corresponding discovery algorithm
		System.out.println("MiningAlgorithmSelectionActionListener actionPerformed ");
		
		String selectedItemInCombo = (String) miningAlgorithmSelection.getSelectedItem();
		
		// gets the corresponding algorithm from the factory and saves the user selection
		MiningAlgorithmsFactory miningAlgorithmsFactory = new MiningAlgorithmsFactory();
		MiningAlgorithmsFactory.algorithms newUserSelection = miningAlgorithmsFactory.algorithmsLabels.get(selectedItemInCombo);
		
		if (newUserSelection != rightPanel.selectedMiningAlgorithm) {
			// the selection has gone into something different
			// save the new option and update the model
			rightPanel.selectedMiningAlgorithm = newUserSelection;
			rightPanel.updateModel(true);
		}
	}
}

class FrequencyParametersActionListener implements ActionListener {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	// store the JComboBox object
	JComboBox<AbsteractionType> abstractionType;
	
	// filtering options panel (stored object)
	
	FilteringMethodOptionInterface optionsPanel;
	
	FrequencyFilteringMethodOptionInterface parent;
	
	public FrequencyParametersActionListener(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel, JComboBox<AbsteractionType> abstractionType, FrequencyFilteringMethodOptionInterface parent) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.abstractionType = abstractionType;
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("FrequencyParametersActionListener something happened");
		System.out.println("selected value: "+this.abstractionType.getSelectedItem());
		this.parent.setAbstractionType((AbsteractionType) this.abstractionType.getSelectedItem());
	}
}

class PrecisionSelectionActionListener implements ActionListener {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	// store the JComboBox object
	JComboBox<String> precisionAlgorithmSelection;
	
	// filtering options panel (stored object)
	FilteringMethodOptionInterface optionsPanel;
	
	public PrecisionSelectionActionListener(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel, JComboBox<String> precisionAlgorithmSelection) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.precisionAlgorithmSelection = precisionAlgorithmSelection;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("FilteringAlgorithmSelectionActionListener actionPerformed ");
		
		String selectedItemInCombo = (String) precisionAlgorithmSelection.getSelectedItem();
		
		this.rightPanel.precisionMeasurementSelectedValue = selectedItemInCombo;
		
		this.rightPanel.updateModel(false);
	}
}

class FilteringSelectionActionListener implements ActionListener {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	// store the JComboBox object
	JComboBox<String> filteringSelection;
	
	// filtering options panel (stored object)
	FilteringMethodOptionInterface optionsPanel;
	
	public FilteringSelectionActionListener(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel, JComboBox<String> filteringSelection) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.filteringSelection = filteringSelection;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("FilteringAlgorithmSelectionActionListener actionPerformed ");
		
		String selectedItemInCombo = (String) filteringSelection.getSelectedItem();
		
		this.rightPanel.filteringSelectionSelectedValue = selectedItemInCombo;
		
		this.rightPanel.updateModel(false);
	}
}

class FilteringAlgorithmSelectionActionListener implements ActionListener {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	// store the JComboBox object
	JComboBox<String> filteringAlgorithmSelection;
	
	// filtering options panel (stored object)
	FilteringMethodOptionInterface optionsPanel;
	
	public FilteringAlgorithmSelectionActionListener(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel, JComboBox<String> filteringAlgorithmSelection) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		this.filteringAlgorithmSelection = filteringAlgorithmSelection;
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		System.out.println("FilteringAlgorithmSelectionActionListener actionPerformed ");
		
		String selectedItemInCombo = (String) filteringAlgorithmSelection.getSelectedItem();
		
		// gets the corresponding algorithm from the factory and saves the user selection
		FilteringAlgorithmFactory filteringAlgorithmFactory = new FilteringAlgorithmFactory();
		FilteringAlgorithmFactory.algorithms newUserSelection = filteringAlgorithmFactory.algorithmsLabels.get(selectedItemInCombo);
		
		if (newUserSelection != rightPanel.selectedFilteringAlgorithm) {
			this.deleteAndUpdateSeletion(newUserSelection);
		}
	}
	
	public FilteringMethodOptionInterface deleteAndUpdateSeletion(FilteringAlgorithmFactory.algorithms newUserSelection) {
		// the selection has gone into something different
		// save the new option and update the model
		rightPanel.selectedFilteringAlgorithm = newUserSelection;
		rightPanel.updateModel(false);
		
		System.out.println("ZZZZZZZZZZZ "+rightPanel.selectedFilteringAlgorithm);
		
		// we get the panel to be added to the right panel (options)
		FilteringMethodOptionInterfaceFactory interfaceFactory = new FilteringMethodOptionInterfaceFactory(context, model, leftPanel, rightPanel);
		FilteringMethodOptionInterface newOptionsPanel = interfaceFactory.getOptionsInterface(newUserSelection);
		
		// there was previously a panel of options .. destroy him
		if (this.optionsPanel != null) {
			this.optionsPanel.removeItself();
			this.optionsPanel = null;
		}
		
		// check if it's not null .. if it's not null then add it
		if (newOptionsPanel != null) {
			System.out.println("newOptionsPanel != null");
			
			this.optionsPanel = newOptionsPanel;
			this.optionsPanel.addItself();
		}
		
		return newOptionsPanel;
	}
}

class FilteringMethodOptionInterfaceFactory {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	
	public FilteringMethodOptionInterfaceFactory(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
	}
	
	public FilteringMethodOptionInterface getOptionsInterface(FilteringAlgorithmFactory.algorithms userSelection) {
		if (userSelection != null) {
			if (userSelection.equals(FilteringAlgorithmFactory.algorithms.VARIANT)) {
				return new VariantFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
			}
			else if (userSelection.equals(FilteringAlgorithmFactory.algorithms.NO)) {
				return new NoFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
			}
			else if (userSelection.equals(FilteringAlgorithmFactory.algorithms.MATRIX)) {
				return new MatrixFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
			}
			else if (userSelection.equals(FilteringAlgorithmFactory.algorithms.AFA)) {
				return new AFAFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
			}
			else if (userSelection.equals(FilteringAlgorithmFactory.algorithms.SF)) {
				return new SFFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
			}
			else if (userSelection.equals(FilteringAlgorithmFactory.algorithms.FF)) {
				return new FrequencyFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
			}
		}
		return new NoFilteringMethodOptionInterface(context, model, leftPanel, rightPanel);
	}
}

class FilteringMethodOptionInterface extends JPanel {
	// store always the context and the model
	PluginContext context;
	FilteringVisualizerModel model;
	// store the left panel object
	LeftPanel leftPanel;
	// store the right panel (father) object
	RightPanel rightPanel;
	
	public FilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
	}

	/**
	 * The elements adds itself to the right panel
	 */
	public void addItself() {
		this.rightPanel.add(this);
	}
	
	/**
	 * The element removes itself from the right panel
	 */
	public void removeItself() {
		this.rightPanel.remove(this);
	}
	
	public Map<String, Object> getParameters() {
		return null;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		
	}
	
	public String getParametersDescription() {
		return "";
	}
	
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return null;
	}
}


class FilteringOptionsChangeListener implements ChangeListener {
	// we need to store the parent object:
	// parameters will change in that object
	FilteringMethodOptionInterface parentInterface;
	// we keep the method name to call in the parent
	String methodName;
	
	public FilteringOptionsChangeListener(FilteringMethodOptionInterface parentInterface, String methodName) {
		this.parentInterface = parentInterface;
		this.methodName = methodName;
	}
	
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
		System.out.println("FilteringOptionsChangeListener stateChanged");
		
		// search the method from name in parent object
		java.lang.reflect.Method method = null;
		try {
			method = this.parentInterface.getClass().getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			method.invoke(this.parentInterface);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

class FilteringOptionsMouseListener implements MouseListener {
	// we need to store the parent object:
	// parameters will change in that object
	FilteringMethodOptionInterface parentInterface;
	// we keep the method name to call in the parent
	String methodName;
	
	public FilteringOptionsMouseListener(FilteringMethodOptionInterface parentInterface, String methodName) {
		this.parentInterface = parentInterface;
		this.methodName = methodName;
	}
	
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("FilteringOptionsMouseListener stateChanged");
		
		// search the method from name in parent object
		java.lang.reflect.Method method = null;
		try {
			method = this.parentInterface.getClass().getMethod(methodName);
		} catch (NoSuchMethodException | SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			method.invoke(this.parentInterface);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}

class VariantFilteringMethodOptionInterface extends FilteringMethodOptionInterface {
	NiceIntegerSlider thresholdSlider;
	VariantFilteringAlgorithmParameters variantFilteringAlgorithmParameters;
	FilteringOptionsChangeListener thresholdChangeListener;
	
	public VariantFilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model,
			LeftPanel leftPanel, RightPanel rightPanel) {
		super(context, model, leftPanel, rightPanel);
		// TODO Auto-generated constructor stub
		
		// define the parameters we are looking at
		variantFilteringAlgorithmParameters = new VariantFilteringAlgorithmParameters();
		
		// define the layout for this component
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.thresholdSlider = SlickerFactory.instance()
				.createNiceIntegerSlider("Select the threshold", FilteringConstants.minThresholdVariantFiltering, FilteringConstants.maxThresholdVariantFiltering, FilteringConstants.defaultThresholdVariantFiltering, Orientation.HORIZONTAL);
		this.thresholdChangeListener = new FilteringOptionsChangeListener(this, "setThreshold");
		this.thresholdSlider.addChangeListener(this.thresholdChangeListener);
		
		this.add(this.thresholdSlider);
	}
	
	public void setThreshold() {
		System.out.println("VariantFilteringMethodOptionInterface setThreshold");
		variantFilteringAlgorithmParameters.setThreshold(thresholdSlider.getValue());
		rightPanel.filteringAlgorithmParameters = variantFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public void setParameters(Map<String, Object> parameters) {
		thresholdSlider.setValue((Integer)parameters.get("threshold"));
		variantFilteringAlgorithmParameters.setThreshold(thresholdSlider.getValue());
		
		rightPanel.filteringAlgorithmParameters = variantFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		ret.put("threshold", thresholdSlider.getValue());
		
		return ret;
	}
	
	@Override
	public String getParametersDescription() {
		return "VariantFilteringMethodOptionInterface";
	}
	
	@Override
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return FilteringAlgorithmFactory.algorithms.VARIANT;
	}
	
}
class AFAFilteringMethodOptionInterface extends FilteringMethodOptionInterface {
	NiceDoubleSlider thresholdSlider;
	AFAFilteringAlgorithmParameters aFAFilteringAlgorithmParameters;
	FilteringOptionsChangeListener thresholdChangeListener;
	
	public AFAFilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model,
			LeftPanel leftPanel, RightPanel rightPanel) {
		super(context, model, leftPanel, rightPanel);
		// TODO Auto-generated constructor stub
		
		// define the parameters we are looking at
		aFAFilteringAlgorithmParameters = new AFAFilteringAlgorithmParameters();
		
		// define the layout for this component
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.thresholdSlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Select the threshold", FilteringConstants.minProbabilityThresholdAFAFiltering, FilteringConstants.maxProbabilityThresholdAFAFiltering, FilteringConstants.defaultProbabilityThresholdAFAFiltering, Orientation.HORIZONTAL);
		this.thresholdChangeListener = new FilteringOptionsChangeListener(this, "setThreshold");
		this.thresholdSlider.addChangeListener(this.thresholdChangeListener);
		
		this.add(this.thresholdSlider);
	}
	
	public void setThreshold() {
		System.out.println("VariantFilteringMethodOptionInterface setThreshold");
		aFAFilteringAlgorithmParameters.setThreshold(thresholdSlider.getValue());
		rightPanel.filteringAlgorithmParameters = aFAFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public void setParameters(Map<String, Object> parameters) {
		thresholdSlider.setValue((Double)parameters.get("threshold"));
		aFAFilteringAlgorithmParameters.setThreshold(thresholdSlider.getValue());
		
		rightPanel.filteringAlgorithmParameters = aFAFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		ret.put("threshold", aFAFilteringAlgorithmParameters.getThreshold());
		
		return ret;
	}
	
	@Override
	public String getParametersDescription() {
		return "AFAFilteringMethodOptionInterface";
	}
	
	@Override
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return FilteringAlgorithmFactory.algorithms.AFA;
	}
}

class MatrixFilteringMethodOptionInterface extends FilteringMethodOptionInterface {
	NiceIntegerSlider subsequenceSlider;
	MatrixFilteringAlgorithmParameters matrixFilteringAlgorithmParameters;
	NiceDoubleSlider probabilitySlider;
	FilteringOptionsChangeListener thresholdSubsequenceLength;
	FilteringOptionsChangeListener thresholdChangeListener;
	FilteringOptionsMouseListener thresholdKeepOrRemove;
	
	JLabel KeepOrRemoveOutliers;
	
	JCheckBox keepOrRemoveOutliers;
	
	public MatrixFilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model,
			LeftPanel leftPanel, RightPanel rightPanel) {
		super(context, model, leftPanel, rightPanel);
		// TODO Auto-generated constructor stub
		
		// define the parameters we are looking at
		matrixFilteringAlgorithmParameters = new MatrixFilteringAlgorithmParameters();
		
		// define the layout for this layout component
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.subsequenceSlider = SlickerFactory.instance()
				.createNiceIntegerSlider("Select the subsequencelength", FilteringConstants.minSubsequenceThresholdMatrixFiltering, FilteringConstants.maxSubsequenceThresholdMatrixFiltering, FilteringConstants.defaultSubsequenceThresholdMatrixFiltering, Orientation.HORIZONTAL);
		this.thresholdSubsequenceLength = new FilteringOptionsChangeListener(this, "setSubsequenceThreshold");
		this.subsequenceSlider.addChangeListener(this.thresholdSubsequenceLength);
		
		this.add(this.subsequenceSlider);
		
		
		this.probabilitySlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Select the probability threshold", FilteringConstants.minProbabilityThresholdMatrixFiltering, FilteringConstants.maxProbabilityThresholdMatrixFiltering, FilteringConstants.defaultProbabilityThresholdMatrixFiltering, Orientation.HORIZONTAL);
		this.thresholdChangeListener = new FilteringOptionsChangeListener(this, "setProbablityThreshold");
		this.probabilitySlider.addChangeListener(this.thresholdChangeListener);
		this.add(this.probabilitySlider);
		/*this.keepOrRemoveOutliers=new JCheckBox( "Remove outliers:", true );
        
        this.thresholdKeepOrRemove = new FilteringOptionsMouseListener(this, "setKeepOrRemove");
		this.keepOrRemoveOutliers.addMouseListener(this.thresholdKeepOrRemove);
		this.KeepOrRemoveOutliers =new JLabel("Keep or Remove Outliers");
		this.add(KeepOrRemoveOutliers);
        this.add(keepOrRemoveOutliers);*/
	}
	
	public void setProbablityThreshold() {
		System.out.println("MatrixFilteringMethodOptionInterface setProbabilityThreshold");
		matrixFilteringAlgorithmParameters.setProbabilityThreshold(probabilitySlider.getValue());
		rightPanel.filteringAlgorithmParameters = matrixFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	public void setSubsequenceThreshold() {
		System.out.println("MatrixFilteringMethodOptionInterface setSubsequenceThreshold");
		matrixFilteringAlgorithmParameters.setSubsequenceThreshold(subsequenceSlider.getValue());
		rightPanel.filteringAlgorithmParameters = matrixFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	public void setKeepOrRemove() {
		/*System.out.println("MatrixFilteringMethodOptionInterface setsetKeepOrRemove");
		if(keepOrRemoveOutliers.isSelected()) {
			matrixFilteringAlgorithmParameters.setFilteringSelection(FilterSelection.REMOVE);
		}
		else {
			matrixFilteringAlgorithmParameters.setFilteringSelection(FilterSelection.SELECT);
		}
		rightPanel.filteringAlgorithmParameters = matrixFilteringAlgorithmParameters;
		rightPanel.updateModel();*/
	}
	
	public void setParameters(Map<String, Object> parameters) {
		probabilitySlider.setValue((Double)parameters.get("probabilitySlider"));
		subsequenceSlider.setValue((Integer)parameters.get("subsequenceSlider"));
		
		matrixFilteringAlgorithmParameters.setProbabilityThreshold(probabilitySlider.getValue());
		matrixFilteringAlgorithmParameters.setSubsequenceThreshold(subsequenceSlider.getValue());
		
		rightPanel.filteringAlgorithmParameters = matrixFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		ret.put("probabilitySlider", probabilitySlider.getValue());
		ret.put("subsequenceSlider", subsequenceSlider.getValue());
		
		return ret;
	}
	
	@Override
	public String getParametersDescription() {
		return "MatrixFilteringMethodOptionInterface";
	}
	
	@Override
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return FilteringAlgorithmFactory.algorithms.MATRIX;
	}
	
}
class FrequencyFilteringMethodOptionInterface extends FilteringMethodOptionInterface {
	NiceIntegerSlider subsequenceSlider;
	FrequencyFilteringAlgorithmParameters frequencyFilteringAlgorithmParameters;
	NiceDoubleSlider probabilitySlider;
	FilteringOptionsChangeListener thresholdSubsequenceLength;
	FilteringOptionsChangeListener thresholdChangeListener;
	FilteringOptionsMouseListener thresholdKeepOrRemove;
	AbsteractionType abstractionType;
	JLabel selectAbstraction;
	JComboBox<String> filteringAbstractionSelection;
	FilteringAlgorithmSelectionActionListener filteringAbstractionSelectionActionListener;
	
	JLabel KeepOrRemoveOutliers;
	
	JCheckBox keepOrRemoveOutliers;
	
	public FrequencyFilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model,
			LeftPanel leftPanel, RightPanel rightPanel) {
		super(context, model, leftPanel, rightPanel);
		// TODO Auto-generated constructor stub
	
		// define the layout for this layout component
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// define the parameters we are looking at
		frequencyFilteringAlgorithmParameters = new FrequencyFilteringAlgorithmParameters();
		
		
        AbsteractionType [] abstractionTypes = AbsteractionType.values();
        
        final JComboBox<AbsteractionType> comboAbsteractionMethod = SlickerFactory.instance().createComboBox(abstractionTypes);
        
        this.add(comboAbsteractionMethod);
        
        FrequencyParametersActionListener frequencyParametersActionListener;
        frequencyParametersActionListener = new FrequencyParametersActionListener(context, model, leftPanel, rightPanel, comboAbsteractionMethod, this);
        
        comboAbsteractionMethod.addActionListener(frequencyParametersActionListener);
        
        //context, model, leftPanel, rightPanel, abstractionType
        //frequencyParametersActionListener = new FrequencyParametersActionListener(context, )
        //this.abstractionType= new FilteringAbstractionSelectionActionListener(this, "setAbstraction");
        //comboAbsteractionMethod.addActionListener(selectAbstraction);
        
		this.subsequenceSlider = SlickerFactory.instance()
				.createNiceIntegerSlider("Select the subsequencelength", FilteringConstants.minSubsequenceThresholdMatrixFiltering, FilteringConstants.maxSubsequenceThresholdMatrixFiltering, FilteringConstants.defaultSubsequenceThresholdMatrixFiltering, Orientation.HORIZONTAL);
		this.thresholdSubsequenceLength = new FilteringOptionsChangeListener(this, "setSubsequenceThreshold");
		this.subsequenceSlider.addChangeListener(this.thresholdSubsequenceLength);
		
		this.add(this.subsequenceSlider);
	
		
		this.probabilitySlider = SlickerFactory.instance()
				.createNiceDoubleSlider("Select the probability threshold", FilteringConstants.minProbabilityThresholdMatrixFiltering, FilteringConstants.maxProbabilityThresholdMatrixFiltering, FilteringConstants.defaultProbabilityThresholdMatrixFiltering, Orientation.HORIZONTAL);
		this.thresholdChangeListener = new FilteringOptionsChangeListener(this, "setProbablityThreshold");
		this.probabilitySlider.addChangeListener(this.thresholdChangeListener);
		this.add(this.probabilitySlider);
		//this.keepOrRemoveOutliers=new JCheckBox( "Remove outliers:", true );
        
        //this.thresholdKeepOrRemove = new FilteringOptionsMouseListener(this, "setKeepOrRemove");
		//this.keepOrRemoveOutliers.addMouseListener(this.thresholdKeepOrRemove);
		//this.KeepOrRemoveOutliers =new JLabel("Keep or Remove Outliers");
		//this.add(KeepOrRemoveOutliers);
        //this.add(keepOrRemoveOutliers);
	}
	
	public void setProbablityThreshold() {
		System.out.println("FrequencyilteringMethodOptionInterface setProbabilityThreshold");
		frequencyFilteringAlgorithmParameters.setProbablitytThreshold(probabilitySlider.getValue());
		rightPanel.filteringAlgorithmParameters = frequencyFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	public void setSubsequenceThreshold() {
		System.out.println("FrequencyFilteringMethodOptionInterface setSubsequenceThreshold");
		frequencyFilteringAlgorithmParameters.setSubsequenceThreshold(subsequenceSlider.getValue());
		rightPanel.filteringAlgorithmParameters = frequencyFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	public void setAbstractionType(AbsteractionType abstractionType) {
		System.out.println("FrequencyFilteringMethodOptionInterface setAbstractionType");
		frequencyFilteringAlgorithmParameters.setAbsteractionUsed(abstractionType);
		rightPanel.filteringAlgorithmParameters = frequencyFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	public void setKeepOrRemove() {
		/*System.out.println("FrequencyFilteringMethodOptionInterface setsetKeepOrRemove");
		if(keepOrRemoveOutliers.isSelected()) {
			frequencyFilteringAlgorithmParameters.setFilteringSelection(FilterSelection.REMOVE);
		}
		else {
			frequencyFilteringAlgorithmParameters.setFilteringSelection(FilterSelection.SELECT);
		}
		rightPanel.filteringAlgorithmParameters = frequencyFilteringAlgorithmParameters;
		rightPanel.updateModel();*/
	}
	
	@Override
	public void setParameters(Map<String, Object> parameters) {
		probabilitySlider.setValue((Double)parameters.get("probabilitySlider"));
		subsequenceSlider.setValue((int)parameters.get("subsequenceSlider"));
		
		
		frequencyFilteringAlgorithmParameters.setSubsequenceThreshold(subsequenceSlider.getValue());
		frequencyFilteringAlgorithmParameters.setProbablitytThreshold(probabilitySlider.getValue());
		frequencyFilteringAlgorithmParameters.setAbsteractionUsed((AbsteractionType)parameters.get("abstractionType"));
		
		rightPanel.filteringAlgorithmParameters = frequencyFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> ret = new HashMap<String, Object>();
		
		ret.put("probabilitySlider", probabilitySlider.getValue());
		ret.put("subsequenceSlider", subsequenceSlider.getValue());
		ret.put("abstractionType", frequencyFilteringAlgorithmParameters.getAbsteractionUsed());
		
		return ret;
	}
	
	@Override
	public String getParametersDescription() {
		return "FrequencyFilteringMethodOptionInterface";
	}
	
	@Override
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return FilteringAlgorithmFactory.algorithms.FF;
	}
}

class NoFilteringMethodOptionInterface extends FilteringMethodOptionInterface {
	NiceIntegerSlider thresholdSlider;
	NoFilteringAlgorithmParameters noFilteringAlgorithmParameters;
	FilteringOptionsChangeListener thresholdChangeListener;
	
	public NoFilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model,
			LeftPanel leftPanel, RightPanel rightPanel) {
		super(context, model, leftPanel, rightPanel);
		// TODO Auto-generated constructor stub
		
		// define the parameters we are looking at
		noFilteringAlgorithmParameters = new NoFilteringAlgorithmParameters();
		rightPanel.filteringAlgorithmParameters = noFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	
	}
	
	@Override
	public void setParameters(Map<String, Object> parameters) {
		rightPanel.updateModel(false);
	}
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> ret = new HashMap<String, Object>();
				
		return ret;
	}
	
	@Override
	public String getParametersDescription() {
		return "NoFilteringMethodOptionInterface";
	}
	
	@Override
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return FilteringAlgorithmFactory.algorithms.NO;
	}
}



class SFFilteringMethodOptionInterface extends FilteringMethodOptionInterface {
	NiceDoubleSlider maxSupportPattern;
	SFFilteringAlgorithmParameters sfFilteringAlgorithmParameters;
	NiceDoubleSlider minSupportRule;
	NiceDoubleSlider minConfRule;
	FilteringOptionsChangeListener thresholdmaxSupportPattern;
	FilteringOptionsChangeListener thresholdminSupportRule;
	FilteringOptionsChangeListener thresholdminConfRule;
	
	
	public SFFilteringMethodOptionInterface(PluginContext context, FilteringVisualizerModel model,
			LeftPanel leftPanel, RightPanel rightPanel) {
		super(context, model, leftPanel, rightPanel);
		// TODO Auto-generated constructor stub
		
		// define the parameters we are looking at
		sfFilteringAlgorithmParameters = new SFFilteringAlgorithmParameters();
		
		// define the layout for this layout component
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.maxSupportPattern = SlickerFactory.instance()
				.createNiceDoubleSlider("Select the maximimum support of Odd patterns", FilteringConstants.minSupportOddpattern, FilteringConstants.maxSupportOddpattern, FilteringConstants.defaultSupportOddpattern, Orientation.HORIZONTAL);
		this.thresholdmaxSupportPattern = new FilteringOptionsChangeListener(this, "setmaxSupportOddThreshold");
		this.maxSupportPattern.addChangeListener(this.thresholdmaxSupportPattern);
		
		this.add(this.maxSupportPattern);
		
		
		this.minSupportRule = SlickerFactory.instance()
				.createNiceDoubleSlider("Select the minimum support of high probable rules", FilteringConstants.minSupportHighRules, FilteringConstants.maxSupportHighRules, FilteringConstants.defaultSupportHighRules, Orientation.HORIZONTAL);
		this.thresholdminSupportRule = new FilteringOptionsChangeListener(this, "setminSupportHighRule");
		this.minSupportRule.addChangeListener(this.thresholdminSupportRule);
		this.add(this.minSupportRule);
		
		this.minConfRule = SlickerFactory.instance()
				.createNiceDoubleSlider("Select the minimum confidence of high probable rules", FilteringConstants.minConfHighRules, FilteringConstants.maxConfHighRules, FilteringConstants.defaultConfHighRules, Orientation.HORIZONTAL);
		this.thresholdminConfRule = new FilteringOptionsChangeListener(this, "setminConfHighRule");
		this.minConfRule.addChangeListener(this.thresholdminSupportRule);
		this.add(this.minConfRule);
	}
	
	public void setmaxSupportOddThreshold() {
		System.out.println("SFFilteringMethodOptionInterface setmaxSupportOddThreshold");
		sfFilteringAlgorithmParameters.setMaximumSupportOddPatters(maxSupportPattern.getValue());
		rightPanel.filteringAlgorithmParameters = sfFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	public void setminSupportHighRule() {
		System.out.println("SFFilteringMethodOptionInterface setminSupportHighRule");
		sfFilteringAlgorithmParameters.setMinimumSupportHighRules(minSupportRule.getValue());
		rightPanel.filteringAlgorithmParameters = sfFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	public void setKeepOrRemove() {
		System.out.println("SFFilteringMethodOptionInterface setminConfHighRule");
		sfFilteringAlgorithmParameters.setMinimumConfidenceHighRules(minConfRule.getValue());
		rightPanel.filteringAlgorithmParameters = sfFilteringAlgorithmParameters;
		rightPanel.updateModel(false);
	}
	
	@Override
	public void setParameters(Map<String, Object> parameters) {
		maxSupportPattern.setValue((Double)parameters.get("maxSupportPattern"));
		minSupportRule.setValue((Double)parameters.get("minSupportRule"));
		minConfRule.setValue((Double)parameters.get("minConfRule"));
		
	}
	
	@Override
	public Map<String, Object> getParameters() {
		Map<String, Object> ret = new HashMap<String, Object>();
		sfFilteringAlgorithmParameters.setMaximumSupportOddPatters(maxSupportPattern.getValue());
		sfFilteringAlgorithmParameters.setMinimumSupportHighRules(minSupportRule.getValue());
		sfFilteringAlgorithmParameters.setMinimumConfidenceHighRules(minConfRule.getValue());
		
		ret.put("maxSupportPattern", maxSupportPattern.getValue());
		ret.put("minSupportRule", minSupportRule.getValue());
		ret.put("minConfRule", minConfRule.getValue());
		
		return ret;
	}
	
	@Override
	public String getParametersDescription() {
		return "SFFilteringMethodOptionInterface";
	}
	
	@Override
	public FilteringAlgorithmFactory.algorithms getFilteringType() {
		return FilteringAlgorithmFactory.algorithms.SF;
	}
}


class ZoomListenerOnLeftPanel implements MouseWheelListener {
    LeftPanel leftPanel;
    
    public ZoomListenerOnLeftPanel(LeftPanel leftPanel) {
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

class ETConformanceVisualizer extends JPanel {
	PluginContext context;
	FilteringVisualizerModel model;
	LeftPanel leftPanel;
	RightPanel rightPanel;
	
	JLabel fitnessLabel;
	JLabel fitnessValue;
	JLabel precisionLabel;
	JLabel precisionValue;
	JLabel fmeasureLabel;
	JLabel fmeasureValue;
	
	public ETConformanceVisualizer(PluginContext context, FilteringVisualizerModel model, LeftPanel leftPanel, RightPanel rightPanel) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.rightPanel = rightPanel;
		
		this.fitnessLabel = new JLabel("Fitness value: ");
		this.fitnessValue = new JLabel("");
		Font font = this.fitnessValue.getFont();
		Font fontBold = new Font(font.getFontName(), font.BOLD, font.getSize());
		this.fitnessValue.setFont(fontBold);
		this.precisionLabel = new JLabel("Precision value: ");
		this.precisionValue = new JLabel("");
		this.precisionValue.setFont(fontBold);
		
		this.fmeasureLabel = new JLabel("F-Measure: ");
		this.fmeasureValue = new JLabel("");
		this.fmeasureValue.setFont(fontBold);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.add(this.fitnessLabel);
		this.add(this.fitnessValue);
		this.add(this.precisionLabel);
		this.add(this.precisionValue);
		this.add(this.fmeasureLabel);
		this.add(this.fmeasureValue);
	}
	
	public void update() {

		this.fitnessValue.setText(String.format("%.3f", this.model.fitness));
		this.precisionValue.setText(String.format("%.3f", this.model.precision));
		Double fValue = 0.0;
		if ((this.model.fitness + this.model.precision) > 0.0) {
			fValue = (2.0 * this.model.fitness * this.model.precision)/(this.model.fitness + this.model.precision);
		}
		this.fmeasureValue.setText(String.format("%.3f", fValue));
	}
}

class LogExportButtonMouseListener implements MouseListener {
	PluginContext context;
	FilteringVisualizerModel model;
	
	public LogExportButtonMouseListener(PluginContext context, FilteringVisualizerModel model) {
		this.context = context;
		this.model = model;
	}

	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		this.model.exportLog();
		
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

class PetriNetExportMouseListener implements MouseListener {
	PluginContext context;
	FilteringVisualizerModel model;
	
	public PetriNetExportMouseListener(PluginContext context, FilteringVisualizerModel model) {
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

class RestoreParametersMouseListener implements MouseListener {
	PluginContext context;
	FilteringVisualizerModel model;
	RightPanel rightPanel;
	
	public RestoreParametersMouseListener(PluginContext context, FilteringVisualizerModel model, RightPanel rightPanel) {
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

class SaveParametersMouseListener implements MouseListener {
	PluginContext context;
	FilteringVisualizerModel model;
	RightPanel rightPanel;
	
	public SaveParametersMouseListener(PluginContext context, FilteringVisualizerModel model, RightPanel rightPanel) {
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
	
}