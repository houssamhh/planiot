package iotSys.broker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.Defaults;
import jmt.gui.common.definitions.CommonModel;
import jmt.gui.common.definitions.SimulationDefinition;
import jmt.gui.common.distributions.Deterministic;
import jmt.gui.common.distributions.Exponential;
import jmt.gui.common.routingStrategies.ProbabilityRouting;
import jmt.gui.common.xml.XMLWriter;

/**
 * Parse JSON file and write to jsimg file
 *
 */


/*
 * TODO
 * - utilization of fcr for all
 * - throughput of fcr for all
 * - dropping for fcr for all
 * - 
 */

public class App 
{
	private static CommonModel model = new CommonModel();
	
	public static ArrayList<IoTdevice> iotDevices = new ArrayList<IoTdevice>();
	public static ArrayList<VirtualSensor> virtualSensors = new ArrayList<VirtualSensor>();
	public static ArrayList<Actuator> actuators = new ArrayList<Actuator>();
	public static HashMap<String, Application> applications = new HashMap<String, Application>();
	public static ArrayList<OutputQueue> outputQueues = new ArrayList<OutputQueue>();
	public static HashMap<String, Topic> topics = new HashMap<String, Topic>();
	public static HashMap<String, Subtopic> subtopics = new HashMap<String, Subtopic>();
	public static HashMap<String, ArrayList<String>> topics_subtopicsList = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, String> subtopicsClassSwitches = new HashMap<String, String>();	//to add the name of the class switch for each subtopic
	public static HashMap<String, Double> topicsRate = new HashMap<String, Double>();		//not according to global message size
	public static ArrayList<Broker> brokers = new ArrayList<Broker>();
	public static double systemBandwidth = 0;
	public static String bandwidthPolicy = "";
	
	
	/**
	 * Global message size: 100 MB
	 */
	public final double GLOBAL_MESSAGE_SIZE = 52428800.0;
	public static Object FINITE_CAPACITY_REGION_OBJECT;
	
	public double CHANNEL_LOSS_AN = 0;
	public double CHANNEL_LOSS_RT = 0;
	public double CHANNEL_LOSS_TS = 0;
	public double CHANNEL_LOSS_VS = 0;
	
	public int BROKER_CAPACITY = 0;
	public final int PRIORITY_RT = 0;
	public final int PRIORITY_VS = 0;
	public final int PRIORITY_TS = 0;
	public final int PRIORITY_AN = 0;
	
	public static double nbOfSubs_AN = 0;
	public static double nbOfSubs_RT = 0;
	public static double nbOfSubs_TS = 0;
	public static double nbOfSubs_VS = 0;
	
	
	public static double allocatedBw_AN = 0;		//bandwidth allocated to AN apps
	public static double allocatedBw_RT = 0;		//bandwidth allocated to RT apps 
	public static double allocatedBw_TS = 0;		//bandwidth allocated to TS apps
	public static double allocatedBw_VS = 0;		//bandwidth allocated to VS apps
	
	public static double totalBw_AN = 0;			//bandwidth needed by AN apps
	public static double totalBw_RT = 0;			//bandwidth needed by RT apps
	public static double totalBw_TS = 0;			//bandwidth needed by TS apps
	public static double totalBw_VS = 0;			//bandwidth needed by VS apps
	
	public static double arrivalRate_AN = 0;
	public static double arrivalRate_RT = 0;
	public static double arrivalRate_TS = 0;
	public static double arrivalRate_VS = 0;
	
	public final double CONFIDENCE_INTERVAL = 0.99;
	public final double RELATIVE_ERROR = 0.03;
	
	
	public final String INPUT_FILE_NAME = "/home/houssamhh/phd/mdw22/experiments/setup1/mediumload/maxmin/systemspecifications.json";
	
//	Change the path depending on which laptop you are working on
	public final String OUTPUT_FILE_NAME = "/home/houssamhh/phd/mdw22/experiments/setup1/mediumload/maxmin/iotsystem_testing.jsimg";
//	public final String OUTPUT_FILE_NAME = "dummySim.jsimg";
	
    public static void main( String[] args ) throws IOException
    {

        App app = new App();
        app.readJSON(app.INPUT_FILE_NAME);
        app.addActuators(actuators);
        app.addApplications(applications);
        app.addInputQueue();			
        app.addSources(iotDevices);
        app.addTopics(topics);	
        app.convertToJmtPriorities(applications);
        app.addSubTopics(topics);
        app.addOutputQueue();
        app.addVirtualSensor(virtualSensors, brokers);
        app.addClassSwitchForJoins();
        app.addDroppingSink();
        app.calculateBwNeededByEachCategory(applications);
        app.allocateBandwidth();
//        app.setConnections();
        app.setConnectionsDropping();

        app.addClasses(iotDevices, virtualSensors, actuators, topics, subtopics);  //adding priorities here
        for (Topic topic : topics.values())
        	app.setClassSwitchMatrix(topic);  	//this is correct, I checked it
        	
        for (Subtopic subtopic : subtopics.values())
        	app.setClassSwitchMatrixSubtopics(subtopic);		//this is correct, I checked it in the simulation
        
        for (Subtopic subtopic : subtopics.values())	{
        	app.setTopicsClassSwitchMatrix(subtopic);			//this is correct otherwise JMT would give an error
        	app.setTopicsClassSwitchDroppingMatrix(subtopic);
        }
        	
        
        
        app.setFiniteCapacityRegion();
        app.setTopicsClassSwitchRouting(subtopics);
        app.setApplicationsRouting(applications);
        app.setActuatorRouting(actuators);

        app.setInputQueueServiceTime(brokers, topics);
        app.setInputQueueRouting(topics);
        app.setOutputQueueServiceTime(topics);
        app.setOutputQueueRouting(subtopics);
        app.setApplicationsServiceTime(applications);
        app.setVirtualSensorsServiceTime(virtualSensors);
        app.setVirtualSensorsRouting(virtualSensors);
        app.setActuatorServiceTime(actuators);
        
        app.addDroppingRouting(subtopicsClassSwitches);
        app.setPerformanceMetrics();
        
		File jsimFile = new File (app.OUTPUT_FILE_NAME);
		XMLWriter.writeXML(jsimFile, model);
		
		System.out.println("Done");
    }
  



	private void convertToJmtPriorities(HashMap<String, Application> applications) {
		int maxPriority = 0;
		int minPriority = 10;
		for (Application app : applications.values()) {
			if (app.priority > maxPriority)
				maxPriority = app.priority;
			if (app.priority < minPriority)
				minPriority = app.priority;
		}		Object classSwitch = model.getStationByName("topics_class_switch");
		for (Subtopic subtopic : subtopics.values()) {
    		ProbabilityRouting probaRouting = new ProbabilityRouting();
    		probaRouting.getValues().put(model.getStationByName(subtopic.parentTopicName + "_join"), 1d);
    		model.setRoutingStrategy(classSwitch, model.getClassByName(subtopic.name + "_class"), probaRouting);
    	}
		
		for (Application app : applications.values())
			app.jmtPriority = maxPriority - app.priority + minPriority;
	}


	public void addDroppingSink() {
    	model.addStation("dropping_sink", CommonConstants.STATION_TYPE_SINK);
    	model.addStation("topics_class_switch_dropping", CommonConstants.STATION_TYPE_CLASSSWITCH);
    	model.addStation("dropping_join", CommonConstants.STATION_TYPE_JOIN);
//    	for (Topic topic : topics.values())
//    		model.addStation(topic.topicName + "_join_dropping", CommonConstants.STATION_TYPE_JOIN);
    }
    
    public void addDroppingRouting(HashMap<String, String> subtopicsClassSwitches) {
    	for (String subtopicName : subtopicsClassSwitches.keySet()) {
			String className = subtopicName + "_class"; 
    		String classSwitchName = subtopicsClassSwitches.get(subtopicName);
    		ProbabilityRouting probaRouting = new ProbabilityRouting();
    		double channelLoss = 0;
    		if (subtopics.get(subtopicName).category.equals("AN"))
    			channelLoss = CHANNEL_LOSS_AN;
    		else if (subtopics.get(subtopicName).category.equals("RT"))
    			channelLoss = CHANNEL_LOSS_RT;
    		else if (subtopics.get(subtopicName).category.equals("TS"))
    			channelLoss = CHANNEL_LOSS_TS;
    		else if (subtopics.get(subtopicName).category.equals("VS"))
    			channelLoss = CHANNEL_LOSS_VS;
    		probaRouting.getValues().put(model.getStationByName("topics_class_switch_dropping"), channelLoss);
    		probaRouting.getValues().put(model.getStationByName("outputQueue"), 1-channelLoss);
    		model.setRoutingStrategy(model.getStationByName(classSwitchName), model.getClassByName(className), probaRouting);
		}
    }

	public void setFiniteCapacityRegion() {
		FINITE_CAPACITY_REGION_OBJECT = model.addBlockingRegion("finite_capacity_region", CommonConstants.INFINITE_CAPACITY);
		model.addRegionStation(FINITE_CAPACITY_REGION_OBJECT, model.getStationByName("input"));
		model.addRegionStation(FINITE_CAPACITY_REGION_OBJECT, model.getStationByName("outputQueue"));
		if (BROKER_CAPACITY > 0) {
			model.setRegionCustomerConstraint(FINITE_CAPACITY_REGION_OBJECT, BROKER_CAPACITY);
			for (Subtopic subtopic : subtopics.values()) {
				String subtopicname = subtopic.name;
				model.setRegionClassCustomerConstraint(FINITE_CAPACITY_REGION_OBJECT, model.getClassByName(subtopicname + "_class"), BROKER_CAPACITY);
				model.setRegionClassDropRule(FINITE_CAPACITY_REGION_OBJECT, model.getClassByName(subtopicname + "_class"), true);
			}
		}
	}


	private void setTopicsClassSwitchRouting(HashMap<String, Subtopic> subtopics) {
		Object classSwitch = model.getStationByName("topics_class_switch");
		for (Subtopic subtopic : subtopics.values()) {
    		ProbabilityRouting probaRouting = new ProbabilityRouting();
    		probaRouting.getValues().put(model.getStationByName(subtopic.parentTopicName + "_join"), 1d);
    		model.setRoutingStrategy(classSwitch, model.getClassByName(subtopic.name + "_class"), probaRouting);
    	}
	}


	private void setTopicsClassSwitchMatrix(Subtopic subtopic) {
		Object classSwitch = model.getStationByName("topics_class_switch");
		model.setClassSwitchMatrix(classSwitch, model.getClassByName(subtopic.name + "_class"), model.getClassByName(subtopic.name + "_class"), 0.0f);
		model.setClassSwitchMatrix(classSwitch, model.getClassByName(subtopic.name + "_class"), model.getClassByName(subtopic.parentTopicName + "_class"), 1.0f);
		
	}
	
	public void setTopicsClassSwitchDroppingMatrix(Subtopic subtopic) {
		Object classSwitch = model.getStationByName("topics_class_switch_dropping");
		model.setClassSwitchMatrix(classSwitch, model.getClassByName(subtopic.name + "_class"), model.getClassByName(subtopic.name + "_class"), 0.0f);
		model.setClassSwitchMatrix(classSwitch, model.getClassByName(subtopic.name + "_class"), model.getClassByName(subtopic.parentTopicName + "_class"), 1.0f);
	}


	public void addClassSwitchForJoins() {
		model.addStation("topics_class_switch", CommonConstants.STATION_TYPE_CLASSSWITCH);
	}


	public void readJSON(String file) throws IOException {
    	String text = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
		String jsonString = text; //assign your JSON String here
		JSONObject obj = new JSONObject(jsonString);
		
		//add iot devices
		JSONArray arr = obj.getJSONArray("IoTdevices");
		for (int i = 0; i < arr.length(); i++)
		{
		    String deviceId = arr.getJSONObject(i).getString("deviceId");
		    String deviceName = arr.getJSONObject(i).getString("deviceName");
		    double publishFrequency = arr.getJSONObject(i).getInt("publishFrequency");
		    double messageSize = arr.getJSONObject(i).getInt("messageSize");
//		    messageSize = messageSize / 60.0;   //to convert from bytes/min to bytes/sec  KEEP THIS FOR LATER
		    String distribution = arr.getJSONObject(i).getString("distribution");
		    JSONArray array = arr.getJSONObject(i).getJSONArray("publishesTo");
		    ArrayList<String> publishedTopics = new ArrayList<String>();
		    for (Object o : array) {
		    	publishedTopics.add((String) o);
		    	if (!topicsRate.containsKey((String) o))
		    			topicsRate.put((String) o, (messageSize * publishFrequency) / GLOBAL_MESSAGE_SIZE);
		    	else {
		    		double rate = topicsRate.get((String) o);
		    		rate += (messageSize * publishFrequency) / GLOBAL_MESSAGE_SIZE;
		    		topicsRate.put((String) o, rate);
		    	}
		    }
		    
		    IoTdevice device = new IoTdevice(deviceId, deviceName, publishFrequency, messageSize, distribution, publishedTopics);
		    iotDevices.add(device);
		}
	
		
		//add virtual sensors
		arr = obj.getJSONArray("virtualSensors");
		for (int i = 0; i < arr.length(); i++) 
		{
		    String deviceId = arr.getJSONObject(i).getString("deviceId");
		    String deviceName = arr.getJSONObject(i).getString("deviceName");
		    String applicationCategory = arr.getJSONObject(i).getString("applicationCategory");
		    double publishFrequency = arr.getJSONObject(i).getInt("publishFrequency");
		    double messageSize = arr.getJSONObject(i).getInt("messageSize");
//		    messageSize = messageSize / 60.0;   //to convert from bytes/min to bytes/sec
		    int processingRate = arr.getJSONObject(i).getInt("processingRate");
		    String processingDistribution = arr.getJSONObject(i).getString("processingDistribution");
		    JSONArray array = arr.getJSONObject(i).getJSONArray("subscribesTo");
		    ArrayList<String> publishedTopics = new ArrayList<String>();
		    ArrayList<String> subscribedTopics = new ArrayList<String>();
		    for (Object o : array) {
		    	publishedTopics.add((String) o);
		    	if (!topicsRate.containsKey((String) o))
		    			topicsRate.put((String) o, messageSize * publishFrequency);
		    	else {
		    		double rate = topicsRate.get((String) o);
		    		rate += messageSize * publishFrequency;
		    		topicsRate.put((String) o, rate);
		    	}
		    }
		    array = arr.getJSONObject(i).getJSONArray("publishesTo");
		    double bwNeeded = 0;
		    for (Object o : array) {
		    	subscribedTopics.add((String) o);
		    	bwNeeded += topicsRate.get((String) o);
		    }
		    VirtualSensor sensor = new VirtualSensor(deviceId, deviceName, applicationCategory, publishFrequency, messageSize,
		    		processingRate, processingDistribution, subscribedTopics, publishedTopics);
		    virtualSensors.add(sensor);
		    OutputQueue outputQueue = new OutputQueue(deviceId + "_outputQueue", applicationCategory, subscribedTopics, bwNeeded);
		    outputQueues.add(outputQueue);
		}
		
		//add actuators
		arr = obj.getJSONArray("actuators");
		for (int i = 0; i < arr.length(); i++) 
		{
		    String deviceId = arr.getJSONObject(i).getString("deviceId");
		    String deviceName = arr.getJSONObject(i).getString("deviceName");
		    String applicationCategory = arr.getJSONObject(i).getString("applicationCategory");
		    int publishFrequency = arr.getJSONObject(i).getInt("publishFrequency");
		    int messageSize = arr.getJSONObject(i).getInt("messageSize");
//		    messageSize = messageSize / 60;   //to convert from bytes/min to bytes/sec
		    JSONArray array = arr.getJSONObject(i).getJSONArray("subscribesTo");
		    ArrayList<String> subscribedTopics = new ArrayList<String>();
		    double bwNeeded = 0;
		    for (Object o : array) {
		    	subscribedTopics.add((String) o);
		    	bwNeeded += topicsRate.get((String) o);
		    }
		    	
		    Actuator actuator = new Actuator(deviceId, deviceName, applicationCategory, publishFrequency, messageSize, subscribedTopics);
		    actuators.add(actuator);
		    OutputQueue outputQueue = new OutputQueue(deviceId + "_outputQueue", applicationCategory, subscribedTopics, bwNeeded);
		    outputQueues.add(outputQueue);
		    
		}
		
		//add applications
		arr = obj.getJSONArray("applications");
		for (int i = 0; i < arr.length(); i++) 
		{
		    String applicationId = arr.getJSONObject(i).getString("applicationId");
		    String applicationName = arr.getJSONObject(i).getString("applicationName");
		    String applicationCategory = arr.getJSONObject(i).getString("applicationCategory");
		    int priority = arr.getJSONObject(i).getInt("priority");
		    int processingRate = arr.getJSONObject(i).getInt("processingRate");
		    String processingDistribution = arr.getJSONObject(i).getString("processingDistribution");
		    JSONArray array = arr.getJSONObject(i).getJSONArray("subscribesTo");
		    ArrayList<String> subscribedTopics = new ArrayList<String>();
		    double bwNeeded = 0;
		    for (Object o : array) {
		    	subscribedTopics.add((String) o);
		    	bwNeeded += topicsRate.get((String) o);
		    }
		    	
		    Application application = new Application(applicationId, applicationName, applicationCategory, processingRate, 
		    		processingDistribution, subscribedTopics, priority);
		    applications.put(applicationId, application);
		    OutputQueue outputQueue = new OutputQueue(applicationId + "_outputQueue", applicationCategory, subscribedTopics, bwNeeded);
		    outputQueues.add(outputQueue);
		}
		
		//add topics
		arr = obj.getJSONArray("topics");
		for (int i = 0; i < arr.length(); i++) 
		{
		    String topicId = arr.getJSONObject(i).getString("topicId");
		    String topicName = arr.getJSONObject(i).getString("topicName");
		    JSONArray array = arr.getJSONObject(i).getJSONArray("subscribers");
		    ArrayList<String> subscribers = new ArrayList<String>();
		    for (Object o : array)
		    	subscribers.add((String) o);
		    array = arr.getJSONObject(i).getJSONArray("publishers");
		    ArrayList<String> publishers = new ArrayList<String>();
		    for (Object o : array)
		    	publishers.add((String) o);
		    Topic topic = new Topic(topicId, topicName, publishers, subscribers);
		    topics.put(topicId, topic);
		}
		
		//add brokers
		arr = obj.getJSONArray("broker");
		for (int i = 0; i < arr.length(); i++) 
		{
		    String brokerId = arr.getJSONObject(i).getString("brokerId");
		    String brokerName = arr.getJSONObject(i).getString("brokerName");
		    int bufferSize = arr.getJSONObject(i).getInt("bufferSize");
		    int processingRate = arr.getJSONObject(i).getInt("processingRate");
		    JSONArray array = arr.getJSONObject(i).getJSONArray("topics");
		    ArrayList<String> topics = new ArrayList<String>();
		    for (Object o : array)
		    	topics.add((String) o);
		    Broker broker = new Broker(brokerId, brokerName, bufferSize, processingRate, topics);
		    brokers.add(broker);
		}
		
		//get system available bandwidth
		double systemBandwidth = obj.getDouble("systemBandwidth");
		App.systemBandwidth = systemBandwidth;
		String bandwidthPolicy = obj.getString("bandwidthPolicy");
		App.bandwidthPolicy = bandwidthPolicy;
		double channelLossAN = obj.getDouble("commChannelLossAN");
		CHANNEL_LOSS_AN = channelLossAN;
		double channelLossRT = obj.getDouble("commChannelLossRT");
		CHANNEL_LOSS_RT = channelLossRT;
		double channelLossTS = obj.getDouble("commChannelLossTS");
		CHANNEL_LOSS_TS = channelLossTS;
		double channelLossVS = obj.getDouble("commChannelLossVS");
		CHANNEL_LOSS_VS = channelLossVS;
		int brokerCapacity = obj.getInt("brokerCapacity");
		BROKER_CAPACITY = brokerCapacity;
    }

    public void addSources(ArrayList<IoTdevice> devices) {
    	for (IoTdevice device : devices)
    		model.addStation(device.deviceName, CommonConstants.STATION_TYPE_SOURCE);
    }
    
    public void addClasses(ArrayList<IoTdevice> devices, ArrayList<VirtualSensor> virtualSensors, ArrayList<Actuator> actuators, HashMap<String, Topic> topics, 
    						HashMap<String, Subtopic> subtopics) {
		for (IoTdevice device : devices) {			
			if (device.distribution.equals("exponential")) {
				Exponential exp = new Exponential();
//				double arrivalRate = (device.publishFrequency * device.messageSize) / GLOBAL_MESSAGE_SIZE;    //TODO changed this to not having a global message size
				double arrivalRate = (device.publishFrequency * device.messageSize);
				double mean = (double) 1.0 / (arrivalRate / GLOBAL_MESSAGE_SIZE);
//				System.out.println("arrival rate of " + device.deviceName + ": " + arrivalRate);
//				System.out.println("after conversion " + arrivalRate / GLOBAL_MESSAGE_SIZE);
				exp.setMean(mean);
				String className = device.deviceName + "_class";
				model.addClass(className, CommonConstants.CLASS_TYPE_OPEN, Defaults.getAsInteger("classPriority"), null, exp);
				model.setClassRefStation(model.getClassByName(className), model.getStationByName(device.deviceName));
				//TODO add arrival rates to according to subtopics
			}
			else if (device.distribution.equals("deterministic")) {
				Deterministic determinstic = new Deterministic();
//				double arrivalRate = (device.publishFrequency * device.messageSize) / GLOBAL_MESSAGE_SIZE;  //TODO changed this to not having a global message size
				double arrivalRate = (device.publishFrequency * device.messageSize);
				double mean = (double) 1.0 / (arrivalRate / GLOBAL_MESSAGE_SIZE);
				determinstic.setMean(mean);
				String className = device.deviceName + "_class";
				model.addClass(className, CommonConstants.CLASS_TYPE_OPEN, Defaults.getAsInteger("classPriority"), null, determinstic);
				model.setClassRefStation(model.getClassByName(className), model.getStationByName(device.deviceName));
			}
		}
		
		for (VirtualSensor device : virtualSensors) {
			if (device.processingDistribution.equals("exponential")) {
				Exponential exp = new Exponential();
//				double arrivalRate = (device.publishFrequency * device.messageSize) / GLOBAL_MESSAGE_SIZE;   //TODO changed this to not having a global message size
				double arrivalRate = (device.publishFrequency * device.messageSize);
				double mean = (double) 1.0 / (arrivalRate / GLOBAL_MESSAGE_SIZE);
				exp.setMean(mean);
				String className = device.deviceName + "_source_class";
				model.addClass(className, CommonConstants.CLASS_TYPE_OPEN, Defaults.getAsInteger("classPriority"), null, exp);
				model.setClassRefStation(model.getClassByName(className), model.getStationByName(device.deviceName + "_source"));
			}
			else if (device.processingDistribution.equals("deterministic")) {
				Deterministic deterministic = new Deterministic();
//				double arrivalRate = (device.publishFrequency * device.messageSize) / GLOBAL_MESSAGE_SIZE;  //TODO changed this to not having a global message size
				double arrivalRate = (device.publishFrequency * device.messageSize);
				double mean = (double) 1.0 / (arrivalRate / GLOBAL_MESSAGE_SIZE);
				deterministic.setMean(mean);
				String className = device.deviceName + "_source_class";
				model.addClass(className, CommonConstants.CLASS_TYPE_OPEN, Defaults.getAsInteger("classPriority"), null, deterministic);
				model.setClassRefStation(model.getClassByName(className), model.getStationByName(device.deviceName + "_source"));
			}
		}
		
		for (Topic topic : topics.values()) {
			Exponential exp = new Exponential();
			String className = topic.topicName + "_class";
			Object classObject = model.addClass(className, CommonConstants.CLASS_TYPE_OPEN, Defaults.getAsInteger("classPriority"), null, exp);
			model.setClassRefStation(classObject, CommonConstants.STATION_TYPE_CLASSSWITCH);
		}
		
		//adding priorities here
		for (Subtopic subtopic : subtopics.values()) {
			Exponential exp = new Exponential();
			String className = subtopic.name + "_class";
			Object classObject = new Object();
			classObject = model.addClass(className, CommonConstants.CLASS_TYPE_OPEN, subtopic.jmtPriority, null, exp);
			model.setClassRefStation(classObject, CommonConstants.STATION_TYPE_CLASSSWITCH);
		}	
    }
    
    public void addInputQueue() {
		model.addStation("input", CommonConstants.STATION_TYPE_SERVER);
    }
    
    public void addTopics(HashMap<String, Topic> topics) {
		model.addStation("topics_join", CommonConstants.STATION_TYPE_JOIN);
		model.addStation("topics_sink", CommonConstants.STATION_TYPE_SINK);
    	for (Topic topic : topics.values()) {
    		model.addStation(topic.topicName + "_fork", CommonConstants.STATION_TYPE_FORK);
    		model.addStation(topic.topicName + "_switch", CommonConstants.STATION_TYPE_CLASSSWITCH);
    	}
    }
    

	public void addSubTopics(HashMap<String, Topic> topics) {
		for (Topic topic : topics.values()) {
			ArrayList<String> subtopicsList = new ArrayList<String>();
			for (String appName : topic.subscribers) {
				Application app = applications.get(appName);
				String subTopicName = topic.topicId + "_" + appName + "_" + app.applicationCategory + new Integer(app.priority).toString();
				String classSwitchName = subTopicName + "_switch";  //topic_app_prio
				subtopicsClassSwitches.put(subTopicName, classSwitchName);
				model.addStation(classSwitchName, CommonConstants.STATION_TYPE_CLASSSWITCH);
				topic.subTopics.add(subTopicName);
				Subtopic subTopic = new Subtopic(subTopicName, topic.topicId, appName, app.applicationCategory, app.priority, app.jmtPriority);
				subtopics.put(subTopicName, subTopic);
				subtopicsList.add(subTopicName);
			}
		topics_subtopicsList.put(topic.topicId, subtopicsList);	
		}
	}
    
    
    public void addOutputQueue() {
    	model.addStation("outputQueue", CommonConstants.STATION_TYPE_SERVER);
    	model.setStationQueueStrategy(model.getStationByName("outputQueue"), model.STATION_QUEUE_STRATEGY_NON_PREEMPTIVE_PRIORITY);
    }
    
    public void addApplications(HashMap<String, Application> applications) {
    	for (Application app : applications.values()) {
    		model.addStation(app.applicationName, CommonConstants.STATION_TYPE_SERVER);
    		if (app.applicationCategory.equals("AN"))
    			nbOfSubs_AN++;
    		else if (app.applicationCategory.equals("RT"))
    			nbOfSubs_RT++;
    		else if (app.applicationCategory.equals("TS"))
    			nbOfSubs_TS++;
    		else if (app.applicationCategory.equals("VS"))
    			nbOfSubs_VS++;
    	}		
    }
    
    public void addActuators(ArrayList<Actuator> actuators) {
    	for (Actuator actuator : actuators) {
    		model.addStation(actuator.deviceName, CommonConstants.STATION_TYPE_SERVER);		
    		if (actuator.applicationCategory.equals("AN"))
    			nbOfSubs_AN++;
    		else if (actuator.applicationCategory.equals("RT"))
    			nbOfSubs_RT++;
    		else if (actuator.applicationCategory.equals("TS"))
    			nbOfSubs_TS++;
    		else if (actuator.applicationCategory.equals("VS"))
    			nbOfSubs_VS++;
    	}
    		
    }
    
    public void addVirtualSensor(ArrayList<VirtualSensor> virtualSensors, ArrayList<Broker> brokers) {
    	for (VirtualSensor sensor : virtualSensors) {
    		model.addStation(sensor.deviceName + "_processing", CommonConstants.STATION_TYPE_SERVER);
    		model.addStation(sensor.deviceName + "_source", CommonConstants.STATION_TYPE_SOURCE);
    		if (sensor.applicationCategory.equals("AN"))
    			nbOfSubs_AN++;
    		else if (sensor.applicationCategory.equals("RT"))
    			nbOfSubs_RT++;
    		else if (sensor.applicationCategory.equals("TS"))
    			nbOfSubs_TS++;
    		else if (sensor.applicationCategory.equals("VS"))
    			nbOfSubs_VS++;
    	}
    }
    
    public void setConnections() {
    	Broker broker = brokers.get(0);
    	
    	//connecting sources to topic class switches
    	for (IoTdevice device : iotDevices) {
    		for (String topic : device.publishedTopics)
    			model.setConnected(model.getStationByName(device.deviceName), model.getStationByName(topic + "_switch"), true);
    	}
    	
    	//connecting class switches to the input queue
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName(topic.topicName + "_switch"), broker.brokerName, true);
    	
    	//connecting the input queue to topic forks
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName("input"), model.getStationByName(topic.topicName + "_fork"), true);
    	
    	
    	//connecting the topic forks (for subtopics) to subtopics class switches
    	for (Topic topic : topics.values()) 
    		for (String subTopicName : topic.subTopics) {
    			String classSwitchName = subtopicsClassSwitches.get(subTopicName);
    			model.setConnected(model.getStationByName(topic.topicName + "_fork"), model.getStationByName(classSwitchName), true);
    		}
    			
    	//connecting the subtopics class switches to the output queue
    	for (Topic topic : topics.values()) 
    		for (String subTopicName : topic.subTopics) {
    			String classSwitchName = subtopicsClassSwitches.get(subTopicName);
    			model.setConnected(model.getStationByName(classSwitchName), model.getStationByName("outputQueue"), true);
    		}
    			
    	
    	
//    	//connecting the output queue to the topic forks
//    	for (Topic topic : topics.values()) {
//    		model.setConnected(model.getStationByName("outputQueue"), model.getStationByName(topic.topicName + "_fork"), true);
//    	}
    	
    	for (Actuator actuator : actuators)
    		for (String topic : actuator.subscribedTopics)
    			model.setConnected(model.getStationByName(topic + "_fork"), model.getStationByName(actuator.deviceId + "_outputQueue"), true);
    	
    	for (VirtualSensor virtualSensor : virtualSensors)
    		for (String topic : virtualSensor.subscribedTopics)
    			model.setConnected(model.getStationByName(topic + "_fork"), model.getStationByName(virtualSensor.deviceId + "_outputQueue"), true);
    	
//    	connecting apps to
//    	1. output queue
//    	2. topics class switch
//    	for (String appId : applications.keySet())
//    		model.setConnected(model.getStationByName(appId + "_outputQueue"), model.getStationByName(appId), true);
    	for (Application app : applications.values()) {
    			model.setConnected(model.getStationByName("outputQueue"), model.getStationByName(app.applicationName), true);
    			model.setConnected(model.getStationByName(app.applicationName), model.getStationByName("topics_class_switch"), true);
    	}
    	
    	//connecting the topics class switch to topic joins
    	for (Topic topic : topics.values())
			model.setConnected(model.getStationByName("topics_class_switch"), model.getStationByName(topic.topicName + "_join"), true);
    	
//    	connecting topic joins to topic sinks
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName(topic.topicName + "_join"), model.getStationByName(topic.topicName + "_sink"), true);
    	
//    	connecting actuators to
//    	1. output queue
//    	2. topics class switch
    		
    	for (Actuator actuator : actuators) {
			model.setConnected(model.getStationByName("output queue"), model.getStationByName(actuator.deviceId), true);
			model.setConnected(model.getStationByName(actuator.deviceId), model.getStationByName("topics_class_switch"), true);
    	}
    	
//    	connecting virtual sensors to 
//    	1. output queue
//    	2. topic forks
//    	3. class switch
    	for (VirtualSensor sensor : virtualSensors) {
    		for (String topic : sensor.subscribedTopics) {
    			model.setConnected(model.getStationByName("output queue"), model.getStationByName(sensor.deviceId + "_processing"), true);
    			model.setConnected(model.getStationByName(sensor.deviceName + "_processing"), model.getStationByName(topic + "_join"), true);
    		}
    			
    		for (String topic : sensor.publishedTopics)
    			model.setConnected(model.getStationByName(sensor.deviceName + "_source"), model.getStationByName(topic + "_switch"), true);
    	}

    	//connecting class switches to broker
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName(topic.topicName + "_switch"), model.getStationByName(broker.brokerName), true);
    }
    
    
    
    public void setConnectionsDropping() {
    	Broker broker = brokers.get(0);
    	
    	//connecting sources to topic class switches
    	for (IoTdevice device : iotDevices) {
    		for (String topic : device.publishedTopics)
    			model.setConnected(model.getStationByName(device.deviceName), model.getStationByName(topic + "_switch"), true);
    	}
    	
    	//connecting class switches to the input queue
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName(topic.topicName + "_switch"), broker.brokerName, true);
    	
    	//connecting the input queue to topic forks
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName("input"), model.getStationByName(topic.topicName + "_fork"), true);
    	
    	//connecting the topic forks (for subtopics) to subtopics class switches
    	for (Topic topic : topics.values()) 
    		for (String subTopicName : topic.subTopics) {
    			String classSwitchName = subtopicsClassSwitches.get(subTopicName);
    			model.setConnected(model.getStationByName(topic.topicName + "_fork"), model.getStationByName(classSwitchName), true);
    		}
	
    	//connecting the subtopics class switches to the output queue
    	for (Topic topic : topics.values()) 
    		for (String subTopicName : topic.subTopics) {
    			String classSwitchName = subtopicsClassSwitches.get(subTopicName);
    			model.setConnected(model.getStationByName(classSwitchName), model.getStationByName("outputQueue"), true);
    		}
    			
    	//connecting the subtopics class switches to the class switch before dropping sink    	
    	for (Topic topic : topics.values()) 
    		for (String subTopicName : topic.subTopics) {
    			String classSwitchName = subtopicsClassSwitches.get(subTopicName);
//    			model.setConnected(model.getStationByName(classSwitchName), model.getStationByName("dropping_sink"), true);
    			model.setConnected(model.getStationByName(classSwitchName), model.getStationByName("topics_class_switch_dropping"), true);
    		}
    	
    	//connecting class switch for dropping to topic joins for dropping
//    	for (Topic topic : topics.values()){
//    		model.setConnected(model.getStationByName("topics_class_switch_dropping"), model.getStationByName(topic.topicName + "_join_dropping"), true);
//    		model.setConnected( model.getStationByName(topic.topicName + "_join_dropping"), model.getStationByName("dropping_sink"), true);
//    	}
    	model.setConnected(model.getStationByName("topics_class_switch_dropping"), model.getStationByName("dropping_join"), true);
		model.setConnected( model.getStationByName("dropping_join"), model.getStationByName("dropping_sink"), true);
    			
    	
//    	//connecting the output queue to the topic forks
//    	for (Topic topic : topics.values()) {
//    		model.setConnected(model.getStationByName("outputQueue"), model.getStationByName(topic.topicName + "_fork"), true);
//    	}
    	
    	for (Actuator actuator : actuators)
    		for (String topic : actuator.subscribedTopics)
    			model.setConnected(model.getStationByName(topic + "_fork"), model.getStationByName(actuator.deviceId + "_outputQueue"), true);
    	
    	for (VirtualSensor virtualSensor : virtualSensors)
    		for (String topic : virtualSensor.subscribedTopics)
    			model.setConnected(model.getStationByName(topic + "_fork"), model.getStationByName(virtualSensor.deviceId + "_outputQueue"), true);
    	
//    	connecting apps to
//    	1. output queue
//    	2. topics class switch
//    	for (String appId : applications.keySet())
//    		model.setConnected(model.getStationByName(appId + "_outputQueue"), model.getStationByName(appId), true);
    	for (Application app : applications.values()) {
    			model.setConnected(model.getStationByName("outputQueue"), model.getStationByName(app.applicationName), true);
    			model.setConnected(model.getStationByName(app.applicationName), model.getStationByName("topics_class_switch"), true);
    	}
    	
    	//connecting the topics class switch to topic joins
//    	for (Topic topic : topics.values())
//			model.setConnected(model.getStationByName("topics_class_switch"), model.getStationByName(topic.topicName + "_join"), true);
    	model.setConnected(model.getStationByName("topics_class_switch"), model.getStationByName("topics_join"), true);
    	
//    	connecting topic joins to topic sinks
//    	for (Topic topic : topics.values())
//    		model.setConnected(model.getStationByName(topic.topicName + "_join"), model.getStationByName(topic.topicName + "_sink"), true);
    	model.setConnected(model.getStationByName("topics_join"), model.getStationByName("topics_sink"), true);
    	
//    	connecting actuators to
//    	1. output queue
//    	2. topics class switch
    		
    	for (Actuator actuator : actuators) {
			model.setConnected(model.getStationByName("output queue"), model.getStationByName(actuator.deviceId), true);
			model.setConnected(model.getStationByName(actuator.deviceId), model.getStationByName("topics_class_switch"), true);
    	}
    	
//    	connecting virtual sensors to 
//    	1. output queue
//    	2. topic forks
//    	3. class switch
    	for (VirtualSensor sensor : virtualSensors) {
    		for (String topic : sensor.subscribedTopics) {
    			model.setConnected(model.getStationByName("output queue"), model.getStationByName(sensor.deviceId + "_processing"), true);
    			model.setConnected(model.getStationByName(sensor.deviceName + "_processing"), model.getStationByName(topic + "_join"), true);
    		}
    			
    		for (String topic : sensor.publishedTopics)
    			model.setConnected(model.getStationByName(sensor.deviceName + "_source"), model.getStationByName(topic + "_switch"), true);
    	}

    	//connecting class switches to broker
    	for (Topic topic : topics.values())
    		model.setConnected(model.getStationByName(topic.topicName + "_switch"), model.getStationByName(broker.brokerName), true);
    }
    
    public void setClassSwitchMatrix(Topic topic) {
    	Object classSwitch = model.getStationByName(topic.topicName + "_switch");
//		System.out.println(topic.topicName);
    	for (String publisher : topic.publishers) {
//    		System.out.println(publisher);
    		model.setClassSwitchMatrix(classSwitch, model.getClassByName(publisher + "_class"), model.getClassByName(publisher + "_class"), 0.0f);
    		model.setClassSwitchMatrix(classSwitch, model.getClassByName(publisher + "_class"), model.getClassByName(topic.topicName + "_class"), 1.0f);
    	}
    	
    }
    
    public void setClassSwitchMatrixSubtopics(Subtopic subtopic) {
    	Object classSwitch = model.getStationByName(subtopicsClassSwitches.get(subtopic.name));
//    	System.out.println(subtopic.name);
//    	System.out.println(subtopic.parentTopicName);
		model.setClassSwitchMatrix(classSwitch, model.getClassByName(subtopic.parentTopicName + "_class"), model.getClassByName(subtopic.parentTopicName + "_class"), 0.0f);
		model.setClassSwitchMatrix(classSwitch, model.getClassByName(subtopic.parentTopicName + "_class"), model.getClassByName(subtopic.name + "_class"), 1.0f);
    }

    
    /**
     * this is only used in case we have one output queue per application 
     */
    public void setInputQueueRouting(HashMap<String, Topic> topics) {
    	Object inputQueue = model.getStationByName("input");
    	for (Topic topic : topics.values()) {
    		ProbabilityRouting probaRouting = new ProbabilityRouting();
    		probaRouting.getValues().put(model.getStationByName(topic.topicName + "_fork"), 1d);
    		model.setRoutingStrategy(inputQueue, model.getClassByName(topic.topicName + "_class"), probaRouting);
    	}
    }
    
    public void setVirtualSensorsRouting(ArrayList<VirtualSensor> virtualSensors) {
    	for (VirtualSensor sensor : virtualSensors) {
    		for (String topicName : sensor.subscribedTopics) {
    			ProbabilityRouting probaRouting = new ProbabilityRouting();
    			probaRouting.getValues().put(model.getStationByName(topicName + "_join"), 1d);
    			model.setRoutingStrategy(model.getStationByName(sensor.deviceName + "_processing"), model.getClassByName(topicName + "_class"), probaRouting);
    		}
    	}
    }
    
    public void setActuatorRouting(ArrayList<Actuator> actuators) {
    	for (Actuator actuator : actuators) {
    		for (String topicName : actuator.subscribedTopics) {
    			ProbabilityRouting probaRouting = new ProbabilityRouting();
    			probaRouting.getValues().put(model.getStationByName(topicName + "_join"), 1d);
    			model.setRoutingStrategy(model.getStationByName(actuator.deviceName), model.getClassByName(topicName + "_class"), probaRouting);
    		}
    	}
    }
    

    
    
    public void calculateBwNeededByEachCategory(HashMap<String, Application> applications) {
    	for (Application app : applications.values())
    		for (String topicName : app.subscribedTopics) {
    			if (app.applicationCategory.equals("AN"))
    				arrivalRate_AN += topicsRate.get(topicName);
    			else if (app.applicationCategory.equals("RT"))
    				arrivalRate_RT += topicsRate.get(topicName);
    			else if (app.applicationCategory.equals("TS"))
    				arrivalRate_TS += topicsRate.get(topicName);
    			else if (app.applicationCategory.equals("VS"))
    				arrivalRate_VS += topicsRate.get(topicName);
    		}			
    }
    
    //calculate service time based on application category
    
    public void setOutputQueueServiceTime(HashMap<String, Topic> topics) {
    	Object outputQueue = model.getStationByName("outputQueue");
    	if (!bandwidthPolicy.equals("none")) {
    		System.out.println(bandwidthPolicy);
    		model.setStationNumberOfServers(outputQueue, 4);
    	}
    	System.out.println(bandwidthPolicy);
		for (Subtopic subtopic : subtopics.values()) {
			if (subtopic.category.equals("RT")) {
				Deterministic serviceTimeDistribution = new Deterministic();
				if (bandwidthPolicy.equals("none"))
					serviceTimeDistribution.setMean(1/(allocatedBw_RT * (1048576.0/GLOBAL_MESSAGE_SIZE)));
				else
					serviceTimeDistribution.setMean(1/(allocatedBw_RT));
//				if (bandwidthPolicy.equals("shared"))
//					serviceTimeDistribution.setMean(1/(allocatedBw_RT * nbOfSubs_RT));
//				else if (bandwidthPolicy.equals("topics"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_RT * (topicsRate.get(subtopic.parentTopicName) / arrivalRate_RT)));
//				else if (bandwidthPolicy.equals("maxmin"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_RT * nbOfSubs_RT));
				model.setServiceTimeDistribution(outputQueue, model.getClassByName(subtopic.name + "_class"), serviceTimeDistribution);
			}
			else if (subtopic.category.equals("AN")) {
				Deterministic serviceTimeDistribution = new Deterministic();
				if (bandwidthPolicy.equals("none"))
					serviceTimeDistribution.setMean(1/(allocatedBw_AN * (1048576.0/GLOBAL_MESSAGE_SIZE)));
				else 
					serviceTimeDistribution.setMean(1/(allocatedBw_AN));
//				if (bandwidthPolicy.equals("shared"))
//					serviceTimeDistribution.setMean(1/(allocatedBw_AN * nbOfSubs_AN));
//				else if (bandwidthPolicy.equals("topics"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_AN * (topicsRate.get(subtopic.parentTopicName) / arrivalRate_AN)));
//				else if (bandwidthPolicy.equals("maxmin"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_AN * nbOfSubs_AN));
				model.setServiceTimeDistribution(outputQueue, model.getClassByName(subtopic.name + "_class"), serviceTimeDistribution);
			}
			else if (subtopic.category.equals("TS")) {
				Deterministic serviceTimeDistribution = new Deterministic();
				if (bandwidthPolicy.equals("none"))
					serviceTimeDistribution.setMean(1/(allocatedBw_TS * (1048576.0/GLOBAL_MESSAGE_SIZE)));
				else
					serviceTimeDistribution.setMean(1/(allocatedBw_TS));
//				if (bandwidthPolicy.equals("shared"))
//					serviceTimeDistribution.setMean(1/(allocatedBw_TS * nbOfSubs_TS));
//				else if (bandwidthPolicy.equals("topics"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_TS * (topicsRate.get(subtopic.parentTopicName) / arrivalRate_TS)));
//				else if (bandwidthPolicy.equals("maxmin"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_TS * nbOfSubs_TS));
				model.setServiceTimeDistribution(outputQueue, model.getClassByName(subtopic.name + "_class"), serviceTimeDistribution);
			}
			else if (subtopic.category.equals("VS")) {
				Deterministic serviceTimeDistribution = new Deterministic();
				if (bandwidthPolicy.equals("none"))
					serviceTimeDistribution.setMean(1/(allocatedBw_VS * (1048576.0/GLOBAL_MESSAGE_SIZE)));
				else 
					serviceTimeDistribution.setMean(1/(allocatedBw_VS));
//				if (bandwidthPolicy.equals("shared"))
//					serviceTimeDistribution.setMean(1/(allocatedBw_VS * nbOfSubs_VS));
//				else if (bandwidthPolicy.equals("topics"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_VS * (topicsRate.get(subtopic.parentTopicName) / arrivalRate_VS)));
//				else if (bandwidthPolicy.equals("maxmin"))
//					serviceTimeDistribution.setMean(1 / (allocatedBw_VS * nbOfSubs_VS));
				model.setServiceTimeDistribution(outputQueue, model.getClassByName(subtopic.name + "_class"), serviceTimeDistribution);
			}
		}
  
    }    

    public void setOutputQueuesBufferSize(ArrayList<OutputQueue> outputQueues) {
    	for (OutputQueue queue : outputQueues)
			model.setStationQueueCapacity(model.getStationByName(queue.name), 5);
    }
    
    public void setInputQueueServiceTime(ArrayList<Broker> brokers, HashMap<String, Topic> topics) {
    	Broker broker = brokers.get(0);
    	Exponential exponential = new Exponential();
    	exponential.setMean(1.0/broker.processingRate);
    	for (Topic topic : topics.values())
    		model.setServiceTimeDistribution(model.getStationByName(broker.brokerName), model.getClassByName(topic.topicName + "_class"), exponential);
    }
    
    public void setApplicationsServiceTime(HashMap<String, Application> applications) {
    	for (Application app : applications.values()) {
    		Exponential exponential = new Exponential();
    		exponential.setMean(1.0/app.processingRate);
    		for (String subscribedTopic : app.subscribedTopics) {
    			model.setServiceTimeDistribution(model.getStationByName(app.applicationName), model.getClassByName(subscribedTopic + "_class"),
    					exponential);
    		}
    	}
    }
    
    public void setVirtualSensorsServiceTime(ArrayList<VirtualSensor> virtualSensors) {
    	for (VirtualSensor sensor : virtualSensors) {
    		Exponential exponential = new Exponential();
    		exponential.setMean(1.0/sensor.processingRate);
    		for (String subscribedTopic : sensor.subscribedTopics) {
    			model.setServiceTimeDistribution(model.getStationByName(sensor.deviceName + "_processing"), model.getClassByName(subscribedTopic + "_class"),
    					exponential);
    		}
    	}
    }

    public void setApplicationsRouting(HashMap<String, Application> applications) {
    	for (Application app : applications.values()) {
    		for (String topicName : app.subscribedTopics) {
    			ProbabilityRouting probaRouting = new ProbabilityRouting();
    			probaRouting.getValues().put(model.getStationByName(topicName + "_join"), 1d);
    			model.setRoutingStrategy(model.getStationByName(app.applicationName), model.getClassByName(topicName + "_class"), probaRouting);
    		}
    	}
    }

    public void setActuatorServiceTime(ArrayList<Actuator> actuators) {
    	for (Actuator actuator : actuators) {
    		Object actuatorObject = model.getStationByName(actuator.deviceName);
    		Exponential exp = new Exponential();
    		exp.setMean(1.0/actuator.publishFrequency);
    		for (String topicName : actuator.subscribedTopics) {
    			model.setServiceTimeDistribution(actuatorObject, model.getClassByName(topicName + "_class"), exp);
    			model.setStationQueueCapacity(actuatorObject, actuator.publishFrequency);
    		}
    	}
    }
    
    public void setOutputQueueRouting(HashMap<String, Subtopic> subtopics) {
    	Object outputQueue = model.getStationByName("outputQueue");
    	for (Subtopic subtopic : subtopics.values()) {
    		ProbabilityRouting probaRouting = new ProbabilityRouting();
    		probaRouting.getValues().put(model.getStationByName(subtopic.appName), 1d);
    		model.setRoutingStrategy(outputQueue, model.getClassByName(subtopic.name + "_class"), probaRouting);
    	}
	}
    
    public void setPerformanceMetrics() {
    	
    	//To measure the response time for each subtopic in the fcr
    	for (Subtopic subtopic : subtopics.values())
			model.addMeasure(SimulationDefinition.MEASURE_RP, FINITE_CAPACITY_REGION_OBJECT, model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
			
		
		//Measure fcr throughput for each class of topics
		for (Subtopic subtopic : subtopics.values())
			model.addMeasure(SimulationDefinition.MEASURE_X, FINITE_CAPACITY_REGION_OBJECT,  model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		
		//Measure total fcr throughput
		model.addMeasure(SimulationDefinition.MEASURE_X, FINITE_CAPACITY_REGION_OBJECT, "", CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		
		//Measure fcr dropping for each class of topics
		for (Subtopic subtopic : subtopics.values())
			model.addMeasure(SimulationDefinition.MEASURE_DR, FINITE_CAPACITY_REGION_OBJECT, model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		
		//Measure utilization of fcr for each class
		for (Subtopic subtopic : subtopics.values())
			model.addMeasure(SimulationDefinition.MEASURE_U, FINITE_CAPACITY_REGION_OBJECT, model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		
		//Measure total fcr utilization
		model.addMeasure(SimulationDefinition.MEASURE_U, FINITE_CAPACITY_REGION_OBJECT, "", CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		
    	//To measure the utilization of the output queue
		model.addMeasure(SimulationDefinition.MEASURE_U, model.getStationByName("outputQueue"), "", CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		for (Subtopic subtopic : subtopics.values()) {
			model.addMeasure(SimulationDefinition.MEASURE_U, model.getStationByName("outputQueue"), model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		}
			
		
		//To measure the throughput of the output queue 
		model.addMeasure(SimulationDefinition.MEASURE_X, model.getStationByName("outputQueue"), "", CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		for (Subtopic subtopic : subtopics.values()) {
			model.addMeasure(SimulationDefinition.MEASURE_X, model.getStationByName("outputQueue"), model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		}
    		
		//To measure the drop rate for the output queue
		model.addMeasure(SimulationDefinition.MEASURE_DR, model.getStationByName("outputQueue"), "", CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		for (Subtopic subtopic : subtopics.values()) {
			model.addMeasure(SimulationDefinition.MEASURE_DR, model.getStationByName("outputQueue"), model.getClassByName(subtopic.name + "_class"), CONFIDENCE_INTERVAL, RELATIVE_ERROR, true);
		}
	
		//Maximum duration of the simulation is 5 minutes
		model.setMaximumDuration(300.0);
    }
    
    public void allocateBandwidth() {
    	if (bandwidthPolicy.equals("none")) {
    		allocatedBw_AN = systemBandwidth;
    		allocatedBw_RT = systemBandwidth;
    		allocatedBw_TS = systemBandwidth;
    		allocatedBw_VS = systemBandwidth;
    	}
    	
    	else if (bandwidthPolicy.equals("shared")) {
    		double bandwidth = systemBandwidth * GLOBAL_MESSAGE_SIZE / 4.0;
    		allocatedBw_AN = bandwidth;
    		allocatedBw_RT = bandwidth;
    		allocatedBw_TS = bandwidth;
    		allocatedBw_VS = bandwidth;
    	}
    	else if (bandwidthPolicy.equals("topics")) {
    		double arrivalRate_sum = arrivalRate_AN + arrivalRate_RT + arrivalRate_TS + arrivalRate_VS;
    		allocatedBw_AN = ((arrivalRate_AN / arrivalRate_sum) * systemBandwidth * GLOBAL_MESSAGE_SIZE);
    		allocatedBw_RT = ((arrivalRate_RT / arrivalRate_sum) * systemBandwidth * GLOBAL_MESSAGE_SIZE);
    		allocatedBw_TS = ((arrivalRate_TS / arrivalRate_sum) * systemBandwidth * GLOBAL_MESSAGE_SIZE);
    		allocatedBw_VS = ((arrivalRate_VS / arrivalRate_sum) * systemBandwidth * GLOBAL_MESSAGE_SIZE);
    	}
    	
    	else if (bandwidthPolicy.equals("maxmin")) {
    		ArrayList<Flow> flows = new ArrayList<Flow>();
    		Flow flow_AN = new Flow("AN", arrivalRate_AN);
    		Flow flow_RT = new Flow("RT", arrivalRate_RT);
    		Flow flow_TS = new Flow("TS", arrivalRate_TS);
    		Flow flow_VS = new Flow("VS", arrivalRate_VS);
    		flows.add(flow_AN);
    		flows.add(flow_RT);
    		flows.add(flow_TS);
    		flows.add(flow_VS);
    		
    		ArrayList<Flow> bandwidthAllocation = maxMinAlgorithm(systemBandwidth, flows);
    		for (Flow flow : bandwidthAllocation) {
    			if (flow.type.equals("AN")) {
    				allocatedBw_AN = flow.allocatedBandwidth;
    			}
    				
    			else if (flow.type.equals("RT")) {
    				allocatedBw_RT = flow.allocatedBandwidth;
    			}
    				
    			else if (flow.type.equals("TS")) {
    				allocatedBw_TS = flow.allocatedBandwidth;
    			}
    				
    			else {
    				allocatedBw_VS = flow.allocatedBandwidth;
    			}
    				
    		}
    	}
    	
    	System.out.println(allocatedBw_AN);
    	System.out.println(allocatedBw_RT);
    	System.out.println(allocatedBw_TS);
    	System.out.println(allocatedBw_VS);
    	System.out.println(allocatedBw_AN + allocatedBw_TS + allocatedBw_RT + allocatedBw_VS);
    	
    	
    	
    }
    
    public ArrayList<Flow> maxMinAlgorithm(double totalBandwidth, ArrayList<Flow> flows){
		double overflowBandwidth = 0d;
		totalBandwidth = totalBandwidth * 1048576 / GLOBAL_MESSAGE_SIZE;
		double sum = 0;
//		System.out.println(flows.size());
		for (Flow flow : flows) {
			flow.allocatedBandwidth = (totalBandwidth/flows.size());
//			flow.allocatedBandwidth = totalBandwidth/(flows.size());
			sum += flow.requiredBandwidth;
		}
		
		for (Flow f : flows)
			System.out.println(f.type + ": " + f.requiredBandwidth);
		
		/*
		 * DEBUG
		 */
//		System.out.println("Needed bw to transmit all data: " + sum / GLOBAL_MESSAGE_SIZE);
//		System.out.println("Available bw: " + systemBandwidth);
//		System.out.println("Allocated bw for each flow before overflow: ");
//		for (Flow flow : flows)
//			System.out.println(flow.type + ": " + "needed bw: " + flow.requiredBandwidth/GLOBAL_MESSAGE_SIZE + " allocated bw: " + flow.allocatedBandwidth / GLOBAL_MESSAGE_SIZE);
		
			
		
		for (Flow flow : flows) {
			if (flow.allocatedBandwidth >= flow.requiredBandwidth) {
				flow.isSatisfied = true;
				overflowBandwidth += (flow.allocatedBandwidth - flow.requiredBandwidth) ;
				flow.allocatedBandwidth = flow.requiredBandwidth;
			}
		}
		
		/*
		 * DEBUG
		 */
//		for (Flow flow : flows)
//			System.out.println(flow.type + ": " + flow.isSatisfied);
		
//		System.out.println("overflow: " + overflowBandwidth / GLOBAL_MESSAGE_SIZE);
//		System.out.println("overflow: " + overflowBandwidth / GLOBAL_MESSAGE_SIZE);
		while (overflowBandwidth != 0) {
//			System.out.println("overflow: " + overflowBandwidth / GLOBAL_MESSAGE_SIZE);
			boolean existsFlowUnsatisfied = false;
			int unsatisfiedFlows = 0;
			//check if there are flows that are unsatisfied
			for (Flow flow : flows) 
				if (!flow.isSatisfied) {
					unsatisfiedFlows++;
					existsFlowUnsatisfied = true;
				}
			
			if (!existsFlowUnsatisfied) {
				for (Flow flow : flows) {
//					System.out.println(flow.type  + " before allocation:"  + flow.allocatedBandwidth / GLOBAL_MESSAGE_SIZE);
					flow.allocatedBandwidth += overflowBandwidth / flows.size();
//					System.out.println("after allocation: " + flow.allocatedBandwidth / GLOBAL_MESSAGE_SIZE);
				}
				break;
			}
					
			for (Flow flow : flows) {
				if (!flow.isSatisfied) {
					flow.allocatedBandwidth += overflowBandwidth/unsatisfiedFlows;
				}
			}
			overflowBandwidth = 0;
			
			for (Flow flow : flows) {
				if (flow.allocatedBandwidth >= flow.requiredBandwidth) {
					flow.isSatisfied = true;
					overflowBandwidth += (flow.allocatedBandwidth - flow.requiredBandwidth);
					flow.allocatedBandwidth = flow.requiredBandwidth;
				}
			}

		}
		
		double totalbw = 0;
		for (Flow flow : flows) {
			totalbw += flow.allocatedBandwidth;
		}
		System.out.println("system bw: " + systemBandwidth);
		System.out.println("Total allocated bw: " + totalbw);
		return flows;
	}
}
