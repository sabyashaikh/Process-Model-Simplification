package org.processmining.logfiltering.plugins.Sabya;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XesXmlSerializer;

public class generateLog {

	public void generate(ArrayList<String> traceList, String address) {
		XFactoryBufferedImpl xfactory = new XFactoryBufferedImpl();
		XLog log = xfactory.createLog();
		int index = 0;
		for (int i = 0; i < traceList.size(); i++) {
			String[] t = traceList.get(i).split("_");
			int num = Integer.parseInt(t[0]);
			for (int j = 0; j < num; j++) {
				XAttributeMapImpl case_map = new XAttributeMapImpl();
				String case_id = String.valueOf(index++);
				case_map.put("concept:name", new XAttributeLiteralImpl("concept:name", case_id));
				XTraceImpl trace = new XTraceImpl(case_map);
				for (int m = 1; m < t.length; m++) {
					String event_name = t[m];
					XEvent event = new XEventImpl();
					XAttributeMapImpl att_map = new XAttributeMapImpl();
					XAttributeLiteralImpl e_name = new XAttributeLiteralImpl("concept:name", event_name);
					// for adding timestamp
					// for adding life:cycle
					att_map.put(event_name, e_name);
					event.setAttributes(att_map);
					trace.add(event);
				}
				log.add(trace);
			}
		}

		log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);	
		XesXmlSerializer xxs = new XesXmlSerializer();
		File file = new File(address);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			xxs.serialize(log, fos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String t1 = "1_L_I_O_P_K_A_M_J_N_N_A_K_A_G_P_A_K_P_N_N_A_K_A_K_A_K_A_K_A_K_A_K_A_C_H";
		String t2 = "1_L_I_O_P_K_A_M_J_N_N_A_A_A_C_H";
		String t3 = "7_L_I_O_P_K_A_M_J_N_N_A_C_H";
		String t4 = "4_L_I_O_P_K_A_M_J_N_C";
		String t5 = "35_L_I_O";
		String t6 = "24_L_I_O_K_A";
		String t7 = "11_L_I_O_K_A_P";
		String t8 = "1_L_I_O_K_A_K";
		String t9 = "9_L_I_O_K_A_P_M_J";
		String t10 = "1_A_P_L_I_O_K_M_J";
		String t11 = "1_M_O_K_P_L_I_J";*/
		String t1 = "5_a_b_b_b_b";
		String t2 = "5_a_b_b";
		String t3 = "1_a_b";
		//String t4 = "1_I_F_L_H_B";
		//String t5 = "1_I_F_L_H_B_A";
		//String t6 = "1_I_F_L_H_B_A_J_G";
		
		//String t3 = "1000_a_z_y_x_b";
		ArrayList<String> list = new ArrayList<String>();
		list.add(t1);
		list.add(t2);
		list.add(t3);
		//list.add(t4);
		//list.add(t5);
		//list.add(t6);
		//list.add(t7);
		//list.add(t8);
		//list.add(t9);
		//list.add(t10);
		//list.add(t11);
		generateLog g = new generateLog();
		g.generate(list, "C://Users/sabya/eclipse-workspace/testingPTcreation.xes");
	}

}
