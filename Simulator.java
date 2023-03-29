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
	private Map<String, Float> distribution;
	private String bestAgent;
	private NegotiationSession negotiationSession;
	private HHAAgent HHagent;
	
	public Simulator(NegotiationSession negotiationSession, UtilitySpace utilSpace) {
		this.negotiationSession = negotiationSession;
		distribution = new HashMap<String, Float>();
		HHagent = new HHAAgent(negotiationSession, utilSpace);
	}
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		HHagent.updateUtilitySpace(utilSpace);
	}
	
	public void receiveAction(Action action) {
		HHagent.ReceiveMessage(action);
	}
	
	public void getDistances(Bid bid) {
		DefaultActionWithBid HHaction = (DefaultActionWithBid) HHagent.chooseAction();
		Bid hhBid = HHaction.getBid();
		double distanceHH = bid.getDistance(hhBid);
	}
	
	public Map<String, Float> getDistributions() {
		return this.distribution;
	}
	
	private void computeBestAgent() {
		Set<String> keys = distribution.keySet();
		Iterator<String> iter = keys.iterator();
		String bestAgent = new String();
		float bestAgentVal = 0;
		
		while(iter.hasNext()) {
			
			String agent = iter.next();
			float agentVal = this.distribution.get(agent);
			if (agentVal > bestAgentVal){
					bestAgent = agent;
			}
		}
		this.bestAgent = bestAgent;
	}
}
