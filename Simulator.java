package OurAgent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.actions.DefaultActionWithBid;
import genius.core.boaframework.NegotiationSession;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

public class Simulator {
	private Map<String, Double> distribution;
	private String bestAgent;
	private NegotiationSession negotiationSession;
	
	private double learningRate;
	private double worstPredictionDistanceSoFar; // We use this to normalize distances. 

	// Agents
	private BoaHH HHagent;
	private BoaBRAM BramAgent;
	private BoaTFT tftAgent;
	
	// We initialize our predictions with chooseAction() for now.
	private Action HHPrediction = null;
	private Action BRAMPrediction = null;
	private Action TFTPrediction = null;
	
	// Confidence in agents:
	private double confBram = 1D/3;
	private double confHH = 1D/3;
	private double confTFT = 1D/3;
	
	public Simulator(NegotiationSession negotiationSession, UtilitySpace utilSpace) {
		this.negotiationSession = negotiationSession;
		distribution = new HashMap<String, Double>();
		
		this.learningRate = 0.3;
		this.worstPredictionDistanceSoFar = 0.3; // Initial value is just an idea/guess.
		
		initiate(negotiationSession, utilSpace);
	}
	
	public Simulator() {
		
		distribution = new HashMap<String, Double>();
		
		
		this.learningRate = 0.3;
		this.worstPredictionDistanceSoFar = 0.3; // Initial value is just an idea/guess.
		
		
	}
	
	public void initiate(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.negotiationSession = negoSession;
		HHagent = new BoaHH(negoSession, utilSpace);
		BramAgent = new BoaBRAM(negoSession, utilSpace);
		tftAgent = new BoaTFT(negoSession, utilSpace);
		
	}
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		HHagent.setUtilitySpace((AdditiveUtilitySpace)utilSpace);
		BramAgent.setUtilitySpace((AdditiveUtilitySpace)utilSpace);
		tftAgent.setUtilitySpace(utilSpace);
	}
	
	public void receiveAction(Action action) {
		// We call this method when we TAKE an action. Arguments: our action. The simulated models RECEIVE our actions.                             
		
		HHagent.ReceiveMessage(action);
		BramAgent.ReceiveMessage(action);
		tftAgent.ReceiveMessage(action);
		
		// assumption: after the message is received, we can make our predictions
		HHPrediction = HHagent.chooseAction();
		BRAMPrediction = BramAgent.chooseAction();
		TFTPrediction = tftAgent.chooseAction();
		
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
		
		DefaultActionWithBid tftAction = (DefaultActionWithBid) TFTPrediction;
		Bid tftBid = tftAction.getBid();
		double distanceTFT = receivedBid.getDistance(tftBid);
		
		double bestPredictionThisRound = 1;
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceHH);
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceBram);
		bestPredictionThisRound = Math.min(bestPredictionThisRound, distanceTFT);
				
		// update worstPredictionSoFar.
		this.worstPredictionDistanceSoFar = Math.max(distanceHH, this.worstPredictionDistanceSoFar);
		this.worstPredictionDistanceSoFar = Math.max(distanceBram, this.worstPredictionDistanceSoFar); // etc
		this.worstPredictionDistanceSoFar = Math.max(distanceTFT, this.worstPredictionDistanceSoFar);
		
		this.confHH = distanceHH == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceHH)/this.worstPredictionDistanceSoFar * confHH:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceHH)/this.worstPredictionDistanceSoFar * confHH;
		
		this.confBram = distanceBram == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * confBram:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceBram)/this.worstPredictionDistanceSoFar * confBram;
		this.confTFT = distanceTFT == bestPredictionThisRound ? 
				learningRate + (1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceTFT)/this.worstPredictionDistanceSoFar * confTFT:
				(1 - learningRate) * (this.worstPredictionDistanceSoFar - distanceTFT)/this.worstPredictionDistanceSoFar * confTFT;
		
		// The idea is: 
		//    - The confidence only increases if it made the best prediction. Other confidences decreases
		//    - We take into account how much the confidence decreases when an agent didn't make the best prediction, based on qualit of prediction
		
		// Now we normalize values:
		double totalConf = confHH + confBram + confTFT;
		confHH = confHH / totalConf;
		confBram = confBram / totalConf;
		confTFT = confTFT / totalConf;
		
		this.distribution.put("HH", confHH);
		this.distribution.put("Bram", confBram);
		this.distribution.put("TFT", confTFT);
		
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