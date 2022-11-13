package iotSys.broker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReadSubscriptions {

	public static void main (String[] args) throws IOException {
		HashMap<String, ArrayList<String>> subscriptions = new HashMap<String, ArrayList<String>>();
		int nbOfSubsPerTopic = 0;
		int nbOfSubscriptions = 0;
		for (int i = 1; i < 31; i++) {
			subscriptions.put("topic_topic" + i, new ArrayList<String>());
		}
		String file = "/home/houssamhh/phd/mdw22/experiments/setup2-overloadedsystem/baseline/systemspecifications.json";
		String text = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
		String jsonString = text; //assign your JSON String here
		JSONObject obj = new JSONObject(jsonString);
		JSONArray arr = obj.getJSONArray("applications");
		for (int i = 0; i < arr.length(); i++) 
		{
			nbOfSubsPerTopic = 0;
		    String applicationName = arr.getJSONObject(i).getString("applicationName");
		    JSONArray array = arr.getJSONObject(i).getJSONArray("subscribesTo");
		    ArrayList<String> subscribedTopics = new ArrayList<String>();
		    for (Object o : array) {
		    	subscribedTopics.add((String) o);
		    }
		    for (String topic : subscribedTopics) {
		    	ArrayList<String> list = subscriptions.get(topic);
		    	list.add(applicationName);
		    	subscriptions.put(topic, list);
		    	nbOfSubscriptions++;
		    }
		    
		    
		}
		int i = 0;
		TreeMap<String, ArrayList<String>> sortedSubscriptions = new TreeMap<String, ArrayList<String>>();
		sortedSubscriptions.putAll(subscriptions);
		for (String topic : sortedSubscriptions.keySet()) {
			i++;
			String subscribers = "";
			ArrayList<String> subs = sortedSubscriptions.get(topic);
			for (String subscriber : subs) {
				subscribers += "\"" + subscriber + "\", ";
			}
			subscribers = subscribers.substring(0, subscribers.length() - 2);
			String topicSource = topic.replace("topic_", "");
			topicSource += "_source";
//			System.out.println("    {\n"
//					+ "      \"topicId\": \"" + topic + "\",\n"
//					+ "      \"topicName\": \"" + topic + "\",\n"
//					+ "      \"publishers\": [\"" + topicSource + "\"],\n"
//					+ "      \"subscribers\": ["+ subscribers + "]\n"
//					+ "    },");
		}
		
		System.out.println(nbOfSubscriptions);
		for (String topic : sortedSubscriptions.keySet()) {
			System.out.print(topic + ": ");
			for (String app : sortedSubscriptions.get(topic)) {
				System.out.print("\"" + app + "\", ");
			}
		System.out.println();
		}
			
			
	}
	
	    	
}
