package org.processmining.logfiltering.algorithms;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayer.matchinstances.PNLogMatchInstancesReplayer;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.AllOptAlignmentsGraphILPAlg;
import org.processmining.plugins.petrinet.replayer.matchinstances.algorithms.express.AllOptAlignmentsTreeAlg;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import com.raffaeleconforti.context.FakePluginContext;

import nl.tue.astar.AStarException;


public class EvaluationTest 
{
	public TransEvClassMapping mapping = null;
	public Marking initMarking = null;
	public Marking finalMarking = null;
	public static int iteration = 0;
	public PNMatchInstancesRepResult replayResult;
	public PNMatchInstancesRepResult resRepresentative;
	public PNMatchInstancesRepResult resTree;
	public PNRepResult resOneOptimal;
	public long timePerformanceRepresentative;
	public long timePreparations;
	public long timePerformanceTree;
	public long timePerformanceOneOptimal;
	public boolean timeout = false;
	public double FFitness= 0;
	static {
		try {
			System.loadLibrary("lpsolve55");
			System.loadLibrary("lpsolve55j");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public EvaluationTest(XLog xLog, Petrinet net, int stateLimit,Marking iMarking,Marking fMarking, TransEvClassMapping Mapp) throws AStarException
	{
		long start = System.currentTimeMillis();
 // only one marking is used so far
		Map<Transition, Integer> costMOS = null; // movements on system
		Map<XEventClass, Integer> costMOT = null; // movements on trace
		/*initMarking = getInitialMarking(net);
		finalMarking = getFinalMarkings(net);*/
		initMarking= iMarking;
		finalMarking=fMarking;
		costMOS = constructMOSCostFunction(net);
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
		XEventClasses classes = XEventClasses.deriveEventClasses(XLogInfoImpl.NAME_CLASSIFIER, xLog);
		costMOT = constructMOTCostFunction(net, xLog, classes, dummyEvClass);
		//mapping = this.constructMapping(net, xLog, classes, eventClassifier, dummyEvClass);
		mapping= Mapp;
		PNLogMatchInstancesReplayer repEngine = new PNLogMatchInstancesReplayer();
		Object[] parameters = new Object[3];
		parameters[0] = costMOS;
		parameters[2] = costMOT;
		parameters[1] = stateLimit;
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog emptyLog = factory.createLog();
		XTrace emptyTrace = factory.createTrace();
		emptyLog.add(emptyTrace);
		CostBasedCompleteParam ipnPars = new CostBasedCompleteParam(costMOT, costMOS);
		ipnPars.setFinalMarkings(fMarking);
		IPNReplayParameter parameters2 = new CostBasedCompleteParam(costMOT, costMOS);
		parameters2.setInitialMarking(iMarking);
		parameters2.setFinalMarkings(fMarking);
		parameters2.setGUIMode(false);
		parameters2.setCreateConn(false);
		parameters2.setNumThreads(6);
		

		PetrinetReplayerWithILP replayEngine2 = new PetrinetReplayerWithILP (true,true);
		PNRepResult result = replayEngine2.replayLog(new FakePluginContext(), net, xLog, mapping, parameters2);
		FFitness = (double)result.getInfo().get("Trace Fitness");
		//this.resOneOptimal = new PetrinetReplayerWithILP().replayLog(new FakePluginContext(), net, xLog, mapping, ipnPars);//replayer.replayLog(new FakePluginContext(), net, xLog, mapping, new CostBasedCompletePruneAlg(), ipnPars);
		//FFitness = (double) resOneOptimal.getInfo().get("Trace Fitness");
				//replayResult = repEngine.replayLog(new FakePluginContext(), net, emptyLog, mapping, initMarking, finalMarking, new AllOptAlignmentsGraphAlg(), parameters);

		try {
//replayResult = repEngine.replayLog(new FakePluginContext(), net, xLog, mapping, initMarking, finalMarking, new AllOptAlignmentsGraphAlg(), parameters);
			
			long end = System.currentTimeMillis();
			this.timePreparations = end - start;
		
 			replayResult = repEngine.replayLog(new FakePluginContext(), net, xLog, mapping, initMarking, finalMarking, new AllOptAlignmentsGraphILPAlg(), parameters);
			double minModelMoves = replayResult.first().getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST);			//resRepresentative=replayResult;
			this.resRepresentative = new PNMatchInstancesRepResult(new TreeSet<AllSyncReplayResult>());

			double time = 0;
			double rawFitnessCost = 0;
			double numStates = 0;
			double numAlignments = 0;
			double traceFitness = 0;
			double moveModelFitness = 0;
			double moveLogFitness = 0;
			double traceLength = 0;
			double queuedStates = 0;
			double logSize =  xLog.size();
			for (AllSyncReplayResult res : resRepresentative) {
				time += res.getInfo().get(PNMatchInstancesRepResult.TIME) * res.getTraceIndex().size();
				rawFitnessCost += ( res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) * res.getTraceIndex().size());
				numStates += res.getInfo().get(PNMatchInstancesRepResult.NUMSTATES) * res.getTraceIndex().size();
				numAlignments += res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS);
				res.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, 1 - res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / (res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + minModelMoves));
				DoubleArrayList moveLogFitnessList = new DoubleArrayList();
				DoubleArrayList moveModelFitnessList = new DoubleArrayList();
				for(List<StepTypes> alignment : res.getStepTypesLst())
				{
					double movesOnLog = 0;
					double movesOnModel = 0;
					double movesMatching = 0;
					for(StepTypes step : alignment)
					{
						if(step==StepTypes.L) movesOnLog++;
						else if(step==StepTypes.MREAL) movesOnModel++;
						else if(step==StepTypes.LMGOOD) movesMatching++;
					}
					moveLogFitnessList.add(1 - movesOnLog / (movesOnLog + movesMatching));
					moveModelFitnessList.add(1- movesOnModel / (movesOnModel + movesMatching));
				}
				res.addInfo(PNRepResult.MOVEMODELFITNESS, moveModelFitnessList.average());
				res.addInfo(PNRepResult.MOVELOGFITNESS, moveLogFitnessList.average());
				traceFitness += res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * res.getTraceIndex().size();
				moveModelFitness += (res.getInfo().get(PNRepResult.MOVEMODELFITNESS) * res.getTraceIndex().size());
				moveLogFitness += (res.getInfo().get(PNRepResult.MOVELOGFITNESS) * res.getTraceIndex().size());
				traceLength += (res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) * res.getTraceIndex().size());
				queuedStates += res.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) * res.getTraceIndex().size();
			}
			
			resRepresentative.addInfo(PNMatchInstancesRepResult.TIME, "" + (time / logSize));
			resRepresentative.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, "" + (rawFitnessCost / logSize));
			resRepresentative.addInfo(PNMatchInstancesRepResult.NUMSTATES, "" + (numStates / logSize));
			resRepresentative.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + numAlignments);
			resRepresentative.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" + (traceFitness / logSize));
			resRepresentative.addInfo(PNRepResult.MOVEMODELFITNESS, "" + (moveModelFitness  / logSize));
			resRepresentative.addInfo(PNRepResult.MOVELOGFITNESS, "" + (moveLogFitness / logSize));
			resRepresentative.addInfo(PNRepResult.ORIGTRACELENGTH, "" + traceLength / logSize);
			resRepresentative.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "" + queuedStates / logSize);
			this.timePerformanceRepresentative = System.currentTimeMillis() - end;
			//System.out.println("Representatives done");
			
			ipnPars.setInitialMarking(initMarking);
			ipnPars.setFinalMarkings(finalMarking);
			ipnPars.setGUIMode(false);
			ipnPars.setCreateConn(false);
			ipnPars.setNumThreads(1);
			ipnPars.setMaxNumOfStates(stateLimit);
			start = System.currentTimeMillis();
			//this.resOneOptimal = new PNRepResult(new TreeSet<SyncReplayResult>());
			this.resOneOptimal = new PetrinetReplayerWithILP().replayLog(new FakePluginContext(), net, xLog, mapping, ipnPars);//replayer.replayLog(new FakePluginContext(), net, xLog, mapping, new CostBasedCompletePruneAlg(), ipnPars);
			//this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + this.resOneOptimal.size());
			System.out.println("FINISHEEEEEED");
		} catch (Exception ex) {
			// TODO: handle exception
			ex.printStackTrace();
		}
		
/*		
*/		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> control = executor.submit(new OneAlignmentJob(net, xLog, mapping, ipnPars, this.resOneOptimal));
		 try 
		{
			control.get(2, TimeUnit.MINUTES);
		} catch (InterruptedException exp) {
			executor.shutdownNow();
			 this.timeout = true;
			this.timePerformanceTree = 300000;
			System.out.println("No worries!");
		} catch (TimeoutException ex) {
			// 5 minutes expired, we cancel the job !!!
			control.cancel(true);
			executor.shutdownNow();
			this.timePerformanceOneOptimal=300000;
			this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + this.resOneOptimal.size());
			System.out.println("Timeout for One Opt trace Alignment");
		}  catch (ExecutionException exp2) {
			
		}
		//System.out.println(System.currentTimeMillis() - start);
		 if(this.timePerformanceOneOptimal!=300000)
			 this.timePerformanceOneOptimal=System.currentTimeMillis() - start;
		executor.shutdownNow();

	}
	
	public static class TreeAlignmentJob implements Callable<String> 
	{

        private Petrinet net;
        private XLog xLog;
        private TransEvClassMapping mapping;
        private Marking initMarking;
        private Marking finalMarking;
        private Object[] parameters;
        private PNMatchInstancesRepResult resTree;
        private double minModelMoves;
        
        public TreeAlignmentJob(Petrinet net, XLog xLog, TransEvClassMapping mapping, Marking initMarking, Marking finalMarking, Object[] parameters, PNMatchInstancesRepResult resTree, double minModelMoves)
        {
        	this.net = net;
        	this.xLog = xLog;
        	this.mapping = mapping;
        	this.initMarking = initMarking;
        	this.finalMarking = finalMarking;
        	this.parameters = parameters;
        	this.resTree = resTree;
        	this.minModelMoves = minModelMoves;
        }
        
		@Override
        public String call() throws Exception {
        	PNLogMatchInstancesReplayer repEngine = new PNLogMatchInstancesReplayer();
        	PNMatchInstancesRepResult tmpRes;
        	tmpRes= repEngine.replayLog(new FakePluginContext(), net, xLog, mapping, initMarking, finalMarking, new AllOptAlignmentsTreeAlg(), parameters);
    		double time = 0;
    		double rawFitnessCost = 0;
    		double numStates = 0;
    		double numAlignments = 0;
    		double traceFitness = 0;
    		double moveModelFitness = 0;
    		double moveLogFitness = 0;
    		double traceLength = 0;
    		double queuedStates = 0;
    		double logSize =  xLog.size();
    		for (AllSyncReplayResult res : tmpRes) {
    			time += res.getInfo().get(PNMatchInstancesRepResult.TIME) * res.getTraceIndex().size();
    			rawFitnessCost += ( res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) * res.getTraceIndex().size());
    			numStates += res.getInfo().get(PNMatchInstancesRepResult.NUMSTATES) * res.getTraceIndex().size();
    			numAlignments += res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS);
    			res.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, 1 - res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / (res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + minModelMoves));
    			DoubleArrayList moveLogFitnessList = new DoubleArrayList();
    			DoubleArrayList moveModelFitnessList = new DoubleArrayList();
    			for(List<StepTypes> alignment : res.getStepTypesLst())
    			{
    				double movesOnLog = 0;
    				double movesOnModel = 0;
    				double movesMatching = 0;
    				for(StepTypes step : alignment)
    				{
    					if(step==StepTypes.L) movesOnLog++;
    					else if(step==StepTypes.MREAL) movesOnModel++;
    					else if(step==StepTypes.LMGOOD) movesMatching++;
    				}
    				moveLogFitnessList.add(1 - movesOnLog / (movesOnLog + movesMatching));
    				moveModelFitnessList.add(1- movesOnModel / (movesOnModel + movesMatching));
    			}
    			res.addInfo(PNRepResult.MOVEMODELFITNESS, moveModelFitnessList.average());
    			res.addInfo(PNRepResult.MOVELOGFITNESS, moveLogFitnessList.average());
    			traceFitness += res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * res.getTraceIndex().size();
    			moveModelFitness += (res.getInfo().get(PNRepResult.MOVEMODELFITNESS) * res.getTraceIndex().size());
    			moveLogFitness += (res.getInfo().get(PNRepResult.MOVELOGFITNESS) * res.getTraceIndex().size());
    			traceLength += (res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) * res.getTraceIndex().size());
    			queuedStates += res.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) * res.getTraceIndex().size();
    			this.resTree.add(res);
    		}
    		
    		resTree.addInfo(PNMatchInstancesRepResult.TIME, "" + (time / logSize));
    		resTree.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, "" + (rawFitnessCost / logSize));
    		resTree.addInfo(PNMatchInstancesRepResult.NUMSTATES, "" + (numStates / logSize));
    		resTree.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + numAlignments);
    		resTree.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" + (traceFitness / logSize));
    		resTree.addInfo(PNRepResult.MOVEMODELFITNESS, "" + (moveModelFitness  / logSize));
    		resTree.addInfo(PNRepResult.MOVELOGFITNESS, "" + (moveLogFitness / logSize));
    		resTree.addInfo(PNRepResult.ORIGTRACELENGTH, "" + traceLength / logSize);
    		resTree.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "" + queuedStates / logSize);
            return "result";
        }
	}
	
	public static class OneAlignmentJob implements Callable<String> 
	{

        private Petrinet net;
        private XLog xLog;
        private TransEvClassMapping mapping;
        private CostBasedCompleteParam ipnPars;
        private PNRepResult resOneOptimal;
        
        public OneAlignmentJob(Petrinet net, XLog xLog, TransEvClassMapping mapping, CostBasedCompleteParam ipnPars, PNRepResult resOneOptimal)
        {
        	this.net = net;
        	this.xLog = xLog;
        	this.mapping = mapping;
        	this.ipnPars = ipnPars;
        	this.resOneOptimal = resOneOptimal;
        }
        
		@Override
        public String call() throws Exception {
        	PNRepResult tmp = new PetrinetReplayerWithILP().replayLog(new FakePluginContext(), net, xLog, mapping, ipnPars);
        	
			this.resOneOptimal.addAll(tmp);
			this.resOneOptimal.setInfo(tmp.getInfo());
			this.resOneOptimal.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + this.resOneOptimal.size());
    		
            return "result";
        }
	}
	
	public static PetrinetGraph constructNet(String netFile) {
		PNMLSerializer PNML = new PNMLSerializer();
		NetSystem sys = PNML.parse(netFile);

		//System.err.println(sys.getMarkedPlaces());

		//		int pi, ti;
		//		pi = ti = 1;
		//		for (org.jbpt.petri.Place p : sys.getPlaces())
		//			p.setName("p" + pi++);
		//		for (org.jbpt.petri.Transition t : sys.getTransitions())
		//				t.setName("t" + ti++);

		PetrinetGraph net = PetrinetFactory.newPetrinet(netFile);

		// places
		Map<org.jbpt.petri.Place, Place> p2p = new HashMap<org.jbpt.petri.Place, Place>();
		for (org.jbpt.petri.Place p : sys.getPlaces()) {
			Place pp = net.addPlace(p.toString());
			p2p.put(p, pp);
		}

		// transitions
		//int l = 0;
		Map<org.jbpt.petri.Transition, Transition> t2t = new HashMap<org.jbpt.petri.Transition, Transition>();
		for (org.jbpt.petri.Transition t : sys.getTransitions()) {
			Transition tt = net.addTransition(t.getLabel());
			tt.setInvisible(t.isSilent());
			t2t.put(t, tt);
		}

		// flow
		for (Flow f : sys.getFlow()) {
			if (f.getSource() instanceof org.jbpt.petri.Place) {
				net.addArc(p2p.get(f.getSource()), t2t.get(f.getTarget()));
			} else {
				net.addArc(t2t.get(f.getSource()), p2p.get(f.getTarget()));
			}
		}

		// add unique start node
		if (sys.getSourceNodes().isEmpty()) {
			Place i = net.addPlace("START_P");
			Transition t = net.addTransition("");
			t.setInvisible(true);
			net.addArc(i, t);

			for (org.jbpt.petri.Place p : sys.getMarkedPlaces()) {
				net.addArc(t, p2p.get(p));
			}
		}

		return net;
	}

	private static Marking getFinalMarkings(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}

	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}

	private static Map<Transition, Integer> constructMOSCostFunction(PetrinetGraph net) {
		Map<Transition, Integer> costMOS = new HashMap<Transition, Integer>();

		for (Transition t : net.getTransitions())
		{
			if(t.getLabel().contains("tau") || t.getLabel().contains("Tau") || t.getLabel().contains("invisible") || t.getLabel().isEmpty())
				t.setInvisible(true);
			if (t.isInvisible())
				costMOS.put(t, 0);
			else
				costMOS.put(t, 1);
		}
		return costMOS;
	}

	private Map<XEventClass, Integer> constructMOTCostFunction(PetrinetGraph net, XLog log,
			XEventClasses classes, XEventClass dummyEvClass) {
		Map<XEventClass, Integer> costMOT = new HashMap<XEventClass, Integer>();
		//XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (XEventClass evClass : classes.getClasses()) 
		{
			costMOT.put(evClass, 1);
		}
		costMOT.put(dummyEvClass, 1);

		return costMOT;
	}

	private TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, XEventClasses classes, XEventClassifier eventClassifier, XEventClass dummyEvClass) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);
		
		for (Transition t : net.getTransitions()) {
			for (XEventClass evClass : classes.getClasses()) {
				String id = evClass.getId();
				//String id2 = id.substring(0, id.indexOf("+"));
				String model = t.getLabel();
				if (model.equals(id)) {
					mapping.put(t, evClass);
					break;
				}
			}
		}
		return mapping;
	}

	public PNMatchInstancesRepResult getResRepresentative() {
		return resRepresentative;
	}

	public void setResRepresentative(PNMatchInstancesRepResult resRepresentative) {
		this.resRepresentative = resRepresentative;
	}

	public double getFFitness() {
		return FFitness;
	}

	public void setFFitness(double fFitness) {
		FFitness = fFitness;
	}

	public PNMatchInstancesRepResult getReplayResult() {
		return replayResult;
	}

	public void setReplayResult(PNMatchInstancesRepResult replayResult) {
		this.replayResult = replayResult;
	}

	public PNMatchInstancesRepResult getResTree() {
		return resTree;
	}

	public void setResTree(PNMatchInstancesRepResult resTree) {
		this.resTree = resTree;
	}

	
}
//
//start = System.currentTimeMillis();
//parameters[1] = stateLimit;
//this.resTree= repEngine.replayLog(new FakePluginContext(), net, xLog, mapping, initMarking, finalMarking, new AllOptAlignmentsTreeAlg(), parameters);
//	time = 0;
//	rawFitnessCost = 0;
//	numStates = 0;
//	numAlignments = 0;
//	traceFitness = 0;
//	moveModelFitness = 0;
//	moveLogFitness = 0;
//	traceLength = 0;
//	queuedStates = 0;
//	logSize =  xLog.size();
//	for (AllSyncReplayResult res : this.resTree) {
//		time += res.getInfo().get(PNMatchInstancesRepResult.TIME) * res.getTraceIndex().size();
//		rawFitnessCost += ( res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) * res.getTraceIndex().size());
//		numStates += res.getInfo().get(PNMatchInstancesRepResult.NUMSTATES) * res.getTraceIndex().size();
//		numAlignments += res.getInfo().get(PNMatchInstancesRepResult.NUMALIGNMENTS);
//		res.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, 1 - res.getInfo().get(PNMatchInstancesRepResult.RAWFITNESSCOST) / (res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) + minModelMoves));
//		DoubleArrayList moveLogFitnessList = new DoubleArrayList();
//		DoubleArrayList moveModelFitnessList = new DoubleArrayList();
//		for(List<StepTypes> alignment : res.getStepTypesLst())
//		{
//			double movesOnLog = 0;
//			double movesOnModel = 0;
//			double movesMatching = 0;
//			for(StepTypes step : alignment)
//			{
//				if(step==StepTypes.L) movesOnLog++;
//				else if(step==StepTypes.MREAL) movesOnModel++;
//				else if(step==StepTypes.LMGOOD) movesMatching++;
//			}
//			moveLogFitnessList.add(1 - movesOnLog / (movesOnLog + movesMatching));
//			moveModelFitnessList.add(1- movesOnModel / (movesOnModel + movesMatching));
//		}
//		res.addInfo(PNRepResult.MOVEMODELFITNESS, moveModelFitnessList.average());
//		res.addInfo(PNRepResult.MOVELOGFITNESS, moveLogFitnessList.average());
//		traceFitness += res.getInfo().get(PNMatchInstancesRepResult.TRACEFITNESS) * res.getTraceIndex().size();
//		moveModelFitness += (res.getInfo().get(PNRepResult.MOVEMODELFITNESS) * res.getTraceIndex().size());
//		moveLogFitness += (res.getInfo().get(PNRepResult.MOVELOGFITNESS) * res.getTraceIndex().size());
//		traceLength += (res.getInfo().get(PNMatchInstancesRepResult.ORIGTRACELENGTH) * res.getTraceIndex().size());
//		queuedStates += res.getInfo().get(PNMatchInstancesRepResult.QUEUEDSTATE) * res.getTraceIndex().size();
//		this.resTree.add(res);
//	}
//	
//	resTree.addInfo(PNMatchInstancesRepResult.TIME, "" + (time / logSize));
//	resTree.addInfo(PNMatchInstancesRepResult.RAWFITNESSCOST, "" + (rawFitnessCost / logSize));
//	resTree.addInfo(PNMatchInstancesRepResult.NUMSTATES, "" + (numStates / logSize));
//	resTree.addInfo(PNMatchInstancesRepResult.NUMALIGNMENTS, "" + numAlignments);
//	resTree.addInfo(PNMatchInstancesRepResult.TRACEFITNESS, "" + (traceFitness / logSize));
//	resTree.addInfo(PNRepResult.MOVEMODELFITNESS, "" + (moveModelFitness  / logSize));
//	resTree.addInfo(PNRepResult.MOVELOGFITNESS, "" + (moveLogFitness / logSize));
//	resTree.addInfo(PNRepResult.ORIGTRACELENGTH, "" + traceLength / logSize);
//	resTree.addInfo(PNMatchInstancesRepResult.QUEUEDSTATE, "" + queuedStates / logSize);
//	this.timePerformanceTree = System.currentTimeMillis() - start;