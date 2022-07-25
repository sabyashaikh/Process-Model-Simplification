package org.processmining.logfiltering.plugins.Sabya;

import java.util.ArrayList;
import java.util.List;

public class TestingFunctions {

	public List<String> getAndPatternsFromVariant(String variantWithOnlyAndChildren) {
		List<String> patterns = new ArrayList<String>();
		String pattern = "";
		for(char c: variantWithOnlyAndChildren.toCharArray()) {
			String stringC = Character.toString(c);
			if(pattern.contains(stringC)) {
				patterns.add(pattern);
				pattern = "";
			}
			pattern = pattern + stringC;
		}
		patterns.add(pattern);
		return patterns;
	}
	
	public static void main(String[] args) {
		String str = "abc";
		
		TestingFunctions g = new TestingFunctions();
		System.out.println(g.getAndPatternsFromVariant(str));
	}
}
