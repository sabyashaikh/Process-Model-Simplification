package org.processmining.logfiltering.plugins.Sabya;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.InductiveMiner.conversion.ReduceTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParameters;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;

public class SimplifiedProcessTree {
	private XLog InputLog;
	private ProcessTree processTree;
	private Parameters parameters;
	private Map<UUID, NodeDetailStore> treeNodeFreq;
	private Petrinet petrinet;
	
	public SimplifiedProcessTree(XLog InputLog, ProcessTree processTree, Parameters parameters) {
		this.InputLog = InputLog;
		this.processTree = processTree;
		this.treeNodeFreq = new LinkedHashMap<UUID, NodeDetailStore>();
		this.parameters = parameters;
		this.petrinet = null;
	}
	@SuppressWarnings("deprecation")
	public Object[] apply() throws UnknownTreeNodeException   {
		System.out.println("computePTwithProbability is called with event classifier: "+ this.parameters.getEventClassifier());
		
		System.out.println("The Initial process tree in text format:");
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
		for (Map.Entry<String, String> entry : this.parameters.getEventCharacterMap().entrySet()) {
			System.out.println(entry.getKey() +" => "+entry.getValue());
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
		
		//Populate necessary ds because same code used by other plugin
		this.parameters.setUpdatedEventWithVariantWiseActivityFrequency(this.parameters.getEventWithVariantWiseActivityFrequency());
		this.parameters.setUpdatedEventWithVariantWiseCaseFrequency(this.parameters.getEventWithVariantWiseCaseFrequency());
		this.parameters.setUpdatedTotalTraces(this.parameters.getTotalTraces());
		this.parameters.setUpdatedEventActivityFrequency(this.parameters.getEventActivityFrequency());
		this.parameters.setUpdatedEventCaseFrequency(this.parameters.getEventCaseFrequency());
		
		//Set frequencies on nodes
		System.out.println("***********************Calculating frequency for nodes without considering tau frequency******************");
		this.treeNodeFreq = HelperFunctions.setFrequenciesOnProcessTree(this.treeNodeFreq, this.processTree, this.parameters);
		
		System.out.println("***********************Setting Probability *************************************");
		this.treeNodeFreq = HelperFunctions.setProbability(this.treeNodeFreq, this.parameters.getTotalTraces());
		
		//Before deleting nodes store the variant freq and total traces in temporary variable
		this.parameters.setUpdatedVariantFrequency(this.parameters.getVariantFrequency());
		
		
		System.out.println("***********************Altering tree based on AND node children patterns probability *************************************");
		//get list of all and nodes
		List<NodeDetailStore> andNodes = new ArrayList <NodeDetailStore>();
		for(Map.Entry<UUID, NodeDetailStore> entry: this.treeNodeFreq.entrySet()) {
			//System.out.println("The node is "+entry.getValue().getName() );
			if(entry.getValue().getName().contains("and")) {
				andNodes.add(entry.getValue());
			}
		}
		
		//Now for each "and" node in the list create log and delete the child trees 
		//create new sub tree when necessary  based on threshold
		for(NodeDetailStore andNode: andNodes) {
			if(this.treeNodeFreq.get(andNode.getNode().getID())!=null) {
				ProcessTree subProcessTree = HelperFunctions.createXlogAndSubPTBasedOnPatterns2(andNode, this.treeNodeFreq, this.parameters, this.InputLog, this.processTree,false);
				if(subProcessTree != null) {
					System.out.println("The Previous sub tree for and node was");
					System.out.println(andNode.getNode());
					System.out.println("The sub tree generated for and node: "+andNode.getName());
					System.out.println(subProcessTree);
					if(!andNode.getNode().toString().contentEquals(subProcessTree.toString())) {
						//System.out.println("The frequency before and node replacement");
						//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
						Object[] data = HelperFunctions.replaceWithSubtree(processTree, subProcessTree, andNode, treeNodeFreq, parameters);
						this.processTree = (ProcessTree) data[0];
						this.treeNodeFreq = (Map<UUID, NodeDetailStore>) data[1];
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
		}
		
		System.out.println("***********************Nodes to be deleted*************************************");
		List<NodeDetailStore> toBeDeletedNodes =new ArrayList<NodeDetailStore>();
		toBeDeletedNodes = HelperFunctions.getNodesToDeleteBasedOnProbability(this.treeNodeFreq, this.parameters.getThreshold(), this.parameters.getProbabilityType(), this.parameters.getFrequencyType());
		for(NodeDetailStore deleteNDS:toBeDeletedNodes) {
			System.out.println(deleteNDS.getName()+" -> "+ deleteNDS.getActualName() +" parent: "+ treeNodeFreq.get(deleteNDS.getParent()).getNode());
		}
		
		System.out.println("***********************Alter tree based on xor and loop probability*************************************");
		if(toBeDeletedNodes.size()>0) {
			for(NodeDetailStore deleteNDS:toBeDeletedNodes) {
				System.out.println(deleteNDS.getName()+" -> "+ deleteNDS.getActualName() +" parent: "+ treeNodeFreq.get(deleteNDS.getParent()).getNode() +" prob: "+deleteNDS.getCFProbabilityBasedOnParent());
				System.out.println(deleteNDS.getVariantWiseActivityFrequency());
			}
			System.out.println("The processTree is");
			System.out.println(processTree.toString());
			
			this.processTree = HelperFunctions.modififyTreeBasedOnNodeProbability(this.processTree, toBeDeletedNodes, this.treeNodeFreq, this.parameters, false);
			
			System.out.println("***********************Calculating frequency again as variants have been updated******************");
			this.treeNodeFreq = HelperFunctions.setNodeSuretyToFalse(this.treeNodeFreq);
			this.treeNodeFreq = HelperFunctions.setFrequenciesOnProcessTree(this.treeNodeFreq, this.processTree, this.parameters);
			this.treeNodeFreq = HelperFunctions.setProbability(this.treeNodeFreq, this.parameters.getUpdatedTotalTraces());
			
		}
		
		System.out.println("*******************After deletion of nodes with less probability Based On Parent(xor and loop children)**********************");
		NodeDetailStore rootNDS = treeNodeFreq.get(this.processTree.getRoot().getID());
		System.out.println("Remaining variants count in root: "+rootNDS.getVariantWiseCaseFrequency().size());
		System.out.println("Remaining variants in root: "+rootNDS.getVariantWiseCaseFrequency());
		System.out.println("Remaining variants count in ds: "+ this.parameters.getUpdatedVariantFrequency().size()+" for threshold: "+this.parameters.getThreshold());
		System.out.println("Remaining trace count: "+ this.parameters.getUpdatedTotalTraces()+" for threshold: "+this.parameters.getThreshold());
		
		try {
			this.processTree =  ReduceTree.reduceTree(this.processTree,  new EfficientTreeReduceParameters(false, false));
		} catch (UnknownTreeNodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReductionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.petrinet = ProcessTree2Petrinet.convert(this.processTree).petrinet;
		} catch (NotYetImplementedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidProcessTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Object[] {this.processTree, this.petrinet};
	}

}
