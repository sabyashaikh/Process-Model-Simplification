package org.processmining.logfiltering.visMiningAlgorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParametersForPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.mining.InductiveMiner;
import org.processmining.plugins.inductiveminer2.mining.MiningParameters;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerPlugin;

public class ApplyInductiveMiner extends ApplyMiningAlgorithm {
	@Override
	public Pair<Petrinet, Marking> getPetrinet(PluginContext context, XLog log) {
		// if no parameters are provided by the user, allocate the default parameters and use them
		InductiveMinerParameters parameters = new InductiveMinerParameters();
		return getPetrinet(context, log, parameters);
	}
	
	@Override  
	public Pair<Petrinet, Marking> getPetrinet(PluginContext context, XLog log, MiningAlgorithmParameters parameters0) {
		// cast it to AlphaAlgorithmParameters (this because we must override a method with the exact signature of the father)
		InductiveMinerParameters parameters = (InductiveMinerParameters) parameters0;
		
		System.out.println("InductiveMinerParameters");
		System.out.println(parameters.getParameters());
		
		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		IMInternalParameters2 param = new IMInternalParameters2(classifier);
		
		Double noiseThreshold = (Double)parameters.getParameter("noiseThreshold");
		if (noiseThreshold != null) {
			System.out.println("noiseThreshold");
			System.out.println(noiseThreshold);
			param.setNoiseThreshold2((float)(double)noiseThreshold);
		}
		
		InductiveMinerPlugin im = new InductiveMinerPlugin();
		IMLog imlog = param.getIMLog(log);
		EfficientTree tree = InductiveMiner.mineEfficientTree(imlog, (MiningParameters)param, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});
		AcceptingPetriNet petri = null;
		if (tree != null) {
			try {
				EfficientTreeReduce.reduce(tree, new EfficientTreeReduceParametersForPetriNet(false));
			} catch (UnknownTreeNodeException | ReductionFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			petri = EfficientTree2AcceptingPetriNet.convert(tree);
		}
		Canceller canceller = new Canceller() { public boolean isCancelled() { return false; } };
		ReduceAcceptingPetriNetKeepLanguage.reduce(petri, canceller);
		Petrinet net = petri.getNet();
		Marking marking = petri.getInitialMarking();

		return new Pair<Petrinet, Marking>(net, marking);
	}
}
