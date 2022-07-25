package org.processmining.logfiltering.plugins.Sabya;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.processmining.processtree.Node;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

class FrequencyDetails{
	Integer ActivityFrequency;
	Integer CaseFrequency;
	Map<String, Integer> VariantWiseCaseFrequency;
	Map<String, Integer> VariantWiseActivityFrequency;
	double AF_OverallProbability;
	double AF_ProbabilityBasedOnParent;
	double CF_OverallProbability;
	double CF_ProbabilityBasedOnParent;
	
	FrequencyDetails(NodeDetailStore nodeNDS){
		//NodeDetailStore clonedNDS  = nodeNDS.clone();
		ActivityFrequency = nodeNDS.getFrequency();
		CaseFrequency = nodeNDS.getCaseFrequency();
		VariantWiseCaseFrequency = nodeNDS.cloneVariantWiseFrequency(nodeNDS.getVariantWiseCaseFrequency());
		VariantWiseActivityFrequency = nodeNDS.cloneVariantWiseFrequency(nodeNDS.getVariantWiseActivityFrequency());
		AF_OverallProbability = nodeNDS.getAFOverallProbability();
		AF_ProbabilityBasedOnParent = nodeNDS.getAFProbabilityBasedOnParent();
		CF_OverallProbability = nodeNDS.getCFOverallProbability();
		CF_ProbabilityBasedOnParent = nodeNDS.getCFProbabilityBasedOnParent();
	}
	
}

public class NodeDetailStore {
	private String Name;
	private String ActualName;
	private Node Node ;
	private UUID Parent;
	private static Integer Counter = 1;
	private List<Node> Children;
	private boolean SuretyOfActivityFrequency;
	private boolean SuretyOfCaseFrequency;
	private Integer Frequency;
	private Integer CaseFrequency;
	private Map<String, Integer> VariantWiseCaseFrequency;
	private Map<String, Integer> VariantWiseActivityFrequency;
	private double AF_OverallProbability;
	private double AF_ProbabilityBasedOnParent;
	private double CF_OverallProbability;
	private double CF_ProbabilityBasedOnParent;
	private boolean ChildIsActivity; 
	private String Type;
	private List<String> LeafChildren;
	private FrequencyDetails PreviousFrequencyDetails;
	
	NodeDetailStore(){
		Name = "";
		ActualName = "";
		Frequency = 0;
		CaseFrequency = 0;
		ChildIsActivity = false;
		AF_OverallProbability = 0;
		AF_ProbabilityBasedOnParent = 0;
		CF_OverallProbability = 0;
		CF_ProbabilityBasedOnParent = 0;
		SuretyOfActivityFrequency = false;
		SuretyOfCaseFrequency = false;
		Type = "unknown";
		LeafChildren = new ArrayList<>();
		VariantWiseCaseFrequency = new LinkedHashMap<String, Integer>();
		VariantWiseActivityFrequency = new LinkedHashMap<String, Integer>();
	}
	NodeDetailStore(String name,String actualName, Node node, UUID parent, List<Node> children){
		if(name.equals("tau")||name.equals("seq")||name.equals("and")||name.equals("loop")||name.equals("xor")||name.equals("or") ) {
			Name = name+Counter;
			Counter++;
			if(name.equals("tau")){
				Type = "tau";
			}
			else {
				Type = "operator";
			}
		}else {
			Name = name;
			Type = "activity";
		}
		ActualName = actualName;
		Node = node;
		Parent = parent;
		Children = children;
		Frequency = 0;
		CaseFrequency = 0;
		ChildIsActivity = false;
		AF_OverallProbability = 0;
		AF_ProbabilityBasedOnParent = 0;
		CF_OverallProbability = 0;
		CF_ProbabilityBasedOnParent = 0;
		SuretyOfActivityFrequency = false;
		SuretyOfCaseFrequency = false;
		LeafChildren = new ArrayList<String>();
		VariantWiseCaseFrequency = new LinkedHashMap<String, Integer>();
		VariantWiseActivityFrequency = new LinkedHashMap<String, Integer>();
	}
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name=name;
	}
	
	public String getActualName() {
		return ActualName;
	}
	public void setActualName(String actualName) {
		ActualName=actualName;
	}

	public Node getNode() {
		return Node;
	}
	public void setNode(Node node) {
		Node=node;
	}
	
	public UUID getParent() {
		return Parent;
	}
	public void setParent(UUID parent) {
		 Parent = parent;
	}

	public List<Node> getChildren() {
		return Children;
	}
	
	public void setChildren(List<Node> children) {
		Children = children;
	}

	public Integer getFrequency() {
		return Frequency;
	}
	
	public void setFrequency(Integer frequency) {
		Frequency = frequency;
	}
	
	public Integer getCaseFrequency() {
		return CaseFrequency;
	}
	
	public void setCaseFrequency(Integer casefrequency) {
		CaseFrequency = casefrequency;
	}
	
	public boolean getChildIsActivity() {
		return ChildIsActivity;
	}
	
	public void setChildIsActivity(boolean childIsActivity) {
		ChildIsActivity = childIsActivity;
	}
	
	public double getAFOverallProbability() {
		return AF_OverallProbability;
	}
	
	public void setAFOverallProbability(double af_overallProbability) {
		AF_OverallProbability = af_overallProbability;
	}

	public void setAFProbabilityBasedOnParent(double af_probabilityBasedOnParent) {
		AF_ProbabilityBasedOnParent = af_probabilityBasedOnParent;
	}
	
	public double getAFProbabilityBasedOnParent() {
		return AF_ProbabilityBasedOnParent;
	}

	public double getCFOverallProbability() {
		return CF_OverallProbability;
	}
	
	public void setCFOverallProbability(double cf_overallProbability) {
		CF_OverallProbability = cf_overallProbability;
	}

	public void setCFProbabilityBasedOnParent(double cf_probabilityBasedOnParent) {
		CF_ProbabilityBasedOnParent = cf_probabilityBasedOnParent;
	}
	
	public double getCFProbabilityBasedOnParent() {
		return CF_ProbabilityBasedOnParent;
	}
	
	public void removeChild(Node child) {
		Children.remove(child);
	}
	
	public void setSuretyOfActivityFrequency(Boolean suretyOfActivityFrequency) {
		SuretyOfActivityFrequency = suretyOfActivityFrequency;
	}
	
	public boolean getSuretyOfActivityFrequency() {
		return SuretyOfActivityFrequency;
	}
	
	public void setSuretyOfCaseFrequency(Boolean suretyOfCaseFrequency) {
		SuretyOfCaseFrequency = suretyOfCaseFrequency;
	}
	
	public boolean getSuretyOfCaseFrequency() {
		return SuretyOfCaseFrequency;
	}
	
	public void setType(String type) {
		Type = type;
	}
	
	public String getType() {
		return Type;
	}
	
	public void setLeafChildren(List<String> leafChildren) {
		LeafChildren = leafChildren;
	}
	
	public List<String> getLeafChildren() {
		return LeafChildren;
	}
	
	public static void resetCounter() {
		Counter = 0;
	}
	
	public void storeOldFrequencyDetails() {
		PreviousFrequencyDetails = new FrequencyDetails(this);
	}
	
	public FrequencyDetails getPreviousFrequencyDetails() {
		return PreviousFrequencyDetails;
	}
	
	public Map<String, Integer> cloneVariantWiseFrequency(Map<String, Integer> variantWiseFrequency) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(variantWiseFrequency);
		java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String, Integer>>(){}.getType();
		LinkedHashMap<String, Integer> clonedMap = gson.fromJson(jsonString, type); 
		return clonedMap;
	}
	
	public void setVariantWiseCaseFrequency(Map<String, Integer> variantWiseCaseFrequency) {
		VariantWiseCaseFrequency = cloneVariantWiseFrequency(variantWiseCaseFrequency);
	}
	
	public Map<String, Integer> getVariantWiseCaseFrequency(){
		return VariantWiseCaseFrequency;
	}
	
	public void setVariantWiseActivityFrequency(Map<String, Integer> variantWiseActivityFrequency) {
		VariantWiseActivityFrequency = cloneVariantWiseFrequency(variantWiseActivityFrequency);
	}
		
	
	public Map<String, Integer> getVariantWiseActivityFrequency(){
		return VariantWiseActivityFrequency;
	}
	
	public void copyFrequencyDetails(NodeDetailStore nodeNDS){
		Frequency = nodeNDS.getFrequency();
		CaseFrequency = nodeNDS.getCaseFrequency();
		VariantWiseCaseFrequency = cloneVariantWiseFrequency(nodeNDS.getVariantWiseCaseFrequency());
		VariantWiseActivityFrequency = cloneVariantWiseFrequency(nodeNDS.getVariantWiseActivityFrequency());
		CF_OverallProbability = nodeNDS.getCFOverallProbability();
		CF_ProbabilityBasedOnParent = nodeNDS.getCFProbabilityBasedOnParent();
		AF_OverallProbability = nodeNDS.getAFOverallProbability();
		AF_ProbabilityBasedOnParent = nodeNDS.getAFProbabilityBasedOnParent();
	}
	
	public void setPreviousFrequencyDetails(FrequencyDetails previousFrequencyDetails) {
		PreviousFrequencyDetails = previousFrequencyDetails;
	}
	
	public NodeDetailStore clone() {
		NodeDetailStore  clonedNodeNDS = new NodeDetailStore();
		clonedNodeNDS.setActualName(this.getActualName());
		clonedNodeNDS.setName(this.getName());
		clonedNodeNDS.setNode(this.getNode());
		clonedNodeNDS.setType(this.getType());
		clonedNodeNDS.setChildren(this.getChildren());
		clonedNodeNDS.setChildIsActivity(this.getChildIsActivity());
		clonedNodeNDS.setCaseFrequency(this.getCaseFrequency());
		clonedNodeNDS.setFrequency(this.getFrequency());
		clonedNodeNDS.setLeafChildren(this.getLeafChildren());
		clonedNodeNDS.setParent(this.getParent());
		clonedNodeNDS.setCFOverallProbability(this.getCFOverallProbability());
		clonedNodeNDS.setCFProbabilityBasedOnParent(this.getCFProbabilityBasedOnParent());
		clonedNodeNDS.setAFOverallProbability(this.getAFOverallProbability());
		clonedNodeNDS.setAFProbabilityBasedOnParent(this.getAFProbabilityBasedOnParent());
		clonedNodeNDS.setSuretyOfActivityFrequency(this.getSuretyOfActivityFrequency());
		clonedNodeNDS.setSuretyOfCaseFrequency(this.getSuretyOfCaseFrequency());
		clonedNodeNDS.setVariantWiseActivityFrequency(this.getVariantWiseActivityFrequency());
		clonedNodeNDS.setVariantWiseCaseFrequency(this.getVariantWiseCaseFrequency());
		FrequencyDetails PreviousFrequencyDetails = new FrequencyDetails(this);
		clonedNodeNDS.setPreviousFrequencyDetails(PreviousFrequencyDetails);
		return clonedNodeNDS;		
	}
	
	public Boolean isEqual(NodeDetailStore nodeNDS) {
		if(nodeNDS == null) {
			return false;
		}
		return this.getNode().getID() == nodeNDS.getNode().getID();
	}
}
