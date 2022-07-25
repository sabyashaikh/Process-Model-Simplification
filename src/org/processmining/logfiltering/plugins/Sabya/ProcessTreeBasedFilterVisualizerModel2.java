package org.processmining.logfiltering.plugins.Sabya;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageDescriptor.OS;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.logfiltering.visualizerModel.FilteredLogExporterSS;
import org.processmining.logfiltering.visualizerModel.PetriNetExporterSS;
import org.processmining.logfiltering.visualizerModel.PluginDescriptorImpl2;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.conversion.ReduceTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParameters;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;

public class ProcessTreeBasedFilterVisualizerModel2 {
	PluginContext context;
	XLog InputLog;
	XLog FilteredLog;
	ProcessTree processTree;
	Parameters parameters;
	Map<UUID, NodeDetailStore> treeNodeFreq;
	ProcessTree initialProcessTree;
	Map<UUID, NodeDetailStore> initialTreeNodeFreq;
	Boolean processTreeAvailable;
	Boolean updateLog;
	Petrinet petrinet;
	Boolean processing;
	Boolean modificationTypeIsConvert;
	
	public  ProcessTreeBasedFilterVisualizerModel2(PluginContext context, XLog InputLog, ProcessTree processTree) {
		this.context = context;
		this.InputLog = InputLog;
		this.processTree = processTree;
		this.processTreeAvailable = true;
		this.treeNodeFreq = new LinkedHashMap<UUID, NodeDetailStore>();
		this.parameters = new Parameters(this.InputLog.getClassifiers().toArray(new XEventClassifier[this.InputLog.getClassifiers().size()])[0], "Case Frequency", "Probability based on parent", "ProcessTreeBasedFilterVisualizer2");
		this.updateLog = false;
		this.processing = false;
		this.modificationTypeIsConvert = true;
	}
	
	public ProcessTreeBasedFilterVisualizerModel2(PluginContext context, XLog InputLog ) {
		this.context = context;
		this.InputLog = InputLog;
		this.treeNodeFreq = new LinkedHashMap<UUID, NodeDetailStore>();
		this.parameters = new Parameters(this.InputLog.getClassifiers().toArray(new XEventClassifier[this.InputLog.getClassifiers().size()])[0], "Case Frequency", "Probability based on parent", "ProcessTreeBasedFilterVisualizer2");
		this.processTreeAvailable = false;
		this.updateLog = false;
		this.processing = false;
		this.modificationTypeIsConvert = true;
	}
	
	public void computePTwithProbability() {
		this.processing = true;
		System.out.println("setting processing to true in computePTwithProbability");
		System.out.println("computePTwithProbability is called with event classifier: "+ this.parameters.getEventClassifier());
		System.out.println("Frequency type: " +this.parameters.getFrequencyType());
		//Create process tree if not provided by user
		if(!processTreeAvailable) {
			this.processTree = HelperFunctions.mineProcessTree(InputLog, this.parameters.getEventClassifier());
			System.out.println("Process tree is created");
		}
		else {
			System.out.println("Process tree is provided by user");
		}
		
		System.out.println("The Process tree in text format:");
		System.out.println(processTree.toString());
		
		System.out.println("The DS at the start of the functions:");
		this.treeNodeFreq.clear();
		HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
		
		//Reset datastructure before processing again
		this.parameters.resetData();
		
		/****Populate Event to character Coder and Decoder*****/
		System.out.println("Populating event character map");
		HelperFunctions.populateEventCharacterMaps(this.InputLog, this.parameters);
		System.out.println("End of populating event character map");
		System.out.println("******************The activity coder************");
		for (Map.Entry<String, String> entry : parameters.getEventCharacterMap().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}
		
		/****Test if the process tree is compatible with the event log****/
		boolean error = HelperFunctions.getCompatibilityBetweenPTandLog(this.processTree, this.InputLog, this.parameters.getEventCharacterMap());
		if(error) {
			throw new UnknownTreeNodeException();
		}
		
		
		/****Populate the EventFrequency, VariantFrequency, TraceIdMap and variantTracesMap ****/
		System.out.println("******************Start populating frequency ************");
		HelperFunctions.populateFrequencyMaps(this.InputLog, this.parameters);
		System.out.println("******************Done populating frequency ************");
		System.out.println("Total trace count: "+this.parameters.getTotalTraces());
		
		System.out.println("******************Trace Frequency************");
		for (Map.Entry<String, Integer> entry : this.parameters.getVariantFrequency().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}
		System.out.println("******************The Event total activity Frequency Mapper************");
		for (Map.Entry<String, Integer> entry : this.parameters.getEventActivityFrequency().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}
		
		System.out.println("******************The Trace and its Id Mapper************");
		for (Map.Entry<Integer, String> entry : this.parameters.getTraceIdMap().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}
		System.out.println("******************The Variant and the traces which belong to this variant************");
		for (Map.Entry<String, List<Integer>> entry : this.parameters.getVariantTracesMap().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}

		
		/****Calculate each event has occurred in how many traces****/
		HelperFunctions.populateEventVariantFrequency(this.parameters);
		System.out.println("******************The Event trace Frequency Mapper************");
		for (Map.Entry<String, Map<String, Integer>> entry : this.parameters.getEventVariantMap().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}
		
		System.out.println("******************The Event total case Frequency Mapper************");
		for (Map.Entry<String, Integer> entry : this.parameters.getEventCaseFrequency().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
		}
		
		
		/**** Resetting the counter used for naming operator and tau nodes in NodeDetailStore and creating initial structure****/
		NodeDetailStore NDS = new NodeDetailStore();
		NDS.resetCounter();
		
		System.out.println("******************Mapping process tree to DS ************");
		treeNodeFreq = HelperFunctions.mapPTtoNodeDetailStructure(this.processTree, this.processTree.getRoot(), UUID.randomUUID(), this.treeNodeFreq, this.parameters);
				
		/****Calculating frequency for nodes without considering tau frequency*****/
		System.out.println("******************Computing variant wise event frequencies ************");
		HelperFunctions.computeVariantWiseFrequencyForEvent(this.parameters);
		System.out.println("******************Events with Activity frequencies ************");
		//for (Map.Entry<String, Map<String, Integer>> entry1 : parameters.getEventWithVariantWiseActivityFrequency().entrySet()) {
		//	System.out.println("Activity = "+entry1.getKey());
		//	for (Map.Entry<String, Integer> entry2 : entry1.getValue().entrySet()) {
		//		System.out.println("	"+entry2.getKey() +" => "+entry2.getValue());
		//	}
		//}
		System.out.println("******************Events with Case frequencies ************");
		//for (Map.Entry<String, Map<String, Integer>> entry1 : parameters.getEventWithVariantWiseCaseFrequency().entrySet()) {
		//	System.out.println("Activity = "+entry1.getKey());
		//	for (Map.Entry<String, Integer> entry2 : entry1.getValue().entrySet()) {
		//		System.out.println("	"+entry2.getKey() +" => "+entry2.getValue());
		//	}
		//}
		
		
		//Set frequencies on nodes
		this.parameters.setUpdatedEventWithVariantWiseActivityFrequency(this.parameters.getEventWithVariantWiseActivityFrequency());
		this.parameters.setUpdatedEventWithVariantWiseCaseFrequency(this.parameters.getEventWithVariantWiseCaseFrequency());
		this.parameters.setUpdatedEventActivityFrequency(this.parameters.getEventActivityFrequency());
		this.parameters.setUpdatedEventCaseFrequency(this.parameters.getEventCaseFrequency());
		this.parameters.setUpdatedTotalTraces(this.parameters.getTotalTraces());
		System.out.println("***********************Calculating frequency for nodes without considering tau frequency******************");
		this.treeNodeFreq = HelperFunctions.setFrequenciesOnProcessTree(this.treeNodeFreq, this.processTree, this.parameters);
		this.setProbability();
	}
	public void setProbability() {
		//Now for every node find probability
		System.out.println("***********************Setting Probability *************************************");
		this.treeNodeFreq = HelperFunctions.setProbability(this.treeNodeFreq, this.parameters.getUpdatedTotalTraces());
		
		System.out.println("***********************Display after Setting probability*************************************");
		HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
		
		//save initial process tree and initial treeNodeFreq
		CloneProcessTree cpt2 = new CloneProcessTree();
		this.initialProcessTree = cpt2.clone(this.processTree);
		this.initialTreeNodeFreq = CloneDataStructureWithNewNodes.clone(this.treeNodeFreq, cpt2.getNodeIdMap(), initialProcessTree) ;
		
		//System.out.println("***********************Display after Cloning*************************************");
		//HelperFunctions.displayDataStructure(this.initialTreeNodeFreq, false);
		
		this.processing = false;
		System.out.println(this.processing);
		System.out.println("Done computing frequency");
		//Modify process tree based on the threshold given by user
		this.modifyPT();
	}
	public void modifyPT() {
		this.processing = true;
		System.out.println("setting processing to true in modifyPTBasedOnThreshold");
		//Take the initial process tree and datastructure
		System.out.println("modifyPTBasedOnThreshold is called inside with threshold: "+ this.parameters.getThreshold());
		
		CloneProcessTree cpt = new CloneProcessTree();
		this.processTree = cpt.clone(this.initialProcessTree);
		this.treeNodeFreq = CloneDataStructureWithNewNodes.clone(this.initialTreeNodeFreq, cpt.getNodeIdMap(), processTree) ;
		
		this.parameters.getVariantTracesMapUpdated().clear();
		this.parameters.getVariantsToChange().clear();
		this.parameters.getDataStructureWithUpdatedActivities().clear();
		this.parameters.setUpdatedVariantFrequency(this.parameters.getVariantFrequency());
		this.parameters.setUpdatedTotalTraces(this.parameters.getTotalTraces());
		this.parameters.setUpdatedEventWithVariantWiseActivityFrequency(this.parameters.getEventWithVariantWiseActivityFrequency());
		this.parameters.setUpdatedEventWithVariantWiseCaseFrequency(this.parameters.getEventWithVariantWiseCaseFrequency());
		this.parameters.setUpdatedEventActivityFrequency(this.parameters.getEventActivityFrequency());
		this.parameters.setUpdatedEventCaseFrequency(this.parameters.getEventCaseFrequency());

		if(this.parameters.getThreshold()!=0.0) {	
			//Before altering tree and nodes frequency details, we need to save it. Hence copy and save the data in node itself under PreviousFrequencyDetails
			System.out.println("*******************Before altering tree and nodes frequency details copy old frequency details*************************************");
			for (Map.Entry<UUID, NodeDetailStore> entry : this.treeNodeFreq.entrySet()) {
				entry.getValue().storeOldFrequencyDetails();
				this.treeNodeFreq.put(entry.getKey(),entry.getValue()); 
			}
					
			//System.out.println("***********************Display after copying frequency details*************************************");
			//HelperFunctions.displayDataStructure(this.treeNodeFreq, true);
			
			System.out.println("***********************Altering tree based on AND node children patterns probability *************************************");
			
			//get list of all "and" nodes
			List<NodeDetailStore> andNodes = new ArrayList <NodeDetailStore>();
			for(Map.Entry<UUID, NodeDetailStore> entry: this.treeNodeFreq.entrySet()) {
				//System.out.println("The node is "+entry.getValue().getName() );
				if(entry.getValue().getName().contains("and")) {
					andNodes.add(entry.getValue());
				}
			}
			//Now for each "and" node in the list create log and delete the subtrees and 
			//create new sub tree when necessary based on threshold
			for(NodeDetailStore andNode: andNodes) {
				System.out.println("Deleting and node "+andNode.getName()+" => " +andNode.getNode());
				if(this.treeNodeFreq.get(andNode.getNode().getID())!=null) {
					ProcessTree subProcessTree = null;
					
					subProcessTree = HelperFunctions.createXlogAndSubPTBasedOnPatterns2(andNode, this.treeNodeFreq, this.parameters, this.InputLog, this.processTree, this.modificationTypeIsConvert);
					
					if(subProcessTree != null) {
						System.out.println("The Previous sub tree for and node was");
						System.out.println(andNode.getNode());
						System.out.println("The sub tree generated for and node: "+andNode.getName());
						System.out.println(subProcessTree);
						if(!andNode.getNode().toString().contentEquals(subProcessTree.toString())) {
							Object data [] = HelperFunctions.replaceWithSubtree(processTree, subProcessTree, andNode, treeNodeFreq, parameters);
							processTree = (ProcessTree) data[0];
							treeNodeFreq = (Map<UUID, NodeDetailStore>) data[1];	
						}
						else {
							System.out.println("The 'and' node doesnt change");
						}
						
					}
					else {
						System.out.println("The 'and' node has equal distribution of probability and hence not altering");
					}
				}
				else {
					System.out.println("The and node: "+andNode.getName()+ "  has already been deleted");
				}
				//System.out.println("The structure after and node replacement");
				//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
				
			}
		
			System.out.println("*******************After deletion of 'and' nodes Remaining variant**********************");
			NodeDetailStore rootNDS = treeNodeFreq.get(this.processTree.getRoot().getID());
			System.out.println("Remaining variants in root node are "+rootNDS.getVariantWiseCaseFrequency().size());
			System.out.println("Remaining variants in root node are "+this.parameters.getUpdatedVariantFrequency().size());
			System.out.println("Remaining trace count: "+this.parameters.getUpdatedTotalTraces());
			
			System.out.println("*******************Alter tree and delete nodes with less probability based On Parent(xor and loop children)**********************");
			List<NodeDetailStore> toBeDeletedNodes =new ArrayList<NodeDetailStore>();
			System.out.println(parameters.getFrequencyType());
			toBeDeletedNodes = HelperFunctions.getNodesToDeleteBasedOnProbability(this.treeNodeFreq, this.parameters.getThreshold(), this.parameters.getProbabilityType(), this.parameters.getFrequencyType());
			if(toBeDeletedNodes.size()>0) {
				for(NodeDetailStore deleteNDS:toBeDeletedNodes) {
					System.out.println(deleteNDS.getName()+" -> "+ deleteNDS.getActualName() +" parent: "+ treeNodeFreq.get(deleteNDS.getParent()).getNode() +" Cprob: "+deleteNDS.getCFProbabilityBasedOnParent()+" Aprob: "+deleteNDS.getAFProbabilityBasedOnParent());;
					System.out.println(deleteNDS.getVariantWiseActivityFrequency());
				}
				System.out.println("The processTree is");
				System.out.println(processTree.toString());
				
				this.processTree = HelperFunctions.modififyTreeBasedOnNodeProbability(this.processTree, toBeDeletedNodes, this.treeNodeFreq, this.parameters, this.modificationTypeIsConvert);
				
				System.out.println("***********************Calculating frequency again as variants have been updated******************");
				this.treeNodeFreq = HelperFunctions.setNodeSuretyToFalse(this.treeNodeFreq);
				this.treeNodeFreq = HelperFunctions.setFrequenciesOnProcessTree(this.treeNodeFreq, this.processTree, this.parameters);
				this.treeNodeFreq = HelperFunctions.setProbability(this.treeNodeFreq, this.parameters.getUpdatedTotalTraces());
				
				System.out.println("*******************After deletion of nodes with less probability Based On Parent(xor and loop children)**********************");
				rootNDS = treeNodeFreq.get(this.processTree.getRoot().getID());
			}
			else {
				System.out.println("No xor or loop children nodes left to delete");
			}
			System.out.println("Remaining variants count: "+rootNDS.getVariantWiseCaseFrequency().size());
			System.out.println("Remaining trace count: "+parameters.getUpdatedTotalTraces());
			
				
			System.out.println("***********************Final Process Tree*************************************");
			// if all variants are not deleted then do the below
			if(this.parameters.getUpdatedTotalTraces()>0) {
				rootNDS = this.treeNodeFreq.get(this.processTree.getRoot().getID());
				System.out.println("variants in root node remaining: ");
				HelperFunctions.displayNode(rootNDS);
				System.out.println(processTree);
				
				//Removing transitivity 
				if(this.modificationTypeIsConvert) {
					System.out.println("***********************Removing transitivity*************************************");
					System.out.println(this.parameters.getVariantsToChange());
					Map<String, String> noTransitiveVariant = new LinkedHashMap<String, String>();
					for(Map.Entry<String, String> entry: this.parameters.getVariantsToChange().entrySet()) {
						//System.out.println(entry.getKey()+" change to "+ entry.getValue());
						String key = entry.getValue();
						String value = "";
						while(this.parameters.getVariantsToChange().containsKey(key)) {
							//System.out.println("The key is: "+ key);
							value = this.parameters.getVariantsToChange().get(key);
							//System.out.println("The value is: "+ value);
							key = value;
						}
						//System.out.println(entry.getKey()+" change to "+ key);
						noTransitiveVariant.put(entry.getKey(), key);
					}
					this.parameters.setVariantsToChange(noTransitiveVariant);	
					//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
				}
			}
			else {
				// if all variants are deleted then return an empty process tree
				this.processTree = HelperFunctions.getEmptyProcessTree(this.InputLog);
			}
		}
		if(this.parameters.getUpdatedTotalTraces()>0) {
			System.out.println(HelperFunctions.ConvertToTextTree(HelperFunctions.changeNodeName(this.processTree, this.treeNodeFreq).toString()));
		}else {
			System.out.println(this.processTree.toString());
		}
		this.processing = false;
		System.out.println(this.processing);
		
		System.out.println("The number of variants available at start: "+this.parameters.getVariantTracesMap().size());
		System.out.println("The number of variants to change are: "+this.parameters.getVariantsToChange().size());
		
		if(this.modificationTypeIsConvert) {
			if(this.parameters.getUpdatedTotalTraces()>0 && this.parameters.getVariantTracesMap().size()!=this.parameters.getVariantsToChange().size()) {
				for(Map.Entry<String, List<Integer>> entry: this.parameters.getVariantTracesMap().entrySet()) {
					String variant = entry.getKey();
					List <Integer> traceIds = entry.getValue();
					//System.out.println("Variant to be checked is:"+ variant );
					if(this.parameters.getVariantsToChange().containsKey(variant)){
						String variantToKeep = this.parameters.getVariantsToChange().get(variant);
						List<Integer> oldTraceIds = this.parameters.getVariantTracesMap().get(variantToKeep);
						oldTraceIds.removeAll(traceIds);
						oldTraceIds.addAll(traceIds);
						this.parameters.getVariantTracesMapUpdated().put(variantToKeep, oldTraceIds);
					}
					else {
						this.parameters.getVariantTracesMapUpdated().put(variant, traceIds);
					}
				}
				System.out.println("The number of variants available after conversion for "+this.parameters.getThreshold() +" are: "+this.parameters.getVariantTracesMapUpdated().size());
				System.out.println("The list of variants remaining after conversion are :");
				for(Map.Entry<String, List<Integer>> entry: this.parameters.getVariantTracesMapUpdated().entrySet()) {
					System.out.println(entry.getKey() +" => "+ entry.getValue().size() +" => "+entry.getValue());
				}
			}else
			{
				System.out.println("All variants deleted");
			}
		}
		else {
			System.out.println("The number of variants after removal for "+this.parameters.getThreshold() +" are: "+this.parameters.getUpdatedVariantFrequency().size());
			System.out.println("The list of variants are :");
			for(Map.Entry<String, Integer> entry: this.parameters.getUpdatedVariantFrequency().entrySet()) {
				System.out.println(entry.getKey() +" => "+entry.getValue());
			}
		}
		this.processTree = this.getReducedProcessTree(this.processTree);
		try {
			this.petrinet = ProcessTree2Petrinet.convert(this.processTree).petrinet;
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidProcessTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void updateLog() {
		this.processing = true;
		System.out.println("setting processing to true in updateLog");
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, parameters.getEventClassifier());
		XLog filteredLog = factory.createLog();
		for (XExtension extension : InputLog.getExtensions()){
			filteredLog.getExtensions().add(extension);
		}
		for (XEventClassifier classifier : InputLog.getClassifiers()){
			filteredLog.getClassifiers().add(classifier);
		}
		XAttributeMap XAMap = (XAttributeMap) InputLog.getAttributes().clone();
		filteredLog.setAttributes(XAMap);
		filteredLog.setInfo(parameters.getEventClassifier(), logInfo);
		if(this.modificationTypeIsConvert) {
			for (XTrace trace : InputLog) {
				if(this.parameters.getXTraceToTraceIdMap().containsKey(trace)) {
					Integer traceId = this.parameters.getXTraceToTraceIdMap().get(trace);
					//Check if below mentioned variant needs to be deleted
					String variant = this.parameters.getTraceIdVariantMap().get(traceId);
					
					if(this.parameters.getVariantsToChange().get(variant)!= null) {
						System.out.println("copying: "+ variant);
						String variantToAdd = this.parameters.getVariantsToChange().get(variant);
						Integer traceIdToAdd = this.parameters.getVariantTracesMap().get(variantToAdd).get(0);
						XTrace traceToAdd = this.parameters.getTraceIdToXTraceMap().get(traceIdToAdd);
						filteredLog.add(traceToAdd);
					}
					else {
						filteredLog.add(trace);
					}
				}
				else {
					System.out.println("Error: Trace not available in our ds");
				}
			}
		}else {
			for (XTrace trace : InputLog) {
				if(this.parameters.getXTraceToTraceIdMap().containsKey(trace)) {
					Integer traceId = this.parameters.getXTraceToTraceIdMap().get(trace);
					String variant = this.parameters.getTraceIdVariantMap().get(traceId);
					if(this.parameters.getUpdatedVariantFrequency().get(variant)!= null) {
						filteredLog.add(trace);
					}
				}
				else {
					System.out.println("Error: Trace not available in our ds");
				}
				
			}
		}
		this.FilteredLog = filteredLog;
		this.processing = false;
		System.out.println(this.processing);
	}
	public void exportPetrinet() throws NotYetImplementedException, InvalidProcessTreeException {
		System.out.println("setting processing to true in exportPetrinet");
		this.processing = true;
		UIPluginContext context2 = (UIPluginContext) this.context;
		
		String actualAction = " ";
		PackageDescriptor pack = new PackageDescriptor(actualAction, actualAction, OS.ALL, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, true, true, new ArrayList<String>(), new ArrayList<String>());
		
		PluginDescriptor descriptor = null;
		
		try {
			descriptor = new PluginDescriptorImpl2(PetriNetExporterSS.class, context.getClass(), pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Object[] objects = new Object[1];
		
		objects[0] = this.getPetriNet();
		
		context.invokePlugin(descriptor, 0, objects);
		this.processing = false;
	    System.out.println(this.processing);
	    JOptionPane.showMessageDialog(new JFrame(), "Exported Petri net! Check the 'all' items in your ProM workspace!", "Dialog",
	            JOptionPane.INFORMATION_MESSAGE);
	    
	}
	public void exportProcessTree() throws NotYetImplementedException, InvalidProcessTreeException {
		this.processing = true;
		System.out.println("setting processing to true in exportProcessTree");
		UIPluginContext context2 = (UIPluginContext) this.context;
		
		String actualAction = " ";
		PackageDescriptor pack = new PackageDescriptor(actualAction, actualAction, OS.ALL, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, true, true, new ArrayList<String>(), new ArrayList<String>());
		
		PluginDescriptor descriptor = null;
		
		try {
			descriptor = new PluginDescriptorImpl2(ProcessTreeExporter.class, context.getClass(), pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Object[] objects = new Object[1];
		
		objects[0] = this.processTree;
		
		context.invokePlugin(descriptor, 0, objects);
		this.processing = false;

		JOptionPane.showMessageDialog(new JFrame(), "Exported process tree! Check the 'all' items in your ProM workspace!", "Dialog",
	            JOptionPane.INFORMATION_MESSAGE);
	}
	public void exportLog() {
		this.processing = true;
		System.out.println("setting processing to true in exportLog");
		UIPluginContext context2 = (UIPluginContext) this.context;
		
		String actualAction = " ";
		PackageDescriptor pack = new PackageDescriptor(actualAction, actualAction, OS.ALL, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, true, true, new ArrayList<String>(), new ArrayList<String>());
		
		PluginDescriptor descriptor = null;
		
		try {
			descriptor = new PluginDescriptorImpl2(FilteredLogExporterSS.class, context2.getClass(), pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(this.InputLog.size());
		System.out.println(this.FilteredLog.size());
		
		if (this.InputLog != this.FilteredLog) {
			Object[] objects = new Object[1];
			
			objects[0] = this.FilteredLog;
			
			context2.invokePlugin(descriptor, 0, objects);
			this.processing = false;
			System.out.println("setting processing to false in exportLog");
			
		    JOptionPane.showMessageDialog(new JFrame(), "Log exported! Check the 'all' items in your ProM workspace!", "Dialog",
		            JOptionPane.INFORMATION_MESSAGE);
		}
		else {
		    JOptionPane.showMessageDialog(new JFrame(), "Log are equals! No export happened!", "Dialog",
		            JOptionPane.ERROR_MESSAGE);
		    this.processing = false;
			System.out.println("setting processing to false in exportLog");
		}
		
	}
	public ProcessTree getReducedProcessTree(ProcessTree processTree) {
		this.processing = true;
		try {
			return ReduceTree.reduceTree(processTree,  new EfficientTreeReduceParameters(false, false));
		} catch (UnknownTreeNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReductionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.processing = false;
		return processTree;
	}
	@SuppressWarnings("deprecation")
	public Petrinet getPetriNet() throws NotYetImplementedException, InvalidProcessTreeException {
		return this.petrinet;
		
	}
	public XEventClassifier getEventClassifier() {
		return this.parameters.getEventClassifier();
		
	}
	public void setEventClassifier (XEventClassifier xEventClassifier) {
		this.parameters.setEventClassifier(xEventClassifier);
	}
	
	public XLog getInputLog() {
		return this.InputLog;
		
	}
	public XLog getFilteredLog() {
		return this.FilteredLog;
		
	}
}
