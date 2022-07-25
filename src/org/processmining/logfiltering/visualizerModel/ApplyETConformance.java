package org.processmining.logfiltering.visualizerModel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.petrinets.EvClassLogPetrinetConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.etconformance.ETCPlugin;
import org.processmining.plugins.etconformance.ETCResults;
import org.processmining.plugins.etconformance.ETCSettings;

public class ApplyETConformance {
	PluginContext context;
	XLog log;
	Petrinet net;
	Marking initialMarking;
	ETCResults resNew;
	
	public void discoverInitialMarking() {
		this.initialMarking = new Marking();
		
		for (Place place : this.net.getPlaces()) {
			if (this.net.getInEdges(place).size() == 0) {
				this.initialMarking.add(place);
			}
		}
	}
	
	public ApplyETConformance(PluginContext context, XLog log, Petrinet net) {
		this.context = context;
		this.log = log;
		this.net = net;
		discoverInitialMarking();
		
		System.out.println(this.initialMarking);
		
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		
		TransEvClassMapping mapping = constructMapping(net, log, dummyEvClass, eventClassifier);
		
		EvClassLogPetrinetConnection evClassLogPetrinetConnection = new EvClassLogPetrinetConnection("", net, log, eventClassifier, mapping);
		
		ETCResults res = new ETCResults();//Create the result object to store the settings on it
		ETCSettings sett = new ETCSettings(res);

		ETCPlugin etcPlugin = new ETCPlugin();
		Object[] etcResults = etcPlugin.doETC(context, log, net, initialMarking, mapping, res);
		
		this.resNew = (ETCResults) etcResults[0];
	}
	
	public ETCResults getEtcResults() {
		return this.resNew;
	}
	
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
