package org.processmining.logfiltering.visMiningAlgorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.hybridilpminer.parameters.LPFilter;
import org.processmining.hybridilpminer.parameters.LPFilterType;
import org.processmining.hybridilpminer.parameters.XLogHybridILPMinerParametersImpl;
import org.processmining.hybridilpminer.plugins.HybridILPMinerPlugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.splitMinerNewVersion.SplitMinerNewVersion;

public class ApplyILPMiner extends ApplyMiningAlgorithm {
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
		XLogHybridILPMinerParametersImpl params = new XLogHybridILPMinerParametersImpl(context, log, xEventClassifier);

		params.setFindSink(true);
		LPFilter filter = new LPFilter();
		filter.setThreshold(0.1);
		filter.setFilterType(LPFilterType.NONE);
		params.setFilter(filter);

		Object[] pnAndMarking = HybridILPMinerPlugin.applyFlexHeur(context, log, null, params);
		Petrinet net = (Petrinet) pnAndMarking[0];
		
		Marking marking =(Marking) pnAndMarking[1] ;

		return new Pair<Petrinet, Marking>(net, marking);
	}

	


}
