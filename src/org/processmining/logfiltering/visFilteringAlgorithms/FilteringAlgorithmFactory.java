package org.processmining.logfiltering.visFilteringAlgorithms;

import java.util.HashMap;
import java.util.Map;

public class FilteringAlgorithmFactory {
	public static enum algorithms { NO, VARIANT,MATRIX,AFA,SF,FF };
	public Map<String, FilteringAlgorithmFactory.algorithms> algorithmsLabels  = new HashMap<String, FilteringAlgorithmFactory.algorithms>();
	public Map<FilteringAlgorithmFactory.algorithms, String> invAlgorithmLabels = new HashMap<FilteringAlgorithmFactory.algorithms, String>();
	
	
	
	public FilteringAlgorithmFactory() {
		algorithmsLabels = new HashMap<String, FilteringAlgorithmFactory.algorithms>();
		
		// add the labels for each algorithm
		algorithmsLabels.put("No filtering", FilteringAlgorithmFactory.algorithms.NO);
		algorithmsLabels.put("Variant filter", FilteringAlgorithmFactory.algorithms.VARIANT);
		algorithmsLabels.put("Matrix filter", FilteringAlgorithmFactory.algorithms.MATRIX);
		algorithmsLabels.put("AFA filter", FilteringAlgorithmFactory.algorithms.AFA);
		algorithmsLabels.put("Sequence Filter", FilteringAlgorithmFactory.algorithms.SF);
		algorithmsLabels.put("Frequency Filter", FilteringAlgorithmFactory.algorithms.FF);
		
		invAlgorithmLabels.put(FilteringAlgorithmFactory.algorithms.NO, "No filtering");
		invAlgorithmLabels.put(FilteringAlgorithmFactory.algorithms.VARIANT, "Variant filter");
		invAlgorithmLabels.put(FilteringAlgorithmFactory.algorithms.MATRIX, "Matrix filter");
		invAlgorithmLabels.put(FilteringAlgorithmFactory.algorithms.AFA, "AFA filter");
		invAlgorithmLabels.put(FilteringAlgorithmFactory.algorithms.SF, "Sequence Filter");
		invAlgorithmLabels.put(FilteringAlgorithmFactory.algorithms.FF, "Frequency Filter");
	}
	
	public ApplyFilteringAlgorithm getFilteringAlgorithm(FilteringAlgorithmFactory.algorithms filteringAlgorithmDescription) {
		System.out.println("KYKYKYKY");
		System.out.println(filteringAlgorithmDescription);
		
		if (filteringAlgorithmDescription != null) {
			if (filteringAlgorithmDescription.equals(FilteringAlgorithmFactory.algorithms.NO)) {
				return new NoFilteringAlgorithm();
			}
			else if (filteringAlgorithmDescription.equals(FilteringAlgorithmFactory.algorithms.VARIANT)) {
				return new VariantFilteringAlgorithm();
			}
			else if (filteringAlgorithmDescription.equals(FilteringAlgorithmFactory.algorithms.MATRIX)) {
				return new MatrixFilteringAlgorithm();
			}
			else if (filteringAlgorithmDescription.equals(FilteringAlgorithmFactory.algorithms.AFA)) {
				return new AFAFilteringAlgorithm();
			}
			else if (filteringAlgorithmDescription.equals(FilteringAlgorithmFactory.algorithms.SF)) {
				return new SFFilteringAlgorithm();
			}
			else if (filteringAlgorithmDescription.equals(FilteringAlgorithmFactory.algorithms.FF)) {
				return new FrequencyFilteringAlgorithm();
			}
		}
		System.out.println("KZKZKZKZ");
		return new NoFilteringAlgorithm();
	}
}
