package org.processmining.logfiltering.visMiningAlgorithms;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.abstractions.AlphaClassicAbstraction;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

public class ApplyAlphaAlgorithm extends ApplyMiningAlgorithm {
	@Override
	public Pair<Petrinet, Marking> getPetrinet(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		AlphaAlgorithmParameters parameters = new AlphaAlgorithmParameters();
		return getPetrinet(context, log, parameters);
	}
	
	@Override
	public Pair<Petrinet, Marking> getPetrinet(PluginContext context, XLog log, MiningAlgorithmParameters parameters0) {
		// cast it to AlphaAlgorithmParameters (this because we must override a method with the exact signature of the father)
		AlphaAlgorithmParameters parameters = (AlphaAlgorithmParameters) parameters0;
		
		System.out.println("AlphaAlgorithmParameters");
		System.out.println(parameters.getParameters());
		
		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		
		// Alpha Miner algorithm itself has its own class of parameters
		// that does not coincide with the parameters we may provide to it
		AlphaMinerParameters param = new AlphaMinerParameters();
		param.setVersion(AlphaVersion.CLASSIC);

		AlphaMiner<XEventClass, ? extends AlphaClassicAbstraction<XEventClass>, ? extends AlphaMinerParameters> miner = AlphaMinerFactory
				.createAlphaMiner(context, log, classifier, param);
		return miner.run();
	}
}
