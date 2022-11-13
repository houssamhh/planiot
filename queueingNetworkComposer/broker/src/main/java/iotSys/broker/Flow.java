package iotSys.broker;

public class Flow {

	public String type;
	public double requiredBandwidth;
	public double  allocatedBandwidth = 0;
	public boolean isSatisfied = false;
	
	public Flow (String type, double requiredBandwidth) {
		this.type = type;
		this.requiredBandwidth = requiredBandwidth;
	}
}
