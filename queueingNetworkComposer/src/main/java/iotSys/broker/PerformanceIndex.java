package iotSys.broker;

public class PerformanceIndex {

	String name;
	String referenceClass;
	Double value;
	
	public PerformanceIndex (String name, String referenceClass, Double value) {
		this.name = name;
		this.referenceClass = referenceClass;
		this.value = value;
	}
}
