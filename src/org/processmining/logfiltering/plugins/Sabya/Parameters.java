package org.processmining.logfiltering.plugins.Sabya;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class Parameters {
	private XEventClassifier EventClassifier = new XEventNameClassifier();
	private Integer TotalTraces;
	private Integer UpdatedTotalTraces;
	private Map<String, String> EventCharacterMap;//Stores the Event to character mapping
	private Map<String, String> CharacterEventMap;//Store the Character to Event Mapping
	private Map<Integer, String> TraceIdMap;//Store the trace and unique id for it
	private Map<String, XEvent> ActivityEventMap;//Store the activity name and the event to which it belongs
	private Map<String, Integer> EventActivityFrequency;//total number of times an event is present in log
	private Map<String, Integer> EventCaseFrequency;//total number of times an event is present in log
	private Map<String, Integer> UpdatedEventActivityFrequency;//total number of times an event is present in log
	private Map<String, Integer> UpdatedEventCaseFrequency;//total number of times an event is present in log
	private Map<String, Integer> VariantFrequency;//Stores the total number of times a variant is present in log
	private Map<String, Map<String, Integer>> EventVariantMap;//Stores each event has occurred in how many variant
	private Map<String, List<Integer>> VariantTracesMap;//Stores each variant with respect to list of trace ids
	private Map<String, List<Integer>> VariantTracesMapUpdated;//Stores each variant with respect to list of trace ids
	private Map<Integer, String> TraceIdVariantMap;//Stores traceId with its variant
	private Map<String, List<Integer>> ActivityTracesMap;//Stores each activity and list of traces its available in
	private double Threshold;
	private Integer CharCounter = 65;
	private Map<String, Map<String, Integer>> EventWithActivityFrequency;
	private Map<String, Map<String, Integer>> EventWithCaseFrequency;
	private Map<String, Map<String, Integer>> UpdatedEventWithActivityFrequency;
	private Map<String, Map<String, Integer>> UpdatedEventWithCaseFrequency;
	private Map<String, String> VariantsToChange;
	private Map<XTrace, Integer> XTraceToTraceIdMap;//stores Xtrace  to id mapping
	private Map<Integer, XTrace> TraceIdToXTraceMap;//stores id  to Xtrace mapping
	private String FrequencyType;
	private String ProbabilityType;
	private Map<String, NodeDetailStore> DataStructureWithUpdatedActivities; 
	private Map<String, Integer> UpdatedVariantFrequency;//Stores the total number of times a variant is present in updatedlog
	//private FrequencyBasedProcessTreeParameters InitialValues;
	private String PlugIn;
	
	public Parameters( XEventClassifier eventClassifier, String frequencyType, String probabilityType, String plugIn) {
		super();
		this.EventClassifier = eventClassifier;
		this.Threshold = 0.0;
		this.TotalTraces = 0;	
		this.UpdatedTotalTraces = 0;	
		this.VariantFrequency = new LinkedHashMap<String, Integer>();
		this.EventVariantMap = new LinkedHashMap<String, Map<String, Integer>>();
		this.CharacterEventMap = new LinkedHashMap<String, String>();
		this.EventActivityFrequency = new LinkedHashMap<String, Integer>();
		this.EventCaseFrequency = new LinkedHashMap<String, Integer>();
		this.UpdatedEventActivityFrequency = new LinkedHashMap<String, Integer>();
		this.UpdatedEventCaseFrequency = new LinkedHashMap<String, Integer>();
		this.EventCharacterMap = new LinkedHashMap<String, String>();
		this.TraceIdMap = new LinkedHashMap<Integer, String>();
		this.VariantTracesMap = new LinkedHashMap<String, List<Integer>>();
		this.VariantTracesMapUpdated = new LinkedHashMap<String, List<Integer>>();
		this.ActivityTracesMap = new LinkedHashMap<String, List<Integer>>();
		this.EventWithActivityFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.EventWithCaseFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.UpdatedEventWithActivityFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.UpdatedEventWithCaseFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.ActivityEventMap = new LinkedHashMap<String, XEvent>();
		this.VariantsToChange = new LinkedHashMap<String, String>();
		this.XTraceToTraceIdMap = new LinkedHashMap<XTrace, Integer>();
		this.TraceIdToXTraceMap = new LinkedHashMap<Integer, XTrace>();
		this.TraceIdVariantMap = new LinkedHashMap<Integer, String>();
		this.FrequencyType = frequencyType;
		this.ProbabilityType = probabilityType; 
		this.DataStructureWithUpdatedActivities = new LinkedHashMap<String, NodeDetailStore>();
		this.UpdatedVariantFrequency = new LinkedHashMap<String, Integer>();
		this.PlugIn = plugIn;
		//this.InitialValues = new FrequencyBasedProcessTreeParameters(eventClassifier);
	}
	public Parameters( XEventClassifier eventClassifier, Double threshold, String frequencyType, String plugIn) {
		super();
		this.EventClassifier = eventClassifier;
		this.Threshold = threshold;
		this.TotalTraces = 0;	
		this.UpdatedTotalTraces = 0;	
		this.VariantFrequency = new LinkedHashMap<String, Integer>();
		this.EventVariantMap = new LinkedHashMap<String, Map<String, Integer>>();
		this.CharacterEventMap = new LinkedHashMap<String, String>();
		this.EventActivityFrequency = new LinkedHashMap<String, Integer>();
		this.EventCaseFrequency = new LinkedHashMap<String, Integer>();
		this.UpdatedEventActivityFrequency = new LinkedHashMap<String, Integer>();
		this.UpdatedEventCaseFrequency = new LinkedHashMap<String, Integer>();
		this.EventCharacterMap = new LinkedHashMap<String, String>();
		this.TraceIdMap = new LinkedHashMap<Integer, String>();
		this.VariantTracesMap = new LinkedHashMap<String, List<Integer>>();
		this.VariantTracesMapUpdated = new LinkedHashMap<String, List<Integer>>();
		this.ActivityTracesMap = new LinkedHashMap<String, List<Integer>>();
		this.EventWithActivityFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.EventWithCaseFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.UpdatedEventWithActivityFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.UpdatedEventWithCaseFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.ActivityEventMap = new LinkedHashMap<String, XEvent>();
		this.VariantsToChange = new LinkedHashMap<String, String>();
		this.XTraceToTraceIdMap = new LinkedHashMap<XTrace, Integer>();
		this.TraceIdToXTraceMap = new LinkedHashMap<Integer, XTrace>();
		this.TraceIdVariantMap = new LinkedHashMap<Integer, String>();
		this.FrequencyType = frequencyType;
		this.ProbabilityType = "Probability based on parent"; 
		this.DataStructureWithUpdatedActivities = new LinkedHashMap<String, NodeDetailStore>();
		this.UpdatedVariantFrequency = new LinkedHashMap<String, Integer>();
		this.PlugIn = plugIn;
	}
	public Map<String, Integer> cloneVariantWiseFrequency(Map<String, Integer> variantWiseFrequency) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(variantWiseFrequency);
		java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String, Integer>>(){}.getType();
		LinkedHashMap<String, Integer> clonedMap = gson.fromJson(jsonString, type); 
		return clonedMap;
	}
	public Map<String, Map<String, Integer>> cloneEventWithVariantWiseFrequency(Map<String, Map<String, Integer>> eventWithVariantWiseFrequency) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(eventWithVariantWiseFrequency);
		java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String, Map <String, Integer>>>(){}.getType();
		LinkedHashMap<String, Map <String, Integer>> clonedMap = gson.fromJson(jsonString, type); 
		return clonedMap;
	}
	public XEventClassifier getEventClassifier() {
		return this.EventClassifier;
	}

	public void setEventClassifier(XEventClassifier eventClassifier) {
		this.EventClassifier = eventClassifier;
	}

	public String getFrequencyType() {
		return this.FrequencyType;
	}

	public void setFrequencyType(String frequencyType) {
		this.FrequencyType = frequencyType;
	}
	
	public String getPlugIn() {
		return this.PlugIn;
	}

	public void setPlugIn(String plugIn) {
		this.PlugIn = plugIn;
	}
	
	public String getProbabilityType() {
		return this.ProbabilityType;
	}

	public void setProbabilityType(String probabilityType) {
		this.ProbabilityType = probabilityType;
	}

	public Integer getTotalTraces() {
		return this.TotalTraces;
	}
	public void setTotalTraces(Integer totalTraces) {
		this.TotalTraces= totalTraces;
	}
	
	public Integer getUpdatedTotalTraces() {
		return this.UpdatedTotalTraces;
	}
	public void setUpdatedTotalTraces(Integer updatedTotalTraces) {
		this.UpdatedTotalTraces= updatedTotalTraces;
	}
	
	
	public Integer getCharCounter() {
		return this.CharCounter;
	}
	
	public void setCharCounter(Integer charCounter) {
		this.CharCounter = charCounter;
	}
	
	public void incrementTotalTraces() {
		this.TotalTraces = TotalTraces+1;
	}
	
	public double getThreshold() {
		return this.Threshold;
	}
	
	public void setThreshold(double threshold) {
		this.Threshold = threshold;
	}
	
	public Map<String, String> getEventCharacterMap() {
		return this.EventCharacterMap;
	}
	
	public void setEventCharacterMap(Map<String, String> eventCharacterMap) {
		this.EventCharacterMap = eventCharacterMap;
	}
	
	public Map<String, String> getVariantsToChange() {
		return this.VariantsToChange;
	}
	
	public void setVariantsToChange(Map<String, String> VariantsToChange) {
		this.VariantsToChange = VariantsToChange;
	}
	
	public Map<String, String> getCharacterEventMap() {
		return this.CharacterEventMap;
	}
	
	public void setCharacterEventMap(Map<String, String> characterEventMap) {
		this.CharacterEventMap = characterEventMap;
	}
	
	public Map<String, Integer> getEventActivityFrequency() {
		return cloneVariantWiseFrequency(this.EventActivityFrequency);
	}
	
	public void setEventActivityFrequency(Map<String, Integer> eventActivityFrequency) {
		this.EventActivityFrequency = cloneVariantWiseFrequency(eventActivityFrequency);
	}
	
	public Map<String, Integer> getEventCaseFrequency() {
		return cloneVariantWiseFrequency(this.EventCaseFrequency);
	}
	
	public void setEventCaseFrequency(Map<String, Integer> eventCaseFrequency) {
		this.EventCaseFrequency = cloneVariantWiseFrequency(eventCaseFrequency);
	}
	
	public Map<String, Integer> getUpdatedEventActivityFrequency() {
		return cloneVariantWiseFrequency(this.UpdatedEventActivityFrequency);
	}
	
	public void setUpdatedEventActivityFrequency(Map<String, Integer> updatedEventActivityFrequency) {
		this.UpdatedEventActivityFrequency = cloneVariantWiseFrequency(updatedEventActivityFrequency);
	}
	
	public Map<String, Integer> getUpdatedEventCaseFrequency() {
		return cloneVariantWiseFrequency(this.UpdatedEventCaseFrequency);
	}
	
	public void setUpdatedEventCaseFrequency(Map<String, Integer> updatedEventCaseFrequency) {
		this.UpdatedEventCaseFrequency = cloneVariantWiseFrequency(updatedEventCaseFrequency);
	}
	
	
	public Map<String, Integer> getVariantFrequency() {
		return cloneVariantWiseFrequency(this.VariantFrequency);
	}
	
	public void setVariantFrequency(Map<String, Integer> variantFrequency) {
		this.VariantFrequency = cloneVariantWiseFrequency(variantFrequency);
	}
	
	
	public Map<String, Integer> getUpdatedVariantFrequency() {
		return this.UpdatedVariantFrequency;
	}
	
	public void setUpdatedVariantFrequency(Map<String, Integer> updatedVariantFrequency) {
		UpdatedVariantFrequency = cloneVariantWiseFrequency(updatedVariantFrequency);
	}
	
	public Map<String, XEvent> getActivityEventMap() {
		return this.ActivityEventMap;
	}
	
	public void setActivityEventMap(Map<String, XEvent> activityEventMap) {
		this.ActivityEventMap = activityEventMap;
	}
	
	public Map<XTrace, Integer> getXTraceToTraceIdMap() {
		return this.XTraceToTraceIdMap;
	}
	
	public void setXTracetoTraceIdMap(Map<XTrace, Integer> xTraceToTraceIdMap) {
		this.XTraceToTraceIdMap = xTraceToTraceIdMap;
	}
	
	public Map<Integer, XTrace> getTraceIdToXTraceMap() {
		return this.TraceIdToXTraceMap;
	}
	
	public void setTraceIdToXTraceMapp(Map<Integer, XTrace> traceIdToXTraceMap) {
		this.TraceIdToXTraceMap = traceIdToXTraceMap;
	}
	
	public Map<Integer, String> getTraceIdMap() {
		return this.TraceIdMap;
	}
	
	public void setTraceIdMap(Map<Integer, String> traceIdMap) {
		this.TraceIdMap = traceIdMap;
	}
	
	public Map<Integer, String> getTraceIdVariantMap() {
		return this.TraceIdVariantMap;
	}
	
	public void setTraceIdVariantMap(Map<Integer, String> traceIdVariantMap) {
		this.TraceIdVariantMap = traceIdVariantMap;
	}
	
	
	public Map<String, Map<String, Integer>> getEventVariantMap() {
		return this.EventVariantMap;
	}
	
	public void setEventVariantMap(Map<String, Map<String, Integer>> eventVariantMap) {
		this.EventVariantMap = eventVariantMap;
	}
	
	public Map<String, List<Integer>> getVariantTracesMap() {
		return this.VariantTracesMap;
	}
	
	public void setVariantTracesMap(Map<String, List<Integer>> variantTracesMap) {
		this.VariantTracesMap = variantTracesMap;
	}

	public Map<String, List<Integer>> getVariantTracesMapUpdated() {
		return this.VariantTracesMapUpdated;
	}
	
	public void setVariantTracesMapUpdated(Map<String, List<Integer>> variantTracesMapUpdated) {
		this.VariantTracesMapUpdated = variantTracesMapUpdated;
	}
	
	public Map<String, List<Integer>> getActivityTracesMap() {
		return this.ActivityTracesMap;
	}
	
	public void setActivityTracesMap(Map<String, List<Integer>> activityTracesMap) {
		this.ActivityTracesMap = activityTracesMap;
	}

	public Integer getActivityFreqOfEventForVariant(String activity, String variant) {
		Map<String, Integer> variantWithActivityFrequency = EventWithActivityFrequency.get(activity);
		return variantWithActivityFrequency.get(variant);
	}
	
	public void incrementFreqOfEventForVariant(String activity, String variant, Integer traceFreqForVariant) {
		if(EventWithActivityFrequency.containsKey(activity)){
			Map<String, Integer> variantWithActivityFrequency = EventWithActivityFrequency.get(activity);
			if(variantWithActivityFrequency.containsKey(variant)) {
				Integer activityFrequency = variantWithActivityFrequency.get(variant);
				variantWithActivityFrequency.put(variant, activityFrequency+traceFreqForVariant);
			}
			else {
				variantWithActivityFrequency.put(variant,traceFreqForVariant);
				//System.out.println("putting in case frequency for activity:"+ activity+ " the variant "+ variant+ "value:"+ variantWithActivityFrequency.get(variant));
				Map<String, Integer> variantWithCaseFrequency = EventWithCaseFrequency.get(activity);
				variantWithCaseFrequency.put(variant,traceFreqForVariant);
				EventWithCaseFrequency.put(activity, variantWithCaseFrequency);
			}
			EventWithActivityFrequency.put(activity,variantWithActivityFrequency);
		}
		else {
			Map<String, Integer> variantWithActivityFrequency = new LinkedHashMap<String, Integer>();
			Map<String, Integer> variantWithCaseFrequency = new LinkedHashMap<String, Integer>();
			variantWithActivityFrequency.put(variant, traceFreqForVariant);
			variantWithCaseFrequency.put(variant, traceFreqForVariant);
			//System.out.println("putting in case frequency for activity:"+ activity+ " the variant "+ variant+ "value:"+ variantWithActivityFrequency.get(variant));
			EventWithActivityFrequency.put(activity, variantWithActivityFrequency);
			EventWithCaseFrequency.put(activity, variantWithCaseFrequency);
		}
	}
	
	public Integer getCaseFreqOfEventForVariant(String activity, String variant) {
		Map<String, Integer> variantWithCaseFrequency = EventWithCaseFrequency.get(activity);
		return variantWithCaseFrequency.get(variant);
	}
	
	public Map<String, Map<String, Integer>>  getEventWithVariantWiseActivityFrequency() {
		return EventWithActivityFrequency;
	}
	
	public Map<String, Map<String, Integer>> getEventWithVariantWiseCaseFrequency() {
		return EventWithCaseFrequency;
	}
	
	public Map<String, Map<String, Integer>>  getUpdatedEventWithVariantWiseActivityFrequency() {
		return UpdatedEventWithActivityFrequency;
	}
	
	public Map<String, Map<String, Integer>> getUpdatedEventWithVariantWiseCaseFrequency() {
		return UpdatedEventWithCaseFrequency;
	}
	
	public void setEventWithVariantWiseActivityFrequency(Map<String, Map<String, Integer>> eventWithActivityFrequency) {
		this.EventWithActivityFrequency = eventWithActivityFrequency;
	}
	
	public void setEventWithVariantWiseCaseFrequency(Map<String, Map<String, Integer>> eventWithCaseFrequency) {
		this.EventWithCaseFrequency = eventWithCaseFrequency;
	}
	
	public void setUpdatedEventWithVariantWiseActivityFrequency(Map<String, Map<String, Integer>> eventWithActivityFrequency) {
		this.UpdatedEventWithActivityFrequency = cloneEventWithVariantWiseFrequency(eventWithActivityFrequency);
	}
	
	public void setUpdatedEventWithVariantWiseCaseFrequency(Map<String, Map<String, Integer>> eventWithCaseFrequency) {
		this.UpdatedEventWithCaseFrequency = cloneEventWithVariantWiseFrequency(eventWithCaseFrequency);
	}
	
	public void resetData() {
		this.CharCounter = 65;
		this.TotalTraces = 0;	
		this.VariantFrequency = new LinkedHashMap<String, Integer>();
		this.EventVariantMap = new LinkedHashMap<String, Map<String, Integer>>();
		this.CharacterEventMap = new LinkedHashMap<String, String>();
		this.EventActivityFrequency = new LinkedHashMap<String, Integer>();
		this.EventCaseFrequency = new LinkedHashMap<String, Integer>();
		this.EventCharacterMap = new LinkedHashMap<String, String>();
		this.UpdatedEventActivityFrequency = new LinkedHashMap<String, Integer>();
		this.UpdatedEventCaseFrequency = new LinkedHashMap<String, Integer>();
		this.TraceIdMap = new LinkedHashMap<Integer, String>();
		this.VariantTracesMap = new LinkedHashMap<String, List<Integer>>();
		this.ActivityTracesMap = new LinkedHashMap<String, List<Integer>>();
		this.EventWithActivityFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.EventWithCaseFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.UpdatedEventWithActivityFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.UpdatedEventWithCaseFrequency = new LinkedHashMap<String, Map<String, Integer>>();
		this.ActivityEventMap = new LinkedHashMap<String, XEvent>();
	}
	
	public Map<String, NodeDetailStore> getDataStructureWithUpdatedActivities() {
		return this.DataStructureWithUpdatedActivities;
	}
	
	public void setDataStructureWithUpdatedActivities(Map<String, NodeDetailStore> dataStructureWithUpdatedActivities) {
		this.DataStructureWithUpdatedActivities = dataStructureWithUpdatedActivities;
	}
	
}

