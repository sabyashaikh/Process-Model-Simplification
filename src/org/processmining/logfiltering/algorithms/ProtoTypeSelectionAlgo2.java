package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
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
import org.processmining.logfiltering.legacy.plugins.logfiltering.enumtypes.ClusteringType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
public class ProtoTypeSelectionAlgo2 {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {
	

			long time = System.currentTimeMillis();
			
			XEventClassifier EventCol = parameters.getEventClassifier();
			
		/*	if (parameters.getPrototypeType()==PrototypeType.Simulation) {
				mapping=mapping2;
			}*/
			
			XFactory factory = XFactoryRegistry.instance().currentDefault();
			XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
			HashMap<String,String >ActivityCoder =new HashMap<String, String>();
			int LogSize = 0;
			PriorityQueue<Integer> pickedVariant=new PriorityQueue<>();
			SortedSet<String> eventAttributeSet = new TreeSet<String>();
			XAttributeMap eventAttributeMap;
			int KLength =parameters.getSubsequenceLength(); 
			int charcounter=65;
			ActivityCoder.put("ArtStart", Character.toString((char)charcounter));
			Set<String> ActivitySet = new HashSet<String>();
			for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
				charcounter++;
				ActivitySet.add(clazz.toString());
				ActivityCoder.put(clazz.toString(), Character.toString((char)charcounter));
			}
			charcounter++;
			ActivityCoder.put("ArtEnd", Character.toString((char)charcounter));
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
			 System.out.println(	System.currentTimeMillis() - time);
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
			 
			 Variant[] VariantSimilarity = new Variant[HashMaper.size()];
			 double[] [] distanceMatrix =new double[HashMaper.size()][HashMaper.size()];
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
					
					//INTRA= INTRA+(temp*1.0/(VariantProfile.length-1));
				}
						
							
			 
			 
			 double Threshold =1- parameters.getSubsequenceLength()*1.0/100;
			 
			 List<Integer> SampledList=  new ArrayList<>();
			 
			 int[] MostSimilarVariant= new int [HashMaper.size()];
			 
			 
			 
			 
			 
			 
			 
			 
			 long timer1 = System.currentTimeMillis() - time ;
			 ///////////////////////////Compute the Replay Values///////////////////
			
				int nThreads = 2;
				int costUpperBound = Integer.MAX_VALUE;
				// timeout per trace in milliseconds
				int timeoutMilliseconds = 10 * 1000;
				// preprocessing time to be added to the statistics if necessary
				long preProcessTimeNanoseconds = 0;
				
	///////////////////////////Selection Phase///////////////////////////////// 
			 ClusteringType  SelectiuonType=parameters.getClusterType(); 
			 switch (SelectiuonType) {
				
				case Automatic:
					 HashMap<Integer,Integer> SelectedList2=new HashMap<Integer,Integer>();
					 int K=2;
					 double rate=0;
					 double INTER=1000;
					 double INTRA=0;
					 List<Integer> instances= null;
					 boolean flag= true;
					 double pickChecker=-10;
					 while (flag) {
						
					
					Threshold=0.960;
					 //int K= (int) ((1-Threshold)* VariantInd.length);
					
					 int iterations=5;
					 for (int i = 0; i < K ; i++) {
						 List<Integer> tempList = new ArrayList<>();
						 Clusters.put(i, tempList);
						 SelectedList.put(i,i);
					}////KMEDOIDS
					 for (int i = 0; i < iterations; i++) {
						 Clusters=  FindCluster(distanceMatrix,SelectedList);
						 SelectedListTemp= UpdateKMedoids(SelectedList, distanceMatrix,VariantFreq,Clusters);
					}
					 
						 double sValue=0;
						 int Counterwrong=0;
						 for (int i = 0; i < Clusters.size(); i++) {
							 double aValue=0;
							 double bValue=0;
							 int cluster1= i;
							 List<Integer> instances1 = Clusters.get(i);
							 double temp2 =1000;
							 for (int j = 0; j < Clusters.size(); j++) {
								 
								 int cluster2= j;
								 List<Integer> instances2 = Clusters.get(j);
								 if(i==j) { ///same cluster
									 double temp=0;
									 for (int k = 0; k < instances2.size(); k++) {
											temp= temp+levenshteinDistance(  VariantChar[SelectedList.get(i)], VariantChar[instances2.get(k)] );
										}
									 aValue=aValue+ (temp/ instances2.size() );
								 }else {
									 double temp=0;
									 for (int k = 0; k < instances2.size(); k++) {
											temp= temp+levenshteinDistance(  VariantChar[SelectedList.get(i)], VariantChar[instances2.get(k)] );
										}
									 temp=temp/ instances2.size();
									 if(temp<temp2)
										 temp2=temp;
								 }
								 
							 }
							 
							 bValue=  temp2;
						sValue=sValue + (bValue-aValue)/ (Math.max(bValue, aValue));	 
						if ((bValue-aValue)/ (Math.max(bValue, aValue))<0)
							Counterwrong++;
						 }
						 sValue= sValue/VariantInd.length;
						 System.out.println(K+ "====>"+sValue);
						 System.out.println(K+ "====>"+Counterwrong);
					
					 for (int i = 0; i < Clusters.size(); i++) {
						 double temp=1000;
						 double temp2 =0;
						 for (int j = 0; j < Clusters.size(); j++) {
							if (levenshteinDistance (  VariantChar[SelectedList.get(i)], VariantChar[SelectedList.get(j)] )< temp && levenshteinDistance (  VariantChar[SelectedList.get(i)], VariantChar[SelectedList.get(j)] )> 0) {
								temp= levenshteinDistance (  VariantChar[SelectedList.get(i)], VariantChar[SelectedList.get(j)] ) ;
							}
							
							
						}
						 //if (INTER>temp)
							 INTER=INTER+temp;
						instances= Clusters.get(i);
						for (int j = 0; j < instances.size(); j++) {
							temp2= temp2+levenshteinDistance (  VariantChar[SelectedList.get(i)], VariantChar[instances.get(j)] );
						}
						temp2=(temp2 *1.0)/instances.size();
						INTRA= INTRA+temp2;
					}
					 INTER= INTER/Clusters.size();
					 rate = INTER*1.0/INTRA;
					 System.out.println(K+ "===>"+rate);
					 if (sValue > pickChecker) {
						 pickChecker=sValue;
						 HashMap<Integer,Integer> SelectedList3=(HashMap<Integer, Integer>) SelectedListTemp.clone();
						 SelectedList2=SelectedList3;
						 
					 }else {
						 flag=false;
						 if(K>21)
							 flag=false;
						 else {

						 }
					 }
					 K++;
					 
					 }
					
					 SelectedList=SelectedList2;
	 
			 break;
				case Manual:
					 int iterations=5;
					  K = parameters.getSubsequenceLength();
					 for (int i = 0; i < K ; i++) {
						 List<Integer> tempList = new ArrayList<>();
						 Clusters.put(i, tempList);
						 SelectedList.put(i,i);
					}////KMEDOIDS
					 for (int i = 0; i < iterations; i++) {
						 Clusters=  FindCluster(distanceMatrix,SelectedList);
						 SelectedListTemp= UpdateKMedoids(SelectedList, distanceMatrix,VariantFreq,Clusters);
					}
			  break;
			}
			 
			 for (int i = 0; i < SelectedList.size(); i++) {
					pickedVariant.add(SelectedList.get(i));
				}
				 for (int i = 0; i < VariantInd.length; i++) {
					 if (!pickedVariant.contains(i)) {
						 SampledList.add(i);
					 }
				 }
				 
				 
			 //////////////////////////////////////////////////
			 double[] [] distanceMatrix2 =new double[HashMaper.size()][HashMaper.size()];
			 for (int i = 0; i < VariantProfile.length; i++) {
				 double temp=0;
					for (int j = 0; j < VariantProfile.length; j++) {
						temp= temp+ levenshteinDistance (VariantChar[i],VariantChar[j]) ;
						distanceMatrix2[i][j]= levenshteinDistance (VariantChar[i],VariantChar[j]);
					}
					VariantSimilarity[i]= new Variant(i, temp );
				}
	
			 XLog TraceLog = factory.createLog();
			 TraceLog.setAttributes(InputLog.getAttributes());
			 XAttributeMapImpl case_map = new XAttributeMapImpl();
			 String case_id = String.valueOf(charcounter);
			 case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));	
			 //double ShortestPath=replayTraceOnNet( TraceLog,  net,  mapping);

			 
			 ///// Compute the actual alignment
			 for (int i = 0; i < SelectedList.size(); i++) {
				 
				 TraceLog.add(VariantMapper.get(SelectedList.get(i)));
				
				// AlignmentCosts[i] = 1- ( 1.0 /replayTraceOnNet( TraceLog,  net,  mapping)* (ShortestPath+VariantMapper.get(SelectedList.get(i)).size() ));
			}//
			 
			 
				return TraceLog;
	}

	public static String apply2(PluginContext context,XLog log, Petrinet net, MatrixFilterParameter parameters,
			TransEvClassMapping mapping) {
		// TODO Auto-generated method stub 
		//Martin Bauer
		long time = System.currentTimeMillis();
		double delta=0.01;
		double alpha=0.99;
		double epsilon=0.01;
		double k=0.6;
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
		if (iccParameters.getGoal().equals("alignment")&& !iccParameters.isApproximate()) replayer=new AlignmentReplayer(iccParameters);
		if (iccParameters.getGoal().equals("alignment") && iccParameters.isApproximate()) {
			replayer= new ApproxAlignmentReplayer(iccParameters);
			//replayer.init(context, net, log);
		}
				
		//make own parameter function for alignment/fitness
		AlignmentReplayResult result= calculateAlignmentWithICC(context, replayer, net, log, iccParameters, mapping);
		result.setTime(System.currentTimeMillis()-time);
		System.out.println("Fitness         : "+result.getFitness());
		System.out.println("Time(ms)        : "+result.getTime());
		System.out.println("Log Size        : "+result.getLogSize());
		System.out.println("No AsynchMoves  : "+result.getTotalNoAsynchMoves());
		System.out.println("AsynchMoves abs : "+result.getAsynchMovesAbs().toString());
		System.out.println("AsynchMoves rel : "+result.getAsynchMovesRel().toString());
		String outp=( 0 +"==>>"+ 1+"==>>"+ result.getFitness()+"==>>"+ result.getLogSize() +"==>>" + result.getTime()+"==>>" +0+"==>>"+ result.getFitness())+"==>>"+ result.getLogSize() ;
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
				 cost=cost+ (distanceMatrix[List.get(j)][List.get(j2)] * variantFreq[List.get(j2)]* variantFreq[List.get(j)]);
				 if( cost <distance) {
						distance = cost;
						SelectedList.put(i,List.get(j));
					}
			}
			 
		}
	}
	 
	 return SelectedList;
	 
 }
 
 
 
 public static AlignObj levenshteinDistanceCost (CharSequence lhs, CharSequence rhs) {    
		
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
	    AlignObj alignObj = new AlignObj(align, cost[len0 - 1]) ;                                                                    
	    // the distance is the cost for transforming all letters in both strings        
	    return alignObj;//alignment[len0][len0-1] ;                                                          
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
 
 public static double levenshteinDistanceCost2 (CharSequence lhs, CharSequence rhs) {    
		
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
	    return (cost[len0 - 1]*1.0 ) ;                                                          
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
		System.out.println("mapping");
		System.out.println(mapping);

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
		  public final int cost;

		  public AlignObj(String Alignment, int cost) {
		    this.Alignment = Alignment;
		    this.cost = cost;
		  }
		}
	

	
}
	



	
	
  



    
    
    
    
    
    

    
    
    

    
    

    

	