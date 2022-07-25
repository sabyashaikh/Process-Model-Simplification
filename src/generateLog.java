

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
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
		String t1 = "100_a_b_c_d_e";
		String t2 = "90_a_c_b_d_e";
		String t3 = "85_a_c_b_f_e";
		String t4 = "80_a_b_c_f_e";
		String t5 = "95_a_c_b";
		String t6 = "94_a_b_f_c_e";
		ArrayList<String> list = new ArrayList<String>();
		list.add(t1);
		list.add(t2);
		list.add(t3);
		list.add(t4);
		list.add(t5);
		list.add(t6);
		generateLog g = new generateLog();
		g.generate(list, "D:/PHD/Experiments/GamingwithLogs/l1.xes");
	}

}
