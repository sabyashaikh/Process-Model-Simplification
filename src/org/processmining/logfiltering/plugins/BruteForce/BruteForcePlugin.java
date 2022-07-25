package org.processmining.logfiltering.plugins.BruteForce;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logfiltering.algorithms.EvaluationTest;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.IllegalTransitionException;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduce.ReductionFailedException;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTreeReduceParametersForPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.reduceacceptingpetrinet.ReduceAcceptingPetriNetKeepLanguage;
import org.processmining.plugins.alignetc.core.ReplayAutomaton;
import org.processmining.plugins.alignetc.result.AlignETCResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.directlyfollowsmodel.DirectlyFollowsModel;
import org.processmining.plugins.directlyfollowsmodel.mining.DFMMiningParameters;
import org.processmining.plugins.directlyfollowsmodel.mining.plugins.DirectlyFollowsModelMinerDialog;
import org.processmining.plugins.etconformance.ETCPlugin;
import org.processmining.plugins.etconformance.ETCResults;
import org.processmining.plugins.etconformance.ETCSettings;
import org.processmining.plugins.inductiveminer2.helperclasses.MultiIntSet;
import org.processmining.plugins.inductiveminer2.helperclasses.graphs.IntGraph;
import org.processmining.plugins.inductiveminer2.logs.IMLog;
import org.processmining.plugins.inductiveminer2.logs.IMLogImpl;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerDialog;
import org.processmining.plugins.inductiveminer2.plugins.InductiveMinerWithoutLogDialog;
import org.processmining.plugins.inductiveminer2.withoutlog.InductiveMinerWithoutLog;
import org.processmining.plugins.inductiveminer2.withoutlog.MiningParametersWithoutLog;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsd;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.DfgMsdImpl;
import org.processmining.plugins.inductiveminer2.withoutlog.dfgmsd.Log2DfgMsd;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;

import nl.tue.astar.AStarException;

@Plugin(name = "Brute_Force_Attacker:D", parameterLabels = { "Event Log" }, returnLabels = { "Petri net", "INI Marking", "FIN Marking" }, returnTypes = { Petrinet.class, Marking.class, Marking.class })
public class BruteForcePlugin {
	@UITopiaVariant(affiliation = "RWTH Aachen University", author = "Mohammadreza", email = "FaniSani@pads.rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public Object[]  filterLogUsingConforti(UIPluginContext context, XLog rawLog) throws IOException, AStarException, ConnectionCannotBeObtained, IllegalTransitionException {
		DirectlyFollowsModelMinerDialog dialog = new DirectlyFollowsModelMinerDialog(rawLog);
		InteractionResult result = context.showWizard("Mine using Directly Follows Model Miner", true, true, dialog);
		Petrinet net =null;
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(false);
			return null;
		}
		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		DFMMiningParameters parameters = dialog.getMiningParameters();
		
		

		//check that the log is not too big and mining might take a long time
		


		
		
		
		IMLog log = new IMLogImpl(rawLog, parameters.getClassifier(), parameters.getLifeCycleClassifier());
		context.log("Mining...");
		DirectlyFollowsModel dfg = Log2DfgMsd.convert(log);
		 Iterable<Long> dfgC = dfg.getDirectlyFollowsGraph().getEdges();
		 Collection<Long> dfgList = new ArrayList<Long>();
		 List<Integer> dfgList2 = new ArrayList<Integer>();
		 List<Integer> dfgListOrdered = new ArrayList<Integer>();
		 List<Integer> DfgListLow = new ArrayList<Integer>();
		 for (Long df :dfgC) {
			 dfgList.add(df);
		 }
		 DfgMsd dfgMSD = Log2DfgMsd.convert(rawLog, MiningParameters.getDefaultClassifier(),
					MiningParameters.getDefaultLifeCycleClassifier());
		 InductiveMinerDialog dialog3= new InductiveMinerDialog(rawLog);
		 InteractionResult result3 = context.showWizard("Mine using Inductive Miner", true, true, dialog);
			if (result != InteractionResult.FINISHED) {
				context.getFutureResult(0).cancel(false);
				return null;
			}
			
			
			InductiveMinerWithoutLogDialog dialog2 = new InductiveMinerWithoutLogDialog(dfgMSD);
			InteractionResult result2 = context.showWizard("Mine using Inductive Miner", true, true, dialog);
			if (result != InteractionResult.FINISHED) {
				context.getFutureResult(0).cancel(false);
				return null;
			}
			
			MiningParametersWithoutLog parameters2 = dialog2.getMiningParameters();
			org.processmining.plugins.inductiveminer2.mining.MiningParameters parameters3 = dialog3.getMiningParameters();
			Canceller canceller = new Canceller() { public boolean isCancelled() { return false; } };
			
			
			
			
		 DirectlyFollowsModel dfgReference = dfg.clone();
		 DfgMsd  dfgZero = dfgMSD.clone();
		// MultiIntSet s = dfgZero.getActivities();s.remove(2);
		 IntGraph nn = dfg.getDirectlyFollowsGraph();
		 int count=0;
		 int nonZero=0;
		 int[][] EdgeFreq = new int[dfg.getNumberOfActivities()][dfg.getNumberOfActivities()]; 
		 int [] InputFreq= new int[dfg.getNumberOfActivities()];
		 int [] OutputFreq= new int[dfg.getNumberOfActivities()];
		 for (int i = 0; i < dfg.getNumberOfActivities(); i++) {
			for (int j = 0; j < dfg.getNumberOfActivities(); j++) {
				
				if(dfg.getDirectlyFollowsGraph().getEdgeWeight(i, j) > 0 ) {
					//dfgList2.add(count);
					OutputFreq[i]=(int) (OutputFreq[i]+dfg.getDirectlyFollowsGraph().getEdgeWeight(i, j));
					InputFreq[j]=(int) (InputFreq[j]+dfg.getDirectlyFollowsGraph().getEdgeWeight(i, j));
					dfgZero.getDirectlyFollowsGraph().addEdge(i,j,-dfg.getDirectlyFollowsGraph().getEdgeWeight(i, j));
					EdgeFreq[i][j]=(int) dfg.getDirectlyFollowsGraph().getEdgeWeight(i, j);
					
				}
				count++;
			}
		}
		 
		 
		 /// put weight (i.e., probability) WieghtScore and Final Score to each edge
		 count=0;
		 
		 double [][] EdgeProb= new double[InputFreq.length][OutputFreq.length];
		 double [][] EdgeFreqScore= new double[InputFreq.length][OutputFreq.length];
		 double [][] EdgeFinalScore= new double[InputFreq.length][OutputFreq.length];
		 double [][]    EdgeFinalScoreArray = new double [InputFreq.length*InputFreq.length][2];
		 for (int i = 0; i < InputFreq.length; i++) {
			for (int j = 0; j < OutputFreq.length; j++) {
				
				EdgeProb[i][j] = (double) dfg.getDirectlyFollowsGraph().getEdgeWeight(i, j)/InputFreq[i];
				EdgeFreqScore[i][j]= (double) EdgeFreq[i][j]/ rawLog.size();
				EdgeFinalScore[i][j]= (0.50 * EdgeFreqScore[i][j]) + (0.50* EdgeProb[i][j]);
				EdgeFinalScoreArray[count][0]=EdgeFinalScore[i][j];
				EdgeFinalScoreArray[count][1]=(double) count;
				if(EdgeFinalScore[i][j] <0.999 && EdgeFinalScore[i][j] > 0) {
					if (EdgeFinalScore[i][j] >0.000001)
						dfgList2.add(count);
					else
						DfgListLow.add(count);
				}
				
				count++;
			}
		}
		
		 Arrays.sort(EdgeFinalScoreArray,new java.util.Comparator<double[]>() {
			    public int compare(double[] a, double[] b) {
			        return Double.compare(a[0], b[0]);
			    }
			});
		 
		 for (int i = 0; i < EdgeFinalScoreArray.length; i++) {
			if (EdgeFinalScoreArray[i][0]> 0) {
				dfgListOrdered.add((int) EdgeFinalScoreArray[i][1]);
			}
		}
		 List<List<java.lang.Integer>> OrderedLists = getSublistsOrdered(dfgListOrdered);
		 
		 
		 DfgMsd dfgTemp2= dfgMSD.clone();
		
		
		 count=0;
		 
		 
		 
		 
		 
		 EfficientTree tree =null;
		 Marking initialMarking=null;
		 Marking finalMarking=null;
		 
		 
		 
		 
		 
		 
		 
		 
	
		 if(1==2) {
			 /*dfgZero=RemoveActivity(dfgZero, 4);
			 dfgZero=RemoveActivity(dfgZero, 5);
			 dfgZero=RemoveActivity(dfgZero, 2);
			 dfgZero=RemoveActivity(dfgZero, 6);
			 dfgZero=RemoveActivity(dfgZero, 7);
			 dfgZero=RemoveActivity(dfgZero, 3);
			 dfgZero=RemoveActivity(dfgZero, 1);
			 dfgZero=RemoveActivity(dfgZero, 9);
			 dfgZero=RemoveActivity(dfgZero, 8);
			 dfgZero=RemoveActivity(dfgZero, 10);*/

			for (int i = 1; i < dfgMSD.getDirectlyFollowsGraph().getNumberOfNodes(); i++) {
				dfgZero.getDirectlyFollowsGraph().addEdge(i,1,-dfg.getDirectlyFollowsGraph().getEdgeWeight(i, 1));
				 //dfgTemp2.getDirectlyFollowsGraph().addEdge(3,i,-dfg.getDirectlyFollowsGraph().getEdgeWeight(12, i));
				 //dfgTemp2.getDirectlyFollowsGraph().addEdge(i,4,-dfg.getDirectlyFollowsGraph().getEdgeWeight(i, 3));
				 //dfgTemp2.getDirectlyFollowsGraph().addEdge(5,i,-dfg.getDirectlyFollowsGraph().getEdgeWeight(5, i));
			}
			// dfgTemp2=RemoveActivity(dfgTemp2, 12);
			// dfgTemp2=RemoveActivity(dfgTemp2, 5);
			 IntGraph x = dfgTemp2.getDirectlyFollowsGraph();
			 tree = InductiveMinerWithoutLog.mineEfficientTree (dfgZero, parameters2,canceller);
				
				AcceptingPetriNet petri2 = null;
				if (tree != null) {
					try {
						EfficientTreeReduce.reduce(tree, new EfficientTreeReduceParametersForPetriNet(false));
					} catch (UnknownTreeNodeException | ReductionFailedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					petri2 = EfficientTree2AcceptingPetriNet.convert(tree);
				}
				
				ReduceAcceptingPetriNetKeepLanguage.reduce(petri2, canceller);
				net = petri2.getNet();
				initialMarking = petri2.getInitialMarking();
				finalMarking = new ArrayList<Marking>(petri2.getFinalMarkings()).get(0);

				
				context.getConnectionManager().addConnection(new InitialMarkingConnection(net, initialMarking));
				context.getConnectionManager().addConnection(new FinalMarkingConnection(net, finalMarking));
			 
				Object[] ret2 = new Object[3];
				ret2[0] = net;
				ret2[1] = initialMarking;
				ret2[2] = finalMarking;
				
				XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
				XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
				TransEvClassMapping mapping = constructMapping(net, rawLog, dummyEvClass, eventClassifier);
  				double fitness =new EvaluationTest(rawLog, net, 1000000,initialMarking, finalMarking,mapping).getFFitness();
				ETCResults res = new ETCResults();//Create the result object to store the settings on it
				ETCSettings sett = new ETCSettings(res);
				
				ETCPlugin etcPlugin = new ETCPlugin();
				Object[] etcResults = etcPlugin.doETC(context, rawLog, net, initialMarking, mapping, res);
				//double fitness = (double)(res.getNTraces() - res.getnNonFitTraces())/(double)res.getNTraces();
				double precision = res.getEtcp();
				double FMeasure= 2*precision*fitness/(fitness+precision);
				
			
		        return ret2;
		 }
		 
		 double FMmaximum=0;
		 
		 
		 
		 List[] SubsetArrayOrdered = OrderedLists.toArray(new List[OrderedLists.size()]);
		 double[] tester = new double[SubsetArrayOrdered.length];
		 double[] tester2 = new double[SubsetArrayOrdered.length];
		 if(1==1) {
		 for (int i = 0; i < SubsetArrayOrdered.length-1 ; i++) {
				// if (SubsetArray[i].size()> 8) {
			 		double ActualFitness=0; 
			 		double ActualPrecision=0;
			 		double PossibleFM=0;
				 	int[] InputFreqTemp = InputFreq.clone();
				 	int[] outputFreqTemp = OutputFreq.clone();
					 DfgMsd dfgTemp= dfgTemp2.clone();
					 List TempList = SubsetArrayOrdered[i];
					 Integer[] TempArray = (java.lang.Integer[]) TempList.toArray(new Integer[TempList.size()]);
					 for (int j = 0; j < TempArray.length; j++) {
						 dfgTemp.getDirectlyFollowsGraph().addEdge(TempArray[j]/dfg.getNumberOfActivities(), TempArray[j]%dfg.getNumberOfActivities(), - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
						 outputFreqTemp[TempArray[j]/dfg.getNumberOfActivities()]= (int) (outputFreqTemp[TempArray[j]/dfg.getNumberOfActivities()]- dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
						 InputFreqTemp[TempArray[j]%dfg.getNumberOfActivities()]=   (int) (InputFreqTemp[TempArray[j]%dfg.getNumberOfActivities()] - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
					 }
					 
					 for (int j = InputFreq.length-1; j >=0 ; j--) {
						 if ( InputFreqTemp[j] ==0 && outputFreqTemp[j] ==0) {
							 dfgTemp=RemoveActivity(dfgTemp, j);
							 //MultiIntSet MultiInt = dfgTemp.getActivities();
							 //MultiInt.remove(j);
						 }
					}
					 
						tree = InductiveMinerWithoutLog.mineEfficientTree (dfgTemp, parameters2,canceller);
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
						
						ReduceAcceptingPetriNetKeepLanguage.reduce(petri, canceller);
						net = petri.getNet();
						initialMarking = petri.getInitialMarking();
						finalMarking = new ArrayList<Marking>(petri.getFinalMarkings()).get(0);

						
						context.getConnectionManager().addConnection(new InitialMarkingConnection(net, initialMarking));
						context.getConnectionManager().addConnection(new FinalMarkingConnection(net, finalMarking));
						
						Object[] ret2 = new Object[3];
						ret2[0] = net;
						ret2[1] = initialMarking;
						ret2[2] = finalMarking;
						
						
						PnmlExportNetToPNML cc =new PnmlExportNetToPNML();
						File file= new File("D:/PHD/Experiments/NewPetriNets/BPIC2018/Tel/Tel_ArtAdded_All_O"+i+".pnml");
		 				cc.exportPetriNetToPNMLFile(context, net, file);
						
						XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
						XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
						TransEvClassMapping mapping = constructMapping(net, rawLog, dummyEvClass, eventClassifier);
		  				ActualFitness =new EvaluationTest(rawLog, net, 1000000,initialMarking, finalMarking,mapping).getFFitness();
						ETCResults res = new ETCResults();//Create the result object to store the settings on it
						ETCSettings sett = new ETCSettings(res);
						
						ETCPlugin etcPlugin = new ETCPlugin();
						Object[] etcResults = etcPlugin.doETC(context, rawLog, net, initialMarking, mapping, res);
						//double fitness = (double)(res.getNTraces() - res.getnNonFitTraces())/(double)res.getNTraces();
						 ActualPrecision = res.getEtcp();
						double ActualFMeasure= 2*ActualPrecision*ActualFitness/(ActualFitness+ActualPrecision);
						if (ActualFMeasure > FMmaximum) {
							FMmaximum= ActualFMeasure;
						}
						PossibleFM = 2*1.0*ActualFitness/(ActualFitness+1.0);
						
						
					
						
						/*
						
						
						XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
						XEventClassifier eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
						
						TransEvClassMapping mapping = constructMapping(net, rawLog, dummyEvClass, eventClassifier);
						
						ETCResults res = new ETCResults();//Create the result object to store the settings on it
						ETCSettings sett = new ETCSettings(res);
						
						ETCPlugin etcPlugin = new ETCPlugin();
						Object[] etcResults = etcPlugin.doETC(context, rawLog, net, initialMarking, mapping, res);
						double fitness = (double)(res.getNTraces() - res.getnNonFitTraces())/(double)res.getNTraces();
						double precision = res.getEtcp();
						AbstractPetrinetReplayer<?, ?>   replayEngine = new PetrinetReplayerWithILP(true,true);
						PluginContext pluginContext =null; 
						Map<Transition, Integer> costMOS = constructMOSCostFunction(net);
						Map<XEventClass, Integer> costMOT = constructMOTCostFunction(net, rawLog, eventClassifier);*/
						
						/*IPNReplayParameter parametersPR = new CostBasedCompleteParam(costMOT, costMOS);
						PNRepResult PRresult = null;
						try {
							 PRresult =replayEngine.replayLog(context, net, rawLog, mapping, parametersPR);
						} catch (AStarException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
						System.out.println(i +" / "+ SubsetArrayOrdered.length );
						System.out.println(ActualFitness);
						tester[i]=ActualFitness;
						tester2[i]=ActualFMeasure;
			//	 }
					
			}
	}
		 
		
		 
		
		 
		 
		// \*
				 Integer[] RemovedEdges = DfgListLow.toArray(new Integer[DfgListLow.size()]);
				 
				 for (int i = 0; i < RemovedEdges.length; i++) {
					 dfgTemp2.getDirectlyFollowsGraph().addEdge(RemovedEdges[i]/dfg.getNumberOfActivities(), RemovedEdges[i]%dfg.getNumberOfActivities(), - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(RemovedEdges[i]));
					 OutputFreq[RemovedEdges[i]/dfg.getNumberOfActivities()]= (int) (OutputFreq[RemovedEdges[i]/dfg.getNumberOfActivities()]- dfgReference.getDirectlyFollowsGraph().getEdgeWeight(RemovedEdges[i]));
					 InputFreq[RemovedEdges[i]%dfg.getNumberOfActivities()]=   (int) (InputFreq[RemovedEdges[i]%dfg.getNumberOfActivities()] - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(RemovedEdges[i]));
				}
				 //*/
				 
		// List<List<java.lang.Integer>> SubsetLists = getSublists(dfgList2);
		// List[] SubsetArray = SubsetLists.toArray(new List[SubsetLists.size()]);
		 List<Integer> TempSublist = new ArrayList<Integer>();
		 List<List<Integer>> Candidates = new ArrayList<List<Integer>>();
		 List<List<Integer>> UnCandidates = new ArrayList<List<Integer>>();
		 int[]  newItem =new int[1];
		 Candidates.add(TempSublist);combinationUtil(newItem,0,0,dfgList2,Candidates);
		// UnCandidates.add(Candidates.get(2)); UnCandidates.add(Candidates.get(3));
		// Candidates=ExtendSublist(Candidates.get(0),dfgList2,Candidates,UnCandidates);
 		 double[] tester3 = new double[Candidates.size()*10000];
		 double[] tester4 = new double[Candidates.size()*10000];

		 int ThebestModel=0;
		 double ThebestFit=0;
		 double ThebestPrec=0;
		 
		 
		 for (int i = 0; i < Candidates.size(); i++) {
			 //if (SubsetArray[i].size()!= RemovedEdges.length) {
			 
			 int[] InputFreqTemp = InputFreq.clone();
			 	int[] outputFreqTemp = OutputFreq.clone();
				 DfgMsd dfgTemp= dfgTemp2.clone();
				 List TempList = Candidates.get(i);
				 Integer[] TempArray = (java.lang.Integer[]) TempList.toArray(new Integer[TempList.size()]);
				 for (int j = 0; j < TempArray.length; j++) {
					 dfgTemp.getDirectlyFollowsGraph().addEdge(TempArray[j]/dfg.getNumberOfActivities(), TempArray[j]%dfg.getNumberOfActivities(), - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
					 outputFreqTemp[TempArray[j]/dfg.getNumberOfActivities()]= (int) (outputFreqTemp[TempArray[j]/dfg.getNumberOfActivities()]- dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
					 InputFreqTemp[TempArray[j]%dfg.getNumberOfActivities()]=   (int) (InputFreqTemp[TempArray[j]%dfg.getNumberOfActivities()] - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
				 }
				
				 for (int j = InputFreq.length-1; j >=0 ; j--) {
					 if ( InputFreqTemp[j] ==0 && outputFreqTemp[j] ==0) {
						 dfgTemp=RemoveActivity(dfgTemp, j);
						 //MultiIntSet MultiInt = dfgTemp.getActivities();
						 //MultiInt.remove(j);
					 }
				}
				 
					tree = InductiveMinerWithoutLog.mineEfficientTree (dfgTemp, parameters2,canceller);
					//tree=InductiveMiner.mineEfficientTree(log, parameters3, canceller) ;                   
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
					
					ReduceAcceptingPetriNetKeepLanguage.reduce(petri, canceller);
					net = petri.getNet();
					initialMarking = petri.getInitialMarking();
					finalMarking = new ArrayList<Marking>(petri.getFinalMarkings()).get(0);

					
					context.getConnectionManager().addConnection(new InitialMarkingConnection(net, initialMarking));
					context.getConnectionManager().addConnection(new FinalMarkingConnection(net, finalMarking));
					
					/*PnmlExportNetToPNML cc =new PnmlExportNetToPNML();
					File file= new File("D:/PHD/Experiments/NewPetriNets/BPIC2018/Tel/Tel_ArtAdded_All_N"+i+".pnml");
	 				cc.exportPetriNetToPNMLFile(context, net, file);*/
					
					/*
					
					
					XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
					XEventClassifier eventClassifier = XLogInfoImpl.STANDARD_CLASSIFIER;
					
					TransEvClassMapping mapping = constructMapping(net, rawLog, dummyEvClass, eventClassifier);
					
					ETCResults res = new ETCResults();//Create the result object to store the settings on it
					ETCSettings sett = new ETCSettings(res);
					
					ETCPlugin etcPlugin = new ETCPlugin();
					Object[] etcResults = etcPlugin.doETC(context, rawLog, net, initialMarking, mapping, res);
					double fitness = (double)(res.getNTraces() - res.getnNonFitTraces())/(double)res.getNTraces();
					double precision = res.getEtcp();
					AbstractPetrinetReplayer<?, ?>   replayEngine = new PetrinetReplayerWithILP(true,true);
					PluginContext pluginContext =null; 
					Map<Transition, Integer> costMOS = constructMOSCostFunction(net);
					Map<XEventClass, Integer> costMOT = constructMOTCostFunction(net, rawLog, eventClassifier);*/
					
					/*IPNReplayParameter parametersPR = new CostBasedCompleteParam(costMOT, costMOS);
					PNRepResult PRresult = null;
					try {
						 PRresult =replayEngine.replayLog(context, net, rawLog, mapping, parametersPR);
					} catch (AStarException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}*/
	 				
	 				
	 				XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
					XEventClassifier eventClassifier = XLogInfoImpl.NAME_CLASSIFIER;
					TransEvClassMapping mapping = constructMapping(net, rawLog, dummyEvClass, eventClassifier);
	  				EvaluationTest Alignment = new EvaluationTest(rawLog, net, 1000000,initialMarking, finalMarking,mapping);
					double ActualFitness =Alignment.getFFitness();
					PNMatchInstancesRepResult align = Alignment.getReplayResult();
					PNMatchInstancesRepResult dddd = Alignment.getResTree();
					ETCResults res = new ETCResults();//Create the result object to store the settings on it
					ETCSettings sett = new ETCSettings(res);
					PNMatchInstancesRepResult ddd = Alignment.getResRepresentative();
					ETCPlugin etcPlugin = new ETCPlugin();
					
					
					
					
					Collection<AllSyncReplayResult> col = new ArrayList<AllSyncReplayResult>();
					double precision = 0;
					
						for (AllSyncReplayResult rep : align) {
				
							//Get all the attributes of the 1-alignment result
							List<List<Object>> nodes = new ArrayList<List<Object>>();
							nodes.addAll(rep.getNodeInstanceLst());
				
							List<List<StepTypes>> types = new ArrayList<List<StepTypes>>();
							types.addAll(rep.getStepTypesLst());
				
							SortedSet<Integer> traces = rep.getTraceIndex();
							boolean rel = rep.isReliable();
				
							//Create a n-alignment result with this attributes
							AllSyncReplayResult allRep = new AllSyncReplayResult(nodes, types, -1, rel);
							allRep.setTraceIndex(traces);//The creator not allow add the set directly
							col.add(allRep);
						}
						PNMatchInstancesRepResult alignments = new PNMatchInstancesRepResult(col);
				
						AlignETCResult res2 = new AlignETCResult();
				
						ReplayAutomaton ra = null;
						
						ra = new ReplayAutomaton(context, alignments, net);
						
						ra.cut(0.0);
						ra.extend(net, initialMarking);
						ra.conformance(res2);
						precision = res2.ap;
					
					Object[] etcResults = etcPlugin.doETC(context, rawLog, net, initialMarking, mapping, res);
					//double fitness = (double)(res.getNTraces() - res.getnNonFitTraces())/(double)res.getNTraces();
					 double ActualPrecision = precision;//res.getEtcp();
					double ActualFMeasure= 2*ActualPrecision*ActualFitness/(ActualFitness+ActualPrecision);
					if (ActualFMeasure > FMmaximum) {
						FMmaximum= ActualFMeasure;
						ThebestModel=i;
						ThebestFit=ActualFitness;
						ThebestPrec=ActualPrecision;
					}
					double PossibleFM = 2*1.0*ActualFitness/(ActualFitness+1.0);
  					if(ActualPrecision==0 || PossibleFM <FMmaximum) {
						UnCandidates.add(TempList);
					}else {ExtendSublist(TempList,dfgList2,Candidates,UnCandidates);}
					
					System.out.println( i +" / "+ Candidates.size() );
					tester3[i]=ActualFitness;
					tester4[i]=ActualFMeasure;
				// }
		}
		 
		 
		 
		 
		 int[] InputFreqTemp = InputFreq.clone();
		 	int[] outputFreqTemp = OutputFreq.clone();
			 DfgMsd dfgTemp= dfgTemp2.clone();
			 List TempList = Candidates.get(ThebestModel);
			 Integer[] TempArray = (java.lang.Integer[]) TempList.toArray(new Integer[TempList.size()]);
			 for (int j = 0; j < TempArray.length; j++) {
				 dfgTemp.getDirectlyFollowsGraph().addEdge(TempArray[j]/dfg.getNumberOfActivities(), TempArray[j]%dfg.getNumberOfActivities(), - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
				 outputFreqTemp[TempArray[j]/dfg.getNumberOfActivities()]= (int) (outputFreqTemp[TempArray[j]/dfg.getNumberOfActivities()]- dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
				 InputFreqTemp[TempArray[j]%dfg.getNumberOfActivities()]=   (int) (InputFreqTemp[TempArray[j]%dfg.getNumberOfActivities()] - dfgReference.getDirectlyFollowsGraph().getEdgeWeight(TempArray[j]));
			 }
			
			 for (int j = InputFreq.length-1; j >=0 ; j--) {
				 if ( InputFreqTemp[j] ==0 && outputFreqTemp[j] ==0) {
					 dfgTemp=RemoveActivity(dfgTemp, j);
					 //MultiIntSet MultiInt = dfgTemp.getActivities();
					 //MultiInt.remove(j);
				 }
			}
			 
				tree = InductiveMinerWithoutLog.mineEfficientTree (dfgTemp, parameters2,canceller);
				//tree=InductiveMiner.mineEfficientTree(log, parameters3, canceller) ;                   
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
				
				ReduceAcceptingPetriNetKeepLanguage.reduce(petri, canceller);
				net = petri.getNet();
		 
		 
		 
		 
/*	
		for (int j = 0; j < 155; j++) {
			 System.out.println(j + " Fitness "+tester[j]+"     FM "+ tester2[j]);
		}
		 System.out.println("=========================================");
		 for (int j = 0; j < tester3.length; j++) {
			 System.out.println(j + " Fitness "+tester3[j]+"     FM "+ tester4[j]);
		}
	 */
		System.out.println(FMmaximum);
		System.out.println(ThebestFit);
		System.out.println(ThebestPrec);
		Object[] ret = new Object[3];
		ret[0] = net;
		ret[1] = initialMarking;
		ret[2] = finalMarking;
		
        return ret;
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
		System.out.println("mapping");
		System.out.println(mapping);

		return mapping;
		}
	
	static <Integer> List<List<Integer>> getSublists(List<Integer> list) {
	    if (list.isEmpty()) {
	        // if empty, return just that empty list
	        return Collections.singletonList(list);
	    } else {
	        List<List<Integer>> sublists = new ArrayList<List<Integer>>();
	        Integer first = list.get(0);
	        // for each sublist starting at second element...
	        for (List<Integer> sublist : getSublists(list.subList(1, list.size()))) {
	            //... add that sublist with and without the first element
	            // (two lines more, but this preserves the original order)
	            List<Integer> sublistWithFirst= new ArrayList<Integer>();
	            sublistWithFirst.add(first);
	            sublistWithFirst.addAll(sublist);
	            sublists.add(sublist);
	            sublists.add(sublistWithFirst);
	        }
	        return sublists;
	    }
	}
		
	
	
	
	static <Integer> List<List<Integer>> getSublistsOrdered(List<Integer> list) {
	    if (list.isEmpty()) {
	        // if empty, return just that empty list
	        return Collections.singletonList(list);
	    } else { 
	        List<List<Integer>> sublists = new ArrayList<List<Integer>>();
	        List <Integer> tempList =new ArrayList<Integer>();
	        for (int i = 0; i < list.size(); i++) {
	        	List <Integer> currentList= new ArrayList <Integer>(tempList);
	        	currentList.add(list.get(i));
				tempList.add(list.get(i));
				sublists.add(currentList);
			}
	        return sublists;
	    } 
	}
	static <Integer> List<List<Integer>> ExtendSublist(List <Integer> item, List<Integer> list,List<List<Integer>> Condidates,List<List<Integer>> UnCondidates) {
		int m =0;
		if (item.size()>0)
			m = (int) item.get(item.size()-1);
		for (Integer iint:list) {
			if(!item.contains(iint) && (int)iint>m) {
				List <Integer> tempList = new ArrayList<Integer>();
				tempList.addAll(item);
				tempList.add(iint);
				boolean flag=true;
				for (int i = 0; i < UnCondidates.size(); i++) {
					boolean flag2=true;
					for (int j = 0; j < UnCondidates.get(i).size(); j++) {
						if(!tempList.contains(UnCondidates.get(i).get(j))){
							flag2=false;
						}
						if(flag2)
							flag=false;
					}
					
				}
				if(flag&& !Condidates.contains(tempList))
					Condidates.add(tempList);
			}
		//	Condidates.remove(item);
		}
		
		return Condidates;
	}
	
	void combinationUtil(int[] tempItems, int index, int i,List<Integer> list, List<List<Integer>> SubList){ 
// Current cobination is ready, print it 
if (index == tempItems.length) { 
	List<Integer> TempSublist = new ArrayList<Integer>();
for (int j = 0; j < tempItems.length; j++) {
	TempSublist.add(tempItems[j]); 
}
SubList.add(TempSublist); 
return; 
} 

// When no more elements are there to put in data[] 
if (i >= list.size()) 
return; 
int[] tempItems2 = tempItems.clone();
// current is included, put next at next location 
tempItems[index]=list.get(i);
combinationUtil(tempItems, index + 1, i + 1,list, SubList); 

// current is excluded, replace it with next 
// (Note that i+1 is passed, but index is not 
// changed) 

combinationUtil(tempItems2,  index, i + 1,list,SubList); 
} 
	private static Map<XEventClass, Integer> constructMOTCostFunction(PetrinetGraph net, XLog log,
			XEventClassifier eventClassifier) {
		Map<XEventClass, Integer> costMOT = new HashMap<XEventClass, Integer>();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (XEventClass evClass : summary.getEventClasses().getClasses()) {
			costMOT.put(evClass, 1);
		}

		return costMOT;
	}
	
	private static Map<Transition, Integer> constructMOSCostFunction(PetrinetGraph net) {
		Map<Transition, Integer> costMOS = new HashMap<Transition, Integer>();

		for (Transition t : net.getTransitions())
			if (t.isInvisible())
				costMOS.put(t, 0);
			else
				costMOS.put(t, 1);

		return costMOS;
	}
	
	private static Long[][]		RemoveActivity (Long[][] inputMatrix, int removed){
		if (removed> inputMatrix.length||inputMatrix.length==1 ) {		
			return inputMatrix;
		}
		Long[][] outPutMatrix= new Long[inputMatrix.length-1][inputMatrix.length];	
		int p = 0;
        for( int i = 0; i < inputMatrix.length; ++i)
        {
            if ( i == removed)
                continue;
            int q = 0;
            for( int j = 0; j < inputMatrix.length; ++j)
            {
                if ( j == removed)
                    continue;
                outPutMatrix[p][q] = inputMatrix[i][j];
                ++q;
            }
            ++p;
        }		
		return outPutMatrix;
	}
	
	private static DfgMsd RemoveActivity (DfgMsd inputDFG, int removed){
		DfgMsd TempDFG = inputDFG.clone();
		MultiIntSet M = TempDFG.getActivities();
		String[] Act = TempDFG.getAllActivities();
		List<String> list = new ArrayList<String>(Arrays.asList(Act));
		MultiIntSet InputMultiSet = inputDFG.getActivities();
		list.remove(Act[removed]);
		M.remove(removed);
		Act=list.toArray(new String[0]);
		DfgMsd outputDFG=new DfgMsdImpl(Act);
		
		
		
		for (int i = 0; i < InputMultiSet.setSize(); i++) {
			int t=i;
			if (i!= removed) {
				if (i>removed)
					t--;
				for (int j = 0; j < InputMultiSet.getCardinalityOf(i); j++) {
					outputDFG.addActivity(t);
				}
			}
		}
		
		Iterable<Long> E = inputDFG.getDirectlyFollowsGraph().getEdges();
		for (Long long1 : E) {
			
			if(inputDFG.getDirectlyFollowsGraph().getEdgeSourceIndex(long1)!=removed&&inputDFG.getDirectlyFollowsGraph().getEdgeTargetIndex(long1)!=removed) {
				int source = inputDFG.getDirectlyFollowsGraph().getEdgeSourceIndex(long1);
				if (source > removed)
					source--;
				int target = inputDFG.getDirectlyFollowsGraph().getEdgeTarget(long1);
				if (target > removed)
					target--;
				outputDFG.getDirectlyFollowsGraph().addEdge(source,target, inputDFG.getDirectlyFollowsGraph().getEdgeWeight(long1));
			}
		}
		
		return outputDFG;
	}
	
	
	static boolean SubSet(List<?> list, List<?> sublist) {
	    return Collections.indexOfSubList(list, sublist) != -1;
	}
	
	
	
	
	
}