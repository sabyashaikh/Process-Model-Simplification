package org.processmining.logfiltering.visMiningAlgorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIMPartialTraces;

public class IMInternalParameters extends MiningParametersIMPartialTraces {
	public IMInternalParameters(XEventClassifier classifier) {
		this.setClassifier(classifier);
		this.setNoiseThreshold((float) 0.0);
		this.setUseMultithreading(true);
	}
}