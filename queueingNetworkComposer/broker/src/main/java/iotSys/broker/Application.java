package iotSys.broker;

import java.util.ArrayList;

public class Application {

	public String applicationId;
	public String applicationName;
	public String applicationCategory;
	public int processingRate;
	public String processingDistribution;
	public ArrayList<String> subscribedTopics;
	public int priority;		
	public int jmtPriority;			//IN JMT IT'S THE INVERSE: HIGHER NUMBER MEANS HIGHER PRIORITY
	
	public Application (String applicationId, String applicationName, String applicationCategory, int processingRate, 
			String processingDistribution, ArrayList<String>subscribedTopics, int priority) {
		this.applicationId = applicationId;
		this.applicationName = applicationName;
		this.applicationCategory = applicationCategory;
		this.processingRate = processingRate;
		this.processingDistribution = processingDistribution;
		this.subscribedTopics = (ArrayList<String>) subscribedTopics.clone();
		this.priority = priority;
		this.jmtPriority = 0;
	}
}
