package org.processmining.logfiltering.visMiningAlgorithms;

import java.util.HashMap;
import java.util.Map;

public class MiningAlgorithmsFactory {
	public static enum algorithms { ALPHA, INDUCTIVE, SPLIT ,ILP};
	public Map<String, MiningAlgorithmsFactory.algorithms> algorithmsLabels = new HashMap<String, MiningAlgorithmsFactory.algorithms>();
	
	public MiningAlgorithmsFactory() {
		algorithmsLabels = new HashMap<String, MiningAlgorithmsFactory.algorithms>();
		// add the labels for each algorithm
		algorithmsLabels.put("Alpha Miner", MiningAlgorithmsFactory.algorithms.ALPHA);
		algorithmsLabels.put("Inductive Miner", MiningAlgorithmsFactory.algorithms.INDUCTIVE);
		algorithmsLabels.put("Split Miner", MiningAlgorithmsFactory.algorithms.SPLIT);
		algorithmsLabels.put("ILP Miner", MiningAlgorithmsFactory.algorithms.ILP);
	}
	
	public ApplyMiningAlgorithm getMiningAlgorithm(MiningAlgorithmsFactory.algorithms miningAlgorithmDescription) {
		if (miningAlgorithmDescription.equals(algorithms.ALPHA)) {
			return new ApplyAlphaAlgorithm();
		}
		else if (miningAlgorithmDescription.equals(algorithms.INDUCTIVE)) {
			return new ApplyInductiveMiner();
		}
		else if (miningAlgorithmDescription.equals(algorithms.SPLIT)) {
			return new ApplySplitMiner();
		}
		else if (miningAlgorithmDescription.equals(algorithms.ILP)) {
			return new ApplySplitMiner();
		}
		
	
		return null;
	}
}
