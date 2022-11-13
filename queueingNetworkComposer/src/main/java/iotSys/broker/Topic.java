package iotSys.broker;

import java.util.ArrayList;

public class Topic {

	public String topicId;
	public String topicName;
	public ArrayList<String> publishers;
	public ArrayList<String> subscribers;
	public ArrayList<String> subTopics = new ArrayList<String>();
	
	public Topic(String topicId, String topicName, ArrayList<String> publishers, ArrayList<String> subscribers) {
		this.topicId = topicId;
		this.topicName = topicName;
//		this.applicationCategory = applicationCategory;
		this.publishers = (ArrayList<String>) publishers.clone();
		this.subscribers = (ArrayList<String>) subscribers.clone();
	}
}
