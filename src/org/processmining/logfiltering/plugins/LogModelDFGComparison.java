package org.processmining.logfiltering.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.StochasticNet.TimeUnit;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.Semantics;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.stochasticpetrinet.StochasticNetUtils;
import org.processmining.plugins.stochasticpetrinet.simulator.PNSimulator;
import org.processmining.plugins.stochasticpetrinet.simulator.PNSimulatorConfig;

@Plugin(name = "Model LOG DFG Comparison", parameterLabels = { "Event Log", "Petri net", "Initial marking" }, returnLabels = {
"Simulated Event Log" }, returnTypes = { XLog.class })
public class LogModelDFGComparison {
	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0, 1,2 })
	public XLog generateLogFromPetrinet(UIPluginContext context, XLog inputLog, PetrinetGraph petriNet, Marking initialMarking) {
        Semantics<Marking, Transition> semantics = StochasticNetUtils.getSemantics(petriNet);
        HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
		HashMap<String, Integer> ModelDFMapper= new HashMap<String, Integer>();
		HashMap<String, Integer> LogDFMapper= new HashMap<String, Integer>();
		HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
		HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
        PNSimulator simulator = new PNSimulator();
        
		XAttributeMap logMap = new XAttributeMapImpl();
		int LogSize=0;
		int chCount=0;
		XLog log = new XLogImpl(logMap);
		XLog VariantModelog = new XLogImpl(logMap);
		XLog Outputlog = new XLogImpl(logMap);
		String DFRelation="";
		int MaxLength=0;
		for (XTrace trace : inputLog) { // for each trace of the log
			 LogSize++;
			 
			 /// Put trace to array
			 if(MaxLength<trace.size()) {
				 MaxLength=trace.size();
			 }
			 String[] Trace = new String[trace.size()];
				List<String> templist = new ArrayList<String>();
				for (XEvent event : trace) { 
					
					templist.add(event.getAttributes().get("concept:name").toString());
				}
				Trace = templist.toArray(new String[trace.size()]);
				String tr= "";
				for (int i =0; i < Trace.length; i++){
					tr= tr.concat(Trace[i]);
					if(i==0) {
						DFRelation="Start" + ">>"+Trace[i]; 
					}else {
						DFRelation =Trace[i-1] + ">>"+ Trace[i];
					}
					if(LogDFMapper.get(DFRelation)==null) {
						LogDFMapper.put(DFRelation, 1);
					}else {
						LogDFMapper.put(DFRelation, LogDFMapper.get(DFRelation)+1);
					}
				}
				DFRelation=Trace[Trace.length-1] +">>"+"End";
				if(LogDFMapper.get(DFRelation)==null) {
					LogDFMapper.put(DFRelation, 1);
				}else {
					LogDFMapper.put(DFRelation, LogDFMapper.get(DFRelation)+1);
				}
				if (ActionMaper.get(tr)==null ){
					ActionMaper.put(tr,1);
					ReverseMapper.put(chCount, tr);
					HashMaper.put(tr, chCount);
					chCount++;
					
				}else{
					ActionMaper.put(tr, ActionMaper.get(tr)+1);
				}
				
		 }
		ActionMaper= new HashMap<String, Integer>();
		 LogSize=0;
		 chCount=0;
		 int newVariants=0;
		 boolean NewVariantArivalFlage=true;
		 int SimulateLogSize=1000;
		while (NewVariantArivalFlage) {
			newVariants=0;
			NewVariantArivalFlage=false;
			PNSimulatorConfig config = new PNSimulatorConfig(SimulateLogSize,TimeUnit.MINUTES, (long) 1.0,1,MaxLength+1);
			//PNSimulatorConfig config = new PNSimulatorConfig(SimulateLogSize);
			SimulateLogSize=SimulateLogSize*2;
			 log = simulator.simulate(context, petriNet, semantics, config, initialMarking);
				for (XTrace trace : log) { // for each trace of the Model'sLog
					 LogSize++;
					 /// Put trace to array
					 String[] Trace = new String[trace.size()];
						List<String> templist = new ArrayList<String>();
						for (XEvent event : trace) { 
							
							templist.add(event.getAttributes().get("concept:name").toString());
						}
						Trace = templist.toArray(new String[trace.size()]);
						String tr= "";
						for (int i =0; i < Trace.length; i++){
							tr= tr.concat(Trace[i]+"==>");
							if(i==0) {
								DFRelation="Start" + ">>"+Trace[i]; 
							}else {
								DFRelation =Trace[i-1] + ">>"+ Trace[i];
							}
							if(ModelDFMapper.get(DFRelation)==null) {
								ModelDFMapper.put(DFRelation, 1);
							}else {
								ModelDFMapper.put(DFRelation, ModelDFMapper.get(DFRelation)+1);
							}
						}
						DFRelation=Trace[Trace.length-1] +">>"+"End";
						if(ModelDFMapper.get(DFRelation)==null) {
							ModelDFMapper.put(DFRelation, 1);
						}else {
							ModelDFMapper.put(DFRelation, ModelDFMapper.get(DFRelation)+1);
						}
						if (ActionMaper.get(tr)==null ){
							ActionMaper.put(tr,1);
							ReverseMapper.put(chCount, tr);
							HashMaper.put(tr, chCount);
							chCount++;
							VariantModelog.add(trace);
							newVariants++;
							
						}else{
							ActionMaper.put(tr, ActionMaper.get(tr)+1);
						}
						
				 }
				if ((1.0*newVariants/SimulateLogSize)>0.01)
					NewVariantArivalFlage=true;
		}
		 
		
        
		
	
		LogSize= ActionMaper.size();
		
		int k =MaxLength;
		double TotalModelBehav=0;
		double TotalLogBehav=0;
		double LogtoModelConflicts=0;
		double ModeltoLogConflict=0;
		ModelDFMapper =DFFinder(VariantModelog, k);
	    LogDFMapper= DFFinder(inputLog, k);
		chCount=0;
		LogSize=0;
		Iterator it = ModelDFMapper.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        String[] Segmehtsunits= pair.getKey().toString().split(">>");
	        TotalModelBehav= TotalModelBehav + (1.0 * 1/Segmehtsunits.length); //// here I consider  negative weight 2 for DF to have weight 1 the -1 should be removed.
	        if(LogDFMapper.get( pair.getKey())==null) {
	        	chCount++;
	        	ModeltoLogConflict=ModeltoLogConflict+(1.0 * 1/Segmehtsunits.length);
	        	System.out.println(pair.getKey() + " InModelnotInLog " + pair.getValue());
	        }
	    }
	    
	    it = LogDFMapper.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pair = (Map.Entry)it.next();
	        String[] Segmehtsunits= pair.getKey().toString().split(">>");
	        TotalLogBehav= TotalLogBehav +  (1.0 * LogDFMapper.get( pair.getKey())/Segmehtsunits.length);
	        if(ModelDFMapper.get( pair.getKey())==null) {
	        	LogSize++;
	        	LogtoModelConflicts= LogtoModelConflicts + (1.0 * LogDFMapper.get( pair.getKey())/Segmehtsunits.length);
	        	System.out.println(pair.getKey() + " InLogNotModel " + pair.getValue());
	        }   
	    }
	    
		LogSize= ModelDFMapper.size();
		LogSize= LogDFMapper.size();
		LogSize= ActionMaper.size();
		double Precision= 1-(ModeltoLogConflict  / TotalModelBehav);
		double Fitness= 1- (LogtoModelConflicts/ TotalLogBehav);
				
		return log;
	}
	
	HashMap<String, Integer> DFFinder (XLog log, int k){
		HashMap<String, Integer> Mapper= new HashMap<String, Integer>();
		for (XTrace trace : log) { // for each trace
			 String DFRelation="";
			 /// Put trace to array
			 String[] Trace = new String[trace.size()];
				List<String> templist = new ArrayList<String>();
				for (XEvent event : trace) { 
					
					templist.add(event.getAttributes().get("concept:name").toString());
				}
				Trace = templist.toArray(new String[trace.size()]);
				String tr= "";
			for ( int j =1; j<k;j++){ /// length of DF
				String [] Window= new String[j+1];
				if(Trace.length>j) {
					for (int i =0; i < Trace.length; i++){
						if(i<j) {
							for (int l = 0; l < Window.length-i-1; l++) {
								Window[l]="Start"+l;
							}
							for (int l = Window.length-i-1; l < Window.length; l++) {
								Window[l]=Trace[l-j+i];
							}
						}else {
							for (int l = Window.length-1; l >=0 ; l--) {
								Window[l]=Trace[i-l];
							}
						}
						DFRelation =Window[0];
						for (int l = 1; l < Window.length; l++) {
							DFRelation=DFRelation+">>"+Window[l];
						}
						if(Mapper.get(DFRelation)==null) {
							Mapper.put(DFRelation, 1);
						}else {
							Mapper.put(DFRelation, Mapper.get(DFRelation)+1);
						}
					}
					for (int i = 0; i < Window.length; i++) {
						Window[i]="End"+i;
					}
					for (int i = 0; i < Window.length; i++) {

						for(int l = 0; l<=i ;l++ ) {
							Window[l]=Trace[Trace.length-1-i+l];
						}
						DFRelation =Window[0];
						for (int l = 1; l < Window.length; l++) {
							DFRelation=DFRelation+">>"+Window[l];
						}
						if(Mapper.get(DFRelation)==null) {
							Mapper.put(DFRelation, 1);
						}else {
							Mapper.put(DFRelation, Mapper.get(DFRelation)+1);
						}
					}
					
				}
				
				DFRelation=Trace[Trace.length-1] +">>"+"End";
				if(Mapper.get(DFRelation)==null) {
					Mapper.put(DFRelation, 1);
				}else {
					Mapper.put(DFRelation, Mapper.get(DFRelation)+1);
				}
				
			}	
		 }
		return Mapper;
	}
	
}
