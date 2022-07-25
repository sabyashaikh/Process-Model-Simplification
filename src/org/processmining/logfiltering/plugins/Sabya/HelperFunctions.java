package org.processmining.logfiltering.plugins.Sabya;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTree.Type;
import org.processmining.processtree.impl.AbstractBlock.Seq;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

class Pair{
	Integer TotalActivityFrequency = 0;
	Map<String, Integer> VariantWiseAF = new LinkedHashMap<String, Integer>();
	
	Pair(Integer totalActivityFrequency, Map<String, Integer> variantWiseAF){
		TotalActivityFrequency = totalActivityFrequency;
		VariantWiseAF = variantWiseAF;
	}
}


public class HelperFunctions {
	public static boolean getCompatibilityBetweenPTandLog(ProcessTree processTree, XLog InputLog, Map<String, String> eventCharacterMap) {
		Collection<Node> treeNodes = processTree.getNodes();
		for(Node node: treeNodes){
			if(node.isLeaf() && processTree.getType(node) == Type.MANTASK){
				if(!eventCharacterMap.containsKey(node.getName())){
					System.out.println("Activity: "+node.getName()+" not available in log");
					System.out.println("Process tree not compatible with event log");
					System.out.println("Tip: While providing the inputs, use the same Event log and classifier that was used to generate Process tree");
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static void displayDataStructure(Map<UUID, NodeDetailStore> treeNodeFreq, Boolean displayOldValue) {
		for (Map.Entry<UUID, NodeDetailStore> entry : treeNodeFreq.entrySet()) {
			System.out.println("__________________________________________________________________________________");
			System.out.println("Key : " + entry.getKey());
			System.out.println("Value : ");
			System.out.println("Name: " + entry.getValue().getName());
			System.out.println("Actual Name: " + entry.getValue().getActualName());
			System.out.println("Node: " + entry.getValue().getNode());
			System.out.println("Parent UUID: " + entry.getValue().getParent());
			System.out.println("Children List: " + entry.getValue().getChildren());
			System.out.println("Leaf Children List: " + entry.getValue().getLeafChildren());
			System.out.println("Child is activity: " + entry.getValue().getChildIsActivity());
			System.out.println("Activity Frequency: " + entry.getValue().getFrequency());
			System.out.println("Surety of AFrequency: " + entry.getValue().getSuretyOfActivityFrequency());
			System.out.println("Case Frequency: " + entry.getValue().getCaseFrequency());
			System.out.println("Surety of CFrequency: " + entry.getValue().getSuretyOfCaseFrequency());
			System.out.println("CF Overall Probability: " + entry.getValue().getCFOverallProbability());
			System.out.println("CF Probability wrt parent: " + entry.getValue().getCFProbabilityBasedOnParent());
			System.out.println("AF Overall Probability: " + entry.getValue().getAFOverallProbability());
			System.out.println("AF Probability wrt parent: " + entry.getValue().getAFProbabilityBasedOnParent());
			System.out.println("Type of Node: " + entry.getValue().getType());
			System.out.println("Variant wise Activity frequency: ");
			for (Map.Entry<String, Integer> entry1 : entry.getValue().getVariantWiseActivityFrequency().entrySet()) {
				System.out.println("	"+entry1.getKey() +" => "+entry1.getValue());
			}
			System.out.println("Variant wise Case frequency: ");
			for (Map.Entry<String, Integer> entry1 : entry.getValue().getVariantWiseCaseFrequency().entrySet()) {
				System.out.println("	"+entry1.getKey() +" => "+entry1.getValue());
			}
			if(displayOldValue) {
				System.out.println("Initial frequency details: ");
				System.out.println("	Activity Frequency: " + entry.getValue().getPreviousFrequencyDetails().ActivityFrequency);
				System.out.println("	Case Frequency: " + entry.getValue().getPreviousFrequencyDetails().CaseFrequency);
				System.out.println("	AF Overall Probability: " + entry.getValue().getPreviousFrequencyDetails().AF_OverallProbability);
				System.out.println("	AF Probability wrt parent: " + entry.getValue().getPreviousFrequencyDetails().AF_ProbabilityBasedOnParent);
				System.out.println("	CF Overall Probability: " + entry.getValue().getPreviousFrequencyDetails().CF_OverallProbability);
				System.out.println("	CF Probability wrt parent: " + entry.getValue().getPreviousFrequencyDetails().CF_ProbabilityBasedOnParent);
				/*System.out.println("	Variant wise Activity  frequency: ");
				for (Map.Entry<String, Integer> entry1 : entry.getValue().getPreviousFrequencyDetails().VariantWiseActivityFrequency.entrySet()) {
					System.out.println("		"+entry1.getKey() +" => "+entry1.getValue());
				}
				System.out.println("	Old Variant wise Case frequency: ");
				for (Map.Entry<String, Integer> entry1 : entry.getValue().getPreviousFrequencyDetails().VariantWiseActivityFrequency.entrySet()) {
//					System.out.println("		"+entry1.getKey() +" => "+entry1.getValue());
				}*/
			}
		}
	}
	
	public static void displayNode(NodeDetailStore nodeNDS) {
			System.out.println("__________________________________________________________________________________");
			System.out.println("Name: " + nodeNDS.getName());
			System.out.println("Actual Name: " + nodeNDS.getActualName());
			System.out.println("ID: " + nodeNDS.getNode().getID());
			System.out.println("Node: " + nodeNDS.getNode());
			System.out.println("Parent UUID: " + nodeNDS.getParent());
			System.out.println("Children List: " + nodeNDS.getChildren());
			System.out.println("Leaf Children List: " + nodeNDS.getLeafChildren());
			System.out.println("Child is activity: " + nodeNDS.getChildIsActivity());
			System.out.println("Activity Frequency: " + nodeNDS.getFrequency());
			System.out.println("Surety of AFrequency: " + nodeNDS.getSuretyOfActivityFrequency());
			System.out.println("Case Frequency: " + nodeNDS.getCaseFrequency());
			System.out.println("Surety of CFrequency: " + nodeNDS.getSuretyOfCaseFrequency());
			System.out.println("CF Overall Probability: " + nodeNDS.getCFOverallProbability());
			System.out.println("CF Probability wrt parent: " + nodeNDS.getCFProbabilityBasedOnParent());
			System.out.println("AF Overall Probability: " + nodeNDS.getAFOverallProbability());
			System.out.println("AF Probability wrt parent: " + nodeNDS.getAFProbabilityBasedOnParent());
			System.out.println("Type of Node: " + nodeNDS.getType());
			System.out.println("Variant wise Activity frequency: ");
			for (Map.Entry<String, Integer> entry1 : nodeNDS.getVariantWiseActivityFrequency().entrySet()) {
				System.out.println("	"+entry1.getKey() +" => "+entry1.getValue());
			}
			System.out.println("Variant wise Case frequency: ");
			for (Map.Entry<String, Integer> entry1 : nodeNDS.getVariantWiseCaseFrequency().entrySet()) {
				System.out.println("	"+entry1.getKey() +" => "+entry1.getValue());
			}
			/*System.out.println("Initial frequency details: ");
			System.out.println("	Activity Frequency: " + nodeNDS.getPreviousFrequencyDetails().ActivityFrequency);
			System.out.println("	Case Frequency: " + nodeNDS.getPreviousFrequencyDetails().CaseFrequency);
			System.out.println("	AF Overall Probability: " + nodeNDS.getPreviousFrequencyDetails().AF_OverallProbability);
			System.out.println("	AF Probability wrt parent: " + nodeNDS.getPreviousFrequencyDetails().AF_ProbabilityBasedOnParent);
			System.out.println("	CF Overall Probability: " + nodeNDS.getPreviousFrequencyDetails().CF_OverallProbability);
			System.out.println("	CF Probability wrt parent: " + nodeNDS.getPreviousFrequencyDetails().CF_ProbabilityBasedOnParent);
			System.out.println("	Variant wise Activity  frequency: ");
			for (Map.Entry<String, Integer> entry1 : nodeNDS.getPreviousFrequencyDetails().VariantWiseActivityFrequency.entrySet()) {
				System.out.println("		"+entry1.getKey() +" => "+entry1.getValue());
			}*/
			/*System.out.println("	Old Variant wise Case frequency: ");
			for (Map.Entry<String, Integer> entry1 : nodeNDS.getPreviousFrequencyDetails().VariantWiseActivityFrequency.entrySet()) {
				//System.out.println("		"+entry1.getKey() +" => "+entry1.getValue());
			}*/
	}
	
	public static void populateFrequencyMaps(XLog InputLog, Parameters parameters) {	
		Map<String, Integer> eventFrequency = parameters.getEventActivityFrequency();
		Map<String, Integer> variantFrequency = parameters.getVariantFrequency();
		Map<String, String> eventCharacterMap = parameters.getEventCharacterMap();
		Map<String, List<Integer>> variantTracesMap = parameters.getVariantTracesMap();
		Map<String, XEvent> activityEventMap = parameters.getActivityEventMap();
		Map<XTrace, Integer> XTraceToTraceIdMap = parameters.getXTraceToTraceIdMap();
		Map<Integer, XTrace> traceIdToXTraceMap = parameters.getTraceIdToXTraceMap();
		Map<Integer, String> traceIdMap = parameters.getTraceIdMap();
		Map<Integer, String> traceIdVariantMap = parameters.getTraceIdVariantMap();
		Map<String, List<Integer>> activityTracesMap = parameters.getActivityTracesMap();
		int traceId = 0;
		String traceInChar = "";
		 for (XTrace xtrace : InputLog) { // for each trace
			 traceId++;
			 traceInChar = ""; 
			 //List<String> templist = new ArrayList<String>();
			 for(XEvent event : xtrace) {
				String activity = "";
				String activityInChar = "";
				for(String eventClass : parameters.getEventClassifier().getDefiningAttributeKeys()) {
					if(!activity.isEmpty()) {
						activity=activity+"+";
					}
					activity = activity+event.getAttributes().get(eventClass).toString();
				}
				activityInChar = eventCharacterMap.get(activity);
				traceInChar= traceInChar+activityInChar;
			}
				
			//Keeping unique id for trace in a log 
			traceIdMap.put(traceId, traceInChar);
			traceIdVariantMap.put(traceId, traceInChar);
			//Keeping a mapping of trace in terms of character with actual trace 
			XTraceToTraceIdMap.put(xtrace, traceId);
			traceIdToXTraceMap.put(traceId, xtrace);
			if(variantFrequency.get(traceInChar)!=null) {
				variantFrequency.put(traceInChar, variantFrequency.get(traceInChar)+1);
				List <Integer> traceIdList = variantTracesMap.get(traceInChar);
				traceIdList.add(traceId);
				variantTracesMap.put(traceInChar, traceIdList);
			}
			else {
				variantFrequency.put(traceInChar,1);
				List <Integer> traceIdList = new  ArrayList<Integer>();
				traceIdList.add(traceId);
				variantTracesMap.put(traceInChar, traceIdList);
			}
		}
		for (Map.Entry<String, Integer> entry :variantFrequency.entrySet()) {
			String variant = entry.getKey();
			Integer variantFreq = entry.getValue();
			for(char activityInChar: variant.toCharArray()) {
				String activityChar = String.valueOf(activityInChar);  
				if (eventFrequency.get(activityChar)!=null) {
					eventFrequency.put(activityChar, eventFrequency.get(activityChar) + variantFreq);
				} else {
					eventFrequency.put(activityChar, variantFreq);
				}
			}
		}	
			
		parameters.setVariantTracesMap(variantTracesMap);
		parameters.setTraceIdMap(traceIdMap);
		parameters.setTraceIdVariantMap(traceIdVariantMap);
		parameters.setEventActivityFrequency(eventFrequency);
		parameters.setVariantFrequency(variantFrequency);
		parameters.setUpdatedVariantFrequency(variantFrequency);
		parameters.setTotalTraces(traceId);
		//TODO:
		//parameters.setActivityEventMap(activityEventMap);
		parameters.setXTracetoTraceIdMap(XTraceToTraceIdMap);
		//TODO:
		//parameters.setActivityTracesMap(activityTracesMap);
		parameters.setTraceIdToXTraceMapp(traceIdToXTraceMap);
	}
	
	public static void populateEventCharacterMaps(XLog InputLog, Parameters parameters) {
		XEventClassifier eventClassifier = parameters.getEventClassifier();
		//System.out.println(eventClassifier);
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, eventClassifier);
		Map<String, String> eventCharacterMap = parameters.getEventCharacterMap();
		Map<String, String> characterEventMap = parameters.getCharacterEventMap();
		int charCounter = parameters.getCharCounter(); 
		for (XEventClass clazz : logInfo.getEventClasses().getClasses()){
			eventCharacterMap.put(clazz.toString(), Character.toString((char)charCounter));
			characterEventMap.put(Character.toString((char)charCounter),clazz.toString());
			charCounter++;
		}
		parameters.setEventCharacterMap(eventCharacterMap);
		parameters.setCharacterEventMap(characterEventMap);	
		parameters.setCharCounter(charCounter);
	}
	public static void populateEventVariantFrequency(Parameters parameters) {
		Map<String, Map<String, Integer>> eventVariantMap = new LinkedHashMap<String, Map<String, Integer>>(); 
		Map<String, Integer> eventCaseFrequency = parameters.getEventCaseFrequency();
		for (Map.Entry<String, Integer> eventEntry : parameters.getEventActivityFrequency().entrySet()) {
			Integer caseFrequency = 0;
			for (Map.Entry<String, Integer> variantEntry : parameters.getVariantFrequency().entrySet()) {
				if(variantEntry.getKey().contains(eventEntry.getKey())){
					if (eventVariantMap.containsKey(eventEntry.getKey())) {
						Map<String, Integer> localVariantFrequency = eventVariantMap.get(eventEntry.getKey()); // this list contains all the traces and their frequency wrt to an event
						localVariantFrequency.put(variantEntry.getKey(), variantEntry.getValue());
						eventVariantMap.put(eventEntry.getKey(), localVariantFrequency);
						caseFrequency = caseFrequency+variantEntry.getValue();
					}
					else {
						Map<String, Integer> localVariantFrequency = new LinkedHashMap<String, Integer>();
						localVariantFrequency.put(variantEntry.getKey(), variantEntry.getValue());
						eventVariantMap.put(eventEntry.getKey(), localVariantFrequency);
						caseFrequency = caseFrequency+variantEntry.getValue();
					}
				}
			}
			eventCaseFrequency.put(eventEntry.getKey(), caseFrequency);
		}
		parameters.setEventCaseFrequency(eventCaseFrequency);
		parameters.setEventVariantMap(eventVariantMap);
	}
	

	public static String ConvertToTextTree(String sTree) {
		String textTree = "";
		String nodeName = "";
		List<String> nodeNames = new ArrayList<String>();
		int tabs= 0;
		for(int i=0; i<sTree.length();i++){
	        String c = String.valueOf(sTree.charAt(i));  
	        if (c.contentEquals("(")) {
	        	c = System.lineSeparator();
	        	tabs += 1;
	        	 for (int j=0; j<tabs ; j++) {
	         		c +="->";
	         	}
	        	nodeNames.add(nodeName);
	        	nodeName="";
	        	nodeNames.add(c);  
	        }else if(c.contentEquals(")")) {
	        	c="";
	        	tabs -=1;
	        	nodeNames.add(nodeName);
	        	nodeName="";
	        	nodeNames.add(c);
	        }else if(c.contentEquals(",")) {
	        	c = System.lineSeparator();
	        	for (int j=0; j<tabs ; j++) {
	         		c +="->";
	         	}
	        	nodeNames.add(nodeName);
	        	nodeName="";
	        	nodeNames.add(c);
	        }else {
	        	nodeName +=c;
	        }
	    	textTree += c; 
	    }
		return textTree;
	}
	public static String getEncodedNodeName(ProcessTree tree, Node node, Map<String, String> eventCharacterMap) {
		Type nodeType = tree.getType(node);
		String nodeName = null;
		if(nodeType == Type.MANTASK) {
			nodeName = eventCharacterMap.get(node.getName());
		}
		else if(nodeType == Type.AUTOTASK) {
			nodeName ="tau";
		}
		else if(nodeType == Type.XOR) {
			nodeName ="xor";
		}
		else if(nodeType == Type.SEQ) {
			nodeName ="seq";
		}
		else if(nodeType == Type.AND) {
			nodeName ="and";
		}
		else if(nodeType == Type.OR){
			nodeName ="or";
		}
		else if(nodeType == Type.LOOPXOR){
			nodeName ="loop";
		}		
		return nodeName;
	}
	public static Map<UUID, NodeDetailStore> mapPTtoNodeDetailStructure(ProcessTree processTree, Node node, UUID parentID, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters){
		String nodeName = getEncodedNodeName(processTree, node, parameters.getEventCharacterMap());
		//System.out.println("activity name is: "+nodeName);
		if(!node.isLeaf()) {
			//if node is operator then call this function for its children
			Block nodeBlock = (Block)node;
			List<Node> children = nodeBlock.getChildren();
			treeNodeFreq.put(node.getID(),new NodeDetailStore(nodeName, node.getName(), node, parentID, children));
			for (Node child : children) {
				treeNodeFreq = mapPTtoNodeDetailStructure(processTree, child, node.getID(), treeNodeFreq, parameters);
			}
		}
		else {
			treeNodeFreq.put(node.getID(),new NodeDetailStore(nodeName,node.getName(),node, parentID, Collections.emptyList()));
		}
		return treeNodeFreq;
	}

	//This function sets loop's activity frequency as same as first child of the loop 
	public static Map<UUID, NodeDetailStore> setActivityFrequenciesOnLoopNode( Map<UUID, NodeDetailStore> treeNodeFreq){
		for (Map.Entry<UUID, NodeDetailStore> nodeEntry : treeNodeFreq.entrySet()) {
			NodeDetailStore nodeNDS = nodeEntry.getValue();
			if(nodeNDS.getName().contains("loop") && nodeNDS.getType().contentEquals("operator")){
				Node firstChild = nodeEntry.getValue().getChildren().get(0);
				NodeDetailStore firstChildNDS = treeNodeFreq.get(firstChild.getID());
				nodeNDS.setFrequency(firstChildNDS.getFrequency());
				nodeNDS.setVariantWiseActivityFrequency(firstChildNDS.getVariantWiseActivityFrequency());
				treeNodeFreq.put(nodeEntry.getKey(), nodeNDS);
			}
		}
		return treeNodeFreq;
	}
	public static Map<UUID, NodeDetailStore> removeActivityFrequenciesOnLoopNode( Map<UUID, NodeDetailStore> treeNodeFreq){
		for (Map.Entry<UUID, NodeDetailStore> nodeEntry : treeNodeFreq.entrySet()) {
			NodeDetailStore nodeNDS = nodeEntry.getValue();
			if(nodeNDS.getName().contentEquals("loop")){
				nodeNDS.setFrequency(nodeNDS.getCaseFrequency());
				nodeNDS.setVariantWiseActivityFrequency(nodeNDS.getVariantWiseCaseFrequency());
				treeNodeFreq.put(nodeEntry.getKey(), nodeNDS);
			}
		}
		return treeNodeFreq;
	}
	public static int getActivityFrequency(NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore>  treeNodeFreq, Parameters parameters) throws UnknownTreeNodeException {
		//System.out.println("getting frequency for the node is :"+node+ "-"+ activityName);
		String nodeName = nodeNDS.getName();
		if(nodeNDS.getType().contains("activity")) {
			//System.out.println("activity name is");	
			if(parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName())!=null) {
				System.out.println("The node:  "+nodeNDS.getName()+ " acti freq from old: "+parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()).getFrequency());
				return parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()).getFrequency();
			}
			else if(parameters.getUpdatedEventActivityFrequency().containsKey(nodeName)){
				System.out.println("The node:  "+nodeNDS.getName()+ " acti freq from init: "+ parameters.getUpdatedEventActivityFrequency().get(nodeName));
				Integer c = parameters.getUpdatedEventActivityFrequency().get(nodeName);
				return c;
			}
		}else if (nodeNDS.getType().contains("tau")) {
			System.out.println("Tau: "+nodeNDS.getName()+" frequency returning 0");
			return 0;
		}
		else if (nodeName.contains("xor")) {
			//for the xor itself, there are no transitions fired
			//so, we take the sum of all children
			Integer result = 0;
			for (Node child : nodeNDS.getChildren()) {
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				result += getActivityFrequency(childNDS, treeNodeFreq, parameters);
			}
			System.out.println("The xor node:  "+nodeNDS.getName()+ " acti freq using sum: "+ result);
			return result;
		} else if ((nodeName.contains("seq"))||(nodeName.contains("and"))) {
			//the sequence has no transitions that can fire
			//pick the maximum of the children
			Integer result = 0;
			for (Node child : nodeNDS.getChildren()) {		
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				result = Math.max(result, getActivityFrequency(childNDS, treeNodeFreq, parameters));
			}
			System.out.println("The seq node:  "+nodeNDS.getName()+ " acti freq using max: "+ result);
			return result;
		} else if (nodeName.contains("loop")) {
			//a loop is executed precisely as often as its exit node.
			//frequency of the right most child node must be considered
			Node thirdChild = nodeNDS.getChildren().get(2);
			NodeDetailStore thirdChildNDS = treeNodeFreq.get(thirdChild.getID());
			Integer result = getActivityFrequency(thirdChildNDS, treeNodeFreq, parameters);
			System.out.println("The loop node:  "+nodeNDS.getActualName()+ " acti freq using max: "+ result);
			return result;
			
		} else if (nodeName.contains("or")) {
			//for the OR, there is no way to determine how often it fired just by its children
			//for now, pick the maximum of the children
			Integer result = 0;
			for (Node child : nodeNDS.getChildren()) {	
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				result = Math.max(result, getActivityFrequency(childNDS, treeNodeFreq, parameters));
			}
			return result;			
		}
		throw new UnknownTreeNodeException();
	}
	public static Map<String, Integer> cloneLinkedHashMap(Map<String,Integer> linkedHashMapToClone){
		Gson gson = new Gson();
		String jsonString = gson.toJson(linkedHashMapToClone);
		java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String, Integer>>(){}.getType();
		LinkedHashMap<String, Integer> clonedMap = gson.fromJson(jsonString, type); 
		return clonedMap;
	}
	public static Map<String, Integer> subtractVariantFrequency(Map<String, Integer> variantWithFrequency1, Map<String, Integer> variantWithFrequency2){
		Map<String, Integer> clonedVariantWithFrequency1 = cloneLinkedHashMap(variantWithFrequency1);
		for (Map.Entry<String, Integer> entry : variantWithFrequency2.entrySet()) {
			if(clonedVariantWithFrequency1.containsKey(entry.getKey())){
				Integer oldFrequency = clonedVariantWithFrequency1.get(entry.getKey());
				if(oldFrequency-entry.getValue()==0) {
					clonedVariantWithFrequency1.remove(entry.getKey());
				}
				else {
					clonedVariantWithFrequency1.put(entry.getKey(), oldFrequency-entry.getValue());
				}
			}
		}
		return clonedVariantWithFrequency1;
	}
	public static Map<String, Integer> aggregateVariantFrequency(Map<String, Integer> variantWithFrequency1, Map<String, Integer> variantWithFrequency2){
		Map<String, Integer> clonedVariantWithFrequency1 = cloneLinkedHashMap(variantWithFrequency1);
		for (Map.Entry<String, Integer> entry : variantWithFrequency2.entrySet()) {
			if(clonedVariantWithFrequency1.containsKey(entry.getKey())){
				Integer oldFrequency = clonedVariantWithFrequency1.get(entry.getKey());
				clonedVariantWithFrequency1.put(entry.getKey(), oldFrequency+entry.getValue());
			}else {
				clonedVariantWithFrequency1.put(entry.getKey(),entry.getValue());
			}
		}
		return clonedVariantWithFrequency1;
	}
	public static Map<String, Integer> maxVariantFrequency(Map<String, Integer> variantWithFrequency1, Map<String, Integer> variantWithFrequency2){
		Map<String, Integer> clonedVariantWithFrequency1 = cloneLinkedHashMap(variantWithFrequency1);
		for (Map.Entry<String, Integer> entry : variantWithFrequency2.entrySet()) {
			if(clonedVariantWithFrequency1.containsKey(entry.getKey())){
				Integer oldFrequency = clonedVariantWithFrequency1.get(entry.getKey());
				if(oldFrequency<entry.getValue()) {
					clonedVariantWithFrequency1.put(entry.getKey(), entry.getValue());
				}
			}else {
				clonedVariantWithFrequency1.put(entry.getKey(),entry.getValue());
			}
		}
		return clonedVariantWithFrequency1;
	}
	
	public static Map<String, Integer> getVariantWiseActivityFrequency(NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore>  treeNodeFreq, Parameters parameters) throws UnknownTreeNodeException {
		//System.out.println("getting frequency for the node is :"+node+ "-"+ activityName);
		String nodeName = nodeNDS.getName();
		if(nodeNDS.getType().contains("activity")) {
			//System.out.println("activity name is");
			if(parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName())!=null) {
				return parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()).getVariantWiseActivityFrequency();
			}
			else if(parameters.getUpdatedEventWithVariantWiseActivityFrequency().containsKey(nodeName)){
				Map<String, Integer> variantWiseAF = parameters.getUpdatedEventWithVariantWiseActivityFrequency().get(nodeName);
				return variantWiseAF;
			}
		}else if (nodeNDS.getType().contains("tau")) {
			//System.out.println("Tau frequency returning 0");
			Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
			return variantWiseAF;
		}
		else if (nodeName.contains("xor")) {
			//for the xor itself, there are no transitions fired
			//so, we take the sum of all children
			Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
			for (Node child : nodeNDS.getChildren()) {
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				variantWiseAF = aggregateVariantFrequency(variantWiseAF, getVariantWiseActivityFrequency(childNDS, treeNodeFreq, parameters));
			}
			return variantWiseAF;
		} else if ((nodeName.contains("seq"))||(nodeName.contains("and"))) {
			//the sequence has no transitions that can fire
			//pick the maximum of the children
			Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
			for (Node child : nodeNDS.getChildren()) {		
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				variantWiseAF = maxVariantFrequency(variantWiseAF, getVariantWiseActivityFrequency(childNDS, treeNodeFreq, parameters));
			}
			return variantWiseAF;
		} else if (nodeName.contains("loop")) {
			//a loop is executed precisely as often as its exit node.
			//frequency of the right most child node must be considered
			Node thirdChild = nodeNDS.getChildren().get(2);
			NodeDetailStore thirdChildNDS = treeNodeFreq.get(thirdChild.getID());
			return getVariantWiseActivityFrequency(thirdChildNDS, treeNodeFreq, parameters);
			
		} else if (nodeName.contains("or")) {
			//for the OR, there is no way to determine how often it fired just by its children
			//for now, pick the maximum of the children
			Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
			for (Node child : nodeNDS.getChildren()) {	
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				variantWiseAF = maxVariantFrequency(variantWiseAF, getVariantWiseActivityFrequency(childNDS, treeNodeFreq, parameters));
			}
			return variantWiseAF;			
		}
		throw new UnknownTreeNodeException();
	}
	public static int getCaseFrequency(NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters){
		//System.out.println("getting frequency for the node :"+nodeNDS.getName());
		
		if(nodeNDS.getType()=="activity") {
			//System.out.println("activity name is");
			if(parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName())!=null) {
				System.out.println("The node:  "+nodeNDS.getName()+ " case freq: "+parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()).getCaseFrequency());
				return parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()).getCaseFrequency();
			
			}
			else if(parameters.getEventVariantMap().containsKey(nodeNDS.getName())){
				Integer caseFrequency = 0;
				//TODO : this can be done previously and stored
				//for (Map.Entry<String, Integer> variantEntry : parameters.getEventVariantMap().get(nodeNDS.getName()).entrySet()) {
				//	caseFrequency = caseFrequency + variantEntry.getValue();
				//}
				if(parameters.getUpdatedEventCaseFrequency().containsKey(nodeNDS.getName())){
					System.out.println("The node:  "+nodeNDS.getName()+ " case freq: "+ parameters.getUpdatedEventCaseFrequency().get(nodeNDS.getName()));
					caseFrequency = parameters.getUpdatedEventCaseFrequency().get(nodeNDS.getName());
				}
				return caseFrequency;
			}
			else {
				System.out.println("Error occured: Activity not found in eventVariantMap");
			}
		}else if (nodeNDS.getType()=="tau") {
			System.out.println("Tau case frequency returning 0");
			return 0;
			
		}
		else if (nodeNDS.getType()=="operator") {
			if(nodeNDS.getName().contains("xor")) {
				Integer caseFrequency = 0;
				//get leaf children of xor and check for those leaf nodes in eventVariantMap to and add up all casefrequency
				//Note: there can be overlapping traces hence remove them before totaling
				
				List<NodeDetailStore> xorLeafChildren = getLeafChildrenNDS(treeNodeFreq, nodeNDS);
				List<String> variantEntriesProcessed = new ArrayList<String>();
				for (NodeDetailStore childNDS: xorLeafChildren) {
					Map<String, Integer> variantWiseCF = getVariantWiseCaseFrequency(childNDS, treeNodeFreq, parameters);
					for (Map.Entry<String, Integer> variantEntry : variantWiseCF.entrySet()) {
						if(!variantEntriesProcessed.contains(variantEntry.getKey())) {
							caseFrequency = caseFrequency + variantEntry.getValue();
							variantEntriesProcessed.add(variantEntry.getKey());
						}
					}
				}
				System.out.println("The xor node:  "+nodeNDS.getName()+ " case freq "+ caseFrequency);
				return caseFrequency;
			} else if ((nodeNDS.getName().contains("seq"))||(nodeNDS.getName().contains("and")) || nodeNDS.getName().contains("loop") || nodeNDS.getName().contains("or")) {
				//the sequence has no transitions that can fire
				//pick the maximum of the children
				Integer result = 0;
				for (Node child : nodeNDS.getChildren()) {		
					NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
					result = Math.max(result, getCaseFrequency(childNDS, treeNodeFreq, parameters));
				}
				System.out.println("The seq/and node:  "+nodeNDS.getName()+ " case freq "+ result);
				return result;
			}
		}
		return 0;
	}
	public static Map<String, Integer> getVariantWiseCaseFrequency(NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters){
		//System.out.println("getting frequency for the node is :"+node+ "-"+ activityName);
		if(nodeNDS.getType()=="activity") {
			//System.out.println("activity name is");
			String nodeName = nodeNDS.getName();
			
			if(parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName())!=null) {
				return parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()).getVariantWiseCaseFrequency();
			}
			else if(parameters.getUpdatedEventWithVariantWiseCaseFrequency().containsKey(nodeName)){
				Map<String, Integer> variantWiseAF = parameters.getUpdatedEventWithVariantWiseCaseFrequency().get(nodeName);
				return variantWiseAF;
			}
			else {
				System.out.println("Error occured: Activity not found in EventWithVariantWiseCaseFrequency");
			}
		}else if (nodeNDS.getType()=="tau") {
			//System.out.println("Tau frequency returning 0");
			Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
			return variantWiseAF;
		}
		else if (nodeNDS.getType()=="operator") {
			//pick the maximum of the children
			Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
				for (Node child : nodeNDS.getChildren()) {		
					NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
					variantWiseAF = maxVariantFrequency(variantWiseAF, getVariantWiseCaseFrequency(childNDS, treeNodeFreq, parameters));
				}
				return variantWiseAF;
			}
		Map<String, Integer> variantWiseAF = new  LinkedHashMap<String, Integer>();
		return variantWiseAF;
	}
	
	public static void computeVariantWiseFrequencyForEvent(Parameters parameters) {
		for (Map.Entry<String, Integer> variant : parameters.getVariantFrequency().entrySet()) {
			// for every activity in the variant check in our if activity is available then increment case and activity freq
			for(char activity: variant.getKey().toCharArray()) {
				parameters.incrementFreqOfEventForVariant(Character.toString(activity), variant.getKey(), variant.getValue());
			}
		}
	}

	public static  Map<UUID, NodeDetailStore> setNodeInitialFrequencies(NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters) throws UnknownTreeNodeException {
 		if(parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName())!=null) {
			nodeNDS.copyFrequencyDetails(parameters.getDataStructureWithUpdatedActivities().get(nodeNDS.getActualName()));
			System.out.println("the activity frequeny of node : " +nodeNDS.getName()+" taken from old ds is: "+ nodeNDS.getFrequency());
			System.out.println("the case frequeny of node : " +nodeNDS.getName()+" taken from old ds is: "+ nodeNDS.getCaseFrequency());
		}
		else {	
			Integer activityFrequency = 0;
			if(!nodeNDS.getSuretyOfActivityFrequency()) {
				activityFrequency = getActivityFrequency(nodeNDS, treeNodeFreq, parameters);
				Map<String, Integer> variantWiseActivityFrequency = getVariantWiseActivityFrequency(nodeNDS, treeNodeFreq, parameters);
				nodeNDS.setFrequency(activityFrequency);
				nodeNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
				System.out.println("the activity frequeny of node : " +nodeNDS.getName()+" freshly calculated is:  "+ activityFrequency);			
			}
			else {
				System.out.println("the activity frequeny of node: " +nodeNDS.getName()+" is already set: "+ nodeNDS.getFrequency());
			}
			Integer caseFrequency =0;
			if(!nodeNDS.getSuretyOfCaseFrequency()) {
				caseFrequency = getCaseFrequency(nodeNDS, treeNodeFreq, parameters);
				Map<String, Integer> variantWiseCaseFrequency = getVariantWiseCaseFrequency(nodeNDS, treeNodeFreq, parameters);
				nodeNDS.setCaseFrequency(caseFrequency);
				nodeNDS.setVariantWiseCaseFrequency(variantWiseCaseFrequency);
				System.out.println("the case frequeny of node: " +nodeNDS.getName()+" is "+ caseFrequency);
			}
			else {
				System.out.println("the case frequeny of node: " +nodeNDS.getName()+" is already set: "+ nodeNDS.getCaseFrequency());
			}
		}
		if(nodeNDS.getType().contains("operator")) {
			//if node is operator then call this function for its children
			List<Node> children = nodeNDS.getChildren();
			for (Node child : children) {	
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				treeNodeFreq = setNodeInitialFrequencies(childNDS, treeNodeFreq, parameters);
			}
		}
		else {
			//if node is activity then set the surety of that node to true
			//if node is activity then frequency determined for its parent node in case of seq/and is sure. hence set its surety to true
			if(nodeNDS.getType().contains("activity")){
				nodeNDS.setSuretyOfActivityFrequency(true);
				nodeNDS.setSuretyOfCaseFrequency(true);
				NodeDetailStore parentNDS =treeNodeFreq.get(nodeNDS.getParent());
				if(parentNDS != null) {
					//in case of all variants being deleted the parent node becomes null 
					if(parentNDS.getName().contains("seq")|| parentNDS.getName().contains("and")) {
						parentNDS.setSuretyOfActivityFrequency(true);
					}
					treeNodeFreq.put(nodeNDS.getParent(), parentNDS);
				}
			}
		}
		treeNodeFreq.put(nodeNDS.getNode().getID(), nodeNDS);
		return treeNodeFreq;
	}
	
	//get list of all "Ands" that are ancestor of the given node
	public static List<String> getAncestorAndsLeafChildren(List<String> allAncestorAndLeafChildren, NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore> treeNodeFreq){
		if(!nodeNDS.getNode().isRoot()) {
			NodeDetailStore parentNDS = treeNodeFreq.get(nodeNDS.getParent());
			if(parentNDS.getName().contains("and")) {
				if(parentNDS.getLeafChildren().isEmpty()) {
					List<String> andChildren = getLeafChildren(treeNodeFreq, parentNDS);
					//System.out.println("leaf children for And node: "+parentNDS.getName() +" are : " + andChildren);
					parentNDS.setLeafChildren(andChildren);
					treeNodeFreq.put(parentNDS.getNode().getID(),parentNDS);
				}
				allAncestorAndLeafChildren.addAll(parentNDS.getLeafChildren());
			}
			getAncestorAndsLeafChildren(allAncestorAndLeafChildren, parentNDS, treeNodeFreq);
		}
		return allAncestorAndLeafChildren;
	}
	//Get the tau frequency and update parents whenever necessary
	public static Map<UUID, NodeDetailStore> setTauActivityFrequency(NodeDetailStore tauNDS, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters) {
		UUID tauID = tauNDS.getNode().getID();
		UUID tauParentId = treeNodeFreq.get(tauID).getParent();
		NodeDetailStore tauParentNDS =  treeNodeFreq.get(tauParentId);
		String tauParentName = tauParentNDS.getName();
		int tauParentFrequency =  tauParentNDS.getFrequency();
		List<Node> children = tauParentNDS.getChildren();
		
		if(tauParentName.contains("loop")){
			Node firstChild = children.get(0);
			Node secondChild = children.get(1);
			Node thirdChild = children.get(2);
			NodeDetailStore firstChildNDS = treeNodeFreq.get(firstChild.getID());
			NodeDetailStore secondChildNDS = treeNodeFreq.get(secondChild.getID());
			NodeDetailStore thirdChildNDS = treeNodeFreq.get(thirdChild.getID());
			//if first child is tau and its surety is unknown then 
			//if  parent freq and second child freq is sure then use that to calculate first child
			//else if  third child freq and second child freq is sure then use that to calculate first child
			if(firstChild.getID() == tauID && !firstChildNDS.getSuretyOfActivityFrequency()) {
				System.out.println("Tau is first child of loop");
				//if  third child freq and second child freq is sure then use that to calculate first child
				if(thirdChildNDS.getSuretyOfActivityFrequency()) {
					 if(secondChildNDS.getSuretyOfActivityFrequency()) {
						 //third child + second child freq = first child freq
						 Integer frequency = secondChildNDS.getFrequency()+thirdChildNDS.getFrequency();
						 Map<String, Integer> variantWiseActivityFrequency = aggregateVariantFrequency(secondChildNDS.getVariantWiseActivityFrequency(),thirdChildNDS.getVariantWiseActivityFrequency());
						 firstChildNDS.setFrequency(frequency);
						 firstChildNDS.setSuretyOfActivityFrequency(true);
						 firstChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
						 treeNodeFreq.put(firstChild.getID(), firstChildNDS);
						 System.out.println("Modified loop first child: tauNode:"+tauNDS.getName()+" with frequency:"+frequency+" based on second and third child");
						 //TODO: Should third child frequency be updated?
					 }
					 else {
						 System.out.println("The first child is tau, third child surety is known and second child surety is unknown "
						 		+ " Hence, do nothing and wait from top to bottom propogation"); 
					 }
				}
				//if  parent freq and second child freq is sure then use that to calculate first child 
				else if(tauParentNDS.getSuretyOfActivityFrequency()) {
					 if(secondChildNDS.getSuretyOfActivityFrequency()) {
						 //loop(parent) freq + second child freq = first child freq
						 Integer frequency = secondChildNDS.getFrequency()+tauParentNDS.getFrequency();
						 firstChildNDS.setFrequency(frequency);
						 Map<String, Integer> variantWiseActivityFrequency = aggregateVariantFrequency(secondChildNDS.getVariantWiseActivityFrequency(),tauParentNDS.getVariantWiseActivityFrequency());
						 firstChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
						 firstChildNDS.setSuretyOfActivityFrequency(true);
						 treeNodeFreq.put(firstChild.getID(), firstChildNDS);
						 System.out.println("Modified loop first child: tauNode:"+tauNDS.getName()+" with frequency:"+frequency+" based on parent and second child");
						 
						//Modify third child frequency because parent has surety
						firstChildNDS.setVariantWiseActivityFrequency(tauParentNDS.getVariantWiseActivityFrequency());
						thirdChildNDS.setFrequency(tauParentNDS.getFrequency());
						thirdChildNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
						System.out.println("Modified loop third child frequency with:"+tauParentNDS.getFrequency()+" because parent has surety");								
						//because third child is updated also update its children
						if(!thirdChild.isLeaf()) {
							treeNodeFreq = updateChildrenFrequency(thirdChildNDS, treeNodeFreq);
						}
						
					 }
					 else {
						 System.out.println("The first child is tau, parent surety is known and second child surety is unknown "
						 		+ " Hence, do nothing and wait from top to bottom propogation"); 
					 }
				}
				else {
					System.out.println("The first child is tau and parent surety is not known and third child surety is unknown."
					 		+ " Hence, do nothing and wait from top to bottom propogation");
				}
			}
			//if second child is tau then check if first child has surety if it doesn't have surety 
			//then use its pattern to get first child frequency and second child frequency 
			else if(secondChild.getID() == tauID && !secondChildNDS.getSuretyOfActivityFrequency()) {
				System.out.println("Tau is second child of loop");
				//if first child surety is known check if third child surety is known or parent surety known if yes then calculate second child freq based on first and third child
				//if first child surety is known check if third child surety or parent surety is unknown if yes then calculate second child freq based on first child pattern with exit frequency
				//if first child surety is unknown calculate its pattern and its freq and check if parent or third child surety is known 
				//if yes calculate the freq based on first child pattern and third/parent freq 
				//if parent or third child frequency is unknown then calculate second child pattern and its frequency from there
				
				if(firstChildNDS.getSuretyOfActivityFrequency()) {
					//if first child surety is known check if third child surety is known or parent surety known
					if(thirdChildNDS.getSuretyOfActivityFrequency()) {
						Integer secondChildFrequency = 0 ;
						secondChildFrequency= firstChildNDS.getFrequency()-thirdChildNDS.getFrequency();
						secondChildNDS.setFrequency(secondChildFrequency);
						Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(firstChildNDS.getVariantWiseActivityFrequency(), thirdChildNDS.getVariantWiseActivityFrequency());
						secondChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
						secondChildNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(secondChild.getID(),secondChildNDS);
						System.out.println("Modified loop second child: tauNode:"+tauNDS.getName()+" with frequency:"+secondChildFrequency+" based on third and first child");
						//No need to update parent because if third child surety was true that means parent is already updated
					}
					else if (tauParentNDS.getSuretyOfActivityFrequency()) {
						Integer secondChildFrequency = 0 ;
						secondChildFrequency= firstChildNDS.getFrequency()-tauParentNDS.getFrequency();
						secondChildNDS.setFrequency(secondChildFrequency);
						Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(firstChildNDS.getVariantWiseActivityFrequency(), tauParentNDS.getVariantWiseActivityFrequency());
						secondChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
						secondChildNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(secondChild.getID(),secondChildNDS);
						System.out.println("Modified loop second child: tauNode:"+tauNDS.getName()+" with frequency:"+secondChildFrequency+" based on parent and first child");						
						
						//Modify third child frequency because parent has surety 
						thirdChildNDS.setFrequency(tauParentNDS.getFrequency());
						thirdChildNDS.setVariantWiseActivityFrequency(tauParentNDS.getVariantWiseActivityFrequency());
						thirdChildNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
						System.out.println("Modified loop third child frequency with:"+tauParentNDS.getFrequency()+" because parent has surety");								
						//because third child is updated also update its children
						if(!thirdChild.isLeaf()) {
							treeNodeFreq = updateChildrenFrequency(thirdChildNDS, treeNodeFreq);
						}
					}
					else {	
						//We know first child surety but we dont know second child and third child surety
						//now we check how many times we had "D".. this D frequency is the count of exits of loop.
						//then based on exit and first child frequency we calculate second child frequency 
						
						//get list of first child's leaf children 
						firstChildNDS.setLeafChildren(getLeafChildren(treeNodeFreq, firstChildNDS));
						treeNodeFreq.put(firstChild.getID(), firstChildNDS);
						//get list of third child's leaf children 
						thirdChildNDS.setLeafChildren(getLeafChildren(treeNodeFreq, thirdChildNDS));
						treeNodeFreq.put(thirdChild.getID(), thirdChildNDS);
						//combine first child and third child to their parent's leaf children
						List<String> loopLeafChildren = new ArrayList<String>();
						loopLeafChildren.addAll(firstChildNDS.getLeafChildren());
						loopLeafChildren.addAll(thirdChildNDS.getLeafChildren());
						
						//To calculate the frequency of "D" we need to refine the trace to remove AND children, and convert all the "DO children" to $. 
						//get list of all "And" ancestor's leaf children
						List<String> allAncestorAndLeafChildren = new ArrayList<String>();
						allAncestorAndLeafChildren	= getAncestorAndsLeafChildren(allAncestorAndLeafChildren, tauParentNDS, treeNodeFreq);
						
						//System.out.println("List of all ancestor And children: "+allAncestorAndLeafChildren);
						//Here the children of second child will be empty ---  secondChildNDS.getLeafChildren().. because its a tau
						Pair data = getLoopExitFrequencyBasedOnTraces(parameters.getUpdatedVariantFrequency(), allAncestorAndLeafChildren, firstChildNDS.getLeafChildren(), loopLeafChildren);
						
						Integer exitFrequency = data.TotalActivityFrequency;
						
						//Assign the exit frequency to third child frequency and also to parent 
						thirdChildNDS.setFrequency(exitFrequency);
						thirdChildNDS.setVariantWiseActivityFrequency(data.VariantWiseAF);
						thirdChildNDS.setSuretyOfActivityFrequency(true);
						thirdChildNDS.setCaseFrequency(exitFrequency);
						thirdChildNDS.setVariantWiseCaseFrequency(data.VariantWiseAF);
						thirdChildNDS.setSuretyOfCaseFrequency(true);
						treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
						System.out.println("Modified loop third child:"+ thirdChildNDS.getName()+" frequency with exit frequency:"+exitFrequency+" based on"
								+ " pattern matching because first child:"+firstChildNDS.getName()+" frequency is known");								
						//because third child is updated also update its children
						if(!thirdChild.isLeaf()) {
							treeNodeFreq = updateChildrenFrequency(thirdChildNDS, treeNodeFreq);
						}
						
						//Calculate second child frequency based on third and first child
						Integer secondChildFrequency= firstChildNDS.getFrequency()-exitFrequency;
						secondChildNDS.setFrequency(secondChildFrequency);
						Map<String, Integer> secondChildVariantWiseActivityFrequency =  subtractVariantFrequency(firstChildNDS.getVariantWiseActivityFrequency(), data.VariantWiseAF);
						secondChildNDS.setVariantWiseActivityFrequency(secondChildVariantWiseActivityFrequency);
						secondChildNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(secondChild.getID(),secondChildNDS);
						System.out.println("Modified loop second child: tauNode:"+tauNDS.getName()+" with frequency:"+secondChildFrequency+" based on pattern matching "
								+ "because first child:"+firstChildNDS.getName()+" frequency is known");						
						
						//Because the third child is updated also update parent node frequency
						tauParentFrequency = exitFrequency;
						tauParentNDS.setFrequency(tauParentFrequency);
						tauParentNDS.setVariantWiseActivityFrequency(data.VariantWiseAF);
						tauParentNDS.setSuretyOfActivityFrequency(true);
						tauParentNDS.setCaseFrequency(tauParentFrequency);
						tauParentNDS.setVariantWiseCaseFrequency(data.VariantWiseAF);
						tauParentNDS.setSuretyOfCaseFrequency(true);
						treeNodeFreq.put(tauParentId,tauParentNDS);
						System.out.println("Modified loop(parent) frequecncy with:"+tauParentFrequency+" because third child changed");								
						
						if(!tauParentNDS.getNode().isRoot()) {
							treeNodeFreq = checkChildParentSync(tauParentNDS, treeNodeFreq);
						}	
					}//end of if third or parent frequency unkown
				}//end of if first child surety known
				else {
					//if first child surety is unknown ...we also don't know second child surety because we are here to calculate the second child surety. ...
					//hence we cannot calculate the frequency. Wait until first child surety becomes true
					System.out.println("Wait for top down updation for this tau node because first child surety is unknown and "
							+ "we also don't know second child surety because we are here to calculate the second child surety "
							+ "hence we cannot calculate the frequency. Wait until first child surety becomes true");
					}
			}//end of if second child of loop is tau
			//third child is a tau node check if its surety is not true then based on first and second child surety update third child frequency
			else if(thirdChild.getID() == tauID && !thirdChildNDS.getSuretyOfActivityFrequency()) {
				//parent - loop has surety true then assign loop value to third child
				if(tauParentNDS.getSuretyOfActivityFrequency()) {
					thirdChildNDS.setFrequency(tauParentNDS.getFrequency());
					thirdChildNDS.setVariantWiseActivityFrequency(tauParentNDS.getVariantWiseActivityFrequency());
					thirdChildNDS.setSuretyOfActivityFrequency(true);
					treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
					System.out.println("Modified loop third child: tauNode:"+tauID+" with its parent frequency:"+tauParentNDS.getFrequency());					
				}
				else {
					if(firstChildNDS.getSuretyOfActivityFrequency() && secondChildNDS.getSuretyOfActivityFrequency()) {
						Integer firstChildFrequency = firstChildNDS.getFrequency();
						Integer secondChildFrequency = secondChildNDS.getFrequency();
						Integer thirdChildFrequency = firstChildFrequency-secondChildFrequency;
						thirdChildNDS.setFrequency(thirdChildFrequency);
						Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(firstChildNDS.getVariantWiseActivityFrequency(), secondChildNDS.getVariantWiseActivityFrequency());
						thirdChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
						thirdChildNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
						System.out.println("Modified loop third child- tauNode:"+tauID+" using first and second child freq:"+thirdChildFrequency);
						
						//Update the tauNode parent-loop frequency in treeNodeFreq and cascade the frequency to loops parent
						tauParentNDS.setFrequency(thirdChildFrequency);
						tauParentNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
						tauParentNDS.setSuretyOfActivityFrequency(true);
						treeNodeFreq.put(tauParentId,tauParentNDS);
						System.out.println("Modified loop(tau's parent) frequency: "+tauParentId+" with frequency:"+thirdChildFrequency);
						//check if tau's parent node(loop) frequency is in sync with its parent
						if(!tauParentNDS.getNode().isRoot()) {
							treeNodeFreq = checkChildParentSync(tauParentNDS, treeNodeFreq);
						}
					}
					else {
						System.out.println("The third child:tau of loop is not being calculated because parent, first and second child freq is uncertain ");
					}
				}
			}//end of if third child tau in loop
			else {
				System.out.println("tau node's:"+tauNDS.getName()+" surety is already set. Hence not modifying");
			}
		}
		else if(tauParentName.contains("seq") || tauParentName.contains("and")) {
			//tau node is seq or and's child hence the tau node frequency must be equal to seq/and node frequency
			tauNDS.setFrequency(tauParentFrequency);
			tauNDS.setVariantWiseActivityFrequency(tauParentNDS.getVariantWiseActivityFrequency());
			if(tauParentNDS.getSuretyOfActivityFrequency()) {
				//only if we are sure that parent frequency is sure to be correct then we set surety flag to true
				tauNDS.setSuretyOfActivityFrequency(true);
			}
			treeNodeFreq.put(tauID, tauNDS);
			System.out.println("Modified child of seq/and: "+ tauParentNDS.getName()+"with tauNode :"+tauNDS.getName()+" with frequency:"+tauParentFrequency);
		}
		else if(tauParentName.contains("xor")) { 
			//if surety of xor and all its children except tau is true than we can calculate tau value
			//otherwise do not update tauNode(child of xor) because the XOR frequency was calculated keeping in mind the tau frequency as zero. 
			//so xor node frequency will surely be same as sum of all children frequencies 
			
			int childrenFrequency = 0;
			Map<String, Integer> childrenVariantWiseActivityFrequency = new LinkedHashMap<String, Integer>();
			boolean suretyOfChildren = false;
			for(Node child : children){
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				childrenFrequency += childNDS.getFrequency();
				childrenVariantWiseActivityFrequency = aggregateVariantFrequency(childrenVariantWiseActivityFrequency, childNDS.getVariantWiseActivityFrequency());
				if(!(childNDS.getType().contains("tau"))) {
					suretyOfChildren = suretyOfChildren && treeNodeFreq.get(child.getID()).getSuretyOfActivityFrequency() ;
				}
			}
			//if surety of children and parent is true we can use those freq to calculate tau freq
			if(suretyOfChildren && tauParentNDS.getSuretyOfActivityFrequency()) {
				Integer frequency = tauParentNDS.getFrequency()-childrenFrequency;
				tauNDS.setFrequency(frequency);
				Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(tauParentNDS.getVariantWiseActivityFrequency(), childrenVariantWiseActivityFrequency);
				tauNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
				tauNDS.setSuretyOfActivityFrequency(true);
				treeNodeFreq.put(tauID, tauNDS);
				System.out.println("Modified child of xor: tauNode :"+tauNDS.getName()+" with frequency:"+frequency+" because xor and all uts surety is true");
			}
			//children frequency can only be lesser than parent. If it is greater xor at this point its an error 
			else if(childrenFrequency>tauParentNDS.getFrequency()) {
				System.out.println("Error occured: children freq cannot be more than xor freq while calculating tau");
			}
			else if(!suretyOfChildren && tauParentNDS.getSuretyOfActivityFrequency()) {
				System.out.println("for tau node: "+tauNDS.getName() +"Wait for the top down force update for calculation because there are multiple children with unknown frequency");
			}
			else if(!tauParentNDS.getSuretyOfActivityFrequency()) {
				System.out.println("for tau node: "+tauNDS.getName() +"Wait for the top down update for calculation because parent frequency is unsure");
			}
		}
		return treeNodeFreq;
	}
	
	public static Pair getLoopExitFrequencyBasedOnTraces(Map<String, Integer> variantFrequency, List<String> allAncestorAndLeafChildren, List<String> doLeafChildren, List<String> loopLeafChildren){
		
		//Remove all ancestor ands children from each trace and convert the doLeafChildren to "$" and reDoLeafChildren to "^"
		List<String> ancestorAndLeafChildrenExcludingLoopChildren = cloneList(allAncestorAndLeafChildren);
		//From list of and children remove those children of current loop
		ancestorAndLeafChildrenExcludingLoopChildren.removeAll(loopLeafChildren);
		
		//For regex pattern creation convert array to string
		String stringAllAncestorAndLeafChildren = String.join(",", ancestorAndLeafChildrenExcludingLoopChildren);
		//System.out.println("Ancestor Ands leaf children after removing current loop's children: " +stringAllAncestorAndLeafChildren);
		String stringDoLeafChildren = String.join(",", doLeafChildren);
		//String stringReDoLeafChildren = String.join(",", reDoLeafChildren);
		
		int patternfrequencyInAllTraces = 0;
		Map<String, Integer> variantWiseAF = new LinkedHashMap<String, Integer>();
		for(Entry<String, Integer> variant: variantFrequency.entrySet()) {
			
			//From each trace remove and's other children
			String variantWithPatterns = variant.getKey();
			
			if(!stringAllAncestorAndLeafChildren.isEmpty()) {
				String traceWithoutAndChildren = variantWithPatterns.replaceAll("["+stringAllAncestorAndLeafChildren+"]","");
				//System.out.println("Trace after removing other AND children: "+ traceWithoutAndChildren);
				variantWithPatterns =traceWithoutAndChildren;
			}
			
			String patternToCheck="";
			
			//Every children of 'Do' must be replaced with $ sign
			if(!stringDoLeafChildren.isEmpty()) {
				patternToCheck +="\\$";
				//System.out.println("The do string is:"+stringDoLeafChildren+":");
				//System.out.println("Regex generated: ["+stringDoLeafChildren+"]");
				String traceWithDoPattern = variantWithPatterns.replaceAll("["+stringDoLeafChildren+"]+","\\$");
				//System.out.println("Trace after replacing do character: "+ traceWithDoPattern);
				variantWithPatterns = traceWithDoPattern;
			}
			/*
			//Every children of 'Redo' must be replaced with ^ sign
			if(!stringReDoLeafChildren.isEmpty()) {
				patternToCheck +="\\^";
				//System.out.println("The redo string is:"+stringReDoLeafChildren+":");
				//System.out.println("Regex generated: ["+stringReDoLeafChildren+"]");
				String traceWithReDoPattern = traceWithPatterns.replaceAll("["+stringReDoLeafChildren+"]+","\\^");
				//System.out.println("Trace after replacing redo character: "+ traceWithReDoPattern);
				traceWithPatterns = traceWithReDoPattern;
			}
			*/
			//Count the number of times $ has occurred 
			int patternFrequencyInOneVariant = getPatternMatchCount(patternToCheck, variantWithPatterns);
	        //System.out.println("Pattern to be checked: "+ patternToCheck+ " in a trace: "+trace.getKey()+" after projecting the trace to:"+traceWithPatterns+" and the count : " + patternFrequencyInOneTrace);
			patternfrequencyInAllTraces = patternfrequencyInAllTraces+(patternFrequencyInOneVariant* variant.getValue());
			
			if (patternFrequencyInOneVariant>0) {
				variantWiseAF.put(variant.getKey(), patternFrequencyInOneVariant* variant.getValue());
			}
		}
		System.out.println("Total count : " + patternfrequencyInAllTraces);
		Pair data = new Pair(patternfrequencyInAllTraces, variantWiseAF); 
		return data;
	}
	public static Integer getPatternMatchCount(String patternToCheck, String variant) {
		Integer patternFrequencyInOneVariant=0;
		Pattern pattern = Pattern.compile(patternToCheck);
		Matcher matcher = pattern.matcher(variant);
        while(matcher.find()) {
        	patternFrequencyInOneVariant++;
        }
		return patternFrequencyInOneVariant;
	}
	
	
	public static ProcessTree createXlogAndSubPTBasedOnPatterns(NodeDetailStore andNode, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters, XLog InputLog, ProcessTree mainProcessTree, String modificationType){
		String leafChildren = String.join(",",getLeafChildren(treeNodeFreq, andNode));
		Map<String, String> variantsToChange = parameters.getVariantsToChange();
		Map<String,List<String>> andPatternWithVariantList = new LinkedHashMap<String, List<String>>();
		Map<String,Double> andPatternWithProb = new LinkedHashMap<String, Double>();
		Map<String,Double> andPatternToKeep = new LinkedHashMap<String, Double>();
		Map<String,Integer> andPatternWithFreq = new LinkedHashMap<String, Integer>();
		List<NodeDetailStore> toBeDeletedNodesExtended = new ArrayList<NodeDetailStore>();
		Map<String, Integer> variantListToDelete = new LinkedHashMap<String, Integer>();
		Map<String, Integer> variantListToKeep = new LinkedHashMap<String, Integer>();
		Map<String, Integer> updatedVariantFrequency = parameters.getUpdatedVariantFrequency();
		System.out.println("The count of variants in and node is: "+andNode.getVariantWiseCaseFrequency());
		System.out.println("The count of variants in main ds is: "+updatedVariantFrequency);
		Map<String, Integer> andNodeVariantList = new LinkedHashMap<String, Integer>();
		//This forloop loops through and node patterns and gets children list and the children pattern in a variant
		//eg: [x, y, z ] are children of and
		// <a x y z b> 0.5, <a y z x b> 0.2, <a x y z c> 0.3 
		// {x y z,  0.8} { y z x , 0.2}
		Integer totalCF = 0;
		if(modificationType.contentEquals("Only Remove")) {
			Object[] data = getVariantsForNode(updatedVariantFrequency, leafChildren);
			andNodeVariantList = (Map<String, Integer>) data[0];
			totalCF = (Integer) data[1];
		}
		else {
			andNodeVariantList = andNode.getVariantWiseCaseFrequency();
			totalCF = andNode.getCaseFrequency();
		}
		
		Integer totalPatternFrequency = 0 ;
		for (Map.Entry<String, Integer> variant : andNodeVariantList.entrySet()){
			//remove the non-children of "and" from pattern
			//double variantProbability = (double) variant.getValue()/(double)totalCF;
			String traceWithAndChildren = variant.getKey().replaceAll("[^"+leafChildren+"]","");
			//System.out.println("The and children in trace: "+traceWithAndChildren);
			if(andPatternWithVariantList.get(traceWithAndChildren)== null) {
				List<String> variantListForPattern = new ArrayList<String>();
				variantListForPattern.add(variant.getKey());
				andPatternWithVariantList.put(traceWithAndChildren, variantListForPattern);
				Integer patternFrequency =  variant.getValue();
				andPatternWithFreq.put(traceWithAndChildren, patternFrequency);
			}
			else {
				List<String> variantListForPattern = andPatternWithVariantList.get(traceWithAndChildren);
				variantListForPattern.add(variant.getKey());
				andPatternWithVariantList.put(traceWithAndChildren, variantListForPattern);
				Integer patternFrequency  = andPatternWithFreq.get(traceWithAndChildren);
				andPatternWithFreq.put(traceWithAndChildren, patternFrequency+variant.getValue());
				//andPatternWithProb.get(traceWithAndChildren);
				//andPatternWithProb.put(traceWithAndChildren, andPatternWithProb.get(traceWithAndChildren)+variantProbability);
			}
			totalPatternFrequency= totalPatternFrequency+variant.getValue();
		}
		System.out.println("The total pattern frequency is: "+totalPatternFrequency);
		for (Map.Entry<String, Integer> pattern : andPatternWithFreq.entrySet()){
			double patternProbability = (double) pattern.getValue()/(double)totalPatternFrequency;
			andPatternWithProb.put(pattern.getKey(), patternProbability);
			System.out.println("The pattern: "+ pattern.getKey()+ " has prob"+ patternProbability);
		}
		
		//find the minimum and max probability of patterns in "and" node
		double maxProbability = 0.0;
		double minProbability = 1.0; 
		for(Map.Entry<String, Double> pattern: andPatternWithProb.entrySet()) {
			if(maxProbability<pattern.getValue()) {
				maxProbability = pattern.getValue();
			}
			if(minProbability>pattern.getValue()) {
				minProbability = pattern.getValue();
			}
		}
		System.out.println("For 'And' node: " +andNode.getName());
		System.out.println("Max prob: " +maxProbability);
		System.out.println("Min prob: " +minProbability);
		if(maxProbability == minProbability && andPatternWithProb.size()!=1 ) {
			System.out.println("The 'and' node patterns' min and max prob = "+maxProbability);
			return null;
		}
		else {
			double adjustedThreshold = parameters.getThreshold()*(maxProbability-minProbability);
			//double adjustedThreshold = parameters.getThreshold();//*(maxProbability-minProbability);
			//for all patterns create a sublog
			XFactory factory = XFactoryRegistry.instance().currentDefault();
			XLog subLog = factory.createLog();
			subLog.setAttributes(InputLog.getAttributes());
			System.out.println("The adjusted threshold is: "+adjustedThreshold);
			
			for (Map.Entry<String, Double> pattern : andPatternWithProb.entrySet()){
				if(pattern.getValue()>= adjustedThreshold) {
					subLog = addTracesToXLog(subLog, pattern.getKey(), parameters.getCharCounter(), parameters.getCharacterEventMap());
					andPatternToKeep.put(pattern.getKey(), pattern.getValue());
					for(String variantsFromPatternToKeep :andPatternWithVariantList.get(pattern.getKey())) {
						variantListToKeep.put(variantsFromPatternToKeep, 0);//add all the variants that need to be kept
					}
				}
				else {
					//get list of all variants that have to be excluded while checking similar patterns 
					for(String variantsFromPatternToDelete :andPatternWithVariantList.get(pattern.getKey())) {
						variantListToDelete.put(variantsFromPatternToDelete, 0);//add all the variants that need to be deleted
						//into this ds so that no other variants are mapped to these variants as their similar variants
					}
				}
			}
			System.out.println("The count of variant to keep is: "+variantListToKeep.size());
			for(Map.Entry<String, Integer> variantToKeep : variantListToKeep.entrySet()) {
				System.out.println("The variant: "+ variantToKeep.getKey()+" is kept");
			}
			System.out.println("The count of variant to delete is: "+variantListToDelete.size());
			/*for(Map.Entry<String, Integer> variantToDelete : variantListToDelete.entrySet()) {
				System.out.println("The variant: "+ variantToDelete.getKey()+" is removed");
			}*/
			if(modificationType.contentEquals("Convert")) {
				for(Map.Entry<String, Integer> entry : variantListToDelete.entrySet()) {
						String variantToDelete = entry.getKey();
						//the variant/pattern must be removed from the ds because the pattern that it belongs to has lower probability
							
						//Get most similar variant from "and" node
						String similarVariant = findMostSimilarVariant(variantToDelete, variantListToKeep, variantListToDelete);	
						//variantToBeDeleted must be converted to similarVariant in list of variants available in log
						if(updatedVariantFrequency.containsKey(variantToDelete)) {
							if(updatedVariantFrequency.containsKey(similarVariant)) { 
								Integer caseFrequencyForVariant = updatedVariantFrequency.get(similarVariant);
								updatedVariantFrequency.put(similarVariant, caseFrequencyForVariant+updatedVariantFrequency.get(variantToDelete));
								//System.out.println("From main list 	The variant being deleted is: "+variantToDelete+ " with freq:  "+updatedVariantFrequency.get(variantToDelete) );
								//System.out.println("and similar variant: "+similarVariant +" with freq: "+updatedVariantFrequency.get(similarVariant));
								updatedVariantFrequency.remove(variantToDelete);
							}
							else {
								System.out.println("Error: The similar variant : "+variantToDelete+" was already removed");
							}
						}
						else {
							System.out.println("Error: The variant to delete: "+variantToDelete+" was already removed");
						}
						
						//variantToBeDeleted must be converted to similarVariant in all nodes
						//Hence delete the variant from the list of variant of nodes and increment frequency of most 
						//similar variant by variantTodelete frequency
						NodeDetailStore rootNDS = treeNodeFreq.get(mainProcessTree.getRoot().getID());
						if(rootNDS.getVariantWiseCaseFrequency().containsKey(variantToDelete) || rootNDS.getVariantWiseCaseFrequency().containsKey(similarVariant)) {							
							treeNodeFreq = updateVariantFrequencyOfNode(andNode, variantToDelete, similarVariant, treeNodeFreq, rootNDS, toBeDeletedNodesExtended);
						}
						//Add the changes needed to be done on log to variantsTodelete
						variantsToChange.put(variantToDelete, similarVariant);
						//System.out.println("The variant: "+variantToDelete+" must be changed to "+similarVariant);
					if(toBeDeletedNodesExtended.size()>0) {
						alterTreeAndConvert(mainProcessTree, toBeDeletedNodesExtended, treeNodeFreq, variantsToChange, updatedVariantFrequency);
					}
				}
				//System.out.println("End of variant update for node "+andNode.getName());
				parameters.setVariantsToChange(variantsToChange);
			}
			else if(modificationType.contentEquals("Remove")){
				//displayDataStructure(treeNodeFreq,false);
				Integer totalTraces = parameters.getUpdatedTotalTraces();
				for(Map.Entry<String, Integer> entry : variantListToDelete.entrySet()) {
					String variantToDelete = entry.getKey();
					//the variant/pattern must be removed from the ds because the pattern that it belongs to has lower probability
					if(updatedVariantFrequency.containsKey(variantToDelete)) {
							//System.out.println("From main list 	The variant being deleted is: "+variantToDelete+ " with freq:  "+updatedVariantFrequency.get(variantToDelete) );
							totalTraces = totalTraces-updatedVariantFrequency.get(variantToDelete);
							updatedVariantFrequency.remove(variantToDelete);
					}
					else {
						System.out.println("The variant to delete: "+variantToDelete+" was already removed");
					}
					
					//variantToBeDeleted must be converted to similarVariant in all nodes
					//Hence delete the variant from the list of variant of nodes and increment frequency of most 
					//similar variant by variantTodelete frequency
					NodeDetailStore rootNDS = treeNodeFreq.get(mainProcessTree.getRoot().getID());
					if(rootNDS.getVariantWiseCaseFrequency().containsKey(variantToDelete) || rootNDS.getVariantWiseActivityFrequency().containsKey(variantToDelete)) {							
						treeNodeFreq = updateVariantsOfNode(andNode, variantToDelete, treeNodeFreq, rootNDS, toBeDeletedNodesExtended);
					}
					
					if(toBeDeletedNodesExtended.size()>0) {
						alterTreeAndRemove(mainProcessTree, toBeDeletedNodesExtended, treeNodeFreq, parameters);
					}
				}
				parameters.setUpdatedTotalTraces(totalTraces);
				//System.out.println("End of variant update for node "+andNode.getName());
			}
			else if(modificationType.contentEquals("Only Remove")){
				Integer totalTraces = parameters.getUpdatedTotalTraces();
				Map<String, Integer> eventCaseFrequency = parameters.getEventCaseFrequency();
				Map<String, Integer> eventActivityFrequency = parameters.getEventActivityFrequency();
				Map<String, Map<String, Integer>> eventWithVariantWiseActivityFrequency = parameters.getUpdatedEventWithVariantWiseActivityFrequency();
				Map<String, Map<String, Integer>> eventWithVariantWiseCaseFrequency = parameters.getUpdatedEventWithVariantWiseCaseFrequency();
				for(Map.Entry<String, Integer> entry : variantListToDelete.entrySet()) {
					String variantToDelete = entry.getKey();
					//the variant/pattern must be removed from the ds because the pattern that it belongs to has lower probability
					//remove it from eventVariant
					if(updatedVariantFrequency.get(variantToDelete)!=null) {
						//System.out.println("From main list 	The variant being deleted is: "+variantToDelete+ " with freq:  "+updatedVariantFrequency.get(variantToDelete) );
						totalTraces = totalTraces-updatedVariantFrequency.get(variantToDelete);
						updatedVariantFrequency.remove(variantToDelete);
						for(Map.Entry<String, Map<String, Integer>> event: eventWithVariantWiseActivityFrequency.entrySet()) {
							Map<String, Integer> variantWiseAF = event.getValue();
							if(variantWiseAF.get(variantToDelete)!=null) {
								String nodeName = event.getKey();
								Integer nodeActivityFrequency = eventActivityFrequency.get(nodeName);
								Integer variantActivityFrequency = variantWiseAF.get(variantToDelete);
								eventActivityFrequency.put(nodeName, nodeActivityFrequency - variantActivityFrequency);
								variantWiseAF.remove(variantToDelete);
								eventWithVariantWiseActivityFrequency.put(nodeName, variantWiseAF);
								Map<String, Integer> variantWiseCF = eventWithVariantWiseCaseFrequency.get(nodeName);
								Integer nodeCaseFrequency = eventCaseFrequency.get(nodeName);
								Integer variantCaseFrequency = variantWiseCF.get(variantToDelete);
								eventCaseFrequency.put(nodeName, nodeCaseFrequency - variantCaseFrequency);
								variantWiseCF.remove(variantToDelete);
								eventWithVariantWiseCaseFrequency.put(nodeName, variantWiseCF);
							}
						}
					}
					else {
						System.out.println("The variant to delete: "+variantToDelete+" was already removed");
					}
				}
				parameters.setUpdatedEventActivityFrequency(eventActivityFrequency);
				parameters.setUpdatedEventCaseFrequency(eventCaseFrequency);
				parameters.setUpdatedEventWithVariantWiseCaseFrequency(eventWithVariantWiseCaseFrequency);
				parameters.setUpdatedEventWithVariantWiseActivityFrequency(eventWithVariantWiseActivityFrequency);
				parameters.setUpdatedTotalTraces(totalTraces);
				System.out.println("End of variant update for node "+andNode.getName());
			}
			parameters.setUpdatedVariantFrequency(updatedVariantFrequency);
			return mineProcessTree(subLog, null);
		}
	} 
	public static Object[] getVariantsForNode(Map<String, Integer> variantWiseFrequency, String leafChildren){
		Map<String, Integer> variantList = new LinkedHashMap<String, Integer>();
		Integer totalCF = 0;
		for (Map.Entry<String, Integer> variant : variantWiseFrequency.entrySet()){		
			Pattern p = Pattern.compile("["+leafChildren+"]"); 
			Matcher m = p.matcher(variant.getKey());
			if (m.find()) {
				variantList.put(variant.getKey(), variant.getValue());
				totalCF = totalCF + variant.getValue();
			}
		}
		return new Object[] {variantList, totalCF};
	}
	public static Parameters updateFrequencyInDSToConvertVariant(Parameters parameters, String variantToDelete, String similarVariant) {
		Map<String, Integer> eventCaseFrequency = parameters.getUpdatedEventCaseFrequency();
		Map<String, Integer> eventActivityFrequency = parameters.getUpdatedEventActivityFrequency();
		Map<String, Map<String, Integer>> eventWithVariantWiseActivityFrequency = parameters.getUpdatedEventWithVariantWiseActivityFrequency();
		Map<String, Map<String, Integer>> eventWithVariantWiseCaseFrequency = parameters.getUpdatedEventWithVariantWiseCaseFrequency();
		Map<String, Integer> updatedVariantFrequency = parameters.getUpdatedVariantFrequency();
		Map<String, String> variantsToChange = parameters.getVariantsToChange();
		Integer totalTraces = parameters.getUpdatedTotalTraces();
		variantsToChange.put(variantToDelete, similarVariant);
		if(updatedVariantFrequency.containsKey(variantToDelete)) {
			Integer caseFrequencyForVariantToDeleteInMainDS = updatedVariantFrequency.get(variantToDelete);
			//System.out.println("From main list 	The variant being deleted is: "+variantToDelete+ " with freq:  "+updatedVariantFrequency.get(variantToDelete) );
			updatedVariantFrequency.remove(variantToDelete);
			if(updatedVariantFrequency.containsKey(similarVariant)) { 
				Integer caseFrequencyForSimilarVariantInMainDS = updatedVariantFrequency.get(similarVariant);
				//System.out.println("and similar variant is updated with : "+similarVariant +" with freq: "+updatedVariantFrequency.get(similarVariant));
				updatedVariantFrequency.put(similarVariant, caseFrequencyForSimilarVariantInMainDS+caseFrequencyForVariantToDeleteInMainDS);
			}else {
				totalTraces = totalTraces-caseFrequencyForVariantToDeleteInMainDS;
				System.out.println("The similar variant :"+ similarVariant  +" for the variant to delete :"+variantToDelete+" was already removed");
			}
			
			// update variant wise frequencies
			for(Map.Entry<String, Map<String, Integer>> event: eventWithVariantWiseActivityFrequency.entrySet()) {
				//System.out.println("Event: " +event.getKey()+" variants: "+event.getValue());
				String eventName = event.getKey();
				Map<String, Integer> variantWiseAF = event.getValue();
				Map<String, Integer> variantWiseCF = eventWithVariantWiseCaseFrequency.get(eventName);
				if(variantWiseAF.containsKey(variantToDelete) && variantWiseAF.containsKey(similarVariant)) {
						Integer activityFrequencyForSimilarVariant = variantWiseAF.get(similarVariant);
						Integer caseFrequencyOfSimilarVariant = variantWiseCF.get(similarVariant);
						Integer activityFreqOfSimilarVariantPerCase = activityFrequencyForSimilarVariant/caseFrequencyOfSimilarVariant;
						Integer caseFrequencyOfVariantToDelete = variantWiseCF.get(variantToDelete);
						Integer activityFrequencyOfVariantToDelete = variantWiseAF.get(variantToDelete);
						Integer additionalActivityFrequencyForSimilarVariant = activityFreqOfSimilarVariantPerCase*caseFrequencyOfVariantToDelete;
						//System.out.println("The variant being deleted is: "+variantToDelete+ " with afreq:  "+activityFrequencyOfVariantToDelete+" case freq: "+caseFrequencyOfVariantToDelete );
						//System.out.println("and similar variant: "+similarVariant +" with freq: "+activityFrequencyForSimilarVariant+" with case freq: "+caseFrequencyOfSimilarVariant+" also available in node: "+ event.getKey());
						//System.out.println("We need to increase the similar variant: "+similarVariant +" based on its activity freq in one case * the case freq of deleted variant which is:"+additionalActivityFrequencyForSimilarVariant);
						Integer oldActivityFrequency = eventActivityFrequency.get(eventName);
						eventActivityFrequency.put(eventName, oldActivityFrequency-activityFrequencyOfVariantToDelete+additionalActivityFrequencyForSimilarVariant);
						variantWiseAF.put(similarVariant, activityFrequencyForSimilarVariant+additionalActivityFrequencyForSimilarVariant);
						variantWiseAF.remove(variantToDelete);
						eventWithVariantWiseActivityFrequency.put(eventName, variantWiseAF);
						//System.out.println("The variant being deleted is: "+variantToDelete+ " with freq:  "+variantWiseCF.get(variantToDelete) );
						//System.out.println("and similar variant: "+similarVariant +" with freq: "+variantWiseCF.get(similarVariant)+" also available in node: "+ nodeNDS.getName());
						variantWiseCF.put(similarVariant, caseFrequencyOfSimilarVariant+caseFrequencyOfVariantToDelete);
						variantWiseCF.remove(variantToDelete);
						eventWithVariantWiseCaseFrequency.put(eventName, variantWiseCF);
				}else if(variantWiseAF.containsKey(variantToDelete) && !variantWiseAF.containsKey(similarVariant)){
						//Integer caseFrequencyOfVariantToDelete = variantWiseCF.get(variantToDelete);
						//Integer activityFrequencyOfVariantToDelete = variantWiseAF.get(variantToDelete);
						//System.out.println("The variant being deleted is: "+variantToDelete+ " with afreq:  "+activityFrequencyOfVariantToDelete+" case freq: "+caseFrequencyOfVariantToDelete );
						Integer oldActivityFrequency = eventActivityFrequency.get(eventName);
						eventActivityFrequency.put(eventName, oldActivityFrequency-variantWiseAF.get(variantToDelete));
						variantWiseAF.remove(variantToDelete);
						eventWithVariantWiseActivityFrequency.put(eventName, variantWiseAF);
						Integer oldCaseFrequency = eventCaseFrequency.get(eventName);
						eventCaseFrequency.put(eventName, oldCaseFrequency-variantWiseCF.get(variantToDelete));
						variantWiseCF.remove(variantToDelete);
						eventWithVariantWiseCaseFrequency.put(eventName, variantWiseCF);
				}else if(!variantWiseAF.containsKey(variantToDelete) && variantWiseAF.containsKey(similarVariant)){
						//System.out.println("The similar variant is not present in event wise frequency ds");
						Integer oldActivityFrequency = eventActivityFrequency.get(eventName);
						Integer caseFrequencyOfSimilarVariant = variantWiseCF.get(similarVariant);
						Integer activityFrequencyForSimilarVariant = variantWiseAF.get(similarVariant);
						Integer activityFreqOfSimilarVariantPerCase = activityFrequencyForSimilarVariant / caseFrequencyOfSimilarVariant;
						Integer additionalActivityFrequencyForSimilarVariant = activityFreqOfSimilarVariantPerCase * caseFrequencyForVariantToDeleteInMainDS;
						variantWiseAF.put(similarVariant, activityFrequencyForSimilarVariant + additionalActivityFrequencyForSimilarVariant); //caseFrequency and activity frequency is same in main ds
						eventWithVariantWiseActivityFrequency.put(eventName, variantWiseAF);
						eventActivityFrequency.put(eventName, oldActivityFrequency+additionalActivityFrequencyForSimilarVariant);
						Integer oldCaseFrequency = eventCaseFrequency.get(eventName);
						variantWiseCF.put(similarVariant, caseFrequencyOfSimilarVariant+caseFrequencyForVariantToDeleteInMainDS);
						eventWithVariantWiseCaseFrequency.put(eventName, variantWiseCF);
						eventCaseFrequency.put(eventName, oldCaseFrequency+caseFrequencyForVariantToDeleteInMainDS);
				}									
			}
		}
		else {
			System.out.println("Error: The variant to delete: "+variantToDelete+" was already removed");
		}
		parameters.setVariantsToChange(variantsToChange);
		parameters.setUpdatedEventActivityFrequency(eventActivityFrequency);
		parameters.setUpdatedEventCaseFrequency(eventCaseFrequency);
		parameters.setUpdatedEventWithVariantWiseCaseFrequency(eventWithVariantWiseCaseFrequency);
		parameters.setUpdatedEventWithVariantWiseActivityFrequency(eventWithVariantWiseActivityFrequency);	
		parameters.setUpdatedTotalTraces(totalTraces);
		return parameters;
	}
	public static Parameters updateFrequencyInDSToRemoveVariant(Parameters parameters, String variantToDelete) {
		Map<String, Integer> updatedVariantFrequency = parameters.getUpdatedVariantFrequency();
		Integer totalTraces = parameters.getUpdatedTotalTraces();
		Map<String, Integer> eventCaseFrequency = parameters.getUpdatedEventCaseFrequency();
		Map<String, Integer> eventActivityFrequency = parameters.getUpdatedEventActivityFrequency();
		Map<String, Map<String, Integer>> eventWithVariantWiseActivityFrequency = parameters.getUpdatedEventWithVariantWiseActivityFrequency();
		Map<String, Map<String, Integer>> eventWithVariantWiseCaseFrequency = parameters.getUpdatedEventWithVariantWiseCaseFrequency();
		
		//the variant/pattern must be removed from the ds because the pattern that it belongs to has lower probability
		//remove it from eventVariant		
		if(updatedVariantFrequency.get(variantToDelete)!=null) {
			//System.out.println("From main list 	The variant being deleted is: "+variantToDelete+ " with freq:  "+updatedVariantFrequency.get(variantToDelete) );
			totalTraces = totalTraces-updatedVariantFrequency.get(variantToDelete);
			updatedVariantFrequency.remove(variantToDelete);
			for(Map.Entry<String, Map<String, Integer>> event: eventWithVariantWiseActivityFrequency.entrySet()) {
				Map<String, Integer> variantWiseAF = event.getValue();
				if(variantWiseAF.get(variantToDelete)!=null) {
					String nodeName = event.getKey();
					Integer nodeActivityFrequency = eventActivityFrequency.get(nodeName);
					Integer variantActivityFrequency = variantWiseAF.get(variantToDelete);
					eventActivityFrequency.put(nodeName, nodeActivityFrequency - variantActivityFrequency);
					variantWiseAF.remove(variantToDelete);
					eventWithVariantWiseActivityFrequency.put(nodeName, variantWiseAF);
					Map<String, Integer> variantWiseCF = eventWithVariantWiseCaseFrequency.get(nodeName);
					Integer nodeCaseFrequency = eventCaseFrequency.get(nodeName);
					Integer variantCaseFrequency = variantWiseCF.get(variantToDelete);
					eventCaseFrequency.put(nodeName, nodeCaseFrequency - variantCaseFrequency);
					variantWiseCF.remove(variantToDelete);
					eventWithVariantWiseCaseFrequency.put(nodeName, variantWiseCF);
				}
			}
		}
		else {
			System.out.println("Error: The variant to delete: "+variantToDelete+" was already removed");
		}
		parameters.setUpdatedEventActivityFrequency(eventActivityFrequency);
		parameters.setUpdatedEventCaseFrequency(eventCaseFrequency);
		parameters.setUpdatedEventWithVariantWiseCaseFrequency(eventWithVariantWiseCaseFrequency);
		parameters.setUpdatedEventWithVariantWiseActivityFrequency(eventWithVariantWiseActivityFrequency);	
		parameters.setUpdatedTotalTraces(totalTraces);
		parameters.setUpdatedVariantFrequency(updatedVariantFrequency);
		return parameters;
	}
	public static ProcessTree createXlogAndSubPTBasedOnPatterns2(NodeDetailStore andNode, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters, XLog InputLog, ProcessTree mainProcessTree, Boolean modificationTypeIsConvert){
		String leafChildren = String.join(",",getLeafChildren(treeNodeFreq, andNode));
		Map<String,List<String>> andPatternWithVariantList = new LinkedHashMap<String, List<String>>();
		Map<String,Double> andPatternWithProb = new LinkedHashMap<String, Double>();
		Map<String,Double> andPatternToKeep = new LinkedHashMap<String, Double>();
		Map<String,Integer> andPatternWithFreq = new LinkedHashMap<String, Integer>();
		Map<String, Integer> variantListToDelete = new LinkedHashMap<String, Integer>();
		Map<String, Integer> variantListToKeep = new LinkedHashMap<String, Integer>();
		System.out.println("The count of variants in and node is: "+andNode.getVariantWiseCaseFrequency());
		System.out.println("The count of variants in main ds is: "+parameters.getUpdatedVariantFrequency());
		Map<String, Integer> andNodeVariantList = new LinkedHashMap<String, Integer>();
		//This forloop loops through and node patterns and gets children list and the children pattern in a variant
		//eg: [x, y, z ] are children of and
		// <a x y z b> 0.5, <a y z x b> 0.2, <a x y z c> 0.3 
		// {x y z,  0.8} { y z x , 0.2}
		Integer totalCF = 0;
		/*if(modificationType.contentEquals("Only Remove")) {
			Object[] data = getVariantsForNode(updatedVariantFrequency, leafChildren);
			andNodeVariantList = (Map<String, Integer>) data[0];
			totalCF = (Integer) data[1];
		}
		else {*/
			andNodeVariantList = andNode.getVariantWiseCaseFrequency();
			totalCF = andNode.getCaseFrequency();
		//}
		
		Integer totalPatternFrequency = 0 ;
		for (Map.Entry<String, Integer> variant : andNodeVariantList.entrySet()){
			//remove the non-children of "and" from pattern
			//double variantProbability = (double) variant.getValue()/(double)totalCF;
			String traceWithAndChildren = variant.getKey().replaceAll("[^"+leafChildren+"]","");
			//System.out.println("The and children in trace: "+traceWithAndChildren);
			if(andPatternWithVariantList.get(traceWithAndChildren)== null) {
				List<String> variantListForPattern = new ArrayList<String>();
				variantListForPattern.add(variant.getKey());
				andPatternWithVariantList.put(traceWithAndChildren, variantListForPattern);
				Integer patternFrequency =  variant.getValue();
				andPatternWithFreq.put(traceWithAndChildren, patternFrequency);
			}
			else {
				List<String> variantListForPattern = andPatternWithVariantList.get(traceWithAndChildren);
				variantListForPattern.add(variant.getKey());
				andPatternWithVariantList.put(traceWithAndChildren, variantListForPattern);
				Integer patternFrequency  = andPatternWithFreq.get(traceWithAndChildren);
				andPatternWithFreq.put(traceWithAndChildren, patternFrequency+variant.getValue());
				//andPatternWithProb.get(traceWithAndChildren);
				//andPatternWithProb.put(traceWithAndChildren, andPatternWithProb.get(traceWithAndChildren)+variantProbability);
			}
			totalPatternFrequency= totalPatternFrequency+variant.getValue();
		}
		System.out.println("The total pattern frequency is: "+totalPatternFrequency);
		for (Map.Entry<String, Integer> pattern : andPatternWithFreq.entrySet()){
			double patternProbability = (double) pattern.getValue()/(double)totalPatternFrequency;
			andPatternWithProb.put(pattern.getKey(), patternProbability);
			System.out.println("The pattern: "+ pattern.getKey()+ " has prob"+ patternProbability);
		}
		
		//find the minimum and max probability of patterns in "and" node
		double maxProbability = 0.0;
		double minProbability = 1.0; 
		for(Map.Entry<String, Double> pattern: andPatternWithProb.entrySet()) {
			if(maxProbability<pattern.getValue()) {
				maxProbability = pattern.getValue();
			}
			if(minProbability>pattern.getValue()) {
				minProbability = pattern.getValue();
			}
		}
		System.out.println("For and node: " +andNode.getName());
		System.out.println("Max prob: " +maxProbability);
		System.out.println("Min prob: " +minProbability);
		if(maxProbability == minProbability && andPatternWithProb.size()!=1 ) {
			System.out.println("The 'and' node patterns' min and max prob = "+maxProbability);
			return null;
		}
		else {
			double adjustedThreshold = parameters.getThreshold()*(maxProbability-minProbability);
			//double adjustedThreshold = parameters.getThreshold();//*(maxProbability-minProbability);
			//for all patterns create a sublog
			XLog subLog = getXLog(InputLog);
			System.out.println("The adjusted threshold is: "+adjustedThreshold);
			
			for (Map.Entry<String, Double> pattern : andPatternWithProb.entrySet()){
				if(pattern.getValue()>= adjustedThreshold) {
					subLog = addTracesToXLog(subLog, pattern.getKey(), parameters.getCharCounter(), parameters.getCharacterEventMap());
					andPatternToKeep.put(pattern.getKey(), pattern.getValue());
					for(String variantsFromPatternToKeep :andPatternWithVariantList.get(pattern.getKey())) {
						variantListToKeep.put(variantsFromPatternToKeep, 0);//add all the variants that need to be kept
					}
				}
				else {
					//get list of all variants that have to be excluded while checking similar patterns 
					for(String variantsFromPatternToDelete :andPatternWithVariantList.get(pattern.getKey())) {
						variantListToDelete.put(variantsFromPatternToDelete, 0);//add all the variants that need to be deleted into this ds so that no other variants are mapped to these variants as their similar variants
					}
				}
			}
			System.out.println("The count of variant to keep is: "+variantListToKeep.size());
			for(Map.Entry<String, Integer> variantToKeep : variantListToKeep.entrySet()) {
				System.out.println("The variant: "+ variantToKeep.getKey()+" is kept");
			}
			System.out.println("The count of variant to delete is: "+variantListToDelete.size());
			/*for(Map.Entry<String, Integer> variantToDelete : variantListToDelete.entrySet()) {
				System.out.println("The variant: "+ variantToDelete.getKey()+" is removed");
			}*/
			
			
			for(Map.Entry<String, Integer> entry : variantListToDelete.entrySet()) {
				String variantToDelete = entry.getKey();
				//the variant/pattern must be removed from the ds because the pattern that it belongs to has lower probability
				if(modificationTypeIsConvert) {
					//Get most similar variant from "and" node
					String similarVariant = findMostSimilarVariant(variantToDelete, variantListToKeep, variantListToDelete);		
					
					//variantToBeDeleted must be converted to similarVariant in list of variants available in log
					parameters = updateFrequencyInDSToConvertVariant(parameters, variantToDelete, similarVariant);
				}
				else {
					parameters = updateFrequencyInDSToRemoveVariant(parameters, variantToDelete);
				}
			}
				
			System.out.println("End of variant update for node "+andNode.getName());
			ProcessTree processTree = mineProcessTree(subLog, null);
			System.out.println("The sub process tree generated by and node deletion "+andNode.getName());
			System.out.println(processTree.toString());
			return processTree;
		}
	}
	public static XLog getXLog(XLog inputLog) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog newLog = factory.createLog();
		newLog.setAttributes(inputLog.getAttributes());
		return newLog;
	}
	
	public static ProcessTree getEmptyProcessTree(XLog inputLog) {
		XLog emptyLog = getXLog(inputLog);
		return mineProcessTree(emptyLog, null);
	}

	public static List<String> getAndPatternsFromVariant(String variantWithOnlyAndChildren) {
		List<String> patterns = new ArrayList<String>();
		String pattern = "";
		for(char c: variantWithOnlyAndChildren.toCharArray()) {
			String stringC = Character.toString(c);
			if(!pattern.contains(stringC)) {
				patterns.add(pattern);
				pattern = "";
			}
			pattern = pattern + stringC;
		}
		return patterns;
	}	
	public static ProcessTree createProcessTreeWithRemainingVariants(ProcessTree processTree, XAttributeMap inputLogAttribute, Map<String, Integer> remainingVariants, Map<String, String> characterEventMap, Integer charCounter) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();                                                           
		XLog subLog = factory.createLog();                                                                                         
		subLog.setAttributes(inputLogAttribute);                                                                            
		                                                                                                                           
		for (Map.Entry<String, Integer> pattern : remainingVariants.entrySet()){                                                   
			subLog = addTracesToXLog(subLog, pattern.getKey(), charCounter, characterEventMap);                                      
		}                                          
		                            
		return mineProcessTree(subLog, null);
	}
	
	public static ProcessTree mineProcessTree(XLog subLog, XEventClassifier xclassifier) {
		if (xclassifier == null) {
			xclassifier = XLogInfoImpl.NAME_CLASSIFIER;
		}
		MiningParametersIMf miningParameters = new MiningParametersIMf();                 
		miningParameters.setNoiseThreshold(0);                                            
		miningParameters.setClassifier(xclassifier);                     
		ProcessTree processTree = IMProcessTree.mineProcessTree(subLog, miningParameters);
		return processTree;
	}
	
	public static XLog addTracesToXLog(XLog subLog, String traceInChar, Integer charCounter, Map<String, String> characterEventMap) {
			String case_id = String.valueOf(charCounter);
			XAttributeMapImpl traceAttributes = new XAttributeMapImpl();
			traceAttributes.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
			XTraceImpl trace = new XTraceImpl(traceAttributes);
			for(char c: traceInChar.toCharArray()) {
				XAttributeMapImpl eventAttributes = new XAttributeMapImpl();
				String activity = Character.toString(c);
				eventAttributes.put("concept:name", new XAttributeLiteralImpl("concept:name",characterEventMap.get(activity)));
				XEvent event = new XEventImpl(eventAttributes);
				trace.add(event);
			}
			subLog.add(trace);
		
		return subLog;
	}
	
	//update the children node with parent frequency from top to bottom whenever necessary
	public static Map<UUID, NodeDetailStore> updateChildrenFrequency(NodeDetailStore parentNDS, Map<UUID, NodeDetailStore> treeNodeFreq) {	
		if(parentNDS.getName().contains("loop")) {
			//In case parent is loop then all the three children must be updated if unsure
			//if third child is unsure then update third child frequency and its children then  
			//if first child is sure then check if second child is unsure then update second child if second child is sure do not update
			//if first child unsure and second child is sure then update first child
			//if first child is unsure and second child also unsure then go for pattern thingy for first child and 
			//update second child based on first and third child instead of getting pattern for second as well
			List<Node> children = parentNDS.getChildren();
			Node firstChild = children.get(0);
			Node secondChild = children.get(1);
			Node thirdChild = children.get(2);
			NodeDetailStore firstChildNDS = treeNodeFreq.get(firstChild.getID());
			NodeDetailStore secondChildNDS = treeNodeFreq.get(secondChild.getID());
			NodeDetailStore thirdChildNDS = treeNodeFreq.get(thirdChild.getID());
			//Update the third child frequency based on parent if not updated already
			if(!thirdChildNDS.getSuretyOfActivityFrequency()) {
				thirdChildNDS.setFrequency(parentNDS.getFrequency());
				thirdChildNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseActivityFrequency());
				thirdChildNDS.setSuretyOfActivityFrequency(true);
				treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
				System.out.println("Modified loop third child:"+thirdChildNDS.getName()+" with its parent frequency:"+parentNDS.getFrequency());
				/*for (Map.Entry<String, Integer> entry : thirdChildNDS.getVariantWiseActivityFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}*/
				if(!thirdChild.isLeaf()) {
					treeNodeFreq = updateChildrenFrequency(thirdChildNDS, treeNodeFreq);
				}
			}
			//if first child is sure and second child is not then update second child using first and third
			if(firstChildNDS.getSuretyOfActivityFrequency() && !secondChildNDS.getSuretyOfActivityFrequency() ) {
				Integer firstChildFrequency = firstChildNDS.getFrequency();
				Integer secondChildFrequency = firstChildFrequency - parentNDS.getFrequency();
				secondChildNDS.setFrequency(secondChildFrequency);
				Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(firstChildNDS.getVariantWiseActivityFrequency(), parentNDS.getVariantWiseActivityFrequency());
				secondChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
				secondChildNDS.setSuretyOfActivityFrequency(true);
				treeNodeFreq.put(secondChild.getID(),secondChildNDS);
				System.out.println("Modified loop second child:"+secondChildNDS.getName()+" with frequency:"+secondChildFrequency+" based on first child and third child");						
				/*System.out.println("---VAF:");
				for (Map.Entry<String, Integer> entry : secondChildNDS.getVariantWiseActivityFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}
				System.out.println("---VCF:");
				for (Map.Entry<String, Integer> entry : secondChildNDS.getVariantWiseCaseFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}*/
				if(!secondChild.isLeaf()) {
					treeNodeFreq = updateChildrenFrequency(secondChildNDS, treeNodeFreq);
				}
			}
			//if second child is sure and first child is not then update first child with second and third
			else if (!firstChildNDS.getSuretyOfActivityFrequency() && secondChildNDS.getSuretyOfActivityFrequency()) {
				Integer secondChildFrequency = secondChildNDS.getFrequency();
				Integer firstChildFrequency = secondChildFrequency + parentNDS.getFrequency();
				firstChildNDS.setFrequency(firstChildFrequency);
				Map<String, Integer> variantWiseActivityFrequency = aggregateVariantFrequency(secondChildNDS.getVariantWiseActivityFrequency(), parentNDS.getVariantWiseActivityFrequency());
				firstChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
				firstChildNDS.setSuretyOfActivityFrequency(true);
				treeNodeFreq.put(firstChild.getID(),firstChildNDS);
				System.out.println("Modified loop first child:"+firstChildNDS.getName()+" with frequency:"+firstChildFrequency+" based on first child and third child");	
				/*for (Map.Entry<String, Integer> entry : firstChildNDS.getVariantWiseActivityFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}*/
				if(!firstChild.isLeaf()) {
					treeNodeFreq = updateChildrenFrequency(firstChildNDS, treeNodeFreq);
				}
			}
			//if both first and second child is unsure then do a force update at end. until then wait for top down update
			else if (!firstChildNDS.getSuretyOfActivityFrequency() && !secondChildNDS.getSuretyOfActivityFrequency()){
				System.out.println("Wait for top down force-update because first and second child both unsure");
			}
			else {
				System.out.println("Not updating loop children because surety is already set");
			}
			
		}//end of if loop
		// if parent is seq/and then all children get the same parent frequency
		else if(parentNDS.getName().contains("seq")||parentNDS.getName().contains("and")) {
			List<Node> children = parentNDS.getChildren();
			for(Node child : children){
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				//if child  is not an activity and it's child is also not activity or child is xor,  then go ahead and change child frequency
				if((childNDS.getType() != "activity" &&  !childNDS.getSuretyOfActivityFrequency())){ 
					System.out.println("The child type is:"+childNDS.getType());
					System.out.println("The child's child is activity:"+childNDS.getChildIsActivity());
					System.out.println("The child's name is :"+childNDS.getName());
					if(isFrequencyChanging(parentNDS.getFrequency(), childNDS)) {
						childNDS.setFrequency(parentNDS.getFrequency());
						childNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseActivityFrequency());
						childNDS.setSuretyOfActivityFrequency(true);
						System.out.println("Modified child :"+childNDS.getName()+" of "+parentNDS.getName()+" with parent(seq/and) frequency:"+parentNDS.getFrequency());
						/*for (Map.Entry<String, Integer> entry : childNDS.getVariantWiseActivityFrequency().entrySet()) {
							System.out.println(entry.getKey() +" => "+entry.getValue());
						}*/
						treeNodeFreq.put(child.getID(), childNDS);
						treeNodeFreq = updateChildrenFrequency(childNDS, treeNodeFreq);
					}	
					else {
						System.out.println("The frequency of child:"+childNDS.getName() +" doesn't change, hence not updating");
					}
				}
				else {
					if(childNDS.getSuretyOfActivityFrequency() && !(parentNDS.getFrequency().equals(childNDS.getFrequency()))) {
						System.out.println("Error Occured: The child of seq/and has frequency which is not same as parent and its surety is already set to true."
								+ "The frequency of child:"+childNDS.getName()+" is "+childNDS.getFrequency()+" but the parent:"+ parentNDS.getName()+" frequency is "+ parentNDS.getFrequency());
					}
					else {
						System.out.println("Child: "+childNDS.getName()+" of "+parentNDS.getName()+" is an activity node or child's child is an activity or the surety is already true, "
								+ "hence not changing the frequency");
					}
				}
				
				//if child frequency is not less then parent node then it means its an inconsistent node and  incorrect tree
				//because the child node must always have lesser frequency while coming top to bottom as 
				//top frequency is calculated based on bottom(leaf nodes) in first search while calling getfrequency 
				if(parentNDS.getFrequency()<childNDS.getFrequency()) {
					System.out.println("----Error: The parent:"+parentNDS.getName()+" has frequency:"+parentNDS.getFrequency()+" lesser than child:"+child+" frequency:"+childNDS.getFrequency()+" which is not acceptable");
					System.out.println("----Check this at the end of frequency if its resolved");
				}
			}
		}//end of if seq or and
		// if parent is xor then children 
		else if(parentNDS.getName().contains("xor")) {
			//only if tau then update the node frequency with [parent - all other children frequency] else keep it as is
			//but this is a problem in case xor child is xor1 and tau, and xor1 is also having a tau so you skip cascading frequency below
			//to subtree of xor1 and xor1 value might not be stable and hence tau value might become incorrect 
			//but xor frequency will be correct as it is cascaded from top
			
			//TODO new: if xor children's surety for more than one child is unsure then we cannot calculate the frequency of children //ASK??
			//If only one child has unsure freq we can calculate
			List<Node> children = parentNDS.getChildren();
			Integer childrenFrequency = 0;
			Map<String, Integer> childrenVariantWiseActivityFrequency = new LinkedHashMap<String, Integer>();
			List<NodeDetailStore> uncertainNodes = new ArrayList<NodeDetailStore>();
			for(Node xorChild : children){
				NodeDetailStore xorChildNDS = treeNodeFreq.get(xorChild.getID());
				childrenFrequency += xorChildNDS.getFrequency();
				childrenVariantWiseActivityFrequency = aggregateVariantFrequency(childrenVariantWiseActivityFrequency, xorChildNDS.getVariantWiseActivityFrequency());
				if(!xorChildNDS.getSuretyOfActivityFrequency()) {
					uncertainNodes.add(xorChildNDS);
				}
			}
			System.out.println("The total children frequency of xor is :"+childrenFrequency+" and parent frequency is"+parentNDS.getFrequency());
			//The number of children whose surety is not known is more than one hence error occurred
			if(uncertainNodes.size()>1) {
				System.out.println("Wait for top down force-update. Cannot calculate the frequency of xor children as we have multiple unknown children.");
			}
			else if(uncertainNodes.size()==0) {
				System.out.println("All children node have surety");
			}
			else {
				//Only one child has unknown frequency than calculate it based on parent and children frequency
				if(parentNDS.getSuretyOfActivityFrequency()) {
					NodeDetailStore xorChildNDS = uncertainNodes.get(0);
					Integer xorChildFrequency = parentNDS.getFrequency() - (childrenFrequency - xorChildNDS.getFrequency()); //Here xorChildNDS.getFrequency() is subtracted because
					//in case there was some value assigned to it but the surety is not right  then childrenFrequency variable will include that error value too so remove it
					xorChildNDS.setFrequency(xorChildFrequency);
					Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(childrenVariantWiseActivityFrequency, xorChildNDS.getVariantWiseActivityFrequency());
					variantWiseActivityFrequency = subtractVariantFrequency(parentNDS.getVariantWiseActivityFrequency(), variantWiseActivityFrequency);
					xorChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
					xorChildNDS.setSuretyOfActivityFrequency(true);
					System.out.println("Modified xor's child :"+xorChildNDS.getName()+" with frequency:"+xorChildFrequency);
					/*for (Map.Entry<String, Integer> entry : xorChildNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
					treeNodeFreq.put(xorChildNDS.getNode().getID(), xorChildNDS);
					//If child is not leaf update its children
					if(!xorChildNDS.getNode().isLeaf()) {
						treeNodeFreq = updateChildrenFrequency(xorChildNDS, treeNodeFreq);
					}
				}
				else{
					//This condition must not occur because update on xor is called once xor node is updated to sure
					System.out.println("Error Occured: The xor frequency is unsure hence that one unsure child cannot be calculated");
				}
			}
		}//end of if xor
		else if(parentNDS.getName().contains("or")) {
					System.out.println("OR children must not be updated");
		}//end of if or
		return treeNodeFreq;
	}
	public static Map<UUID, NodeDetailStore> checkChildParentSync(NodeDetailStore childNDS,  Map<UUID, NodeDetailStore> treeNodeFreq) {
		UUID childID = childNDS.getNode().getID();
		UUID parentID = childNDS.getParent();
		String childName = childNDS.getName();
		int childFrequency = childNDS.getFrequency();
		NodeDetailStore parentNDS = treeNodeFreq.get(parentID);
		String parentName = parentNDS.getName();
		boolean childChildIsActivity = childNDS.getChildIsActivity();
		int parentFrequency = parentNDS.getFrequency();
			if(parentName.contains("seq") || parentName.contains("and")) { 
				//if child frequency which is updated recently, is less than parent frequency 
				//then update child frequency with parent frequency and update child's children
				//also if child has activity as child then do not update child -- only in case of xor update child(xor) anyways
				if(childFrequency<parentFrequency && (!(childName.contains("xor") && !childChildIsActivity ) || (childName.contains("xor")))) {
					//the below has to be done when child is xor and this function is being called from setTaufrequency. 
					//Because xor frequency is not set already before calling
					if(!childNDS.getSuretyOfActivityFrequency()) {
						childNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseActivityFrequency());
						childNDS.setFrequency(parentFrequency);
						childNDS.setSuretyOfActivityFrequency(parentNDS.getSuretyOfActivityFrequency());
						treeNodeFreq.put(childID, childNDS);
						System.out.println("Modified seq/and child :"+childNDS.getName()+" with  parent frequency:"+parentFrequency);
						/*for (Map.Entry<String, Integer> entry : childNDS.getVariantWiseActivityFrequency().entrySet()) {
							System.out.println(entry.getKey() +" => "+entry.getValue());
						}*/
						treeNodeFreq = updateChildrenFrequency(childNDS, treeNodeFreq);
					}
				}
				//if child frequency which is updated recently is more than parent frequency and its surety is true only then propogate to parent
				//then update parent frequency with child frequency and update all seq/and children with its child frequency
				else if (childFrequency>parentFrequency && childNDS.getSuretyOfActivityFrequency()) {
					parentNDS.setFrequency(childFrequency);
					parentNDS.setVariantWiseActivityFrequency(childNDS.getVariantWiseActivityFrequency());
					parentNDS.setSuretyOfActivityFrequency(true);
					treeNodeFreq.put(parentID, parentNDS);
					System.out.println("Modified parent- seq/and:"+parentNDS.getName()+" with child frequency:"+childFrequency);
					/*for (Map.Entry<String, Integer> entry : parentNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
					//Now again check if this parent frequency and its parent node frequency is in sync. 
					//Continue the this check, at one point this parent frequency will be greater than its child  frequency hence wont come in this else block
					//or it might have reached the root node
					if(!parentNDS.getNode().isRoot()) {
						treeNodeFreq = updateChildrenFrequency(parentNDS, treeNodeFreq);
						treeNodeFreq = checkChildParentSync(parentNDS, treeNodeFreq);
					}
					else {
						treeNodeFreq = updateChildrenFrequency(parentNDS, treeNodeFreq);
					}
				}else if (childChildIsActivity ){
					System.out.println("----The child:"+childNDS.getName()+" has activity in its children hence not updating the frequency as per its parent");
				}
				else if(!childNDS.getSuretyOfActivityFrequency()) {
					System.out.println("----The child:"+childNDS.getName()+"  has no surety hence not updating its parent with child frequency");
				}
			//TODO: Check if loop as a child must get updated or not?
			}else if(parentName.contains("loop")) { 
				//Check  if any child was updated. then calculate the value of third child again and its freq must be propagated to its parent-loop 
				List <Node> children = parentNDS.getChildren();
				Node firstChild = children.get(0);
				Node secondChild = children.get(1);
				Node thirdChild = children.get(2);
				if(childID == thirdChild.getID()){
					//It means that child's(third child) frequency must be set to parent if the frequency is not equal
					if(childFrequency!= parentNDS.getFrequency() && childNDS.getSuretyOfActivityFrequency()) {
						parentNDS.setFrequency(childFrequency);
						parentNDS.setVariantWiseActivityFrequency(childNDS.getVariantWiseActivityFrequency());
						parentNDS.setSuretyOfActivityFrequency(childNDS.getSuretyOfActivityFrequency());//if third child freq is sure than parent freq is also sure
						treeNodeFreq.put(parentID, parentNDS);
						System.out.println("Modified parent- loop:"+parentNDS.getName()+" with third child frequency:"+childFrequency);
						/*for (Map.Entry<String, Integer> entry : parentNDS.getVariantWiseActivityFrequency().entrySet()) {
							System.out.println(entry.getKey() +" => "+entry.getValue());
						}*/
						//Now again check if this parent frequency and its parent node frequency is in sync.
						//or it might have reached the root node
						//We dont need to update the children of loop again , coz based on all three child, parent freq was updated
						if(!parentNDS.getNode().isRoot()) {
							treeNodeFreq = checkChildParentSync(parentNDS, treeNodeFreq);
						}
					}
					else {
						System.out.println("Not Modifying the parent- loop:"+parentNDS.getName()+" because the new freq of third child is already same as parent freq");
					}
				}
				else {
					//if the updated child was second or first child(also first child and second child is not zero) then the third child frequency must 
					//be calculated again only if third child is not activity  and also parent must be updated if third child is changed
					NodeDetailStore firstChildNDS = treeNodeFreq.get(firstChild.getID());
					NodeDetailStore secondChildNDS = treeNodeFreq.get(secondChild.getID());
					NodeDetailStore thirdChildNDS = treeNodeFreq.get(thirdChild.getID());
					
					//if first child is sure and second child is sure then update third child
					if( firstChildNDS.getSuretyOfActivityFrequency() && secondChildNDS.getSuretyOfActivityFrequency()) {
						Integer thirdChildNewFrequency = firstChildNDS.getFrequency() - secondChildNDS.getFrequency();
						//Third child is not sure then update using first n second child surety
						if(!thirdChildNDS.getSuretyOfActivityFrequency()) {
							//Update the third child 
							thirdChildNDS.setFrequency(thirdChildNewFrequency);
							Map<String, Integer> variantWiseActivityFrequency = subtractVariantFrequency(firstChildNDS.getVariantWiseActivityFrequency(), secondChildNDS.getVariantWiseActivityFrequency());
							thirdChildNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
							thirdChildNDS.setSuretyOfActivityFrequency(true);
							treeNodeFreq.put(thirdChild.getID(), thirdChildNDS);
							System.out.println("Modified loops third child :"+childNDS.getName()+" with frequency:"+thirdChildNewFrequency);
							/*for (Map.Entry<String, Integer> entry : childNDS.getVariantWiseActivityFrequency().entrySet()) {
								System.out.println(entry.getKey() +" => "+entry.getValue());
							}*/
							//Also after updating third child its children must be updated if its not tau.
							if(!thirdChildNDS.getType().contains("tau")) {
								//there are children to third child so update them too
								treeNodeFreq = updateChildrenFrequency(thirdChildNDS, treeNodeFreq);
							}
							
							//Update parent because third child has changed 
							//before updating parent check if parent frequency will change only then update 
							if(thirdChildNewFrequency!= parentNDS.getFrequency() && !parentNDS.getSuretyOfActivityFrequency()) {
								parentNDS.setFrequency(thirdChildNewFrequency);
								parentNDS.setVariantWiseActivityFrequency(variantWiseActivityFrequency);
								parentNDS.setSuretyOfActivityFrequency(firstChildNDS.getSuretyOfActivityFrequency()&& secondChildNDS.getSuretyOfActivityFrequency());
								treeNodeFreq.put(parentID, parentNDS);
								System.out.println("Modified parent- loop:"+parentNDS.getName()+" with third child frequency:"+thirdChildNewFrequency);
								/*for (Map.Entry<String, Integer> entry : parentNDS.getVariantWiseActivityFrequency().entrySet()) {
									System.out.println(entry.getKey() +" => "+entry.getValue());
								}*/
								//Now again check if this parent frequency and its parent node frequency is in sync. 
								//or it might have reached the root node
								//We dont need to update the children of loop again , coz based on all three child, parent freq was updated
								if(!parentNDS.getNode().isRoot()) {
									treeNodeFreq = checkChildParentSync(parentNDS, treeNodeFreq);
								}
							}
							else {
								System.out.println("Not Modifying the parent- loop:"+parentNDS.getName()+" because the new freq of third child doesnt change");
							}
							
						}
						else {
							System.out.println("Third child: "+thirdChildNDS.getName()+" is already sure with frequency:"+thirdChildNDS.getFrequency());
							if(thirdChildNDS.getFrequency()!=thirdChildNewFrequency) {
								System.out.println("Error occured: third child frequency is not in sync with first  and second child of loop");
							}
						}
					}
					else {
						System.out.println("First child or second child of loop is unsure hence no use of updating third child and no need to update parent loop frequency");
					}
				}
			}
			//TODO: Check if loop as a child must get updated or not?
			else if(parentName.contains("xor")) {
				int childrenFrequency = 0;
				Map<String, Integer> childrenVariantWiseActivityFrequency = new LinkedHashMap<String, Integer>();
				boolean suretyOfChildren = false;
				for(Node neighbor : treeNodeFreq.get(parentID).getChildren()){
					childrenFrequency += treeNodeFreq.get(neighbor.getID()).getFrequency();
					childrenVariantWiseActivityFrequency = aggregateVariantFrequency(childrenVariantWiseActivityFrequency, treeNodeFreq.get(neighbor.getID()).getVariantWiseActivityFrequency());
					suretyOfChildren = suretyOfChildren && treeNodeFreq.get(neighbor.getID()).getSuretyOfActivityFrequency();
				}
				System.out.println("------The children frequency when calculating sync of xor:"+parentNDS.getName()+" as parent  is :"+childrenFrequency);
				//if parent frequency is lesser than all of its children then assign sum of its children frequency to parent 
				if(childrenFrequency>parentFrequency) {
					parentNDS.setFrequency(childrenFrequency);
					parentNDS.setVariantWiseActivityFrequency(childrenVariantWiseActivityFrequency);
					parentNDS.setSuretyOfActivityFrequency(suretyOfChildren);
					treeNodeFreq.put(parentID, parentNDS);
					System.out.println("Modified parent(xor) :"+parentNDS.getName()+" with child  frequency:"+childrenFrequency);
					/*for (Map.Entry<String, Integer> entry : parentNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
					//Now again check if this parent frequency and its parent node frequency is in sync. 
					//Continue the this check, at one point this parent frequency will be greater than its child frequency hence wont come in this else block
					//or it might have reached the root node
					if((!parentNDS.getNode().isRoot()) && parentNDS.getSuretyOfActivityFrequency()) {
						treeNodeFreq = checkChildParentSync(parentNDS, treeNodeFreq);
						//no need to update children because the parent frequency was updated based on all children
					}
				}
				//if parent frequency is lesser than all of its children then, parent frequency must be propogated to its children 
				else if(childrenFrequency<parentFrequency) {
					//then simply call an updatechildrenfrquency for xor because children sum must be equal to parent 
					//so this child's frequency must be changed as per rule in updateChildren
					treeNodeFreq = updateChildrenFrequency(parentNDS, treeNodeFreq);
				}
			}
			//TODO: Check if loop as a child must get updated or not?
			else if(parentName.contains("or")) {
				if(childFrequency>parentFrequency) {
					parentNDS.setFrequency(childFrequency);
					parentNDS.setVariantWiseActivityFrequency(childNDS.getVariantWiseActivityFrequency());
					treeNodeFreq.put(parentID, parentNDS);
					System.out.println("Modified parent-or:"+parentNDS.getName()+" with child frequency:"+childFrequency);
					for (Map.Entry<String, Integer> entry : parentNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}
					if(!parentNDS.getNode().isRoot()) {
						treeNodeFreq = checkChildParentSync(parentNDS,treeNodeFreq);
					}
				}
			}
			return treeNodeFreq; 
	}
	
	public static  Map<UUID, NodeDetailStore> forceUpdateChildrenFrequency(NodeDetailStore parentNDS, Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters ) {	
		if(parentNDS.getName().contains("loop")) {
			//In case parent is loop then all the three children must be updated if unsure
			//if third child is unsure then update third child frequency and its children with parent frequency
			//if first child is sure then check if second child is unsure then update second child if second child is sure do not update
			//if first child unsure and second child is sure then update first child
			//if first child is unsure and second child also unsure then take the case frequency of the second and assign it as activity frequency to it.and update its children if not a leaf 
			//update second child based on first and third child instead of getting pattern for second as well
			List<Node> children = parentNDS.getChildren();
			Node firstChild = children.get(0);
			Node secondChild = children.get(1);
			Node thirdChild = children.get(2);
			NodeDetailStore firstChildNDS = treeNodeFreq.get(firstChild.getID());
			NodeDetailStore secondChildNDS = treeNodeFreq.get(secondChild.getID());
			NodeDetailStore thirdChildNDS = treeNodeFreq.get(thirdChild.getID());
			//Update the third child frequency based on parent if not updated already
			if(!thirdChildNDS.getSuretyOfActivityFrequency()) {
				thirdChildNDS.setFrequency(parentNDS.getFrequency());
				thirdChildNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseActivityFrequency());
				thirdChildNDS.setSuretyOfActivityFrequency(true);
				treeNodeFreq.put(thirdChild.getID(),thirdChildNDS);
				System.out.println("Modified loop third child:"+thirdChildNDS.getName()+" with its parent frequency:"+parentNDS.getFrequency());	
				/*for (Map.Entry<String, Integer> entry : thirdChildNDS.getVariantWiseActivityFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}*/
			}
			else {
				//TODO: check if such condition can occur
				System.out.println("Third child:"+thirdChildNDS.getName()+" was already updated");
			}
			
			//TODO: Check first case possible??
			//if first child is sure  its an error. because this condition is handled in update
			if(firstChildNDS.getSuretyOfActivityFrequency()) {
				System.out.println("First child:"+ firstChildNDS.getName()+" was already updated");
			}
			//if both first and second child is unsure then use case frequency of middle child 
			else if (!firstChildNDS.getSuretyOfActivityFrequency() && !secondChildNDS.getSuretyOfActivityFrequency()){
				//get case frequency of second child and assign to its activity frequency
				Integer secondChildCaseFrequency = getCaseFrequency(secondChildNDS, treeNodeFreq, parameters);
				secondChildNDS.setFrequency(secondChildCaseFrequency);
				Map<String, Integer> variantWiseCaseFrequency = getVariantWiseCaseFrequency(secondChildNDS, treeNodeFreq, parameters);
				secondChildNDS.setVariantWiseActivityFrequency(variantWiseCaseFrequency);
				secondChildNDS.setVariantWiseCaseFrequency(variantWiseCaseFrequency);
				secondChildNDS.setSuretyOfActivityFrequency(true);
				secondChildNDS.setSuretyOfCaseFrequency(true);
				treeNodeFreq.put(secondChild.getID(),secondChildNDS);
				System.out.println("Modified loop second child:"+secondChildNDS.getName()+" with frequency:"+secondChildCaseFrequency+" using its case frequency"); 
				/*System.out.println("---VAF:");
				for (Map.Entry<String, Integer> entry : secondChildNDS.getVariantWiseActivityFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}
				System.out.println("---VCF:");
				for (Map.Entry<String, Integer> entry : secondChildNDS.getVariantWiseCaseFrequency().entrySet()) {
					System.out.println(entry.getKey() +" => "+entry.getValue());
				}*/	
				Integer firstChildFrequency = secondChildNDS.getFrequency()+thirdChildNDS.getFrequency();
				Map<String, Integer> firstChildVariantWiseActivityFrequency = aggregateVariantFrequency(secondChildNDS.getVariantWiseActivityFrequency(), thirdChildNDS.getVariantWiseActivityFrequency());
				//this firstChildFrequency must be greater than or equal to caseFrequency of firstchild 
				//This is only for testing purpose
				Integer firstChildCaseFrequency = getCaseFrequency(firstChildNDS, treeNodeFreq, parameters);
				if(firstChildCaseFrequency>firstChildFrequency) {
					System.out.println("Error occured: First child: "+ firstChildNDS.getName()+" activity frequency never be less than the case frequency");
				}
				else {
					firstChildNDS.setFrequency(firstChildFrequency);
					firstChildNDS.setVariantWiseActivityFrequency(firstChildVariantWiseActivityFrequency);
					firstChildNDS.setSuretyOfActivityFrequency(true);
					treeNodeFreq.put(firstChild.getID(),firstChildNDS);
					System.out.println("Modified loop first child:"+firstChildNDS.getName()+" with frequency:"+firstChildFrequency+" using third child frequency and second childs case freq");
					/*for (Map.Entry<String, Integer> entry : firstChildNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
				}
			}
			else {
				System.out.println("Not updating loop children because surety is already set");
			}
			//force updating all loop children's children event if surety is set 
			for(Node loopChild : children){
				NodeDetailStore loopChildNDS = treeNodeFreq.get(loopChild.getID());
				//If child is not leaf update its children
				if(!loopChild.isLeaf()) {
					treeNodeFreq = forceUpdateChildrenFrequency(loopChildNDS, treeNodeFreq, parameters);
				}
			}
		}//end of if loop
		// if parent is seq/and then all children get the same parent frequency
		else if(parentNDS.getName().contains("seq")||parentNDS.getName().contains("and")) {
			List<Node> children = parentNDS.getChildren();
			for(Node child : children){
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				//if child  is not an activity and it's child is also not activity or child is xor,  then go ahead and change child frequency
				if(!childNDS.getSuretyOfActivityFrequency()) { 
					childNDS.setFrequency(parentNDS.getFrequency());
					childNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseActivityFrequency());
					childNDS.setSuretyOfActivityFrequency(true);
					System.out.println("Modified child of seq/and :"+childNDS.getName()+" with parent(seq/and) frequency:"+childNDS.getFrequency());
					/*for (Map.Entry<String, Integer> entry : childNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
					treeNodeFreq.put(child.getID(), childNDS);
				}	
				else {
					//Testing purpose
					if(childNDS.getSuretyOfActivityFrequency() && !parentNDS.getFrequency().equals(childNDS.getFrequency())) {
						System.out.println("Error Occured: The child: :"+ childNDS.getName()+" of seq/and has frequency which is not same as parent and its surety is already set to true");
					}
					else {
						System.out.println("Child:"+childNDS.getName()+" frequency is sure and is in sync with parent seq/and");
					}
				}
				//force update all seq children even if its surety is true
				if(!child.isLeaf()) {
					treeNodeFreq = forceUpdateChildrenFrequency(childNDS, treeNodeFreq, parameters);
				}
			}
		}//end of if seq or and
		// if parent is xor then children 
		else if(parentNDS.getName().contains("xor")) {
			//if more than one uncertain child nodes available than keep child nodes with their least case frequency 
			//and assign the remaining value to tau node if available
			
			List<Node> children = parentNDS.getChildren();
			Integer childrenFrequency = 0;
			Map<String, Integer> childrenVariantWiseActivityFrequency = new LinkedHashMap<String, Integer>();
			List<NodeDetailStore> uncertainNodes = new ArrayList<NodeDetailStore>();
			for(Node xorChild : children){
				NodeDetailStore xorChildNDS = treeNodeFreq.get(xorChild.getID());
				childrenFrequency += xorChildNDS.getFrequency();
				childrenVariantWiseActivityFrequency = aggregateVariantFrequency(childrenVariantWiseActivityFrequency, xorChildNDS.getVariantWiseActivityFrequency());			
				if(!xorChildNDS.getSuretyOfActivityFrequency()) {
					uncertainNodes.add(xorChildNDS);
				}
			}
			System.out.println("The total children frequency of xor is:"+childrenFrequency+" and xor frequency is: "+parentNDS.getFrequency());
			
			if(uncertainNodes.size()>1) {
				//The number of children whose surety is not known is more than one then assign the case frequency as activity frequency of that child
				System.out.println("The xor child is unsure and is not tau hence using case frequency to update them");
				Integer childrenNewFrequency = 0;
				Map<String, Integer> childrenNewVariantWiseCaseFrequency = new LinkedHashMap<String, Integer>();
				NodeDetailStore tauNDS = new NodeDetailStore();
				for(NodeDetailStore xorChildNDS : uncertainNodes){
					if(xorChildNDS.getType() != "tau") {
						Integer caseFrequency = getCaseFrequency(xorChildNDS, treeNodeFreq, parameters);
						xorChildNDS.setFrequency(caseFrequency);
						Map<String, Integer> xorChildVariantWiseCaseFrequency = getVariantWiseCaseFrequency(xorChildNDS, treeNodeFreq, parameters);
						xorChildNDS.setVariantWiseActivityFrequency(xorChildVariantWiseCaseFrequency);
						xorChildNDS.setSuretyOfActivityFrequency(true);
						xorChildNDS.setVariantWiseCaseFrequency(xorChildVariantWiseCaseFrequency);
						xorChildNDS.setSuretyOfCaseFrequency(true);
						treeNodeFreq.put(xorChildNDS.getNode().getID(), xorChildNDS);
						System.out.println("Modifying xor child :"+ xorChildNDS.getName() +"with case frequency: "+caseFrequency);
						/*for (Map.Entry<String, Integer> entry : xorChildNDS.getVariantWiseActivityFrequency().entrySet()) {
							System.out.println(entry.getKey() +" => "+entry.getValue());
						}
						System.out.println("VCF");
						for (Map.Entry<String, Integer> entry : xorChildNDS.getVariantWiseCaseFrequency().entrySet()) {
							System.out.println(entry.getKey() +" => "+entry.getValue());
						}*/
						childrenNewFrequency = childrenNewFrequency + caseFrequency;
						childrenNewVariantWiseCaseFrequency = aggregateVariantFrequency(childrenNewVariantWiseCaseFrequency, xorChildNDS.getVariantWiseActivityFrequency());
					}
					else {
						tauNDS = treeNodeFreq.get(xorChildNDS.getNode().getID());
						System.out.println("Child is tau: "+tauNDS.getName()+" hence not taking its case frequency");
					}
				}
				if(!tauNDS.getName().equals("")) {
					Integer tauFrequency = parentNDS.getFrequency()-childrenNewFrequency;
					tauNDS.setFrequency(tauFrequency);
					Map<String, Integer> tauVariantWiseCaseFrequency =subtractVariantFrequency(parentNDS.getVariantWiseActivityFrequency(),childrenNewVariantWiseCaseFrequency);
					tauNDS.setVariantWiseActivityFrequency(tauVariantWiseCaseFrequency);
					tauNDS.setSuretyOfActivityFrequency(true);
					treeNodeFreq.put(tauNDS.getNode().getID(), tauNDS);
					System.out.println("Modifying tau:"+ tauNDS.getName() +"frequency with: "+tauFrequency);
					/*for (Map.Entry<String, Integer> entry : tauNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
				}
			}
			else if(uncertainNodes.size()==0) {
				System.out.println("All children node have surety");
			}
			else {
				//Only one child has unknown frequency than calculate it based on parent and children frequency
				if(parentNDS.getSuretyOfActivityFrequency()) {
					NodeDetailStore xorChildNDS = uncertainNodes.get(0);
					Integer xorChildFrequency = parentNDS.getFrequency() - (childrenFrequency - xorChildNDS.getFrequency()); //Here xorChildNDS.getFrequency() is added because
					//TODO: Check if previous is correct
					//Integer xorChildFrequency = parentNDS.getFrequency() - childrenFrequency; //Here xorChildNDS.getFrequency() is removed childrenfrequency doesnt include it
					xorChildNDS.setFrequency(xorChildFrequency);
					Map<String, Integer> xorChildNDSVariantWiseFreq = subtractVariantFrequency(parentNDS.getVariantWiseActivityFrequency(), childrenVariantWiseActivityFrequency);
					xorChildNDSVariantWiseFreq = subtractVariantFrequency(xorChildNDSVariantWiseFreq, xorChildNDS.getVariantWiseActivityFrequency());
					xorChildNDS.setVariantWiseActivityFrequency(xorChildNDSVariantWiseFreq);
					xorChildNDS.setSuretyOfActivityFrequency(true);
					System.out.println("Modified xor's child :"+xorChildNDS.getName()+" with frequency:"+xorChildFrequency);
					/*for (Map.Entry<String, Integer> entry : xorChildNDS.getVariantWiseActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}*/
					treeNodeFreq.put(xorChildNDS.getNode().getID(), xorChildNDS);
				}
				else{
					//This condition must not occur because update on xor is called once xor node is updated to sure
					System.out.println("Error Occured: The xor(parent):"+parentNDS.getName()+" frequency is unsure hence that one unsure child cannot be calculated");
				}
			}
			//force update all xor children even if surety is true
			for(Node xorChild : children){
				NodeDetailStore xorChildNDS = treeNodeFreq.get(xorChild.getID());
				//If child is not leaf update its children
				if(!xorChild.isLeaf()) {
					treeNodeFreq = forceUpdateChildrenFrequency(xorChildNDS, treeNodeFreq, parameters);
				}
			}
		}//end of if xor
		else if(parentNDS.getName().contains("or")) {
					System.out.println("OR children must not be updated");
		}//end of if or
		return treeNodeFreq;
	}
	public static Map<UUID, NodeDetailStore> computeCaseFrequencyForNodes(Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters) {
		for (Map.Entry<UUID, NodeDetailStore> entry : treeNodeFreq.entrySet()) {
			NodeDetailStore nodeNDS = entry.getValue();
			if(!nodeNDS.getSuretyOfCaseFrequency()) {
				//System.out.println("Computing CF for node: "+ nodeNDS.getName());
				Integer totalCaseFrequency = 0;
				Integer totalActivityFrequency = nodeNDS.getFrequency();
				Map<String, Integer> variantWithCF = new LinkedHashMap<String, Integer>(); 
				Gson gson = new Gson();
				String jsonString = gson.toJson(nodeNDS.getVariantWiseActivityFrequency());
				java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String, Integer>>(){}.getType();
				LinkedHashMap<String, Integer> clonedMap = gson.fromJson(jsonString, type); 
				Map<String, Integer> variantWithAFCloned = clonedMap;
				for (Map.Entry<String, Integer> variantWithAF : nodeNDS.getVariantWiseActivityFrequency().entrySet()) {
					if(parameters.getUpdatedVariantFrequency().get(variantWithAF.getKey())!=null){
						if(variantWithAF.getValue()>0) {
							Integer traceCountForVariant = parameters.getUpdatedVariantFrequency().get(variantWithAF.getKey());
							variantWithCF.put(variantWithAF.getKey(), traceCountForVariant);
							//System.out.println("variant: "+variantWithAF.getKey()+" is set with freq: "+traceCountForVariant);
							totalCaseFrequency += traceCountForVariant;
						}
						else {
							//System.out.println("variant: "+variantWithAF.getKey()+" freq: "+variantWithAF.getValue());
						}
					}else if(parameters.getPlugIn().contentEquals("SimplifiedProcessTree")){
						//the variant was removed from main ds but we do not update the node variant details hence update it now
						if(variantWithCF.get(variantWithAF.getKey())!=null) {
							Integer oldVariantCF = variantWithCF.get(variantWithAF.getKey());
							totalCaseFrequency = totalCaseFrequency - oldVariantCF;
							variantWithCF.remove(variantWithAF.getKey());
						};
						Integer oldVariantAF = variantWithAF.getValue();
						totalActivityFrequency = totalActivityFrequency - oldVariantAF;
						variantWithAFCloned.remove(variantWithAF.getKey());
					}
					else {
						System.out.println("Error: The variant is available in node but not in main datastructure"); 
					}
				}
				nodeNDS.setCaseFrequency(totalCaseFrequency);
				nodeNDS.setFrequency(totalActivityFrequency);
				nodeNDS.setVariantWiseCaseFrequency(variantWithCF);
				nodeNDS.setVariantWiseActivityFrequency(variantWithAFCloned);
				nodeNDS.setSuretyOfCaseFrequency(true);
				treeNodeFreq.put(entry.getKey(),nodeNDS);
			}else {
				//System.out.println(nodeNDS.getName()+ " already has CF set");
			}
			
		}
		return treeNodeFreq;
	}
	public static Map<UUID, NodeDetailStore>  setProbability (Map<UUID, NodeDetailStore> treeNodeFreq, int totalFrequency) {
		NodeDetailStore currentNDS, parentNDS;
		for (Map.Entry<UUID, NodeDetailStore> nodeEntry : treeNodeFreq.entrySet()) {
			//System.out.println("Setting probability for node: "+ nodeEntry.getValue().getName());
			currentNDS = nodeEntry.getValue();
			int caseFrequency = currentNDS.getCaseFrequency();
			int activityFrequency = currentNDS.getFrequency();
			
			if(!currentNDS.getNode().isRoot() && currentNDS.getParent() != null) {
				parentNDS = treeNodeFreq.get(currentNDS.getParent());
				double cfProbabilityBasedOnParent = 0.0f;
				double afProbabilityBasedOnParent = 0.0f;
				int parentActivityFrequency = parentNDS.getFrequency();
				int parentCaseFrequency = parentNDS.getCaseFrequency();
				cfProbabilityBasedOnParent = (double)caseFrequency/(double)parentCaseFrequency;
				if(currentNDS.getName().contains("loop") && currentNDS.getType().contentEquals("operator")) {
					afProbabilityBasedOnParent = (double)caseFrequency/(double)parentActivityFrequency;
				}
				else{
					afProbabilityBasedOnParent = (double)activityFrequency/(double)parentActivityFrequency;
				}
				//System.out.println("CF probability for node: "+currentNDS.getName()+" is: "+ cfProbabilityBasedOnParent);
				//System.out.println("AF probability for node: "+currentNDS.getName()+" is: "+ afProbabilityBasedOnParent);
				currentNDS.setAFProbabilityBasedOnParent(afProbabilityBasedOnParent);
				currentNDS.setCFProbabilityBasedOnParent(cfProbabilityBasedOnParent);
			}else {
				//System.out.println("Manually setting probability for root node: "+currentNDS.getName()+" as 1.0" );
				if(caseFrequency <= 0) {
					System.out.println("Case frequency and activity frequecy is 0 or less hence setting probability based on parent to 0");
					currentNDS.setCFProbabilityBasedOnParent(0.0);
					currentNDS.setAFProbabilityBasedOnParent(0.0);
				}	
				else {
					currentNDS.setCFProbabilityBasedOnParent(1.0);
					currentNDS.setAFProbabilityBasedOnParent(1.0);
				}
			}
			float cfOverallProbability = 0.0f;
			float afOverallProbability = 0.0f;
			// if the frequency is 0 then automatically the probabilites must be 0
			if(totalFrequency > 0 && caseFrequency > 0) {
				cfOverallProbability = (float)caseFrequency/ (float)totalFrequency;
				afOverallProbability = (float)activityFrequency/ (float)totalFrequency;
			}
			else {
				System.out.println("Total frequency and case frequency is 0 or less hence setting overall prob to 0");
			}
			//System.out.println("cf overall probability for node is: "+ cfOverallProbability);
			//System.out.println("af overall probability for node is: "+ afOverallProbability);
			currentNDS.setCFOverallProbability(cfOverallProbability);
			currentNDS.setAFOverallProbability(afOverallProbability);
			treeNodeFreq.put(nodeEntry.getKey(), currentNDS);	
		}
		return treeNodeFreq;
	}

	public static boolean isFrequencyChanging(int newFrequency, NodeDetailStore NodeDetailStoreChild) {
		if(newFrequency!=NodeDetailStoreChild.getFrequency()){
			return true;
		}
		return false;
	}
	
	public static List<String> getLeafChildren( Map <UUID, NodeDetailStore> treeNodeFreq, NodeDetailStore nodeNDS) {
		//System.out.println("The child name:"+nodeNDS.getName()+ "with type:"+ nodeNDS.getType());
		List<String> leafChildren = new ArrayList<String>();
		if (nodeNDS.getType() == "activity") {
			//System.out.println("The child is leaf with name:"+nodeNDS.getName());
			leafChildren.add(nodeNDS.getName());
			return leafChildren;
		}	
		else if (nodeNDS.getType() == "operator"){
			//If node is operator then get its children call getLeafChildren for all children until leaf nodes are reached
			List<String> leafChildrenOfOperator = new ArrayList<String>();
			for (Node child: nodeNDS.getChildren()) {
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				//System.out.println("getting the child of node: "+ childNDS.getName());
				leafChildrenOfOperator = getLeafChildren(treeNodeFreq, childNDS);
				//System.out.println("The returned list of children for node: "+child +" are :"+leafChildrenOfOperator);
				leafChildren.addAll(leafChildrenOfOperator);
			}
			//System.out.println("The total list of children for node: "+nodeNDS.getNode() +" are :"+leafChildren);
			return leafChildren;
		}
		else {
			return leafChildren;
		}
	}
	
	public static List<NodeDetailStore> getLeafChildrenNDS( Map <UUID, NodeDetailStore> treeNodeFreq, NodeDetailStore nodeNDS) {
		//System.out.println("The child name:"+nodeNDS.getName()+ "with type:"+ nodeNDS.getType());
		List<NodeDetailStore> leafChildren = new ArrayList<NodeDetailStore>();
		if (nodeNDS.getType() == "activity") {
			//System.out.println("The child is leaf with name:"+nodeNDS.getName());
			leafChildren.add(nodeNDS);
			return leafChildren;
		}	
		else if (nodeNDS.getType() == "operator"){
			//If node is operator then get its children call getLeafChildren for all children until leaf nodes are reached
			List<NodeDetailStore> leafChildrenOfOperator = new ArrayList<NodeDetailStore>();
			for (Node child: nodeNDS.getChildren()) {
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				//System.out.println("getting the child of node: "+ childNDS.getName());
				leafChildrenOfOperator = getLeafChildrenNDS(treeNodeFreq, childNDS);
				//System.out.println("The returned list of children for node: "+child +" are :"+leafChildrenOfOperator);
				leafChildren.addAll(leafChildrenOfOperator);
			}
			//System.out.println("The total list of children for node: "+nodeNDS.getNode() +" are :"+leafChildren);
			return leafChildren;
		}
		else {
			return leafChildren;
		}
	}
	
	public static List<String> cloneList(List<String> listOfString) {
		List<String> output = new ArrayList<>();
		for (String str : listOfString) {
			output.add(str);
		}
		return output;
	}
	
	public static ProcessTree changeNodeName(ProcessTree processTree, Map<UUID, NodeDetailStore> treeNodeFreq) {
		DecimalFormat df = new DecimalFormat("#.##");   
		for(Node n: processTree.getNodes()){
			NodeDetailStore nodeDetailStore = treeNodeFreq.get(n.getID());
			double overallProb = nodeDetailStore.getCFOverallProbability();
			double cfProbBasedOnParent = nodeDetailStore.getCFProbabilityBasedOnParent();
			double afProbBasedOnParent = nodeDetailStore.getAFProbabilityBasedOnParent();
			int frequency = nodeDetailStore.getFrequency();
			String nodeName = nodeDetailStore.getName();
			String nodeActualName = nodeDetailStore.getActualName();
			//String newNodeName = nodeActualName+" - "+nodeName+" [AF="+ frequency+" CF="+nodeDetailStore.getCaseFrequency()+" OP="+df.format(overallProb)+" CPP="+df.format(cfProbBasedOnParent)+"APP="+df.format(afProbBasedOnParent)+"]";
			String newNodeName = nodeActualName+" [AF="+ frequency+" CF="+nodeDetailStore.getCaseFrequency()+" OP="+df.format(overallProb)+" CPP="+df.format(cfProbBasedOnParent)+"APP="+df.format(afProbBasedOnParent)+"]";
			processTree.getNode(n.getID()).setName(newNodeName);
		}
		return processTree;
	}
	
	public static List<NodeDetailStore>  getNodesToDeleteBasedOnProbability(Map<UUID, NodeDetailStore> treeNodeFreq, double thresholdProbability, String probabilityType, String frequencyType) {
		//Assumption: only 1 parent per node 
		List<NodeDetailStore> toBeDeletedNodes =new ArrayList<NodeDetailStore>();
		for (Map.Entry<UUID, NodeDetailStore> nodeEntry : treeNodeFreq.entrySet()) {
			NodeDetailStore currentNodeDetailStore = nodeEntry.getValue();
			double probability = 0.0;
			if(probabilityType.contentEquals("Probability based on root")) {
				if(frequencyType.contentEquals("Activity Frequency")) {
					probability = currentNodeDetailStore.getAFOverallProbability();
				}
				else {
					probability = currentNodeDetailStore.getCFOverallProbability();
				}
			}
			else {
				if(frequencyType.contentEquals("Activity Frequency")) {
					probability = currentNodeDetailStore.getAFProbabilityBasedOnParent();
				}
				else {
					probability = currentNodeDetailStore.getCFProbabilityBasedOnParent();
				}
			}
			// If frequency type is "activity" then there is possibility for loop's third child to have lower probability but it 
			// must not be deleted as the third child is required to be executed if loop still exists
			System.out.println(frequencyType);
			System.out.println(probability);
			if(probability<thresholdProbability && isLoopThirdChild(currentNodeDetailStore, treeNodeFreq) && frequencyType.contentEquals("Activity Frequency")) {
				System.out.println("Not deleting because activity frequency and third child: "+currentNodeDetailStore.getName());
			}
			else {
				if(probability<thresholdProbability) {
					toBeDeletedNodes.add(currentNodeDetailStore);
					System.out.println("Added to 'to-Be-deleted-list' with node: "+currentNodeDetailStore.getName());
				}
				else {
					System.out.println("Not deleting because probability is not less: "+currentNodeDetailStore.getName());
				}
			}
		}
		return toBeDeletedNodes;
	}
	//Increment the variant frequency of mostsimilar variant in all nodes by the frequency of variant to be deleted
	public static Map<UUID, NodeDetailStore> updateVariantFrequencyOfNode(NodeDetailStore nodeNDSBeingDeleted, String variantToDelete, String similarVariant, Map<UUID, NodeDetailStore> treeNodeFreq, NodeDetailStore nodeNDS, List<NodeDetailStore> toBeDeletedNodesExtended){
		Map<String, Integer> variantWiseCF= nodeNDS.getVariantWiseCaseFrequency();
		Integer caseFrequency = nodeNDS.getCaseFrequency();
		Map<String, Integer> variantWiseAF = nodeNDS.getVariantWiseActivityFrequency();
		Integer activityFrequency = nodeNDS.getFrequency();
		if(nodeNDSBeingDeleted.getNode().getID()!=nodeNDS.getNode().getID() || (nodeNDSBeingDeleted.getName().contains("and") && nodeNDSBeingDeleted.getType().contentEquals("operator"))) {
			/*if(nodeNDS.getActualName().contentEquals("C")) {
				System.out.println("The Node is: "+ nodeNDS.getActualName()+" => " +nodeNDS.getName());
				System.out.println("Case frequency:" + nodeNDS.getCaseFrequency());
				for(Map.Entry<String, Integer> variant: variantWiseCF.entrySet() ) {
					System.out.println("variant: "+ variant.getKey() +" = "+ variant.getValue());
				}
				System.out.println("Activity frequency:" + nodeNDS.getFrequency());
				for(Map.Entry<String, Integer> variant: variantWiseAF.entrySet() ) {
					System.out.println("variant: "+ variant.getKey() +" = "+ variant.getValue());
				}
				
			}	*/		
			if(variantWiseAF.containsKey(variantToDelete) && variantWiseAF.containsKey(similarVariant)) {
				Integer activityFrequencyForSimilarVariant = variantWiseAF.get(similarVariant);
				Integer caseFrequencyOfSimilarVariant = variantWiseCF.get(similarVariant);
				Integer activityFreqOfSimilarVariantPerCase = activityFrequencyForSimilarVariant/caseFrequencyOfSimilarVariant;
				Integer caseFrequencyOfVariantToDelete = variantWiseCF.get(variantToDelete);
				Integer activityFrequencyOfVariantToDelete = variantWiseAF.get(variantToDelete);
				Integer additionalActivityFrequencyForSimilarVariant = activityFreqOfSimilarVariantPerCase*caseFrequencyOfVariantToDelete;
				//System.out.println("The variant being deleted is: "+variantToDelete+ " with afreq:  "+activityFrequencyOfVariantToDelete+" case freq: "+caseFrequencyOfVariantToDelete );
				//System.out.println("and similar variant: "+similarVariant +" with freq: "+activityFrequencyForSimilarVariant+" with case freq: "+caseFrequencyOfSimilarVariant+" also available in node: "+ nodeNDS.getName());
				//System.out.println("We need to increase the similar variant: "+similarVariant +" based on its activity freq in one case * the case freq of deleted variant which is:"+additionalActivityFrequencyForSimilarVariant);
				nodeNDS.setFrequency(activityFrequency-activityFrequencyOfVariantToDelete+additionalActivityFrequencyForSimilarVariant);
				variantWiseAF.put(similarVariant, activityFrequencyForSimilarVariant+additionalActivityFrequencyForSimilarVariant);
				variantWiseAF.remove(variantToDelete);
			}
			else if(variantWiseAF.containsKey(variantToDelete) && !variantWiseAF.containsKey(similarVariant)) {
				//only deleting variant and not replacing, then reduce activityFrequency of node
				//System.out.println("The variant being deleted is: "+variantToDelete+ " and similar variant: "+similarVariant +" is not available in node: "+ nodeNDS.getName());
				nodeNDS.setFrequency(activityFrequency-variantWiseAF.get(variantToDelete));
				variantWiseAF.remove(variantToDelete);
			}
			else if(!variantWiseAF.containsKey(variantToDelete) && variantWiseAF.containsKey(similarVariant)) {
				//increase the variant count by checking the value for that variant in parent
				NodeDetailStore parentNDS = treeNodeFreq.get(nodeNDS.getParent());
				Integer activityFrequencyForVariantFromParent = parentNDS.getVariantWiseActivityFrequency().get(similarVariant);
				Integer activityFrequencyForVariant = variantWiseAF.get(similarVariant);
				//System.out.println("The similar variant: "+similarVariant +" has afrequency: "+ variantWiseAF.get(similarVariant)+ 
				//		"now taking parent frequency for that variant: "+activityFrequencyForVariantFromParent);
				variantWiseAF.put(similarVariant, activityFrequencyForVariantFromParent);
				nodeNDS.setFrequency(activityFrequency-activityFrequencyForVariant+activityFrequencyForVariantFromParent);
			}
				//System.out.println("Removing: "+variantToDelete+ " from node: "+ nodeNDS.getName()+" but main node being deleted is "+nodeNDSBeingDeleted.getName());
			nodeNDS.setVariantWiseActivityFrequency(variantWiseAF);
			
			if(variantWiseCF.containsKey(variantToDelete) && variantWiseCF.containsKey(similarVariant)) {
				Integer caseFrequencyForVariant = variantWiseCF.get(similarVariant);
				//System.out.println("The variant being deleted is: "+variantToDelete+ " with freq:  "+variantWiseCF.get(variantToDelete) );
				//System.out.println("and similar variant: "+similarVariant +" with freq: "+variantWiseCF.get(similarVariant)+" also available in node: "+ nodeNDS.getName());
				variantWiseCF.put(similarVariant, caseFrequencyForVariant+variantWiseCF.get(variantToDelete));
				variantWiseCF.remove(variantToDelete);
			}
			else if(variantWiseCF.containsKey(variantToDelete) && !variantWiseCF.containsKey(similarVariant)) {
				//only deleting variant and not replacing, then reduce caseFrequency of node
				//System.out.println("The variant being deleted is: "+variantToDelete+ " and similar variant: "+similarVariant +" is not available in node: "+ nodeNDS.getName());
				nodeNDS.setCaseFrequency(caseFrequency-variantWiseCF.get(variantToDelete));
				variantWiseCF.remove(variantToDelete);
			}
			else if(!variantWiseCF.containsKey(variantToDelete) && variantWiseCF.containsKey(similarVariant)) {
				//increase the variant count by checking the value for that variant in parent
				NodeDetailStore parentNDS = treeNodeFreq.get(nodeNDS.getParent());
				Integer caseFrequencyForVariantFromParent = parentNDS.getVariantWiseCaseFrequency().get(similarVariant);
				Integer caseFrequencyForVariant = variantWiseCF.get(similarVariant);
				//System.out.println("The similar variant: "+similarVariant +" has frequency: "+ variantWiseCF.get(similarVariant)+ 
				//		"now taking parent frequency for that variant: "+caseFrequencyForVariantFromParent);
				variantWiseCF.put(similarVariant, caseFrequencyForVariantFromParent);
				nodeNDS.setCaseFrequency(caseFrequency-caseFrequencyForVariant+caseFrequencyForVariantFromParent);
			}
				//System.out.println("Removing: "+variantToDelete+ " from node: "+ nodeNDS.getName()+" but main node being deleted is "+nodeNDSBeingDeleted.getName());
			nodeNDS.setVariantWiseCaseFrequency(variantWiseCF);
			
			
			treeNodeFreq.put(nodeNDS.getNode().getID(), nodeNDS);
			if(variantWiseAF.size()==0 && variantWiseCF.size()==0 && !toBeDeletedNodesExtended.contains(nodeNDS)) {
				//put the node to tobeDeletednodes.
				//System.out.println("The variant wise af and cf node: "+nodeNDS.getNode()+" - "+nodeNDS.getName()+" is 0. Hence putting it in to delete list");
				toBeDeletedNodesExtended.add(nodeNDS);
			}
			else {
				for(Node child: nodeNDS.getChildren()) {
					NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
					if(childNDS.getVariantWiseCaseFrequency().containsKey(variantToDelete) || childNDS.getVariantWiseCaseFrequency().containsKey(similarVariant)) {
						//System.out.println("updateVariantFrequencyOfNode is called for the node: "+childNDS.getName());
						treeNodeFreq = updateVariantFrequencyOfNode(nodeNDSBeingDeleted, variantToDelete, similarVariant, treeNodeFreq, childNDS, toBeDeletedNodesExtended);
					}
				}
			}
		}
		else {
			System.out.println("The node: "+nodeNDSBeingDeleted.getName()+" is going to be deleted and hence its children need not be updated");
		}
		return treeNodeFreq;
	}
	public static Map<UUID, NodeDetailStore> updateVariantsOfNode(NodeDetailStore nodeNDSBeingDeleted, String variantToDelete, Map<UUID, NodeDetailStore> treeNodeFreq, NodeDetailStore nodeNDS, List<NodeDetailStore> toBeDeletedNodesExtended){
		Map<String, Integer> variantWiseCF= nodeNDS.getVariantWiseCaseFrequency();
		Integer caseFrequency = nodeNDS.getCaseFrequency();
		Map<String, Integer> variantWiseAF = nodeNDS.getVariantWiseActivityFrequency();
		Integer activityFrequency = nodeNDS.getFrequency();
		if(nodeNDSBeingDeleted.getNode().getID()!=nodeNDS.getNode().getID()||(nodeNDSBeingDeleted.getName().contains("and") && nodeNDSBeingDeleted.getType().contentEquals("operator"))) {
			
			if(variantWiseCF.containsKey(variantToDelete)) {
				//only deleting variant and not replacing, then reduce caseFrequency of node
				//System.out.println("The variant being deleted is: "+variantToDelete+ " with cfreq: "+variantWiseCF.get(variantToDelete) +" is  available in node: "+ nodeNDS.getName());
				nodeNDS.setCaseFrequency(caseFrequency-variantWiseCF.get(variantToDelete));
				variantWiseCF.remove(variantToDelete);
				nodeNDS.setVariantWiseCaseFrequency(variantWiseCF);
			}
			
			//System.out.println("Removing: "+variantToDelete+ " from node: "+ nodeNDS.getName()+" but main node being deleted is "+nodeNDSBeingDeleted.getName());
			if(variantWiseAF.containsKey(variantToDelete)) {
				//only deleting variant and not replacing, then reduce activityFrequency of node
				//System.out.println("The variant being deleted is: "+variantToDelete+ " with afreq: "+variantWiseAF.get(variantToDelete) +" is  available in node: "+ nodeNDS.getName());
				nodeNDS.setFrequency(activityFrequency-variantWiseAF.get(variantToDelete));
				variantWiseAF.remove(variantToDelete);
				nodeNDS.setVariantWiseActivityFrequency(variantWiseAF);
			}
			treeNodeFreq.put(nodeNDS.getNode().getID(), nodeNDS);
			if(variantWiseAF.size()==0 && variantWiseCF.size()==0 && !toBeDeletedNodesExtended.contains(nodeNDS)) {
				//put the node to tobeDeletednodes.
				//System.out.println("The variant wise af and cf node: "+nodeNDS.getNode()+" - "+nodeNDS.getName()+" is 0. Hence putting it in to delete list");
				toBeDeletedNodesExtended.add(nodeNDS);
			}
			else {
				for(Node child: nodeNDS.getChildren()) {
					NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
					if(childNDS.getVariantWiseCaseFrequency().containsKey(variantToDelete) || childNDS.getVariantWiseActivityFrequency().containsKey(variantToDelete)) {
						treeNodeFreq = updateVariantsOfNode(nodeNDSBeingDeleted, variantToDelete, treeNodeFreq, childNDS, toBeDeletedNodesExtended);
					}
				}
			}
		}
		/*else {
			System.out.println("The node: "+nodeNDSBeingDeleted.getName()+" is going to be deleted and hence its children need not be updated");
		}*/
		return treeNodeFreq;
	}
	public static ProcessTree alterTreeAndRemove(ProcessTree processTree, List<NodeDetailStore> toBeDeletedNodes, Map <UUID, NodeDetailStore> treeNodeFreq, Parameters parameters ) {
 		List<NodeDetailStore> toBeDeletedNodesExtended = new ArrayList<NodeDetailStore>();
 		Integer totalTraces = parameters.getUpdatedTotalTraces();
 		Map<String, Integer> updatedVariantFrequency = parameters.getUpdatedVariantFrequency();
		for( NodeDetailStore currentNDS: toBeDeletedNodes) {
			//System.out.println("The Node to be deleted is:"+ currentNDS.getName());
			//System.out.println("The node data before deleting is");
			//displayNode(currentNDS);
			if(currentNDS.getNode().isRoot()) {
				System.out.println("Error: The node: "+currentNDS.getName()+" being deleted is root node");
			}
			
			if(treeNodeFreq.containsKey(currentNDS.getNode().getID())){
				//Integer caseFrequencyForVariant = 0;
				for(Map.Entry<String, Integer> variantWiseCF: currentNDS.getVariantWiseCaseFrequency().entrySet()) {
					String variantToDelete = variantWiseCF.getKey();
					//variantToBeDeleted must be converted to similarVariant in updated Variant frequency list 
					if(updatedVariantFrequency.containsKey(variantToDelete)) {
						//System.out.println("The variant being deleted is: "+variantToDelete);
						totalTraces =  totalTraces - updatedVariantFrequency.get(variantToDelete);
						updatedVariantFrequency.remove(variantToDelete);
					}
					else {
						System.out.println("The variant to delete: "+variantToDelete+" was already removed");
					}
					//variantToBeDeleted must be converted to similarVariant in all nodes
					//Hence delete the variant from the list of variant of nodes and increment frequency of most 
					//similar variant by variantTodelete frequency
					NodeDetailStore rootNDS = treeNodeFreq.get(processTree.getRoot().getID());
					if(rootNDS.getVariantWiseCaseFrequency().containsKey(variantToDelete) || rootNDS.getVariantWiseActivityFrequency().containsKey(variantToDelete)) {
						treeNodeFreq = updateVariantsOfNode(currentNDS, variantToDelete, treeNodeFreq, rootNDS, toBeDeletedNodesExtended );
					}
					//Add the changes needed to be done on log to variantsTodelete
					//System.out.println("variant to delete: "+variantToDelete+ " changing to: "+similarVariant);
					//variantsToChange.put(variantToDelete, similarVariant);
					//System.out.println("The variant: "+variantToDelete+" must be changed to "+similarVariant);
				}
				//now delete node
				//System.out.println("End of variant update for node "+currentNDS.getName());
				if(currentNDS.getNode().isRoot()) {
					System.out.println("Error: The node: "+currentNDS.getName()+" being deleted is root node");
				}
				else {                                                                                      
					//System.out.println("The node data after deleting is");
					//displayNode(currentNDS);
					processTree = removeNode(currentNDS, processTree, treeNodeFreq);
				}
			}
			else {
				System.out.println("The node: "+currentNDS.getName()+" was already deleted");
			}
		}
		parameters.setUpdatedTotalTraces(totalTraces);
		parameters.setUpdatedVariantFrequency(updatedVariantFrequency);
		if(toBeDeletedNodesExtended.size()>0) {
			System.out.println("While deleting The nodes the below node few other nodes were added to list");
			for(NodeDetailStore nodeNDS: toBeDeletedNodes) {
				System.out.println(nodeNDS.getName() +" - "+nodeNDS.getActualName());
			}
			System.out.println("They are : "+toBeDeletedNodesExtended);
			for(NodeDetailStore nodeNDS: toBeDeletedNodesExtended) {
				System.out.println(nodeNDS.getName() +" - "+nodeNDS.getActualName());
			}
			alterTreeAndRemove(processTree, toBeDeletedNodesExtended, treeNodeFreq, parameters);
		}
		return processTree;
	}
		
	public static ProcessTree modififyTreeBasedOnNodeProbability(ProcessTree processTree, List<NodeDetailStore> toBeDeletedNodes, Map <UUID, NodeDetailStore> treeNodeFreq, Parameters parameters, Boolean modificationTypeIsConvert) {
		for( NodeDetailStore currentNDS: toBeDeletedNodes) {
			System.out.println("--------------------------------------------------------------------");
			System.out.println("The Node to be deleted is:"+ currentNDS.getName());
			System.out.println("The variants to be deleted is:"+ currentNDS.getVariantWiseCaseFrequency());
			if(currentNDS.getNode().isRoot()) {
				System.out.println("Error: The node: "+currentNDS.getName()+" being deleted is root node");
			}
			
			if(treeNodeFreq.containsKey(currentNDS.getNode().getID())){
				for(Map.Entry<String, Integer> variantWiseCF: currentNDS.getVariantWiseCaseFrequency().entrySet()) {
					String variantToDelete = variantWiseCF.getKey();
					//System.out.println("variant to delete: "+variantToDelete);
					if(modificationTypeIsConvert) {
					
						//Get most similar variant from parent
						NodeDetailStore parentNDS = treeNodeFreq.get(currentNDS.getParent());
						String similarVariant = findMostSimilarVariant(variantToDelete,parentNDS.getVariantWiseCaseFrequency(), currentNDS.getVariantWiseCaseFrequency());
						if(similarVariant.contentEquals("")) {
							System.out.println("Error: there is no similar variant for "+variantToDelete);
						}
						parameters = updateFrequencyInDSToConvertVariant(parameters, variantToDelete, similarVariant);
					}
					else {
						parameters = updateFrequencyInDSToRemoveVariant(parameters, variantToDelete);
					}
				}	
				//now delete node
				System.out.println("End of variant update for node "+currentNDS.getName());
				if(currentNDS.getNode().isRoot()) {
					System.out.println("Error: The node: "+currentNDS.getName()+" being deleted is root node");
				}
				else {                                                                                      
					processTree = removeNode(currentNDS, processTree, treeNodeFreq);
				}
			}
			else {
				System.out.println("The node: "+currentNDS.getName()+" was already deleted");
			}
		}
		return processTree;
	}
	
	public static ProcessTree alterTreeAndConvert(ProcessTree processTree, List<NodeDetailStore> toBeDeletedNodes, Map <UUID, NodeDetailStore> treeNodeFreq, Map<String, String> variantsToChange, Map<String, Integer> updatedVariantFrequency) {
 		List<NodeDetailStore> toBeDeletedNodesExtended = new ArrayList<NodeDetailStore>();
		for( NodeDetailStore currentNDS: toBeDeletedNodes) {
			System.out.println("The Node to be deleted is:"+ currentNDS.getName());
			//System.out.println("The node data before deleting is");
			//displayNode(currentNDS);
			if(currentNDS.getNode().isRoot()) {
				System.out.println("Error: The node: "+currentNDS.getName()+" being deleted is root node");
			}
			
			if(treeNodeFreq.containsKey(currentNDS.getNode().getID())){
				for(Map.Entry<String, Integer> variantWiseCF: currentNDS.getVariantWiseCaseFrequency().entrySet()) {
					String variantToDelete = variantWiseCF.getKey();
					//System.out.println("variant to delete: "+variantToDelete);
					//Get most similar variant from parent
					NodeDetailStore parentNDS = treeNodeFreq.get(currentNDS.getParent());
					String similarVariant = findMostSimilarVariant(variantToDelete,parentNDS.getVariantWiseCaseFrequency(), currentNDS.getVariantWiseCaseFrequency());
					if(similarVariant.contentEquals("")) {
						System.out.println("Error: there is no similar variant for "+variantToDelete);
					}
					//variantToBeDeleted must be converted to similarVariant in updated Variant frequency list 
					if(updatedVariantFrequency.containsKey(variantToDelete)) {
						if(updatedVariantFrequency.containsKey(similarVariant)) { 
							Integer caseFrequencyForVariant = updatedVariantFrequency.get(similarVariant);
							updatedVariantFrequency.put(similarVariant, caseFrequencyForVariant+updatedVariantFrequency.get(variantToDelete));
							//System.out.println("From main The variant being deleted is: "+variantToDelete+ " with freq:  "+updatedVariantFrequency.get(variantToDelete) );
							//System.out.println("and similar variant: "+similarVariant +" with freq: "+updatedVariantFrequency.get(similarVariant));
							updatedVariantFrequency.remove(variantToDelete);
						}
						else {
							System.out.println("Error: The similar variant : "+variantToDelete+" was already removed");
						}
					}
					else {
						System.out.println("Error: The variant to delete: "+variantToDelete+" was already removed");
					}
					//variantToBeDeleted must be converted to similarVariant in all nodes
					//Hence delete the variant from the list of variant of nodes and increment frequency of most 
					//similar variant by variantTodelete frequency
					NodeDetailStore rootNDS = treeNodeFreq.get(processTree.getRoot().getID());
					if(rootNDS.getVariantWiseCaseFrequency().containsKey(variantToDelete) || rootNDS.getVariantWiseCaseFrequency().containsKey(similarVariant)) {
						treeNodeFreq = updateVariantFrequencyOfNode(currentNDS, variantToDelete, similarVariant, treeNodeFreq, rootNDS, toBeDeletedNodesExtended);
					}
					//Add the changes needed to be done on log to variantsTodelete
					//System.out.println("variant to delete: "+variantToDelete+ " changing to: "+similarVariant);
					variantsToChange.put(variantToDelete, similarVariant);
					
				}
				//now delete node
				System.out.println("End of variant update for node "+currentNDS.getName());
				if(currentNDS.getNode().isRoot()) {
					System.out.println("Error: The node: "+currentNDS.getName()+" being deleted is root node");
				}
				else {                                                                                      
					//System.out.println("The node data after deleting is");
					//displayNode(currentNDS);
					processTree = removeNode(currentNDS, processTree, treeNodeFreq);
				}
			}
			else {
				System.out.println("The node: "+currentNDS.getName()+" was already deleted");
			}
		}
		if(toBeDeletedNodesExtended.size()>0) {
			System.out.println("While deleting The nodes the below node few other nodes were added to list");
			for(NodeDetailStore nodeNDS: toBeDeletedNodes) {
				System.out.println(nodeNDS.getName() +" - "+nodeNDS.getActualName());
			}
			System.out.println("They are : "+toBeDeletedNodesExtended);
			for(NodeDetailStore nodeNDS: toBeDeletedNodesExtended) {
				System.out.println(nodeNDS.getName() +" - "+nodeNDS.getActualName());
			}
			alterTreeAndConvert(processTree, toBeDeletedNodesExtended, treeNodeFreq, variantsToChange, updatedVariantFrequency);
		}
		return processTree;
	}
	

	public static ProcessTree removeNode (NodeDetailStore nodeNDS, ProcessTree processTree, Map <UUID, NodeDetailStore> treeNodeFreq ) {
		//check if current node's parent is loop then we need to check if the current node is which child of the loop
		//if the current node is first or third child then don't delete it because it will be handled and deleted when parent node gets deleted in case of lower probability 
		//only check for second child because based on second child we decide to delete the entire loop structure and make a sequence of first and third child(if either of them is not tau) and add it to loop's parent 
		//if currentNDS's parent is anything other than loop then delete currentNDS and attach its children to currentNDS's parent 
		System.out.println("Physical removal of node: "+nodeNDS.getName());
		NodeDetailStore parentNDS = treeNodeFreq.get(nodeNDS.getParent());
		Block parentNodeBlock = (Block) parentNDS.getNode();
		List<Edge> outgoingEdgesFromParent = cloneEdgeList(parentNodeBlock.getOutgoingEdges());
		for(Edge edge: outgoingEdgesFromParent) {
			if (edge.getTarget() ==  nodeNDS.getNode()){
				int currentNodePosition = parentNDS.getChildren().indexOf(nodeNDS.getNode());
				System.out.println("The outgoing  edge(to be deleted)  from parent to child is: "+edge.getID());
				System.out.println(processTree.getEdge(edge.getID()));
				System.out.println("Before deletion the number of children of the parent: "+parentNDS.getName()+" are: "+parentNodeBlock.getOutgoingEdges().size());
					
				//Deleting currentNode and its children from process tree
				processTree = removeChildrenNodeAndEdgesFromPT(processTree, nodeNDS,treeNodeFreq );
				parentNodeBlock.removeOutgoingEdge(edge);
					
				//After removing the child update the parent's new list of children in our data structure
				parentNDS.setChildren(parentNodeBlock.getChildren());
				treeNodeFreq.put(parentNDS.getNode().getID(), parentNDS);
				System.out.println("After deletion Remaining Number of children of the parent is: "+parentNodeBlock.getOutgoingEdges().size());
					
				//if the parent has only one child or the current child was the second child of the loop then we need to delete the parent node 
				//and attach its child/children to its grandparent 
				if(parentNodeBlock.getOutgoingEdges().size()==1 || (currentNodePosition==1 && parentNDS.getName().contains("loop"))) {
					NodeDetailStore grandparentNDS = treeNodeFreq.get(parentNDS.getParent());
					List <Node> nodesToBeAttachedToGrandparent = new ArrayList<Node>();
						
					//if current node is a second child of loop, then first and third child must be added to a new parent node - "sequence" and attach 
					//the new sequence node to grandparent only if its grandparent is not sequence
					//if the grandparent is  already a sequence node then attach first and third child to grandparent directly
					//if current node is not second child of loop then attach that single child of parent to grandparent directly and remove the intermediate parent 
					if(parentNDS.getChildren().size()==2) {
						if(currentNodePosition == 1  && parentNDS.getName().contains("loop")) {
							//if grandparent is not seq then create a seq node and add children to it and attach seq to grandparent  
							if(!grandparentNDS.getName().contains("seq")) {
								//Create a new node 'sequence' and add loops remaining children to this new node
								Node newNode = new Seq("");
								newNode.setProcessTree(processTree);
								processTree.addNode(newNode);
								//creating a new entry for the new node in our data structure.
								NodeDetailStore newNodeNDS = new NodeDetailStore("seq", newNode.getName(), newNode, grandparentNDS.getNode().getID(), parentNDS.getChildren());
								System.out.println("Setting CF probability new sequence node "+newNodeNDS.getName()+ " to "+parentNDS.getCFProbabilityBasedOnParent()+ " from "+parentNDS.getCFProbabilityBasedOnParent());
								System.out.println("Setting AF probability new sequence node "+newNodeNDS.getName()+ " to "+parentNDS.getAFProbabilityBasedOnParent()+ " from "+parentNDS.getAFProbabilityBasedOnParent());
								newNodeNDS.copyFrequencyDetails(parentNDS);
								newNodeNDS.setFrequency(parentNDS.getCaseFrequency());
								newNodeNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseCaseFrequency());//this is needed because loop activity freq was changed to its first child activity freq
								treeNodeFreq.put(newNode.getID(), newNodeNDS);
								Block newSeqNodeBlock = (Block) newNode;
								//adding loops children to sequence node in tree
								for(Node child : parentNDS.getChildren()) {
									//add loops child to new seq node
									newSeqNodeBlock.addChild(child);
									//update the child's parent in our data structure from loop to newly created sequence node UUID 
									NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
									childNDS.setParent(newNode.getID());
									//child's frequency details must now be same as parent  
									System.out.println("Modifying frequency of child: "+childNDS.getName()+ " to "+parentNDS.getCaseFrequency()+ " from "+childNDS.getFrequency());
									childNDS.copyFrequencyDetails(parentNDS);
									childNDS.setFrequency(parentNDS.getCaseFrequency());
									childNDS.setVariantWiseActivityFrequency(parentNDS.getVariantWiseCaseFrequency());//this is needed because loop activity freq was changed to its first child activity freq
									treeNodeFreq.put(child.getID(), childNDS);
								}
								nodesToBeAttachedToGrandparent.add(newNode);
							}
							//grandparent is sequence so attach the first and third child to its grandparent directly
							else {
								for(Node child : parentNDS.getChildren()) {
									nodesToBeAttachedToGrandparent.add(child);
									//update the child's parent in our data structure from loop to grandparent id because this node will be attached to grandparent
									NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
									childNDS.setParent(grandparentNDS.getNode().getID());
									//child's frequency must now be same as grandparent frequency 
									System.out.println("Modifying frequency of child: "+childNDS.getName()+ " to grandparent frequency "+grandparentNDS.getFrequency()+ " from "+childNDS.getFrequency());
									childNDS.copyFrequencyDetails(grandparentNDS);
									treeNodeFreq.put(child.getID(), childNDS);
								}
							}
						}
						else {
							System.out.println("Error occured: The count of children is 2 and so the currentnode must be second child of loop.");
						}
					}	
					else if(parentNDS.getChildren().size()==1){
						nodesToBeAttachedToGrandparent.add(parentNDS.getChildren().get(0));
						//if parent is not root then update the child's parent in our data structure to grandparent id because this node will be attached to grandparent 
						//else make the child as root node
						NodeDetailStore childNDS = treeNodeFreq.get(parentNDS.getChildren().get(0).getID());
						if(!parentNDS.getNode().isRoot()) {
							childNDS.setParent(grandparentNDS.getNode().getID());
						}else {
							childNDS.setParent(null);
							processTree.setRoot(childNDS.getNode());
							System.out.println("The child being deleted is the only child of root node. Hence making it root node ");
						}
						//child's frequency must now be same as parent frequency 
						//childNDS.copyFrequencyDetails(parentNDS);
						treeNodeFreq.put(childNDS.getNode().getID(), childNDS);
					}
					else{
						System.out.println("Error occured: The remaining children count for parent node: "+parentNDS.getName()+" must be 2 in case of loop as parent"
								+ " and count must be 1 in case of other operaters "
								+ "but the actual children count is: "+ parentNDS.getChildren().size());
					}
					if(!parentNDS.getNode().isRoot()) {
						//Find position of parent in grandParent's children
						int parentPosition = grandparentNDS.getChildren().indexOf(parentNDS.getNode());
						System.out.println("The parent is at position: "+ parentPosition);
								
						//Replace parent node with its child(children in case of loop) in grandparent node's children 
						Node grandparent = grandparentNDS.getNode();
						Block grandparentBlock = (Block) grandparent;
						grandparentBlock.swapChildAt(nodesToBeAttachedToGrandparent.get(0), parentPosition);
						System.out.println("Attaching node: "+ nodesToBeAttachedToGrandparent.get(0) +" to grandparent:"+ grandparentNDS.getName());
						if(nodesToBeAttachedToGrandparent.size() == 2) {
							grandparentBlock.addChildAt(nodesToBeAttachedToGrandparent.get(1), parentPosition+1);
							System.out.println("The count of nodes to be attached to the grandparent are: "+nodesToBeAttachedToGrandparent.size());
							System.out.println("Attaching node: "+ nodesToBeAttachedToGrandparent.get(1) +" to grandparent:"+ grandparentNDS.getName());
						}
						else if(nodesToBeAttachedToGrandparent.size()> 2) {
							System.out.println("Error Occured: The count of nodes to be attached to the grandparent are: "+nodesToBeAttachedToGrandparent.size()+" and is greater than 2");
						}
						//update the new children for grandparent node in our data structure
						grandparentNDS.setChildren(grandparentBlock.getChildren());
						System.out.println("The new list of children for grandparent node is: "+ grandparentBlock.getChildren());
						treeNodeFreq.put(grandparent.getID(),grandparentNDS);	
					}
					//Remove the edges from old outdated parent node to its children 
					for(Edge oldEdge : outgoingEdgesFromParent) {
						parentNodeBlock.removeOutgoingEdge(oldEdge);
					}
					//Remove the parentNode from processTree
					processTree.removeNode(parentNDS.getNode());
					//Remove old parent from our datastructure 
					treeNodeFreq.remove(parentNDS.getNode().getID());								
				}
				else {
					System.out.println("There are many children to parent: "+ parentNDS.getName()+ " Hence not deleting parent");	
					//TODO: Set one of the child with increased probability
				}
			}
		}
		return processTree;
	}
	public static List<Edge> cloneEdgeList(List<Edge> listOfEdges) {
		List<Edge> output = new ArrayList<>();
		for (Edge edge : listOfEdges) {
			output.add(edge);
		}
		return output;
	}
	
	public static ProcessTree removeChildrenNodeAndEdgesFromPT(ProcessTree processTree, NodeDetailStore toRemoveNDS, Map<UUID, NodeDetailStore> treeNodeFreq) {
		Node nodeToBeRemoved = toRemoveNDS.getNode();
		if(nodeToBeRemoved.isLeaf()) {
			processTree.removeNode(nodeToBeRemoved);
			treeNodeFreq.remove(nodeToBeRemoved.getID());
			//System.out.println(processTree.toString()+ " is the tree after being removed");
			return processTree;
		}
		else {
			//if node is operator then call this function for its children then delete 
			//all outgoing edges from that node and then delete the operator node itself
			for (Node child: toRemoveNDS.getChildren()){
				NodeDetailStore childNDS = treeNodeFreq.get(child.getID());
				processTree = removeChildrenNodeAndEdgesFromPT(processTree, childNDS, treeNodeFreq );
			}
			
			Block nodeToBeRemovedBlock = (Block) nodeToBeRemoved;
			List<Edge> outgoingEdgesFromParent = cloneEdgeList(nodeToBeRemovedBlock.getOutgoingEdges());
			for(Edge edge: outgoingEdgesFromParent) {
				nodeToBeRemovedBlock.removeOutgoingEdge(edge);
			}
		
			processTree.removeNode(nodeToBeRemoved);
			treeNodeFreq.remove(nodeToBeRemoved.getID());
			
		}
		return processTree;
	}
	public static Map<UUID, NodeDetailStore> setNodeSuretyToFalse(Map<UUID, NodeDetailStore> treeNodeFreq){
		Map<UUID, NodeDetailStore> updatedTreeNodeFreq = new LinkedHashMap<UUID, NodeDetailStore>();
		for(Map.Entry<UUID, NodeDetailStore> entry: treeNodeFreq.entrySet()) {
			entry.getValue().setSuretyOfActivityFrequency(false);
			entry.getValue().setSuretyOfCaseFrequency(false);
			updatedTreeNodeFreq.put(entry.getKey(), entry.getValue());
		}
		return updatedTreeNodeFreq;
	}
	public static Object[] replaceWithSubtree (ProcessTree processTree, ProcessTree subProcessTree, NodeDetailStore nodeNDSToRemove,  
			Map<UUID, NodeDetailStore> treeNodeFreq, Parameters parameters) { 
		//Assumption: only 1 parent per node
		//In case the node being deleted is root node 
		//for all the activity nodes in sub process tree, save the frequency data in datastructure and 
		//use the same frequency going forward
		Map<String, NodeDetailStore> dataStructureWithUpdatedActivities = new LinkedHashMap<String, NodeDetailStore>();
		if(parameters.getPlugIn().contentEquals("ProcessTreeBasedFilterVisualizer")) {
			for(Map.Entry<UUID, NodeDetailStore> entry: treeNodeFreq.entrySet()) {
				if(entry.getValue().getType()=="activity") {
					System.out.println("The node data being transfered is :"+entry.getValue().getName());
					dataStructureWithUpdatedActivities.put(entry.getValue().getActualName(), entry.getValue().clone());
				}
			}
		}
		else {
			treeNodeFreq = setNodeSuretyToFalse(treeNodeFreq);
		}
		parameters.setDataStructureWithUpdatedActivities(dataStructureWithUpdatedActivities);
		
		if(nodeNDSToRemove.getNode().isRoot()||nodeNDSToRemove.getParent()==null ) {
			
			treeNodeFreq.clear();
			
			treeNodeFreq = mapPTtoNodeDetailStructure(subProcessTree, subProcessTree.getRoot(), UUID.randomUUID(), treeNodeFreq, parameters);
			System.out.println("New process tree root is:"+subProcessTree.getRoot());
			
			treeNodeFreq = HelperFunctions.setFrequenciesOnProcessTree(treeNodeFreq, subProcessTree, parameters);
			
			treeNodeFreq = setProbability(treeNodeFreq, parameters.getUpdatedTotalTraces());
			
			System.out.println("The process tree generated when replacing");
			System.out.println(subProcessTree.toString());
			return new Object[] {subProcessTree, treeNodeFreq};
		}
		else {
			//if "and" node being deleted is not root node
			Node parentNode = processTree.getNode(nodeNDSToRemove.getParent());
			NodeDetailStore parentNDS = treeNodeFreq.get(parentNode.getID());
			Block parentNodeBlock = (Block) parentNode;
			Node childToBeRemoved= nodeNDSToRemove.getNode();
			List<Edge> outgoingEdgesFromParent = cloneEdgeList(parentNodeBlock.getOutgoingEdges());
			for(Edge edge: outgoingEdgesFromParent) {
				if (edge.getTarget() == childToBeRemoved){
								
					//Find position of child in its parent's children
					int childToBeRemovedPosition = parentNDS.getChildren().indexOf(childToBeRemoved);
					System.out.println("The and node to be removed is at position :"+ childToBeRemovedPosition);
					
					//Remove all the children of andNode and all their respective edges
					processTree=removeChildrenNodeAndEdgesFromPT(processTree, nodeNDSToRemove,treeNodeFreq );
					parentNodeBlock.removeOutgoingEdge(edge);
					
					Node subPTRootNode = subProcessTree.getRoot();
					
					//Change the process tree of nodes from subtree to main process tree
					setProcessTree(processTree, subPTRootNode);
					
					//add subprocesstree's root node at "and" node position which is being deleted 
					parentNodeBlock.addChildAt(subPTRootNode, childToBeRemovedPosition);
					
					//Remove the andNode from processTree
					processTree.removeNode(childToBeRemoved);
					
					//Remove old parent from our datastructure 
					treeNodeFreq.remove(childToBeRemoved.getID());
					
					System.out.println(processTree.toString()+ " is the tree after being added");
					
					//After removing the child update new list of children in our data structure
					parentNDS.setChildren(parentNodeBlock.getChildren());
					treeNodeFreq.put(parentNode.getID(), parentNDS);
					
					//update our datastructure with new process tree nodes
					System.out.println("******************The Updated Event total case Frequency Mapper************");
					for (Map.Entry<String, Integer> entry : parameters.getUpdatedEventCaseFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}
					System.out.println("******************The Updated Event total activity Frequency Mapper************");
					for (Map.Entry<String, Integer> entry : parameters.getUpdatedEventActivityFrequency().entrySet()) {
						System.out.println(entry.getKey() +" => "+entry.getValue());
					}
					
					treeNodeFreq = mapPTtoNodeDetailStructure(processTree, subPTRootNode, parentNode.getID(), treeNodeFreq, parameters);
	
					treeNodeFreq = HelperFunctions.setFrequenciesOnProcessTree(treeNodeFreq, processTree, parameters);
	
					treeNodeFreq = setProbability(treeNodeFreq, parameters.getUpdatedTotalTraces());
	
					
				}	
			}
			System.out.println("The process tree generated when replacing");
			System.out.println(processTree.toString());
			return new Object[] {processTree, treeNodeFreq};
		}
		
	}
	public static Map<UUID, NodeDetailStore> setFrequenciesOnProcessTree(Map<UUID, NodeDetailStore> treeNodeFreq, ProcessTree processTree, Parameters parameters) {
		System.out.println("***********************Calculating frequency for nodes without considering tau frequency******************");
		NodeDetailStore nodeNDS = treeNodeFreq.get(processTree.getRoot().getID());
		treeNodeFreq = HelperFunctions.setNodeInitialFrequencies(nodeNDS, treeNodeFreq, parameters);
		
		//HelperFunctions.displayDataStructure(treeNodeFreq, false);
		
		/****Updating root node frequency  with trace count and Update all its Children****/
		//root node frequency is same as total number of traces
		//Use root node frequency and update all its children frequency from top to bottom before setting the tau value and its sure is true
		System.out.println("***********************Update children using root frequency  *************************************");
		NodeDetailStore rootNodeNDS = treeNodeFreq.get(processTree.getRoot().getID());
		if(!rootNodeNDS.getSuretyOfActivityFrequency() || !rootNodeNDS.getSuretyOfCaseFrequency()) {
			rootNodeNDS.setVariantWiseActivityFrequency(parameters.getUpdatedVariantFrequency());
			rootNodeNDS.setVariantWiseCaseFrequency(parameters.getUpdatedVariantFrequency());
			/*for (Map.Entry<String, Integer> entry : this.parameters.getVariantFrequency().entrySet()) {
				System.out.println(entry.getKey() +" => "+entry.getValue());
			}*/
			rootNodeNDS.setFrequency(parameters.getUpdatedTotalTraces());
			rootNodeNDS.setCaseFrequency(parameters.getUpdatedTotalTraces());
			rootNodeNDS.setSuretyOfActivityFrequency(true);
			rootNodeNDS.setSuretyOfCaseFrequency(true);
			treeNodeFreq.put(processTree.getRoot().getID(), rootNodeNDS);
		}
		treeNodeFreq = HelperFunctions.updateChildrenFrequency(rootNodeNDS, treeNodeFreq);
		
		//System.out.println("***********************tree frequency before setting tau value**************************");
		//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
		
		/****Setting tau frequency ****/
		//Now for every tau node calculate the tau frequency using previously set frequencies for operator nodes
		System.out.println("***********************Setting tau activity frequency now**************************");
		for (Map.Entry<UUID, NodeDetailStore> entry : treeNodeFreq.entrySet()) {
			if(entry.getValue().getName().contains("tau")) {
				//System.out.println("Setting tau frequency for tau node:"+ entry.getValue().getName());
				if(!entry.getValue().getSuretyOfActivityFrequency()) {
					treeNodeFreq = HelperFunctions.setTauActivityFrequency(entry.getValue(), treeNodeFreq, parameters);
				}
				else {
					System.out.println("Tau frequency for tau node:"+ entry.getValue().getName()+" is already set");
				}
			}
		}
		
		System.out.println("***********************after Setting tau frequency *************************************");
		//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
		
		//do a force update in case the children frequency is still not sure
		System.out.println("***********************Force update children activity frequency*************************************");
		treeNodeFreq = HelperFunctions.forceUpdateChildrenFrequency(rootNodeNDS, treeNodeFreq, parameters);
		
		//System.out.println("***********************Display after force updating activity frequency*************************************");
		//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
		
		System.out.println("***********************Setting activity frequency on Loop Node*************************************");
		treeNodeFreq = HelperFunctions.setActivityFrequenciesOnLoopNode(treeNodeFreq);
		
		//Setting Case frequency for all nodes based on previously calculated activity
		System.out.println("***********************Setting Case frequency for all nodes**************************");
		HelperFunctions.computeCaseFrequencyForNodes(treeNodeFreq, parameters);
		//HelperFunctions.displayDataStructure(this.treeNodeFreq, false);
		return treeNodeFreq;
	}
	public static void setProcessTree (ProcessTree processTree, Node node) {
		node.setProcessTree(processTree);
		processTree.addNode(node);
		if(!node.isLeaf()) {
			Block nodeBlock = (Block)node;
			for (Node child : nodeBlock.getChildren()) {	
				setProcessTree(processTree, child);
			}
		}
	}
	public static String findMostSimilarVariant(String variantToMatch, Map<String, Integer> variantWiseCF, Map<String, Integer> excludeVariantWiseCF) {
		String mostSimilarVariant = "";
		Integer lowestDistance = 999999999;
		Integer frequencyOfSimilarVariant = 0;
		for(Map.Entry<String, Integer> variant: variantWiseCF.entrySet()) {
			//if variant is a part of the list of variants of currently being deleted node than do not check its similarity 
			if(!excludeVariantWiseCF.containsKey(variant.getKey())){
				int distance = calculateLD(variantToMatch, variant.getKey());
				if((lowestDistance==distance && variant.getValue()>frequencyOfSimilarVariant) || lowestDistance>distance) {
					lowestDistance = distance;
					mostSimilarVariant = variant.getKey();
					frequencyOfSimilarVariant = variant.getValue();
				}
			}
		}
		return mostSimilarVariant;
	}
	
	public static int calculateLD(String x, String y) {
	    int[][] dp = new int[x.length() + 1][y.length() + 1];
	    
	    for (int i = 0; i <= x.length(); i++) {
	        for (int j = 0; j <= y.length(); j++) {
	            if (i == 0) {
	                dp[i][j] = j;
	            }
	            else if (j == 0) {
	                dp[i][j] = i;
	            }
	            else {
	                dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
	                  dp[i - 1][j] + 1, 
	                  dp[i][j - 1] + 1);
	            }
	        }
	    }
	 
	    return dp[x.length()][y.length()];
	} 
	
	public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }
	
    public static int min(int... numbers) {
        return Arrays.stream(numbers)
          .min().orElse(Integer.MAX_VALUE);
    }
    
    public static NodeDetailStore getChildAtIndex(Map<UUID, NodeDetailStore> treeNodeFreq, NodeDetailStore parentNDS, int childIndex) {
    	List<Node> children = parentNDS.getChildren();
    	if(childIndex < children.size()) {
    		Node firstChild = children.get(childIndex);
    		return treeNodeFreq.get(firstChild.getID());
    	}
    	return null;
    }
    
    public static Boolean isLoopThirdChild(NodeDetailStore nodeNDS, Map<UUID, NodeDetailStore> treeNodeFreq ) {
    	NodeDetailStore parentNDS = treeNodeFreq.get(nodeNDS.getParent());
    	NodeDetailStore thirdChild = getChildAtIndex(treeNodeFreq, parentNDS, 2);
    	System.out.println("Is third child of loop :" + (parentNDS.getName().contains("loop") && nodeNDS.isEqual(thirdChild)));
    	return parentNDS.getName().contains("loop") && nodeNDS.isEqual(thirdChild);
    }
}

