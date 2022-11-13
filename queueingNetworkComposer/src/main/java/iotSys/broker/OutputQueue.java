package iotSys.broker;

import java.util.ArrayList;

public class OutputQueue {

	public String name;
	public String category;
	public ArrayList<String> subscribedTopics;
	public double serviceTime;
	public double bwNeeded; 
	
	public OutputQueue (String name, String category, ArrayList<String> subscribedTopics, double bwNeeded) {
		this.name = name;
		this.category = category;
		this.subscribedTopics = (ArrayList<String>) subscribedTopics.clone();
		this.bwNeeded = bwNeeded;
	}
}
