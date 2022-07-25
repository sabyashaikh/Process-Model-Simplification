package org.processmining.logfiltering.visMiningAlgorithms;

import java.util.HashMap;
import java.util.Map;

public class MiningAlgorithmParameters {
	public Map<String, Object> allParameters;
	
	public MiningAlgorithmParameters() {
		allParameters = new HashMap<String, Object>();
	}
	
	public Object getParameter(String parameterName) {
		return allParameters.get(parameterName);
	}
	
	public void setParameter(String parameterName, Object parameterValue) {
		allParameters.put(parameterName, parameterValue);
	}
	
	public Map<String, Object> getParameters() {
		return this.allParameters;
	}
}
