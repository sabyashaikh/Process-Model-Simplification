package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logfiltering.algorithms.ICC.AlignmentReplayResult;
import org.processmining.logfiltering.algorithms.ICC.AlignmentReplayer;
import org.processmining.logfiltering.algorithms.ICC.ApproxAlignmentReplayer;
import org.processmining.logfiltering.algorithms.ICC.ApproxFitnessReplayer;
import org.processmining.logfiltering.algorithms.ICC.FitnessReplayer;
import org.processmining.logfiltering.algorithms.ICC.IccParameters;
import org.processmining.logfiltering.algorithms.ICC.IccResult;
import org.processmining.logfiltering.algorithms.ICC.IncrementalConformanceChecker;
import org.processmining.logfiltering.algorithms.ICC.IncrementalReplayer;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.PrototypeType;
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.SimilarityMeasure;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.petrinetsimulator.parameters.SimulationSettings;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

import cern.jet.random.Exponential;
import cern.jet.random.engine.DRand;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.TraceReplayTask;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
public class ProtoTypeSelectionAlgo {

	public static String apply(XLog InputLog,Petrinet net, MatrixFilterParameter parameters,TransEvClassMapping mapping2) {
			long time = System.currentTimeMillis();
			XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
			XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
			TransEvClassMapping mapping = constructMapping(net, InputLog, dummyEvClass, eventClassifier);
			
			XEventClassifier EventCol = parameters.getEventClassifier();
			
		/*	if (parameters.getPrototypeType()==PrototypeType.Simulation) {
				mapping=mapping2;
			}*/
			
			XFactory factory = XFactoryRegistry.instance().currentDefault();
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
			HashMap<String,String >ActivityCoder =new HashMap<String, String>();
			HashMap<String,String >ActivityDeCoder =new HashMap<String, String>();
			HashMap<String, Integer> AsyncrousMoves= new HashMap<String, Integer>();
			HashMap<String, Double> AsyncrousDistribution= new HashMap<String, Double>();
			HashMap<String, Integer> SyncrousMoves= new HashMap<String, Integer>();
			HashMap<String, Integer> ModelMoves= new HashMap<String, Integer>();
			HashMap<String, Integer> LogMoves= new HashMap<String, Integer>();
			int LogSize = 0;
			PriorityQueue<Integer> pickedVariant=new PriorityQueue<>();
			SortedSet<String> eventAttributeSet = new TreeSet<String>();
			XAttributeMap eventAttributeMap;
			int KLength =parameters.getSubsequenceLength(); 
			int charcounter=65;
			ActivityCoder.put("ArtStart", Character.toString((char)charcounter));
			ActivityDeCoder.put(Character.toString((char)charcounter), "ArtStart");
			Set<String> ActivitySet = new HashSet<String>();
			
			for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
				charcounter++;
				ActivitySet.add(clazz.toString());
				ActivityCoder.put(clazz.toString(), Character.toString((char)charcounter));
				ActivityDeCoder.put( Character.toString((char)charcounter),clazz.toString());
				SyncrousMoves.put ( clazz.toString(), 0);
				AsyncrousMoves.put ( clazz.toString(), 0);
				ModelMoves.put ( clazz.toString(), 0);
				LogMoves.put ( clazz.toString(), 0);
			}
			charcounter++;
			ActivityCoder.put("ArtEnd", Character.toString((char)charcounter));
			ActivityDeCoder.put( Character.toString((char)charcounter),"ArtEnd");
			int ActivitiesSize = ActivitySet.size();
			//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
			String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
			List<String> ActivityList = java.util.Arrays.asList(Activities);
			int[] ActivityCount = new int[ActivitiesSize];
			FilterLevel FilteringMethod = parameters.getFilterLevel();
			FilterSelection FilteringSelection =parameters.getFilteringSelection();
			HashMap<String,Integer >HashMaper =new HashMap<String, Integer>();
			HashMap<String,String >TraceHash =new HashMap<String, String>();
			HashMap<String,Integer >HashTraceCounter =new HashMap<String, Integer>();
			HashMap<String,Integer >FilterHashMaper =new HashMap<String, Integer>();
			HashMap<Integer, String> ReverseMapper =new HashMap<Integer, String>();
			HashMap<String, Integer> ActionMaper= new HashMap<String, Integer>();
			HashMap<String, Integer> DFrelationMapper= new HashMap<String, Integer>();
			HashMap<Integer, String> DFrelationReverseMapper =new HashMap<Integer, String>();
			HashMap<Integer, XTrace> VariantMapper =new HashMap<Integer, XTrace>();
			int chCount=0;
			int dfCount=0;
			HashMap<Integer,List<Integer> >Clusters =new HashMap<Integer,List<Integer>>();
			 HashMap<Integer,Integer>SelectedList =new HashMap<Integer,Integer>();
			 HashMap<Integer,Integer>SelectedListTemp =new HashMap<Integer,Integer>();
			 HashMap<String,Integer >ModelBehaviorSim =new HashMap<String, Integer>();
			 HashMap<Integer,String >ModelBehaviorSimReverse =new HashMap< Integer,String>();
			
			 
			 int maxTraceLength= 0;
			 for (XTrace trace : InputLog) { // for each trace
				 LogSize++;
				 /// Put trace to array
				 String[] Trace = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) { 
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Trace = templist.toArray(new String[trace.size()]);
					String tr= Trace[0];
					String TraceinChar=ActivityCoder.get(tr);
					for (int i =1; i < Trace.length; i++){
						tr= tr.concat("=>"+Trace[i]);
						TraceinChar= TraceinChar.concat(ActivityCoder.get(Trace[i]));
					}
					//TraceinChar= TraceinChar.concat(ActivityCoder.get("ArtEnd"));
					//= tr.concat("=>"+"ArtEnd");
					if (ActionMaper.get(tr)==null ){
						ActionMaper.put(tr,1);
						ReverseMapper.put(chCount, tr);
						HashMaper.put(tr, chCount);
						HashTraceCounter.put(TraceinChar, 1);
						TraceHash.put(tr, TraceinChar);
						VariantMapper.put(chCount, trace);
						chCount++;
						maxTraceLength=maxTraceLength+ TraceinChar.length();
						}else{
						ActionMaper.put(tr, ActionMaper.get(tr)+1);
						HashTraceCounter.put(TraceinChar, HashTraceCounter.get(TraceinChar)+1);
					}
			 }
			 maxTraceLength= maxTraceLength/ReverseMapper.size();
			 
			 float [] VariantFreq= new float[HashMaper.size()];
			 int[] VariantInd= new int[HashMaper.size()];
			 for (int i = 0; i < VariantFreq.length; i++) {
				 VariantFreq[i]= ActionMaper.get(ReverseMapper.get(i)); 
				 VariantInd[i]=i;
				 String TraceinChar= TraceHash.get(ReverseMapper.get(i));
				 char[] DFRelations=TraceinChar.toCharArray();
					String DF= "";
					for (int j = 0; j < DFRelations.length-1; j++) {
						DF= Character.toString(DFRelations[j])+Character.toString(DFRelations[j+1]);
						if(DFrelationMapper.get(DF)==null) {
							DFrelationMapper.put(DF,dfCount);
							DFrelationReverseMapper.put(dfCount,DF);
							dfCount++;
						}
					}
			}
			 int[][] VariantProfile = new int [HashMaper.size()][DFrelationMapper.size()];
			 String[] VariantChar = new String[HashMaper.size()];
			 for (int i = 0; i < VariantFreq.length; i++) {
				 String TraceinChar= TraceHash.get(ReverseMapper.get(i));
				 VariantChar[i]=TraceinChar;
				 char[] DFRelations=TraceinChar.toCharArray();
					String DF= "";
					for (int j = 0; j < DFRelations.length-1; j++) {
						DF= Character.toString(DFRelations[j])+Character.toString(DFRelations[j+1]);
						VariantProfile[i][DFrelationMapper.get(DF)]= DFrelationMapper.get(DF)+1; 
					}
			 }
			
			 double INTRA= 0;
			 Variant[] VariantSimilarity = new Variant[HashMaper.size()];
			 double[] [] distanceMatrix =new double[HashMaper.size()][HashMaper.size()];
			 if(parameters.getPrototypeType()==PrototypeType.Cluster) {
				 switch (parameters.getSimilarityMeasure()) {
						case Levenstein :
						
							for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
								 double minTemp= 1000;
									for (int j = 0; j < VariantProfile.length; j++) {
										double x = levenshteinDistance (VariantChar[i],VariantChar[j]) ;
										temp= temp+ x;
										distanceMatrix[i][j]= x;
										if (minTemp> x && x>0)
											minTemp=x;
									}
									VariantSimilarity[i]= new Variant(i, temp );
									INTRA= INTRA+minTemp;
									//INTRA= INTRA+(temp*1.0/(VariantProfile.length-1));
								}
							INTRA=INTRA/(VariantProfile.length-1);
							break;
						case Incremental :
							for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
									for (int j = 0; j < VariantProfile.length; j++) {
										temp= temp+ levenshteinDistance (VariantChar[i],VariantChar[j]) ;
										distanceMatrix[i][j]= levenshteinDistance (VariantChar[i],VariantChar[j]);
									}
									VariantSimilarity[i]= new Variant(i, temp );
								}
							break;
						case Jacard:
							 for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
									for (int j = 0; j < VariantProfile.length; j++) {
										temp= temp+ jaccardSimilarity (VariantProfile[i],VariantProfile[j])  ;
										
									}
									VariantSimilarity[i]= new Variant(i, temp  );
								}
							break;
						case Difference:
							 for (int i = 0; i < VariantProfile.length; i++) {
								 double temp=0;
									for (int j = 0; j < VariantProfile.length; j++) {
										temp= temp+  DiffSimilarity (VariantProfile[i],VariantProfile[j]) ;
										
									}
									VariantSimilarity[i]= new Variant(i, temp  );
								}
							break;
					}
			 }
			 
			 double Threshold =1- parameters.getSubsequenceLength()*1.0/100;
			 
			 List<Integer> SampledList=  new ArrayList<>();
			 
			 int[] MostSimilarVariant= new int [HashMaper.size()];
			 
			 
			 
			 
			 
			 
			 
			 
			 long timer1 = System.currentTimeMillis() - time ;
			 ///////////////////////////Compute the Replay Values///////////////////
				Multiset<String> asynchronousMoveBag=TreeMultiset.create();
				Marking initialMarking = getInitialMarking(net);
				Marking finalMarking = getFinalMarking(net);
				int nThreads = 8;
				int costUpperBound = Integer.MAX_VALUE;
				// timeout per trace in milliseconds
				int timeoutMilliseconds = 10 * 1000;
				// preprocessing time to be added to the statistics if necessary
				long preProcessTimeNanoseconds = 0;
				XLogInfo summary = XLogInfoFactory.createLogInfo(InputLog, eventClassifier);
				XEventClasses classes = summary.getEventClasses();
	///////////////////////////Selection Phase///////////////////////////////// 
			 PrototypeType  SelectiuonType=parameters.getPrototypeType(); 
			 switch (SelectiuonType) {
				 case First:
					 
					 for (int i = 0; i < VariantInd.length; i++) {
							if (i< (1- Threshold)* VariantInd.length) {
								SelectedList.put(i,VariantInd[i]);
							}else {
								
								SampledList.add(VariantInd[i]);
							}
							
						}
					 break;
				 
				case Frequency :
					quicksort (VariantFreq.clone(), VariantInd);
					int counter=0;
					for (int i = 0; i < VariantInd.length; i++) {
						if (i< (Threshold)* VariantInd.length) {
							SampledList.add(VariantInd[i]);
						}else {
							SelectedList.put(counter,VariantInd[i]);
							counter++;
						}
						
					}
					break;

				case Random:
					 Random generator = new Random(System.currentTimeMillis());
					 chCount=0;
					 ArrayList<Integer> TraceNumber = new ArrayList<Integer>();
					 XLog outputLog2 = factory.createLog();
						outputLog2.setAttributes(InputLog.getAttributes());	
						for (int i = 0; i < InputLog.size(); i++) {
							TraceNumber.add(i);
						}
					 while (chCount < (1-Threshold) * VariantInd.length )  {
							int index =ThreadLocalRandom.current().nextInt(0, TraceNumber.size());
							outputLog2.add(InputLog.get(TraceNumber.get(index)));
							TraceNumber.remove(TraceNumber.get(index));// Do not select this trace again 
								int randomTrace=generator.nextInt(VariantInd.length-chCount);
								pickedVariant.add(randomTrace);
								chCount++;
							}//while
				  counter=0;
					 for (int i = 0; i < VariantInd.length; i++) {
							if (pickedVariant.contains(i)) {
								SelectedList.put(counter, i);
								counter++;
							}else {
								SampledList.add(i);
							}
							
						}
					break;
				case Cluster:
					 int K= (int) ((1-Threshold)* VariantInd.length);
					 
					 int iterations=2;
					 for (int i = 0; i < K ; i++) {
						 List<Integer> tempList = new ArrayList<>();
						 Clusters.put(i, tempList);
						 SelectedList.put(i,i);
					}////KMEDOIDS
					 for (int i = 0; i < iterations; i++) {
						 Clusters=  FindCluster(distanceMatrix,SelectedList);
						 SelectedList= UpdateKMedoids(SelectedList, distanceMatrix,VariantFreq,Clusters);
					}
					 for (int i = 0; i < SelectedList.size(); i++) {
						pickedVariant.add(SelectedList.get(i));
					}
					 for (int i = 0; i < VariantInd.length; i++) {
						 if (!pickedVariant.contains(i)) {
							 SampledList.add(i);
						 }
					 }
					 break;
				case Simulation:
					long initialDate=0;
					 
					SimulationSettings SimSet=  new SimulationSettings(0,(int) ((1-Threshold)*10000), maxTraceLength+2,initialDate,  new Exponential(1.0/3600000.0, new DRand(new Date(System.currentTimeMillis()))), new Exponential(1.0/3600000.0, new DRand(new Date(System.currentTimeMillis()))));
					SimulatorCombo lgSimulator= new SimulatorCombo( net,initialMarking,SimSet, factory );
					XLog VariantModelog = lgSimulator.simulate();
					chCount=0;
					for (XTrace trace : VariantModelog) { // for each trace
						 if(trace.size()>0) {
							 String[] Trace = new String[trace.size()];
								List<String> templist = new ArrayList<String>();
								for (XEvent event : trace) { 
									eventAttributeMap = event.getAttributes();
									templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
								}
								Trace = templist.toArray(new String[trace.size()]);
								String tr= Trace[0];
								String TraceinChar=ActivityCoder.get(tr);
								for (int i =1; i < Trace.length; i++){
									TraceinChar= TraceinChar.concat(ActivityCoder.get(Trace[i]));
								}
								
								if (ModelBehaviorSim.get(TraceinChar)==null ){
									ModelBehaviorSim.put(TraceinChar, chCount);
									ModelBehaviorSimReverse.put(chCount, TraceinChar);
									chCount++;
								}
						 }
					 }
					for (int i = 0; i < VariantInd.length; i++) {
						
							 SampledList.add(i);
						 
					 }
					
				break;
			}
			 //////////////////////////////////////////////////
			
				
			 XLog TraceLog = factory.createLog();
			 TraceLog.setAttributes(InputLog.getAttributes());
			 XAttributeMapImpl case_map = new XAttributeMapImpl();
			 String case_id = String.valueOf(charcounter);
			 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
			 XTraceImpl trace = new XTraceImpl(case_map);
			 TraceLog.add(trace);	
			 //double ShortestPath=replayTraceOnNet( TraceLog,  net,  mapping);
			 double[] AlignmentCosts= new double [SelectedList.size()+1];
			 double[] FitnessValues= new double [SelectedList.size()];
			 
			 
			 
			 
			 ///// Compute the actual alignment
			 for (int i = 0; i < SelectedList.size(); i++) {
				
				 TraceLog.add(VariantMapper.get(SelectedList.get(i)));
				 VariantMapper.get(SelectedList.get(i)).toArray();
				// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
			}//
			 	ReplayerParameters RepParameters = new ReplayerParameters.Default(nThreads, costUpperBound, Debug.NONE);
				Replayer replayer = new Replayer(RepParameters, (Petrinet) net, initialMarking, finalMarking, classes, mapping, false);
			 Future<TraceReplayTask>[] futures = new Future[TraceLog.size()];
			 ExecutorService service = Executors.newFixedThreadPool(RepParameters.nThreads);
			
			   
			 for (int i = 0; i < TraceLog.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog.get(i), i, timeoutMilliseconds,
			 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			 		// submit for execution
			 		futures[i] = service.submit(task);
			 	}
			double AlignFit=0;
			double AlignCost=0;
			 	// obtain the results one by one.
			chCount=0;
			 	for (int i = 0; i < TraceLog.size(); i++) {

			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		 AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 		List<Object> ModelBehavior = replayResult.getNodeInstance();
			 		 List<StepTypes> TypeBehavior = replayResult.getStepTypes();
			 		 String modelTrace="";
			 		for (int j=0;j<replayResult.getNodeInstance().size();j++) {
			 			if(i>0) {
				 			if(TypeBehavior.get(j).equals(StepTypes.L)||TypeBehavior.get(j).equals(StepTypes.MREAL)) {
				 				AsyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (AsyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
				 				if(TypeBehavior.get(j).equals(StepTypes.L))
				 					LogMoves.put(ModelBehavior.get(j).toString(), (int) (LogMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
				 				else
				 					ModelMoves.put(ModelBehavior.get(j).toString(), (int) (ModelMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
				 			}else if(TypeBehavior.get(j).equals(StepTypes.LMGOOD))
				 				SyncrousMoves.put(ModelBehavior.get(j).toString(), (int) (SyncrousMoves.get(ModelBehavior.get(j).toString())+VariantFreq[SelectedList.get(i-1)]));
			 			}
			 			
			 		if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
			 				
			 				if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
			 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
			 				}
			 				else {
			 					charcounter++;
			 					ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
			 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
			 				}
			 			}
			 			 
			 		}
			 		
			 		if (ModelBehaviorSim.get(modelTrace)==null) {
			 			ModelBehaviorSim.put(modelTrace, chCount);
			 			ModelBehaviorSimReverse.put(chCount, modelTrace);
			 			chCount++;
			 		}
			 		
			 		
			 		/*for (int j=0;j<replayResult.getStepTypes().size();j++) {
			 			if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 				//System.out.println(replayResult.getNodeInstance().get(j).toString());
			 				if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 					if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
			 						asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
			 					}
			 					else {
			 						asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 					}
			 				}
			 				if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
			 					asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 				}
			 			}
			 		}*/
			 	}
			 	int chCount2=0;
			 for (int i = 0; i < FitnessValues.length; i++) {
				 chCount2= chCount2+ (int)VariantFreq[SelectedList.get(i)];
				 FitnessValues[i]= 1- ( AlignmentCosts[i+1]*1.0/ (AlignmentCosts[0]+VariantChar[SelectedList.get(i)].length() ));
				 AlignFit= AlignFit+FitnessValues[i];
				 AlignCost= AlignCost+ (1- FitnessValues[i]);
			}
			 AlignFit=AlignFit/FitnessValues.length;
			 AlignCost=AlignCost/FitnessValues.length;
			
			 
			 ///////////////////////Compute the approximation Cost
			 double Costs = 0;
			 double SmapleFitness=0;
			 double approximateCosts = 0;
			 double approximateCosts2 = 0;
			 double upperBoundCost=0;
			 double[] SampledFitness = new double[SampledList.size()];
			 chCount2=0;
			 double SampledFitnessApproximationValue=0;
			 double SampledFitnessApproximationValue2=0;
			 double[] distancesApprox= new double [MostSimilarVariant.length];
			 int sampledFreq= 0;
			 boolean increament=false;
			 if (parameters.getPrototypeType()!=PrototypeType.Simulation) {
				 
				 for (int i = 0; i < SampledList.size(); i++) {
					 sampledFreq= (int) (sampledFreq+VariantFreq[SampledList.get(i)]);
					 double similarVariantcost =10000;
					 int tempIndex=0;
					 double tempDist=0;
					 String alignment="";
					SimilarLoop:	 for (int j = 0; j < ModelBehaviorSimReverse.size(); j++) {
						tempDist=levenshteinDistanceCost(VariantChar[SampledList.get(i)],  ModelBehaviorSimReverse.get(j) );
							 if( tempDist  < similarVariantcost) {
									similarVariantcost=tempDist  ;
									tempIndex=j;
									if (similarVariantcost==0)
										break SimilarLoop;
								}
						}
					 AlignObj temp=levenshteinDistancewithAlignment(VariantChar[SampledList.get(i)],  ModelBehaviorSimReverse.get(tempIndex) );
					 alignment=temp.Alignment;
					 String[] Moves = alignment.split(">");
					for (int j = 0; j < Moves.length; j++) {
						if(Moves[j].contains("Sync"))
							SyncrousMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (SyncrousMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
						else {
							AsyncrousMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (AsyncrousMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
							if(Moves[j].contains("Deletion"))
								ModelMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (ModelMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
							else
								LogMoves.put(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)), (int) (LogMoves.get(ActivityDeCoder.get(Moves[j].substring(Moves[j].length()-1)))+VariantFreq[SampledList.get(i)]));
						}
					}
					
					 distancesApprox[SampledList.get(i)]= similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length() );
					 double similarVariantAlignCost=similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());
					 
					 SampledFitness[i]=1-similarVariantAlignCost;
					 if (similarVariantAlignCost< 0.10) {
						 approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
						 approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
					 }else if(parameters.getSimilarityMeasure()!= SimilarityMeasure.Incremental) {
						 
						 SampledFitness[i] =1 -( similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length())   );
						 approximateCosts= approximateCosts+( ((similarVariantAlignCost+ AlignCost)*1.0/2)  * VariantFreq[SampledList.get(i)]);
						 approximateCosts2= approximateCosts2+( AlignCost  * VariantFreq[SampledList.get(i)]);
					 }else {
						 increament=true;
						 chCount2++;
						 futures = new Future[1];
						TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, VariantMapper.get(SampledList.get(i)), 0, timeoutMilliseconds,
						 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
						 		// submit for execution
						 		futures[0] = service.submit(task); 		
						 		TraceReplayTask result;
						 		try {
						 			result = futures[0].get();
						 		} catch (Exception e) {
						 			// execution os the service has terminated.
						 			assert false;
						 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
						 		}
						 		SyncReplayResult replayResult = result.getSuccesfulResult();
						 		double x = replayResult.getInfo().get("Raw Fitness Cost");
						 		
						 		List<Object> ModelBehavior = replayResult.getNodeInstance();
						 		 List<StepTypes> TypeBehavior = replayResult.getStepTypes();
						 		 String modelTrace="";
						 		for (int j=0;j<replayResult.getNodeInstance().size();j++) {
						 			if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
						 				if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
						 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
						 				}
						 				else {
						 					charcounter++;
						 					ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
						 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
						 				}
						 			}
						 		}
						 		
						 		if (ModelBehaviorSim.get(modelTrace)==null) {
						 			ModelBehaviorSim.put(modelTrace, chCount);
						 			ModelBehaviorSimReverse.put(chCount, modelTrace);
						 			chCount++;
						 		}
						 		similarVariantAlignCost= x*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());
						 		SampledFitness[i] =1 - similarVariantAlignCost  ;
						 		approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
								approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
					 }
					 
						 
						 SmapleFitness= SmapleFitness+(   SampledFitness[i]      * VariantFreq[SampledList.get(i)]   );
						 
						 if(!increament) {
							 Costs= Costs + (similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
							 if(VariantChar[SampledList.get(i)].length() < AlignmentCosts[0]) {
							 	upperBoundCost= upperBoundCost+ ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) ) *VariantFreq[SampledList.get(i)] );
							 	if (similarVariantAlignCost< ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) )  )) {
							 		approximateCosts2= approximateCosts2- ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
							 		approximateCosts2= ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) )  );
							 		approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
							 	}
						 	}
						 }else {
							 increament=false;
							 upperBoundCost= upperBoundCost+(similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
						 }
					 
					 
					 
				}
			 }else {   //////simulation or XXX
				 chCount= ModelBehaviorSimReverse.size();
				 for (int i = 0; i < SampledList.size(); i++) {
					 double similarVariantcost =1000000;

						 for (int j = 0; j < ModelBehaviorSimReverse.size(); j++) {
							 int temp=levenshteinDistanceCost(VariantChar[SampledList.get(i)], ModelBehaviorSimReverse.get(j) );
							 if( temp   < similarVariantcost) {
									similarVariantcost=temp  ;

								}
							
						}
						 distancesApprox[SampledList.get(i)]= similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length() );
						 double similarVariantAlignCost=similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());
						 
						 SampledFitness[i] =1 - similarVariantAlignCost  ;
						
						 
						 
						if (parameters.getSimilarityMeasure()== SimilarityMeasure.Incremental && similarVariantAlignCost >0.15) {
							 increament=true;
							 chCount2++;
							 futures = new Future[1];
							TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, VariantMapper.get(SampledList.get(i)), 0, timeoutMilliseconds,
							 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
							 		// submit for execution
							 		futures[0] = service.submit(task); 		
							 		TraceReplayTask result;
							 		try {
							 			result = futures[0].get();
							 		} catch (Exception e) {
							 			// execution os the service has terminated.
							 			assert false;
							 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
							 		}
							 		SyncReplayResult replayResult = result.getSuccesfulResult();
							 		double x = replayResult.getInfo().get("Raw Fitness Cost");
							 		
							 		List<Object> ModelBehavior = replayResult.getNodeInstance();
							 		 List<StepTypes> TypeBehavior = replayResult.getStepTypes();
							 		 String modelTrace="";
							 		for (int j=0;j<replayResult.getNodeInstance().size();j++) {
							 			if(!ModelBehavior.get(j).toString().contains("tau") && !TypeBehavior.get(j).equals(StepTypes.MINVI)&& !TypeBehavior.get(j).equals(StepTypes.L)) {
							 				if(ActivityCoder.containsKey(ModelBehavior.get(j).toString()) )  {
							 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
							 				}
							 				else {
							 					charcounter++;
							 					ActivityCoder.put(ModelBehavior.get(j).toString(), Character.toString((char)charcounter));
							 					modelTrace=modelTrace + ActivityCoder.get(ModelBehavior.get(j).toString());
							 				}
							 			}
							 		}
							 		
							 		if (ModelBehaviorSim.get(modelTrace)==null) {
							 			ModelBehaviorSim.put(modelTrace, chCount);
							 			ModelBehaviorSimReverse.put(chCount, modelTrace);
							 			chCount++;
							 		}
							 		similarVariantAlignCost= x*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length());
							 		SampledFitness[i] =1 - similarVariantAlignCost  ;
							 		approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
									approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
						}
						
					 distancesApprox[SampledList.get(i)]= similarVariantcost*1.0/ (AlignmentCosts[0]+VariantChar[SampledList.get(i)].length() );
					 SampledFitness[i]=1-similarVariantAlignCost;
					 approximateCosts= approximateCosts+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
					 approximateCosts2= approximateCosts2+ ( similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
					 SmapleFitness= SmapleFitness+(   SampledFitness[i]      * VariantFreq[SampledList.get(i)]   );
					 sampledFreq= (int) (sampledFreq+VariantFreq[SampledList.get(i)]);
					 if(!increament) {
						 Costs= Costs + (similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
						 if(VariantChar[SampledList.get(i)].length() < AlignmentCosts[0]) {
						 	upperBoundCost= upperBoundCost+ ( ( ( AlignmentCosts[0]- VariantChar[SampledList.get(i)].length()  ) / ( AlignmentCosts[0]+ VariantChar[SampledList.get(i)].length() ) ) *VariantFreq[SampledList.get(i)] );
					 	}
					 }else {
						 increament=false;
						 upperBoundCost= upperBoundCost+(similarVariantAlignCost * VariantFreq[SampledList.get(i)]);
					 }
				}
				 
			 } ////simulation 
				
			
			 
			 SampledFitnessApproximationValue= 1-(approximateCosts*1.0/sampledFreq);
			 SampledFitnessApproximationValue2= 1-(approximateCosts2*1.0/sampledFreq);
			double SampledFitnessValue= SmapleFitness*1.0/sampledFreq;
			
				double computedFitness=0; int computedFreq=0;
				double computedAlignCost=0;
			for (int i = 0; i < SelectedList.size(); i++) {
				computedFitness= computedFitness+(FitnessValues[i]*VariantFreq[SelectedList.get(i)]);
				computedAlignCost= computedAlignCost+((1-FitnessValues[i])*VariantFreq[SelectedList.get(i)]);
				computedFreq=(int) (computedFreq+VariantFreq[SelectedList.get(i)]);
			}
			
			double computedFitnessValue=0;
			 
			 if (parameters.getPrototypeType()!=PrototypeType.Simulation) {
					computedFitnessValue=computedFitness/computedFreq;
			 }
			
			
			 double LowerBoundCosts = Costs*1.0 /LogSize;
			 double LowerBoundFitness = ( computedFitness+ SmapleFitness)*1.0 / (sampledFreq+computedFreq);
			//double ApproximatedFitness= LowerBoundFitness+ (LowerBound*computedFitnessValue);
			 double ApproximatedFitness= ( computedFitness + (SampledFitnessApproximationValue*sampledFreq) )*1.0 / (sampledFreq+computedFreq);
			 
			 double ApproximatedFitness2= ( (computedFitnessValue *computedFreq) + (SampledFitnessApproximationValue2*sampledFreq) )*1.0 / (sampledFreq+computedFreq);
			 
			 long timer2 = System.currentTimeMillis() - time ;
			 time= System.currentTimeMillis();
			 double UpperBound =1- (( computedAlignCost + upperBoundCost) *1.0/LogSize);
			
			if (parameters.getPrototypeType()==PrototypeType.Simulation){
					ApproximatedFitness=  (LowerBoundFitness + UpperBound )/2;
					
					}
			Set<String> T = SyncrousMoves.keySet();
			for (Iterator t = T.iterator(); t.hasNext();) {
				String string = (String) t.next();
				AsyncrousDistribution.put(string, (double) (AsyncrousMoves.get(string)*1.0/(SyncrousMoves.get(string)+AsyncrousMoves.get(string))));		
			}
			
			
			if (ApproximatedFitness2> UpperBound)
				ApproximatedFitness2=(UpperBound+LowerBoundFitness)/2;
			 String outp=( LowerBoundFitness +"==>>"+ UpperBound+"==>>"+ ApproximatedFitness+"==>>"+ LowerBoundCosts +"==>>" + timer2+"==>>" +timer1 +"==>>" +ApproximatedFitness2 +"==>>"+ (double)(chCount2+SelectedList.size())/VariantMapper.size()+"==>>"+ AsyncrousDistribution);
			 
			 
		//////////////////////////////////Actual Fitness/////////////////////////////////////////////	 
		/*double[] AlignmentCosts2= new double [InputLog.size()];
			 futures = new Future[InputLog.size()];
			 service = Executors.newFixedThreadPool(RepParameters.nThreads);
			 for (int i = 0; i < InputLog.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, InputLog.get(i), i, timeoutMilliseconds,
			 				RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);
			 		// submit for execution
			 		futures[i] = service.submit(task);
			 	}
			 for (int i = 0; i < InputLog.size(); i++) {
			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		AlignmentCosts2[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 	}
			 double ActualFitness=0;
			 for (int i = 0; i < AlignmentCosts2.length; i++) {
				 ActualFitness= ActualFitness + (1-(AlignmentCosts2[i]*1.0/ (AlignmentCosts[0]+InputLog.get(i).size() )  ));
			}
			 double ActualFitnessValue=ActualFitness/InputLog.size();

			 long timer3= System.currentTimeMillis() - time ;
			 double PerformanceImprovement= timer2 *1.0 / timer2;  
			 double AccuracyClosness= Math.abs(ApproximatedFitness -  ActualFitnessValue ); */
		///////////////////////////////////////////////////////////////////////////////////////////
			 
/*			 
			 AlignFit=0;
			double  LogSize2=0;
			 XLog TraceLog2 = factory.createLog();
			 TraceLog2.setAttributes(InputLog.getAttributes());
			 TraceLog2.setAttributes(InputLog.getAttributes());
			 
			  case_id = String.valueOf(charcounter+2000000);
			 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
			 XTraceImpl trace2 = new XTraceImpl(case_map);
			 TraceLog2.add(trace2);	
 for (int i = 0; i < VariantFreq.length; i++) {
				 
				 TraceLog2.add(VariantMapper.get(i));
				
				// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
			}//
 			AlignmentCosts=	 new double [TraceLog2.size()];
 			FitnessValues= new double [TraceLog2.size()-1];
 				futures = new Future[TraceLog2.size()];
			 for (int i = 0; i < TraceLog2.size(); i++) {
			 		// Setup the trace replay task
			 		TraceReplayTask task = new TraceReplayTask(replayer, RepParameters, TraceLog2.get(i), i, timeoutMilliseconds,
			 		RepParameters.maximumNumberOfStates, preProcessTimeNanoseconds);

			 		futures[i] = service.submit(task);
			 	}
			 for (int i = 0; i < TraceLog2.size(); i++) {

			 		TraceReplayTask result;
			 		try {
			 			result = futures[i].get();
			 		} catch (Exception e) {
			 			// execution os the service has terminated.
			 			assert false;
			 			throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			 		}
			 		SyncReplayResult replayResult = result.getSuccesfulResult();
			 		 AlignmentCosts[i]= replayResult.getInfo().get("Raw Fitness Cost");
			 		for (int j=0;j<replayResult.getStepTypes().size();j++) {
			 			if(replayResult.getStepTypes().get(j).toString().equals("Log move") || replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 				//System.out.println(replayResult.getNodeInstance().get(j).toString());
			 				if(replayResult.getStepTypes().get(j).toString().equals("Model move")) {
			 					if(mapping.containsKey(replayResult.getNodeInstance().get(j))) {
			 						asynchronousMoveBag.add(mapping.get(replayResult.getNodeInstance().get(j)).toString());
			 					}
			 					else {
			 						asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 					}
			 				}
			 				if(replayResult.getStepTypes().get(j).toString().equals("Log move")) {
			 					asynchronousMoveBag.add((replayResult.getNodeInstance().get(j)).toString());
			 				}
			 			}
			 		} 	
			 	}
			 for (int i = 0; i < FitnessValues.length; i++) {
				 FitnessValues[i]= 1- ( AlignmentCosts[i+1]*1.0/ (AlignmentCosts[0]+VariantChar[i].length() ));
				 AlignFit= AlignFit+ (FitnessValues[i]* VariantFreq[i]);
				 
				 LogSize2=LogSize2+VariantFreq[i]; 
			}
			 
			 double AlignFit2=AlignFit/LogSize2;
			*/
			
			 
			 
				return outp;
		
		
			
			
		
		
	}

	public static String apply2(PluginContext context,XLog log, Petrinet net, MatrixFilterParameter parameters,
			TransEvClassMapping mapping) {
		// TODO Auto-generated method stub 
		//Martin Bauer
		long time = System.currentTimeMillis();
		System.out.println(	System.currentTimeMillis() - time);
		XLog log2= (XLog) log.clone();
		
		double delta=parameters.getSubsequenceLength()*0.001;//0.01
		double alpha=0.99;
		double epsilon=parameters.getSubsequenceLength()*0.001;//0.01
		double k=0.5;
		int initialSize=20;
		String goal="fitness";
		boolean approximate=false;
		IccParameters iccParameters=new IccParameters(delta, alpha, epsilon, k, initialSize, goal, approximate);

		IncrementalReplayer replayer = null;
		if (goal.equals("fitness")&& !iccParameters.isApproximate()) {
			replayer=new FitnessReplayer(iccParameters);
		}
		if (goal.equals("fitness")&&iccParameters.isApproximate()) {
			replayer=new ApproxFitnessReplayer(iccParameters);
		}
		if (iccParameters.getGoal().equals("alignment")&& !iccParameters.isApproximate())
			replayer=new AlignmentReplayer(iccParameters);
		if (iccParameters.getGoal().equals("alignment") && iccParameters.isApproximate()) {
			replayer= new ApproxAlignmentReplayer(iccParameters);
			//replayer.init(context, net, log);
		}
				
		//make own parameter function for alignment/fitness
		AlignmentReplayResult result= calculateAlignmentWithICC(context, replayer, net, log2, iccParameters, mapping);
		
		result.setTime(System.currentTimeMillis()-time);
		System.out.println("Fitness         : "+result.getFitness());
		System.out.println("Time(ms)        : "+result.getTime());
		System.out.println("Log Size        : "+result.getLogSize());
		System.out.println("No AsynchMoves  : "+result.getTotalNoAsynchMoves());
		System.out.println("AsynchMoves abs : "+result.getAsynchMovesAbs().toString());
		System.out.println("AsynchMoves rel : "+result.getAsynchMovesRel().toString());
		long timer2 = System.currentTimeMillis() - time ;
		String outp=( 0 +"==>>"+ 1+"==>>"+ result.getFitness()+"==>>"+ result.getLogSize() +"==>>" + timer2+"==>>" +0+"==>>"+ result.getFitness())+"==>>"+ result.getLogSize() ;
		
		return outp;
		
	}
	
	static private double jaccardSimilarity(int[] a, int[] b) {

        Set<Integer> s1 = new HashSet<Integer>();
        for (int i = 0; i < a.length; i++) {
            s1.add(a[i]);
        }
        Set<Integer> s2 = new HashSet<Integer>();
        for (int i = 0; i < b.length; i++) {
            s2.add(b[i]);
        }

        final int sa = s1.size();
        final int sb = s2.size();
        s1.retainAll(s2);
        final int intersection = s1.size();
        return 1d / (sa + sb - intersection) * intersection;
    }

    
    static private double DiffSimilarity(int[] a, int[] b) {
    	int Tdf=0;
    	int Sdf=0;
        for (int i = 0; i < b.length; i++) {
			if (a[i]>0 || b[i]>0) {
				if (a[i]>0 && b[i]>0) {
				Sdf++;	
				}
				Tdf++;
			}
        
		}
        
        return  (double)(Sdf*1.0)/Tdf;
    }

 

 static class Variant
	{
	    private int VariantIndex;
	    private int IntScore;
	    private double DoubleScore;
	    
	    public Variant()
	    {
	    	
	    	 	this.VariantIndex = 0;
		        this.IntScore = 0;
		        this.DoubleScore=0.0;
	    }
	    public Variant(int ind, int intScore)
	    {
	       
	        this.VariantIndex = ind;
	        this.IntScore = intScore;
	        this.DoubleScore=0.0;
	    } 
	    public Variant(int ind, double doubleScore)
	    {
	       
	        this.VariantIndex = ind;
	        this.IntScore = 0;
	        this.DoubleScore=doubleScore;
	    } 
	    public Variant(int ind, int intScore,double doubleScore)
	    {
	       
	        this.VariantIndex = ind;
	        this.IntScore = intScore;
	        this.DoubleScore=doubleScore;
	    }
		public int getIntScore() {
			// TODO Auto-generated method stub
			return IntScore;
		} 
		public double getDoubleScore() {
			// TODO Auto-generated method stub
			return DoubleScore;
		} 
		public int getIndex() {
			// TODO Auto-generated method stub
			return VariantIndex;
		} 
		
		
	}
 

 public static HashMap<Integer, List<Integer>> FindCluster (double[][] distanceMatrix,HashMap<Integer,Integer> SelectedList) {
	 int[] belongCluster = new int[distanceMatrix.length];
	 HashMap<Integer,List<Integer> >Clusters =new HashMap<Integer,List<Integer>>();
	 int K = SelectedList.size();
	 for (int i = 0; i < K ; i++) {
		 List<Integer> tempList = new ArrayList<>();
		 Clusters.put(i, tempList);
	}
	 for (int i = 0; i < belongCluster.length; i++) {
		 double cost=1000;
		 int counter =0; int index=0;
		for (int j = 0; j < K; j++) {
			
			if (distanceMatrix[i][SelectedList.get(j)]< cost) {
				cost =distanceMatrix[i][SelectedList.get(j)];
				counter= SelectedList.get(j);
				index=j;
			}
		}
		belongCluster[i]= index; 
		List<Integer> List = Clusters.get(index);
 		List.add(i);
		Clusters.put(index,List);
	}
	 return Clusters;
	 
 }
 
 public static HashMap<Integer, Integer> UpdateKMedoids(HashMap<Integer,Integer> SelectedList,double[][] distanceMatrix, float[] variantFreq, HashMap<Integer,List<Integer> > Clusters) {
	 int K = SelectedList.size();
	 for (int i = 0; i < K; i++) {
		 List<Integer> List= Clusters.get(i);
		 double distance = 1000000;
		 for (int j = 0; j < List.size(); j++) {
			 double cost = 0;
			 int counter=0;
			 
			 for (int j2 = 0; j2 < List.size(); j2++) {
				 cost=cost+ (distanceMatrix[List.get(j)][List.get(j2)] * variantFreq[List.get(j2)] * variantFreq[List.get(j)]);
			}
			 if( cost <distance) {
					distance = cost;
					SelectedList.put(i,List.get(j));
				}
		}
	}
	 
	 return SelectedList;
	 
 }
 
 
 
 public static int levenshteinDistanceCost (CharSequence lhs, CharSequence rhs) {    
		
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	     int maxLen= 0;
	     if (len0>len1) {
	    	 maxLen=len0;
	     }
	     else {
	    	  	maxLen= len1;
	     }
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) 
	    	cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j-1;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; 
	        cost = newcost;
	        newcost = swap;                          
	    }                                                                               
	                                                                          
	    // the distance is the cost for transforming all letters in both strings        
	    return cost[len0 - 1] ;                                                          
	}
 
 public static double levenshteinDistance (CharSequence lhs, CharSequence rhs) {    
	
	    int len0 = lhs.length() + 1;                                                     
	    int len1 = rhs.length() + 1;                                                     
	     int maxLen= 0;
	     if (len0>len1) {
	    	 maxLen=len0;
	     }
	     else {
	    	  	maxLen= len1;
	     }
	    // the array of distances                                                       
	    int[] cost = new int[len0];                                                     
	    int[] newcost = new int[len0];                                                  
	                                                                                    
	    // initial cost of skipping prefix in String s0                                 
	    for (int i = 0; i < len0; i++) 
	    	cost[i] = i;                                     
	                                                                                    
	    // dynamically computing the array of distances                                  
	                                                                                    
	    // transformation cost for each letter in s1                                    
	    for (int j = 1; j < len1; j++) {                                                
	        // initial cost of skipping prefix in String s1                             
	        newcost[0] = j-1;                                                             
	                                                                                    
	        // transformation cost for each letter in s0                                
	        for(int i = 1; i < len0; i++) {                                             
	            // matching current letters in both strings                             
	            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;             
	                                                                                    
	            // computing cost for each transformation                               
	            int cost_replace = cost[i - 1] + match;                                 
	            int cost_insert  = cost[i] + 1;                                         
	            int cost_delete  = newcost[i - 1] + 1;                                  
	                                                                                    
	            // keep minimum cost                                                    
	            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
	        }                                                                           
	                                                                                    
	        // swap cost/newcost arrays                                                 
	        int[] swap = cost; 
	        cost = newcost;
	        newcost = swap;                          
	    }                                                                               
	                                                                          
	    // the distance is the cost for transforming all letters in both strings        
	    return (cost[len0 - 1]*1.0 )/ (len1+len0) ;                                                          
	}
 
 
 public static AlignObj levenshteinDistancewithAlignment (CharSequence lhs, CharSequence rhs) {    
		
   
			
		    int len0 = lhs.length() + 1;                                                     
		    int len1 = rhs.length() + 1;                                                     
		     int maxLen= 0;
		     if (len0>len1) {
		    	 maxLen=len0;
		     }
		     else {
		    	  	maxLen= len1;
		     }
		    // the array of distances                                                       
		    int[] cost = new int[len0];                                                     
		    int[] newcost = new int[len0];    
		    String[] costString = new String[len0];                                                     
		    String[] newcostString = new String[len0];    
		     String[][] alignment= new String [len1][len0];                                                                              
		    // initial cost of skipping prefix in String s0                                 
		    for (int i = 0; i < len0; i++) 
		    	cost[i] = i;                                     
		    alignment[0][0] ="";newcostString[0] ="";
		    for (int i = 1; i < len0; i++) 
		    	alignment[0][i] =alignment[0][i-1]+ "> Deletion " + lhs.charAt(i-1);
		    for (int i = 1; i < len1; i++) 
		    	alignment[i][0] =alignment[i-1][0]+ "> Insertion " + rhs.charAt(i-1);
		    // dynamically computing the array of distances                                  
		    boolean deleted= true;                                                                                
		    // transformation cost for each letter in s1                                    
		    for (int j = 1; j < len1; j++) {                                                
		        // initial cost of skipping prefix in String s1                             
		        newcost[0] = j-1;                                                             
		        // transformation cost for each letter in s0                                
		        for(int i = 1; i < len0; i++) {                                             
		            // matching current letters in both strings                             
		            int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 2;             
		                                                                                    
		            // computing cost for each transformation                               
		            int cost_replace = cost[i - 1] + match;                                 
		            int cost_insert  = cost[i] + 1;                                         
		            int cost_delete  = newcost[i - 1] + 1;                                  
		                                                                                    
		            // keep minimum cost                                                    
		            newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
		            if (match==0)
		            	alignment[j][i]= alignment[j-1][i-1]+"> Sync "+ rhs.charAt(j - 1);
		            else { if (Math.min(cost_insert, cost_delete) < cost_replace ) 
		        	  	{
		            		if(cost_insert>cost_delete)
		            			alignment[j][i]= alignment[j][i-1]+"> Deletion " + lhs.charAt(i-1);
		            		else
		            			alignment[j][i]= alignment[j-1][i]+ "> Insertion " + rhs.charAt(j-1);
		        	  	}
		            else
		            	alignment[j][i]= alignment[j-1][i-1]+ "> Insertion " + rhs.charAt(j-1) +"> Deletion " + lhs.charAt(i-1);

		            }      
		        }
		                                                                                  
		        // swap cost/newcost arrays                                                 
		        int[] swap = cost; 
		        cost = newcost;
		        newcost = swap;         

		       
		    }                                         
		    String align = alignment[len1-1][len0-1].substring(1);
		    AlignObj alignObj = new AlignObj(align, (cost[len0 - 1]*1.0 ) / (len1+len0)) ;                                                                    
		    // the distance is the cost for transforming all letters in both strings        
		    return alignObj;//alignment[len0][len0-1] ;                                                          
		}

 
 
 public static void quicksort(float[] main, int[] index) {
	    quicksort(main, index, 0, index.length - 1);
	}

	// quicksort a[left] to a[right]
	public static void quicksort(float[] a, int[] index, int left, int right) {
	    if (right <= left) return;
	    int i = partition(a, index, left, right);
	    quicksort(a, index, left, i-1);
	    quicksort(a, index, i+1, right);
	}

	// partition a[left] to a[right], assumes left < right
	private static int partition(float[] a, int[] index, 
	int left, int right) {
	    int i = left - 1;
	    int j = right;
	    while (true) {
	        while (less(a[++i], a[right]))      // find item on left to swap
	            ;                               // a[right] acts as sentinel
	        while (less(a[right], a[--j]))      // find item on right to swap
	            if (j == left) break;           // don't go out-of-bounds
	        if (i >= j) break;                  // check if pointers cross
	        exch(a, index, i, j);               // swap two elements into place
	    }
	    exch(a, index, i, right);               // swap with partition element
	    return i;
	}

	// is x < y ?
	private static boolean less(float x, float y) {
	    return (x < y);
	}

	// exchange a[i] and a[j]
	private static void exch(float[] a, int[] index, int i, int j) {
	    float swap = a[i];
	    a[i] = a[j];
	    a[j] = swap;
	    int b = index[i];
	    index[i] = index[j];
	    index[j] = b;
	}
 
 
 
	
	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
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
												
				if (label.equals(id)) {
					mapping.put(t, evClass);
					mapped = true;
					break;
				}
			}
		}

		return mapping;
		}



	
	
	public static AlignmentReplayResult calculateAlignmentWithICC(final PluginContext context, IncrementalReplayer replayer, PetrinetGraph net, XLog log, IccParameters parameters, TransEvClassMapping mapping) 
	{	

		IncrementalConformanceChecker icc =new IncrementalConformanceChecker(context, replayer, parameters, log, net);
		IccResult iccresult = icc.apply(context, log, net, mapping);
		Map<String, Integer> asynchMoveAbs=new TreeMap<String, Integer>();
		Map<String, Double> asynchMoveRel=new TreeMap<String, Double>();
		
		if(parameters.getGoal().equals("alignment")) {
			int asynchMovesSize=iccresult.getAlignmentContainer().getAsynchMoves().size();

			for (String key:iccresult.getAlignmentContainer().getAsynchMoves().elementSet()) {
				int absValue=iccresult.getAlignmentContainer().getAsynchMoves().count(key);
				double relValue=(double)absValue/(double)asynchMovesSize;
				asynchMoveAbs.put(key, absValue);
				asynchMoveRel.put(key, relValue);
			}
			AlignmentReplayResult result = new AlignmentReplayResult(iccresult.getFitness(), iccresult.getTime(), iccresult.getTraces(), asynchMovesSize, asynchMoveAbs, asynchMoveRel);
			return result;
		}
		else {
			AlignmentReplayResult result = new AlignmentReplayResult(iccresult.getFitness(), iccresult.getTime(), iccresult.getTraces(), -1, asynchMoveAbs, asynchMoveRel);
			return result;		
			}
		
		}

	
	
	public static class AlignObj {
		  public final String Alignment;
		  public final double cost;

		  public AlignObj(String Alignment, double d) {
		    this.Alignment = Alignment;
		    this.cost = d;
		  }
		}
	
	
	

	
}
	



	
	
  



    
    
    
    
    
    

    
    
    

    
    

    

	