package org.processmining.logfiltering.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.logfiltering.parameters.AdjustingType;
import org.processmining.logfiltering.parameters.FilterLevel;
import org.processmining.logfiltering.parameters.FilterSelection;
import org.processmining.logfiltering.parameters.MatrixFilterParameter;
import org.processmining.logfiltering.parameters.ProbabilityType;

public class RepairBasedOnWindows {

	public static XLog apply(XLog InputLog, MatrixFilterParameter parameters) {
		 
		/////////////////Initialization/////////////////////////////////////////////
		XEventClassifier EventCol = parameters.getEventClassifier();
		XLog OutputLog = (XLog) InputLog.clone();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(InputLog, EventCol);
		
		int LogSize = 0;

		SortedSet<String> eventAttributeSet = new TreeSet<String>();
		XAttributeMap eventAttributeMap;
		int KLength =parameters.getSubsequenceLength(); 
		
		Set<String> ActivitySet = new HashSet<String>();
		for (XEventClass clazz : logInfo.getNameClasses().getClasses()){
			ActivitySet.add(clazz.toString());
		}
		//int ActivitiesSize = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]).size();
		int ActivitiesSize = ActivitySet.size();
		//Set<String> ActivitySet = eventAttributeValueSetMap.get(EventCol.getDefiningAttributeKeys()[0]);
		String[] Activities = ActivitySet.toArray(new String[ActivitiesSize]);
		List<String> ActivityList = java.util.Arrays.asList(Activities);
		int[] ActivityCount = new int[ActivitiesSize];
		String temp1 = new String();
		String temp2 = new String();
		String temp3 = new String();
		FilterLevel FilteringMethod = parameters.getFilterLevel();
		FilterSelection FilteringSelection =parameters.getFilteringSelection();
		HashMap<String, String> HashMaper =new HashMap<String, String>();
		HashMap<Integer, Integer> ReverseMapper =new HashMap<Integer, Integer>();
		HashMap<Integer, String[]> ActionMaper= new HashMap<Integer, String[]>();
		ArrayList<int[]> action = new ArrayList<int[]>();
		ArrayList<int[]> WinAction = new ArrayList<int []>();
		AdjustingType adjustingType = parameters.getAdjustingThresholdMethod();
		ProbabilityType probabilityType= parameters.getProbabilitycomutingMethod();
		int SimilarityWindow=3;
		
		//////////////////////////////// Computing Activities Frequency /////////////////////
		int [] NewInsert= new int [((ActivitiesSize+1)*2)+4]; //// +1 for start and end // *2 for left and right of subsequent 
		// +3 for frequency, length and normalization   
		//+2 for ActivitiesSize=> null pattern (L=0) and ActivitiesSize +1 => summary
		int [][] WindowsPatterns = new int [(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+2)] [((ActivitiesSize+1) * (ActivitiesSize+1) )+ 4 ]; 
		
		/// Find the number of traces and Frequency of activities and counting activities happens in the first place of the traces
		for (XTrace trace : OutputLog) {
			LogSize++;
			List<String> templist = new ArrayList<String>();
			for (XEvent event : trace) {
				//eventAttributeMap = event.getAttributes();
				temp1 = event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString();
				//				temp1 = eventAttributeMap.get(EventCol).toString();
				ActivityCount[ActivityList.indexOf(temp1)]++;
				
				eventAttributeMap = event.getAttributes();
				templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
				
			}
			String[] Event1 = new String[trace.size()];   
			Event1 = templist.toArray(new String[trace.size()]); // Trace in an Array form
			NewInsert[ActivityList.indexOf(Event1[0])]++;  // Counting how many times each activity comes at the first of Traces
		}
		
	
		///////////////////////////////////////////////Building Action and frequencies///////////////////////////////////
		
		int chCount =0;
		action.add(NewInsert);  /// add the general aggregated NewInsert to action 
		
		int GlobalCounter=0;
		int wA =0; int wB=0; // Make windows to Nulls Activities 
			//////////////////////////////////////////////////////////////////////////////////////////
		
		
		////// Len =0 jsut for windows of null pattern ! 
			 for (XTrace trace : InputLog) { // for each trace
				 /// Put trace to array
				 String[] Event1 = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) { 
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					Event1 = templist.toArray(new String[trace.size()]);
				///
					if(Event1.length >= 0){ 
						for (int i = 0; i <= Event1.length ; i++) { /// sliding window
							if (i==0){  /// for first position of sliding window
								wA= ActivitiesSize;
							}else{  
								 wA=ActivityList.indexOf(Event1[i-1]);
							}
							if(i>=Event1.length){ /// for last position of sliding window
								wB= ActivitiesSize;
							}else{
								wB=ActivityList.indexOf(Event1[i]);
							}
							WindowsPatterns[0][wB+((ActivitiesSize+1)*wA) ]++; /// increase the corresponding window frequencies
							WindowsPatterns[0][((ActivitiesSize+1) * (ActivitiesSize+1) )+ 2 ]++; /// increase the number of time window with size 0 are happened
							WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ]++; /// increase the corresponding window frequencies in summary
						}  /// sliding window
					}
			 } /// trace for
		
		///////////////////////////////////////////////////////////////////////////////////////////////
			 /////////////////////////////len =1 : K ///////////////////////////
		for (int Len = 1; Len <= KLength; Len++) {  // for Len
			String [] Window = new String[Len];

			traceloop:
			 for (XTrace trace : InputLog) { // for each Trace

					String[] Event1 = new String[trace.size()];
					List<String> templist = new ArrayList<String>();
					for (XEvent event : trace) {
						eventAttributeMap = event.getAttributes();
						templist.add(event.getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).toString());
					}
					
					Event1 = templist.toArray(new String[trace.size()]); // Trace in form of String array
					if(Event1.length >= Len){        
						
					for (int i = 0; i <= Event1.length - Len; i++) {  // Sliding Window
						chCount=0;
						String[] StrTemp = new String[Len];
						for (int j = Len-1; j >= 0; j--) { 
							Window[j]= Event1[i+j];
							chCount= (int) (chCount+ (ActivityList.indexOf(Window[j])+1)*java.lang.Math.pow(ActivitiesSize , j));  
							StrTemp[j]= Window[j];
						}
						if (ActionMaper.get(chCount)== ActionMaper.get(100000000)){
							GlobalCounter ++; /// some how a pattern or subsequence Id
							HashMaper.put(Integer.toString(chCount), Integer.toString(GlobalCounter));
							ReverseMapper.put(GlobalCounter, chCount);
							ActionMaper.put(chCount, StrTemp);
						}
						 
						//// NewInsert instruction: Activities, start, end, length, happening]
						 if (Event1.length-1 >= i+Len){ //// if the trace length is more than the current position
							 
							 	wB=ActivityList.indexOf(Event1[i+Len]);
						 }else{ 
							 	//// increase the frequency of being the last subsequence
							 	wB= ActivitiesSize;
						 	}
						 if (i==0) {
							
							wA= ActivitiesSize;
						 }
						 else{
							 
							 wA=ActivityList.indexOf(Event1[i-1]);
						 }
						
						 
						 WindowsPatterns[chCount][wB+((ActivitiesSize+1)*wA) ]++; /// increase the number of times this window occurs with this subpattern
						 WindowsPatterns[chCount][((ActivitiesSize+1) * (ActivitiesSize+1) )+ 2 ]++;
						 WindowsPatterns[chCount][((ActivitiesSize+1) * (ActivitiesSize+1) )+ 1]=Len;
						 WindowsPatterns[chCount][((ActivitiesSize+1) * (ActivitiesSize+1) )+ 3]+=Len;
						 WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ]++;
					}// Sliding window i
					
					}//if
				
				} // each Trace for

		} // for Len

		
		/////////////////////////////////////////////////////   Transforming the log to matrix of Traces 
		
		XTrace TempTrace;
		ArrayList<String[]> Traces = new ArrayList<String[]>();
		XTrace[] TraceStore = new XTrace[InputLog.size()];
		int counter=0;
		for (XTrace trace : OutputLog) { 
			TempTrace = factory.createTrace();
			TempTrace.setAttributes((XAttributeMap) trace.getAttributes().clone());
			String[] Trace = new String[trace.size()];
			List<String> templist = new ArrayList<String>();
			
			for (XEvent event : trace) {
				eventAttributeMap = event.getAttributes();
				templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
				
			}
			Trace = templist.toArray(new String[trace.size()]);
			Traces.add(Trace);
			
			TraceStore[counter]=trace;
			counter++;
		}
		TraceStore[1].get(1).getAttributes().get(EventCol.getDefiningAttributeKeys()[0]);
		XConceptExtension XC= (XConceptExtension) TraceStore[1].get(1).getAttributes().get(EventCol.getDefiningAttributeKeys()[0]).getExtension();
		
		///////////////////////////////////////////////////////////////Transferring array to List
	
		ArrayList<ArrayList<int[]>> WindowList = new ArrayList<ArrayList<int[]>>();
		
		for ( int i = 0; i<= ((ActivitiesSize+1) * (ActivitiesSize+1) )	; i++) {
			ArrayList<int[]> WinTempList = new ArrayList<int[]>();
			int sum =0;
			int count =0;
			double NZ_Avg=0;
			for (int j = 0; j <= java.lang.Math.pow( (ActivitiesSize+1),KLength); j++) {
				if (WindowsPatterns[j][i]>0){
				sum=sum+WindowsPatterns[j][i];
				count++;	
				}
				if (count>0)
					NZ_Avg= (sum*1.0)/count;
			}
			for (int j = 0; j <= java.lang.Math.pow( (ActivitiesSize+1),KLength); j++) {
				if (WindowsPatterns[j][i]>(parameters.getProbabilityOfRemoval() *sum)){
					int[] currentContext = new int [3];
					currentContext[0]= j;currentContext[1]=  WindowsPatterns[j][i]; 
					currentContext[2]= WindowsPatterns[j][((ActivitiesSize+1) * (ActivitiesSize+1) )+ 1];
					WinTempList.add(currentContext);
				}
			}
			WindowList.add(WinTempList);
			
		}
		ArrayList<int[]> [] ContextList =WindowList.toArray(new ArrayList [WindowList.size()]);
		
		///////////////////////////////////////////////// Now repairing //////////////////////////////////////////
		
	switch (parameters.getAdjustingThresholdMethod()) {
		case None : //Low
			LoopforTraces:	for (int t = 0; t < Traces.size(); t++) { // For each trace 
				
				String[] Trace = Traces.get(t);
				XTrace tempTrace= TraceStore[t];
				
				//LoopforLength: for (int len = KLength-1; len > 0; len--) { /// Loop over length of KLength -1 // here we just consider length 1 when user decided to have length 2 !! :)
				LoopforLength: for (int len = 0; len <=KLength; len++) {	// len
				String [] Pattern = new String[len];
					if(Trace.length < len)
						break LoopforLength;
					LoopforSlidingWindow: for (int i = 0; i <= Trace.length-len; i++) { //sliding window 
						chCount=0;
						for (int j = len-1; j >= 0; j--) {
							Pattern[j]= Trace[i+j];
							chCount= (int) (chCount+ (ActivityList.indexOf(Pattern[j])+1)*java.lang.Math.pow(ActivitiesSize , j)); 
						}//chCount
						wB =ActivitiesSize;
						wA=ActivitiesSize;
						
						if (i+len < Trace.length){ /// index of element of after subsequent
							wB = ActivityList.indexOf(Trace[i+len]);
						}
						if (i>0){
							wA= ActivityList.indexOf(Trace[i-1]); 
						}
						
							String[] Anomal=Pattern;
							
							
							String[] SimilarSub=Anomal;
							//  WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ]++;
							if ( WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ] >
									LogSize*parameters.getProbabilityOfRemoval()){ // if window is frequent
								if (WindowsPatterns[chCount][wB+((ActivitiesSize+1)*wA) ] < 
								WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ] * parameters.getProbabilityOfRemoval()
										){
									int TempMax=0;
									int TempDistance=1000000;
									int TempIndex=-1;
									
									int [] [] ContextPatterns = new int [ContextList[wB+((ActivitiesSize+1)*wA) ].size()] [3]; 
									for (int i1 = 0; i1 < ContextPatterns.length; i1++) {
										for (int j = 0; j < 3; j++) {
											ContextPatterns[i1][j]= ContextList[wB+((ActivitiesSize+1)*wA) ].get(i1)[j];
										}
									} 
									
									for (int j = 0; j <ContextPatterns.length; j++) {
									 
										if (Math.abs(ContextPatterns[j][2] -Anomal.length) < TempDistance)
												{
												TempDistance = Math.abs(ContextPatterns[j][2] -Anomal.length);
												TempMax= ContextPatterns[j][1] ;
												TempIndex=ContextPatterns[j][0];
											}else
												if((ContextPatterns[j][2] -Anomal.length) == TempDistance &&
														ContextPatterns[j][1] > TempMax){
											TempMax= ContextPatterns[j][1] ;
											TempIndex=ContextPatterns[j][0];
										}
									  }
									
									SimilarSub = ActionMaper.get(TempIndex);
									
									if( TempIndex==0){  /// Anomaly Should be deleted 
										String[] temp = new String[Trace.length-(Anomal.length)];
										for (int k = 0; k < i; k++) { // for before anomaly
											temp[k]=Trace[k];
										}
										for (int j = i; j < temp.length; j++) { /// for after anomaly
											temp[j]= Trace[j+(Anomal.length)];
											XEvent tempEvent= TraceStore[t].get(j+(Anomal.length));
											tempTrace.set(j, tempEvent);
										}
										for (int j = 1; j <=Anomal.length; j++) { //// for anomaly
											
											tempTrace.remove(Trace.length-j);
										}

										Trace=temp;
									} else{ /// if SimilarSub!=null
							if (TempIndex==chCount || TempIndex==-1){
								continue LoopforSlidingWindow;
							}
							
							if (SimilarSub.length == Anomal.length ){ // if both subsequences have the same size 
								for (int k = 0; k < SimilarSub.length; k++) {
									Trace[i+k] = SimilarSub[k];
									
									XEvent tempEvent= factory.createEvent((XAttributeMap) TraceStore[t].get(i+k).getAttributes().clone());
									XAttributeMap XAM = (XAttributeMap) tempEvent.getAttributes().clone();
									XAM.remove(EventCol.getDefiningAttributeKeys()[0]);
									XAttribute XA= factory.createAttributeLiteral(EventCol.getDefiningAttributeKeys()[0], SimilarSub[k], XC );
									XAM.put(EventCol.getDefiningAttributeKeys()[0], XA);
									tempEvent.setAttributes(XAM);
									tempTrace.set(i+k, tempEvent);
									
								}
							}else if(SimilarSub.length > Anomal.length){ /// If subsequences is longer
								String[] temp = new String[Trace.length+(SimilarSub.length-Anomal.length)];
								
								for (int k = 0; k < i; k++) {//before
									temp[k]=Trace[k];
								}
								for (int j2 = 0; j2 < SimilarSub.length-Anomal.length; j2++) {//anomal
									tempTrace.add(tempTrace.get(tempTrace.size()-1));
								}

								for (int j = temp.length-1 ; j >=i+SimilarSub.length; j--) {  
									temp[j]= Trace[j-(SimilarSub.length-Anomal.length)];
									XEvent tempEvent= TraceStore[t].get(j-(SimilarSub.length-Anomal.length));
									tempTrace.set(j, tempEvent);
								}
								for (int j = 0; j < SimilarSub.length; j++) { /// for anomaly
									temp[i+j]= SimilarSub[j];
									
									XEvent tempEvent= factory.createEvent((XAttributeMap) TraceStore[t].get(i).getAttributes().clone());
									XAttributeMap XAM =  tempEvent.getAttributes();

									XAM.remove(EventCol.getDefiningAttributeKeys()[0]);
									XAttribute XA= factory.createAttributeLiteral(EventCol.getDefiningAttributeKeys()[0], SimilarSub[j], XC );
									XAM.put(EventCol.getDefiningAttributeKeys()[0], XA);
									tempEvent.setAttributes(XAM);
									tempTrace.set(i+j, tempEvent);
								}

								Trace=temp;
							} else{ // simialrSub length is less than anomal
								String[] temp = new String[Trace.length-(Anomal.length-SimilarSub.length)];
								for (int k = 0; k < i; k++) { // for before anomaly
									temp[k]=Trace[k];
								}
								for (int j = i+SimilarSub.length; j < temp.length; j++) { /// for after anomaly
									temp[j]= Trace[j+(Anomal.length-SimilarSub.length)];
									XEvent tempEvent= TraceStore[t].get(j+(Anomal.length-SimilarSub.length));
									tempTrace.set(j, tempEvent);
								}
								for (int j = 0; j < SimilarSub.length; j++) { //// for anomaly
									temp[i+j]= SimilarSub[j];
									
									XEvent tempEvent= factory.createEvent((XAttributeMap) TraceStore[t].get(i).getAttributes().clone());
									XAttributeMap XAM = (XAttributeMap) tempEvent.getAttributes().clone();
									XAM.remove(EventCol.getDefiningAttributeKeys()[0]);
									XAttribute XA= factory.createAttributeLiteral(EventCol.getDefiningAttributeKeys()[0], SimilarSub[j], XC );
									XAM.put(EventCol.getDefiningAttributeKeys()[0], XA);
									tempEvent.setAttributes(XAM);
									tempTrace.set(i+j, tempEvent);
								}
								for (int j = 1; j <= Anomal.length-SimilarSub.length ; j++){
									tempTrace.remove(Trace.length-j);
								}
								
								Trace=temp;
								
								
								}
							}  /// SimilarSub!=null

							Traces.set(t, Trace);
							TraceStore[t]= tempTrace;
							
							
							//XTrace TempTrace2 = factory.createTrace();
							//TempTrace2.setAttributes((XAttributeMap) tempTrace.getAttributes().clone());
							String[] Trace2 = new String[tempTrace.size()];
							List<String> templist = new ArrayList<String>();
							
							for (XEvent event : tempTrace) {
								eventAttributeMap = event.getAttributes();
								templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
								
							}
							Trace2  = templist.toArray(new String[tempTrace.size()]);
							
						
							if (SimilarSub!=null)
								i= i+SimilarSub.length-1;
							else 
								i=-1;//Math.max(i - Anomal.length,-1);
							//continue LoopforTraces;
							
							
								} //pattern  is  not  frequent 
							}//window is frequent 
						}// sliding window
				
				} // For of Length 
			} // for of Trace
			break;

		case MaxVMean: //Top
			LoopforTraces:	for (int t = 0; t < Traces.size(); t++) { // For each trace 
				
				String[] Trace = Traces.get(t);
				XTrace tempTrace= TraceStore[t];
				
				LoopforLength: for (int len = KLength-1; len > 0; len--) { /// Loop over length of KLength -1 // here we just consider length 1 when user decided to have length 2 !! :)
				//LoopforLength: for (int len = 0; len <=KLength; len++) {	// len
				String [] Pattern = new String[len];
					if(Trace.length < len)
						break LoopforLength;
					LoopforSlidingWindow: for (int i = 0; i <= Trace.length-len; i++) { //sliding window 
						chCount=0;
						for (int j = len-1; j >= 0; j--) {
							Pattern[j]= Trace[i+j];
							chCount= (int) (chCount+ (ActivityList.indexOf(Pattern[j])+1)*java.lang.Math.pow(ActivitiesSize , j)); 
						}//chCount
						wB =ActivitiesSize;
						wA=ActivitiesSize;
						
						if (i+len < Trace.length){ /// index of element of after subsequent
							wB = ActivityList.indexOf(Trace[i+len]);
						}
						if (i>0){
							wA= ActivityList.indexOf(Trace[i-1]); 
						}
						
							String[] Anomal=Pattern;
							
							
							String[] SimilarSub=Anomal;
							//  WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ]++;
							if ( WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ] >
									LogSize*parameters.getProbabilityOfRemoval()){ // if window is frequent
								if (WindowsPatterns[chCount][wB+((ActivitiesSize+1)*wA) ] < 
								WindowsPatterns[(int) ((java.lang.Math.pow((ActivitiesSize+1),KLength))+1)][wB+((ActivitiesSize+1)*wA) ] * parameters.getProbabilityOfRemoval()
										){
									int TempMax=0;
									int TempDistance=1000000;
									int TempIndex=-1;
									
									int [] [] ContextPatterns = new int [ContextList[wB+((ActivitiesSize+1)*wA) ].size()] [3]; 
									for (int i1 = 0; i1 < ContextPatterns.length; i1++) {
										for (int j = 0; j < 3; j++) {
											ContextPatterns[i1][j]= ContextList[wB+((ActivitiesSize+1)*wA) ].get(i1)[j];
										}
									} 
									
									for (int j = 0; j <ContextPatterns.length; j++) {
									 
										if (Math.abs(ContextPatterns[j][2] -Anomal.length) < TempDistance)
												{
												TempDistance = Math.abs(ContextPatterns[j][2] -Anomal.length);
												TempMax= ContextPatterns[j][1] ;
												TempIndex=ContextPatterns[j][0];
											}else
												if((ContextPatterns[j][2] -Anomal.length) == TempDistance &&
														ContextPatterns[j][1] > TempMax){
											TempMax= ContextPatterns[j][1] ;
											TempIndex=ContextPatterns[j][0];
										}
									  }
									
									SimilarSub = ActionMaper.get(TempIndex);
									
									if( TempIndex==0){  /// Anomaly Should be deleted 
										String[] temp = new String[Trace.length-(Anomal.length)];
										for (int k = 0; k < i; k++) { // for before anomaly
											temp[k]=Trace[k];
										}
										for (int j = i; j < temp.length; j++) { /// for after anomaly
											temp[j]= Trace[j+(Anomal.length)];
											XEvent tempEvent= TraceStore[t].get(j+(Anomal.length));
											tempTrace.set(j, tempEvent);
										}
										for (int j = 1; j <=Anomal.length; j++) { //// for anomaly
											
											tempTrace.remove(Trace.length-j);
										}

										Trace=temp;
									} else{ /// if SimilarSub!=null
							if (TempIndex==chCount || TempIndex==-1){
								continue LoopforSlidingWindow;
							}
							
							if (SimilarSub.length == Anomal.length ){ // if both subsequences have the same size 
								for (int k = 0; k < SimilarSub.length; k++) {
									Trace[i+k] = SimilarSub[k];
									
									XEvent tempEvent= factory.createEvent((XAttributeMap) TraceStore[t].get(i+k).getAttributes().clone());
									XAttributeMap XAM = (XAttributeMap) tempEvent.getAttributes().clone();
									XAM.remove(EventCol.getDefiningAttributeKeys()[0]);
									XAttribute XA= factory.createAttributeLiteral(EventCol.getDefiningAttributeKeys()[0], SimilarSub[k], XC );
									XAM.put(EventCol.getDefiningAttributeKeys()[0], XA);
									tempEvent.setAttributes(XAM);
									tempTrace.set(i+k, tempEvent);
									
								}
							}else if(SimilarSub.length > Anomal.length){ /// If subsequences is longer
								String[] temp = new String[Trace.length+(SimilarSub.length-Anomal.length)];
								
								for (int k = 0; k < i; k++) {//before
									temp[k]=Trace[k];
								}
								for (int j2 = 0; j2 < SimilarSub.length-Anomal.length; j2++) {//anomal
									tempTrace.add(tempTrace.get(tempTrace.size()-1));
								}

								for (int j = temp.length-1 ; j >=i+SimilarSub.length; j--) {  
									temp[j]= Trace[j-(SimilarSub.length-Anomal.length)];
									XEvent tempEvent= TraceStore[t].get(j-(SimilarSub.length-Anomal.length));
									tempTrace.set(j, tempEvent);
								}
								for (int j = 0; j < SimilarSub.length; j++) { /// for anomaly
									temp[i+j]= SimilarSub[j];
									
									XEvent tempEvent= factory.createEvent((XAttributeMap) TraceStore[t].get(i).getAttributes().clone());
									XAttributeMap XAM =  tempEvent.getAttributes();

									XAM.remove(EventCol.getDefiningAttributeKeys()[0]);
									XAttribute XA= factory.createAttributeLiteral(EventCol.getDefiningAttributeKeys()[0], SimilarSub[j], XC );
									XAM.put(EventCol.getDefiningAttributeKeys()[0], XA);
									tempEvent.setAttributes(XAM);
									tempTrace.set(i+j, tempEvent);
								}

								Trace=temp;
							} else{ // simialrSub length is less than anomal
								String[] temp = new String[Trace.length-(Anomal.length-SimilarSub.length)];
								for (int k = 0; k < i; k++) { // for before anomaly
									temp[k]=Trace[k];
								}
								for (int j = i+SimilarSub.length; j < temp.length; j++) { /// for after anomaly
									temp[j]= Trace[j+(Anomal.length-SimilarSub.length)];
									XEvent tempEvent= TraceStore[t].get(j+(Anomal.length-SimilarSub.length));
									tempTrace.set(j, tempEvent);
								}
								for (int j = 0; j < SimilarSub.length; j++) { //// for anomaly
									temp[i+j]= SimilarSub[j];
									
									XEvent tempEvent= factory.createEvent((XAttributeMap) TraceStore[t].get(i).getAttributes().clone());
									XAttributeMap XAM = (XAttributeMap) tempEvent.getAttributes().clone();
									XAM.remove(EventCol.getDefiningAttributeKeys()[0]);
									XAttribute XA= factory.createAttributeLiteral(EventCol.getDefiningAttributeKeys()[0], SimilarSub[j], XC );
									XAM.put(EventCol.getDefiningAttributeKeys()[0], XA);
									tempEvent.setAttributes(XAM);
									tempTrace.set(i+j, tempEvent);
								}
								for (int j = 1; j <= Anomal.length-SimilarSub.length ; j++){
									tempTrace.remove(Trace.length-j);
								}
								
								Trace=temp;
								
								
								}
							}  /// SimilarSub!=null

							Traces.set(t, Trace);
							TraceStore[t]= tempTrace;
							
							
							//XTrace TempTrace2 = factory.createTrace();
							//TempTrace2.setAttributes((XAttributeMap) tempTrace.getAttributes().clone());
							String[] Trace2 = new String[tempTrace.size()];
							List<String> templist = new ArrayList<String>();
							
							for (XEvent event : tempTrace) {
								eventAttributeMap = event.getAttributes();
								templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
								
							}
							Trace2  = templist.toArray(new String[tempTrace.size()]);
							
						
							if (SimilarSub!=null)
								i= i+SimilarSub.length-1;
							else 
								i=-1;//Math.max(i - Anomal.length,-1);
							//continue LoopforTraces;
							
							
								} //pattern  is  not  frequent 
							}//window is frequent 
						}// sliding window
				
				} // For of Length 
			} // for of Trace
		break;
	}
		
		
	/////////////////////////////////////////////////////////Building the new Log
		XLog outputLog2 = factory.createLog();
		
		for (XExtension extension : InputLog.getExtensions()){
			outputLog2.getExtensions().add(extension);
			}
		outputLog2.setAttributes(InputLog.getAttributes());
		XTrace tt = factory.createTrace();
		XEvent dd =factory.createEvent();
		
		for (int j = 0; j < TraceStore.length; j++) {
			List<String> templist = new ArrayList<String>();
			for (XEvent event : TraceStore[j]) {
				eventAttributeMap = event.getAttributes();
				templist.add(eventAttributeMap.get(EventCol.getDefiningAttributeKeys()[0]).toString());
				
			}
			String[] Trace2 = new String[TraceStore[j].size()];
		
			Trace2  = templist.toArray(new String[TraceStore[j].size()]);
			outputLog2.add(TraceStore[j]);
			
		}
		return outputLog2;
	}

	
	
}
	

	
	
	

