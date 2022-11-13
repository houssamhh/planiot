package iotSys.broker;

import java.util.ArrayList;
import java.util.HashMap;

public class Playground {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hi");
		Playground app = new Playground();
		Flow analyticsFlow = new Flow ("AN", 2);
		Flow realtimeFlow = new Flow ("RT", 2.6);
		Flow transactionalFlow = new Flow ("TS", 4);
		Flow videostreamingFlow = new Flow ("VS", 5);
	
		ArrayList<Flow> flows = new ArrayList<Flow>();
		flows.add(analyticsFlow);
		flows.add(realtimeFlow);
		flows.add(transactionalFlow);
		flows.add(videostreamingFlow);
		
		ArrayList<Flow> bandwidthAllocation = (ArrayList<Flow>) app.minMaxAlgorithm(10.0, flows).clone();
		for (Flow flow : bandwidthAllocation) {
			System.out.println(flow.type + ": " + flow.allocatedBandwidth);
			System.out.println("Hello!");
		}
			
	}

	public ArrayList<Flow> minMaxAlgorithm(double totalBandwidth, ArrayList<Flow> flows){
		ArrayList<Flow> bandwidthAllocation = new ArrayList<Flow>(0);
		double overflowBandwidth = 0d;
		for (Flow flow : flows) 
			flow.allocatedBandwidth = totalBandwidth/(flows.size());
		
		for (Flow flow : flows) {
			if (flow.allocatedBandwidth >= flow.requiredBandwidth) {
				flow.isSatisfied = true;
				overflowBandwidth = flow.allocatedBandwidth - flow.requiredBandwidth;
				flow.allocatedBandwidth = flow.requiredBandwidth;
			}
		}
		
		while (overflowBandwidth != 0) {
			int unstatisfiedFlows = 0;
			for (Flow flow : flows) 
				if (!flow.isSatisfied)
					unstatisfiedFlows++;
			
			for (Flow flow : flows) {
				if (!flow.isSatisfied) {
					flow.allocatedBandwidth += overflowBandwidth/unstatisfiedFlows;
				}
			}
			
			for (Flow flow : flows) {
				if (flow.allocatedBandwidth >= flow.requiredBandwidth) {
					flow.isSatisfied = true;
					overflowBandwidth = flow.allocatedBandwidth - flow.requiredBandwidth;
					flow.allocatedBandwidth = flow.requiredBandwidth;
				}
			}	
		}
		
		for (Flow flow : flows) {
			System.out.println(flow.type + ": " + flow.allocatedBandwidth);
		}
		return bandwidthAllocation;
	}
}
