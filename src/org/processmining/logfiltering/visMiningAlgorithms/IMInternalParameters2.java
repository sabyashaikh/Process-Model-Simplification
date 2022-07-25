package org.processmining.logfiltering.visMiningAlgorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.plugins.inductiveminer2.variants.MiningParametersIMPartialTraces;

public class IMInternalParameters2 extends MiningParametersIMPartialTraces {
	public IMInternalParameters2(XEventClassifier classifier) {
		this.setClassifier(classifier);
		this.setNoiseThreshold((float) 0.0);
		this.setUseMultithreading(true);
	}
	
	public void setNoiseThreshold2(float value) {
		setNoiseThreshold(value);
	}
}