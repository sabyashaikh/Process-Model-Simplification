package org.processmining.logfiltering.visualizerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageDescriptor.OS;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.util.Pair;
import org.processmining.logfiltering.visConstants.ModelConstants;
import org.processmining.logfiltering.visFilteringAlgorithms.ApplyFilteringAlgorithm;
import org.processmining.logfiltering.visFilteringAlgorithms.FilteringAlgorithmFactory;
import org.processmining.logfiltering.visFilteringAlgorithms.FilteringAlgorithmParameters;
import org.processmining.logfiltering.visMiningAlgorithms.ApplyMiningAlgorithm;
import org.processmining.logfiltering.visMiningAlgorithms.MiningAlgorithmParameters;
import org.processmining.logfiltering.visMiningAlgorithms.MiningAlgorithmsFactory;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.etconformance.ETCResults;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.raffaeleconforti.efficientlog.XAttributeLiteralImpl;
import com.raffaeleconforti.efficientlog.XTraceImpl;

import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

/**
 * Model that is stored as ProM object and after that is shown by a custom visualizer
 * @author berti
 *
 */
public class FilteringVisualizerModel {
	// the plugin execution context
	PluginContext context;
	// the original log that is being filtered
	XLog originalLog;
	// the filtered log (for export and mining purposes)
	XLog filteredLog;
	// The Petrinet obtained by the algorithm
	Petrinet net;
	// ETC results
	ETCResults results;
	public Double fitness;
	public Double precision;
	 XAttributeMap eventAttributeMap;	 
	public int[] variantsFreq;
	XLog variantLog;

	public XLog reportTraceAttributesIntoEvents(XLog log) {
		for (XTrace trace : log) {
			XAttributeMap traceAttributeMap = trace.getAttributes();
			
			for (XEvent event : trace) {
				XAttributeMap eventAttributeMap = event.getAttributes();
				
				for (String traceAttribute : traceAttributeMap.keySet()) {
					if (!eventAttributeMap.containsKey(traceAttribute)) {
						//System.out.println("replicating attribute "+traceAttribute+" in trace "+trace.getAttributes().get("concept:name").toString()+" to event with hashcode "+event.hashCode());
						
						// we enter here if the trace attribute is not also an attribute of the event
						// (so we don't replicate concept:name that in trace means the case ID and in events means the activity)
						
						XAttribute traceAttributeValue = traceAttributeMap.get(traceAttribute);
						eventAttributeMap.put(traceAttribute, traceAttributeValue);
						
					}
				}
			}
		}

		return log;
	}
	
	public FilteringVisualizerModel(PluginContext context, XLog originalLog) {
		this.context = context;
		
		this.originalLog = originalLog;
		this.originalLog = reportTraceAttributesIntoEvents(this.originalLog);
		// at the start no filtering is applied
		this.filteredLog = originalLog;
		// Petri net has not been mined
		this.net = null;
		variantsFinder(originalLog);
		
		// apply the default algorithms at the end of this constructor
		this.filterLogAndGetPetrinet();
	}
	
	public XLog getOriginalLog() {
		return originalLog;
	}
	public void variantsFinder (XLog originalLog ) {
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
		HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
		HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
		HashMap<Integer, XTrace> VariantMapper =new HashMap<Integer, XTrace>();
		int chCount=0;
		 for (XTrace trace : originalLog) { // for each trace
			
			 /// Put trace to array
			 String[] Trace = new String[trace.size()];
				List<String> templist = new ArrayList<String>();
				for (XEvent event : trace) { 
					eventAttributeMap = event.getAttributes();
					templist.add(event.getAttributes().get(eventClassifier.getDefiningAttributeKeys()[0]).toString());
				}
				Trace = templist.toArray(new String[trace.size()]);
				String tr= Trace[0];
				
				for (int i =1; i < Trace.length; i++){
					tr= tr.concat("=>"+Trace[i]);
				}
				if (ActionMaper.get(tr)==null ){
					ActionMaper.put(tr,1);
					ReverseMapper.put(chCount, tr);
					HashMaper.put(tr, chCount);

					VariantMapper.put(chCount, trace);
					chCount++;
				}else{
					ActionMaper.put(tr, ActionMaper.get(tr)+1);
				}
		 }
		 int [] VariantFreq= new int[HashMaper.size()];
		 for (int i = 0; i < VariantFreq.length; i++) {
			 VariantFreq[i]= ActionMaper.get(ReverseMapper.get(i)); 
		 }
		 this.variantsFreq=VariantFreq;
		 XFactory factory = XFactoryRegistry.instance().currentDefault();
		 XLog TraceLog = factory.createLog();
		 TraceLog.setAttributes(originalLog.getAttributes());
		 TraceLog.setAttributes(originalLog.getAttributes());
		 XAttributeMapImpl case_map = new XAttributeMapImpl();
		String  case_id = String.valueOf(2000000);
		 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
		 XTraceImpl trace2 = new XTraceImpl(case_map);
		 TraceLog.add(trace2);	
		 for (int i = 0; i < VariantFreq.length; i++) {
			 
			 TraceLog.add(VariantMapper.get(i));
			
			// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
		}
		 this.variantLog=TraceLog;
	}
	public XLog getVariantLog() {
		return variantLog;
	}
	public int[] getVariantFreq() {
		return variantsFreq;
	}

	public void setOriginalLog(XLog originalLog) {
		this.originalLog = originalLog;
	}

	public XLog getFilteredLog() {
		return filteredLog;
	}

	public void setFilteredLog(XLog filteredLog) {
		this.filteredLog = filteredLog;
	}

	public Petrinet getNet() {
		return net;
	}

	public void setNet(Petrinet net) {
		this.net = net;
	}
	
	public void filterLogAndGetPetrinet() {
		// by default apply Alpha Miner
		MiningAlgorithmsFactory.algorithms miningAlgorithmDescription = ModelConstants.defaultMiningAlgorithm;
		filterLogAndGetPetrinet(miningAlgorithmDescription);
	}
	
	public void filterLogAndGetPetrinet(MiningAlgorithmsFactory.algorithms miningAlgorithmDescription) {
		// calls the method to do the mining allocating the default parameters
		MiningAlgorithmsFactory miningFactory = new MiningAlgorithmsFactory();
		ApplyMiningAlgorithm miningAlgorithm = miningFactory.getMiningAlgorithm(miningAlgorithmDescription);
		filterLogAndGetPetrinet(miningAlgorithm);
	}
	
	public void filterLogAndGetPetrinet(ApplyMiningAlgorithm miningAlgorithm) {
		FilteringAlgorithmFactory filteringFactory = new FilteringAlgorithmFactory();
		ApplyFilteringAlgorithm filteringAlgorithm = filteringFactory.getFilteringAlgorithm(ModelConstants.defaultFilteringMethod);
		filterLogAndGetPetrinet(miningAlgorithm, filteringAlgorithm);
	}
	
	public void filterLogAndGetPetrinet(ApplyMiningAlgorithm miningAlgorithm, ApplyFilteringAlgorithm filteringAlgorithm) {
		filterLogAndGetPetrinet(miningAlgorithm, filteringAlgorithm, null, null, "etconformance");
	}
	
	public void filterLogAndGetPetrinet(ApplyMiningAlgorithm miningAlgorithm, ApplyFilteringAlgorithm filteringAlgorithm, MiningAlgorithmParameters algorithmParameters, FilteringAlgorithmParameters filteringParameters, String precisionMeasurement) {
		// some tricks are used here to avoid duplicating code: if the parameters provided in the method are null then
		// the default options are applied :)
		if (filteringParameters != null) {
			System.out.println("filteringParameters != null");
			this.filteredLog = filteringAlgorithm.getFilteredLog(this.context, this.originalLog, filteringParameters);
		}
		else {
			System.out.println("filteringParameters == null");
			this.filteredLog = filteringAlgorithm.getFilteredLog(this.context, this.originalLog);
		}
		
		Pair<Petrinet, Marking> result;
		if (algorithmParameters != null) {
			result = miningAlgorithm.getPetrinet(this.context, this.filteredLog, algorithmParameters);
		}
		else {
			result = miningAlgorithm.getPetrinet(this.context, this.filteredLog);
		}
		this.net = result.getFirst();
		
		// some debug prints
		System.out.println("originalLog size = "+this.originalLog.size());
		System.out.println("filteredLog size = "+this.filteredLog.size());
		System.out.println("net places size = "+this.net.getPlaces().size());
		
		System.out.println("precisionMeasurement = "+precisionMeasurement);
		
		
		
			ApplyETConformance etConformanceAppl = new ApplyETConformance(this.context, this.originalLog, this.net);
			
			this.results = etConformanceAppl.getEtcResults();
			
			//System.out.println(this.results.getnNonFitTraces());
			//System.out.println(this.results.getEscTh());
			//System.out.println(this.results.getEtcp());
			
			System.out.println("old fitness: "+this.fitness);
			System.out.println("old precision: "+this.precision);
			
			this.fitness = 0.0;
			if (this.results.getNTraces() > 0) {
				this.fitness = (double)(this.results.getNTraces() - this.results.getnNonFitTraces())/(double)this.results.getNTraces();
			}
			
			this.precision = this.results.getEtcp();
			
			System.out.println("new fitness: "+this.fitness);
			System.out.println("new precision: "+this.precision);
		
			if (!precisionMeasurement.toLowerCase().equals("etconformance"))  {
			// add for alignments
				
				
				double[] AlignmentCosts= new double [this.variantsFreq.length+1];
				 double[] FitnessValues= new double [this.variantsFreq.length];
				 String[] ModelTrace= new String[this.variantsFreq.length];
				 int timeoutMilliseconds = 10 * 1000;
				 long preProcessTimeNanoseconds = 0;
				 Multiset<String> asynchronousMoveBag=TreeMultiset.create();
				 double AlignFit =0;
				 double AlignCost=0;
				 int LogSize= 0;
				 ///// Compute the actual alignment
				 int costUpperBound = Integer.MAX_VALUE;
				 XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
					XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
					TransEvClassMapping mapping = constructMapping(this.net, this.originalLog, dummyEvClass, eventClassifier);
					Marking initialMarking2 = getInitialMarking(this.net);
					Marking finalMarking2 = getFinalMarking(this.net);
					ReplayerParameters RepParameters = new ReplayerParameters.Default(3, costUpperBound, Debug.NONE);
					 XLogInfo summary = XLogInfoFactory.createLogInfo(this.originalLog, eventClassifier);
					XEventClasses classes = summary.getEventClasses();
					Future<TraceReplayTask>[] futures = new Future[this.variantLog.size()];
					Replayer replayer = new Replayer(RepParameters, this.net, initialMarking2, finalMarking2, classes, mapping, false);
					 ExecutorService service = Executors.newFixedThreadPool(RepParameters.nThreads);
					for (int i = 0; i < this.variantLog.size(); i++) {
				 		// Setup the trace replay task
				 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, this.variantLog.get(i), i, timeoutMilliseconds,
				 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

				 		// submit for execution
				 		futures[i] = service.submit(task);
				 	}
				 
				 for (int i = 0; i < this.variantLog.size(); i++) {

				 		TraceReplayTask result1;
				 		try {
				 			result1 = futures[i].get();
				 		} catch (Exception e) {
				 			// execution os the service has terminated.
				 			assert false;
				 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
				 		}
				 		SyncReplayResult replayResult = result1.getSuccesfulResult();
				 		 AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
				 		
				 			
				 	}
				 for (int i = 1; i < AlignmentCosts.length; i++) {
					 FitnessValues[i-1]= 1- ( AlignmentCosts[i]*1.0/ (AlignmentCosts[0]+this.variantLog.get(i).size() ));
					 AlignFit= AlignFit+ (FitnessValues[i-1]* this.variantsFreq[i-1]);
					 LogSize=LogSize+this.variantsFreq[i-1]; 
				}

			 		
				
				
				
				
			this.fitness = AlignFit/LogSize;
			//this.precision = 0.0;
		}
	}
	
	public void exportLog() {
		UIPluginContext context2 = (UIPluginContext) this.context;
		
		String actualAction = " ";
		PackageDescriptor pack = new PackageDescriptor(actualAction, actualAction, OS.ALL, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, actualAction, true, true, new ArrayList<String>(), new ArrayList<String>());
		
		PluginDescriptor descriptor = null;
		
		try {
			descriptor = new PluginDescriptorImpl2(FilteredLogExporterSS.class, context.getClass(), pack);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(this.originalLog.size());
		System.out.println(this.filteredLog.size());
		
		if (this.originalLog != this.filteredLog) {
			Object[] objects = new Object[1];
			
			objects[0] = this.filteredLog;
			
			context.invokePlugin(descriptor, 0, objects);
			
		    JOptionPane.showMessageDialog(new JFrame(), "Log exported! Check the 'all' items in your ProM workspace!", "Dialog",
		            JOptionPane.INFORMATION_MESSAGE);
		}
		else {
		    JOptionPane.showMessageDialog(new JFrame(), "Log are equals! No export happened!", "Dialog",
		            JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void exportPetrinet() {
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
		
		objects[0] = this.net;
		
		context.invokePlugin(descriptor, 0, objects);
		
	    JOptionPane.showMessageDialog(new JFrame(), "Exported Petri net! Check the 'all' items in your ProM workspace!", "Dialog",
	            JOptionPane.INFORMATION_MESSAGE);
	}
	private static Marking getInitialMarking(Petrinet net) {
		Marking initMarking = new Marking();

		for (org.processmining.models.graphbased.directed.petrinet.elements.Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	
	private static Marking getFinalMarking(Petrinet net) {
		Marking finalMarking = new Marking();

		for (org.processmining.models.graphbased.directed.petrinet.elements.Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;}
	
	private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClass dummyEvClass,
			XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			boolean mapped = false;

			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();
				String label = t.getLabel();
												
				if (label.equals(id)||label.equals(id.concat("+complete"))) {
					mapping.put(t, evClass);
					mapped = true;
					break;
				}
			}
		}
		return mapping;
	}
	
}
