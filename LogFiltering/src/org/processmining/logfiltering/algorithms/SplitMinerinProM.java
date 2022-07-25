package org.processmining.logfiltering.algorithms;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.models.graphbased.directed.bpmn.BPMNDiagram;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.splitMinerNewVersion.SplitMinerNewVersion;

import com.raffaeleconforti.conversion.bpmn.BPMNToPetriNetConverter;

import au.edu.qut.processmining.miners.splitminer.ui.dfgp.DFGPUIResult.FilterType;
import au.edu.qut.processmining.miners.splitminer.ui.miner.SplitMinerUIResult.StructuringTime;

public class SplitMinerinProM {

	public static Petrinet apply(XLog log, MatrixFilterParameter parameters) {
		// TODO Auto-generated method stub
			
			System.out.println("org.processmining.logfiltering.algorithms.SplitMinerinProM.apply(XLog, MatrixFilterParameter)");
			XLog OutputLog = (XLog) log.clone();
			SplitMinerNewVersion miner = new SplitMinerNewVersion();
			// added the classifier to the arguments of mineBPMNModel
			XEventClassifier xEventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
			BPMNDiagram bpmn = miner.mineBPMNModel(OutputLog,  xEventClassifier, parameters.getSecondDoubleVariable(),parameters.getProbabilityOfRemoval(), FilterType.WTH, false, true, false, StructuringTime.NONE);
			
		Object[] resultedModel =BPMNToPetriNetConverter.convert(bpmn);
		
			return (Petrinet) resultedModel[0];
		
		
	}
	public static Object[] apply2(XLog log, MatrixFilterParameter parameters) {
		// TODO Auto-generated method stub

		
			
			SplitMinerNewVersion miner = new SplitMinerNewVersion();
			// added the classifier to the arguments of mineBPMNModel
			XEventClassifier xEventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
			BPMNDiagram bpmn = miner.mineBPMNModel(log,  xEventClassifier, parameters.getSecondDoubleVariable(),parameters.getProbabilityOfRemoval(), FilterType.WTH, false, true, true, StructuringTime.NONE);
			Object[] resultedModel =BPMNToPetriNetConverter.convert(bpmn);
			
			return resultedModel;
		
		
	}
}