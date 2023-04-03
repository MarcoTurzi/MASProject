import java.util.List;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.actions.DefaultActionWithBid;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.utility.UtilitySpace;

public class Simulator {
	private Map<String, Double> distribution;
	private String bestAgent;
	private NegotiationSession negotiationSession;
	
	private double learningRate;
	private double worstPredictionDistanceSoFar; // We use this to normalize distances. 

	// Agents
	private HHAAgent HHagent;
	private BRAMAgent BramAgent;
	
	// We initialize our predictions with chooseAction() for now.
	private Action HHPrediction = null;
	private Action BRAMPrediction = null;
	
	// Confidence in agents:
	private double confBram = 0.5;
	private double confHH = 0.5;
	
	public Simulator(NegotiationSession negotiationSession, UtilitySpace utilSpace) {
		this.negotiationSession = negotiationSession;
		distribution = new HashMap<String, Double>();
		
		this.learningRate = 0.3;
		this.worstPredictionDistanceSoFar = 0.3; // Initial value is just an idea/guess.
		
		HHagent = new HHAAgent(negotiationSession, utilSpace);
		BramAgent = new BRAMAgent(negotiationSession, utilSpace);
	}
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		HHagent.updateUtilitySpace(utilSpace);
		BramAgent.updateUtilitySpace(utilSpace);
	}
	
	public void receiveAction(Action action) {
		// We call this method when we TAKE an action. Arguments: our action. The simulated models RECEIVE our actions.                             
		
		HHagent.ReceiveMessage(action);
		BramAgent.ReceiveMessage(action);
		
		// assumption: after the message is received, we can make our predictions
		HHPrediction = HHagent.chooseAction();
		BRAMPrediction = HHagent.chooseAction();
		
	}
	
	public void evaluatePredictions(Bid receivedBid) {
		// We call this method when we RECEIVE an Action from our opponent
		// We can (probably should) also place this method in our eventual agent class, because we also call it there.
		
		
		DefaultActionWithBid HHaction = (DefaultActionWithBid) HHPrediction;
		Bid hhBid = HHaction.getBid();
		double distanceHH = receivedBid.getDistance(hhBid);
		
		DefaultActionWithBid bramAction = (DefaultActionWithBid) BRAMPrediction;
		Bid bramBid = bramAction.getBid();
		double distanceBram = receivedBid.getDistance(bramBid);
		
		double bestPredictionThisRound = 1;
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceHH);
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceBram);
				
		// update worstPredictionSoFar.
		this.worstPredictionDistanceSoFar = Math.max(distanceHH, this.worstPredictionDistanceSoFar);
		this.worstPredictionDistanceSoFar = Math.max(distanceBram, this.worstPredictionDistanceSoFar); // etc
		
		this.confHH = distanceHH == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceHH)/this.worstPredictionDistanceSoFar * confHH:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceHH)/this.worstPredictionDistanceSoFar * confHH;
		
		this.confBram = distanceBram == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * confBram:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * confBram;
		
		// The idea is: 
		//    - The confidence only increases if it made the best prediction. Other confidences decreases
		//    - We take into account how much the confidence decreases when an agent didn't make the best prediction, based on qualit of prediction
		
		// Now we normalize values:
		double totalConf = confHH + confBram;
		confHH = confHH / totalConf;
		confBram = confBram / totalConf;
		
		this.distribution.put("HH", confHH);
		this.distribution.put("Bram", confBram);
		
		// Formula we use to learn: 
		// Given 	C-bram (confidence/probability of opponent being Bram) 
		//			C-hh (confidence/probability of opponent being HH)
		//			LP (learning rate)
		//			WP (worst prediction so far)
		//			d-bram (distance prediction bram)
		//			d-hh (distance prediction hh)
		
		
		// C-bramNew = LR + (1 - LR) * ((WP - d-bram)/WP) * C-bramOld 		if best prediction
		// C-bramNew = 		(1 - LR) * ((WP - d-bram)/WP) * C-bramOld 		if not best prediction
		// for all agents, than normalize.
		computeBestAgent();
	}
	
	public String getBestAgent() {
		return this.bestAgent;
	}
	
	public Map<String, Double> getDistributions() {
		return this.distribution;
	}
	
	private void computeBestAgent() {
		
		// We should translate the confidences to a distribution-set, or just take the highest confidence and return the corresponding agent 
		
		
		//
		Set<String> keys = distribution.keySet();
		Iterator<String> iter = keys.iterator();
		String bestAgent = new String();
		float bestAgentVal = 0;
		
		while(iter.hasNext()) {
			
			String agent = iter.next();
			double agentVal = this.distribution.get(agent);
			if (agentVal > bestAgentVal){
					bestAgent = agent;
			}
		}
		this.bestAgent = bestAgent;
	}
}