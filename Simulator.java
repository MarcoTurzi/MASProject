package OurAgent;

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
	public Map<String, Double> distribution;
	public String bestAgent;
	public NegotiationSession negotiationSession;
	
	private double learningRate;
	private double worstPredictionDistanceSoFar; // We use this to normalize distances. 


	// Agents
	private Integer numberOfAgents = 3;
	private HHAAgent HHagent;
	private OurBRAMAgent BramAgent;
	private TFTAgent TFTAgent;
	
	// We initialize our predictions with chooseAction() for now.
	public Action HHPrediction = null;
	public Action BRAMPrediction = null;
	public Action TFTPrediction = null;
	
	// Confidence in agents:
	private double confBram = 1D/numberOfAgents;
	private double confHH = 1D/numberOfAgents;
	private double confTFT = 1D/numberOfAgents;
	
	public Simulator() { //NegotiationSession negotiationSession, UtilitySpace utilSpace) {
		//this.negotiationSession = negotiationSession;
		distribution = new HashMap<String, Double>();
		
		this.bestAgent = "TFT";
		this.learningRate = 0.3;
		this.worstPredictionDistanceSoFar = 0.3; // Initial value is just an idea/guess.
		
		
	}
	
	public void initializeAgents() {
		this.HHagent = new HHAAgent(this.negotiationSession, this.negotiationSession.getUtilitySpace());
		this.BramAgent = new OurBRAMAgent(this.negotiationSession, this.negotiationSession.getUtilitySpace());
		this.TFTAgent = new TFTAgent(this.negotiationSession, this.negotiationSession.getUtilitySpace());
	}
	
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		HHagent.updateUtilitySpace(utilSpace);
		BramAgent.updateUtilitySpace(utilSpace);
		TFTAgent.updateUtilitySpace(utilSpace);
	}
	
	public void receiveAction(Action action) {
		// We call this method when we TAKE an action. Arguments: our action. The simulated models RECEIVE our actions.                             
		
		HHagent.ReceiveMessage(action);
		BramAgent.ReceiveMessage(action);
		TFTAgent.ReceiveMessage(action);
		
		// assumption: after the message is received, we can make our predictions
		HHPrediction = HHagent.chooseAction();
		BRAMPrediction = BramAgent.chooseAction();
		TFTPrediction = TFTAgent.chooseAction();
		
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
		
		DefaultActionWithBid TFTAction = (DefaultActionWithBid) TFTPrediction;
		Bid tftBid = TFTAction.getBid();
		double distanceTFT = receivedBid.getDistance(bramBid);
		
		double bestPredictionThisRound = 1;
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceHH);
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceBram);
				
		// update worstPredictionSoFar.
		this.worstPredictionDistanceSoFar = Math.max(distanceHH, this.worstPredictionDistanceSoFar);
		this.worstPredictionDistanceSoFar = Math.max(distanceBram, this.worstPredictionDistanceSoFar); // etc
		this.worstPredictionDistanceSoFar = Math.max(distanceTFT, this.worstPredictionDistanceSoFar); 
		
		this.confHH = distanceHH == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceHH)/this.worstPredictionDistanceSoFar * this.confHH:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceHH)/this.worstPredictionDistanceSoFar * this.confHH;
		
		this.confBram = distanceBram == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * this.confBram:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * this.confBram;
		
		this.confTFT = distanceTFT == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceTFT)/this.worstPredictionDistanceSoFar * this.confTFT:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * this.confBram;
		
		// The idea is: 
		//    - The confidence only increases if it made the best prediction. Other confidences decreases
		//    - We take into account how much the confidence decreases when an agent didn't make the best prediction, based on qualit of prediction
		
		// Now we normalize values:
		double totalConf = this.confHH + this.confBram + this.confTFT;
		this.confHH = this.confHH / totalConf;
		this.confBram = this.confBram / totalConf;
		this.confTFT = this.confTFT / totalConf;
		
		this.distribution.put("HH", this.confHH);
		this.distribution.put("Bram", this.confBram);
		this.distribution.put("TFT", this.confTFT);
		
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