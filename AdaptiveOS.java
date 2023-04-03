import java.util.HashMap;
import java.util.Map;

import agents.bayesianopponentmodel.OpponentModelUtilSpace;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.ActionWithBid;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.utility.UtilitySpace;

public class AdaptiveOS extends OfferingStrategy {

	private Simulator simulator;
	private AdaptiveAC acceptanceStrategy;
	private HHAAgent HHagent;
	private BRAMAgent bramAgent;

	private final double MIN_UTILITY_OPEN = 0.9;
	
	public AdaptiveOS(Simulator simulator, AdaptiveAC accStrategy) {
		this.simulator = simulator;
		this.acceptanceStrategy = accStrategy;
	}
	
	
	
	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy,
			Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		this.HHagent = new HHAAgent(negotiationSession, negotiationSession.getUtilitySpace());
		this.bramAgent = new BRAMAgent(negotiationSession, negotiationSession.getUtilitySpace());
		
		this.acceptanceStrategy.setBramAgent(bramAgent);
		this.acceptanceStrategy.setHHagent(HHagent);
		      
	}
	
	



	@Override
	public BidDetails determineOpeningBid() {
		return this.negotiationSession.getMaxBidinDomain();
	}

	@Override
	public BidDetails determineNextBid() {
		
		
		this.HHagent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOwnBidHistory().getLastBid()));
		this.bramAgent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOwnBidHistory().getLastBid()));
		
		
		//Get best Agent
		
	    simulator.evaluatePredictions(negotiationSession.getOpponentBidHistory().getLastBid());
	    String bestAgent = simulator.getBestAgent();
	    
		Bid nextBid = null;
		
		
		
		
		ActionWithBid hhAction = (ActionWithBid) this.HHagent.chooseAction();
		ActionWithBid bramAction = (ActionWithBid) this.bramAgent.chooseAction();
		
		
		
		if (bestAgent.equals("HH")) {
			nextBid = hhAction.getBid();
		}else if (bestAgent.equals("Bram")) {
			nextBid = bramAction.getBid();
		}
		
		
		simulator.receiveAction(new Offer(new AgentID("simu"), nextBid));
		return new BidDetails(nextBid, negotiationSession.getUtilitySpace().getUtility(nextBid));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
