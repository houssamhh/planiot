package iotSys.broker;

import java.util.ArrayList;

public class VirtualSensor {
	
	public String deviceId;
	public String deviceName;
	public String applicationCategory;
	public double publishFrequency;
	public double messageSize; //in bytes
	public int processingRate;
	public String processingDistribution;
	public ArrayList<String> subscribedTopics;
	public ArrayList<String> publishedTopics;
	
	public VirtualSensor(String deviceId, String deviceName, String applicationCategory, double publishFrequency, double messageSize, 
			int processingRate, String processingDistribution, ArrayList<String> subscribedTopics, ArrayList<String> publishedTopics) {
		this.deviceId = deviceId;
		this.deviceName = deviceName;
		this.applicationCategory = applicationCategory;
		this.publishFrequency = publishFrequency;
		this.messageSize = messageSize;
		this.processingRate = processingRate;
		this.processingDistribution = processingDistribution;
		this.subscribedTopics = (ArrayList<String>) subscribedTopics.clone();
		this.publishedTopics = (ArrayList<String>) publishedTopics.clone();
	}
	

}
