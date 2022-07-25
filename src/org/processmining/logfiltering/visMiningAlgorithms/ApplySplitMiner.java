package org.processmining.logfiltering.visMiningAlgorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.splitMinerNewVersion.SplitMinerNewVersion;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;

import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult.FilterType;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult.StructuringTime;

public class ApplySplitMiner extends ApplyMiningAlgorithm {
	@Override
	public Pair<Petrinet, Marking> getPetrinet(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		InductiveMinerParameters parameters = new InductiveMinerParameters();
		return getPetrinet(context, log, parameters);
	}
	
	@Override  
	public Pair<Petrinet, Marking> getPetrinet(PluginContext context, XLog log, MiningAlgorithmParameters parameters0) {
		// cast it to AlphaAlgorithmParameters (this because we must override a method with the exact signature of the father)
	
	
		System.out.println("org.processmining.logfiltering.algorithms.SplitMinerinProM.apply(XLog, MatrixFilterParameter)");
		XLog OutputLog = (XLog) log.clone();
		SplitMinerNewVersion miner = new SplitMinerNewVersion();
		// added the classifier to the arguments of mineBPMNModel
		XEventClassifier xEventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		BPMNDiagram bpmn = miner.mineBPMNModel(OutputLog,  xEventClassifier, 0.4,0.6, FilterType.WTH, false, true, false, StructuringTime.NONE);
		
	Object[] resultedModel =BPMNToPetriNetConverter.convert(bpmn);
	
		Petrinet net = (Petrinet) resultedModel[0];
		
		Marking marking =(Marking) resultedModel[1] ;
		return new Pair<Petrinet, Marking>(net, marking);
	}
}
