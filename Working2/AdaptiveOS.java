
import java.util.Map;


import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.ActionWithBid;
import genius.core.actions.Offer;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.utility.AdditiveUtilitySpace;

public class AdaptiveOS extends OfferingStrategy {

	private Simulator simulator;
	private AdaptiveAC acceptanceStrategy;
	private KLH HHagent;
	private OurBRAMAgent bramAgent;
	private TFTAgent tftAgent;

	
	public AdaptiveOS(Simulator simulator, AdaptiveAC accStrategy) {
		this.simulator = simulator;
		this.acceptanceStrategy = accStrategy;
	}
	
	
	
	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel opponentModel, OMStrategy omStrategy,
			Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, opponentModel, omStrategy, parameters);
		simulator.initiate(negotiationSession, negotiationSession.getUtilitySpace());
		this.HHagent = new KLH(negotiationSession, (AdditiveUtilitySpace)negotiationSession.getUtilitySpace());
		this.bramAgent = new OurBRAMAgent(negotiationSession, negotiationSession.getUtilitySpace());
		this.tftAgent = new TFTAgent(negotiationSession, negotiationSession.getUtilitySpace());
		
		this.acceptanceStrategy.setBramAgent(bramAgent);
		this.acceptanceStrategy.setHHagent(HHagent);
		this.acceptanceStrategy.setTFTAgent(tftAgent);
		      
	}
	
	



	@Override
	public BidDetails determineOpeningBid() {
		Bid nextBid = this.negotiationSession.getMaxBidinDomain().getBid();
	    simulator.receiveAction(new Offer(new AgentID("simu"), nextBid));
		return this.negotiationSession.getMaxBidinDomain();
	}

	@Override
	public BidDetails determineNextBid() {
		
		
			
		    
		simulator.evaluatePredictions(negotiationSession.getOpponentBidHistory().getLastBid());
		
		this.simulator.updateUtilitySpace(opponentModel.getOpponentUtilitySpace());
		
		try {
			this.HHagent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOpponentBidHistory().getLastBid()));
		} catch (Exception e) {
			
		}
		this.bramAgent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOpponentBidHistory().getLastBid()));
		this.tftAgent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOpponentBidHistory().getLastBid()));
		negotiationSession.getOpponentBidHistory().getHistory().remove(negotiationSession.getOpponentBidHistory().size() - 1);
		//Get best Agent
		
	    simulator.evaluatePredictions(negotiationSession.getOpponentBidHistory().getLastBid());
	    String bestAgent = simulator.getBestAgent();
	    
		Bid nextBid = null;
		
		
		
		
		ActionWithBid hhAction = (ActionWithBid) this.HHagent.chooseAction();
		ActionWithBid bramAction = (ActionWithBid) this.bramAgent.chooseAction();
		ActionWithBid tftAction = (ActionWithBid) this.tftAgent.chooseAction();
		
		HHagent.setLastAction(hhAction);
		bramAgent.setLastAction(bramAction);
		tftAgent.setLastAction(tftAction);
		
		
		if (bestAgent.equals("Bram")) {
			nextBid = hhAction.getBid();
		}else if (bestAgent.equals("HH")) {
			nextBid = bramAction.getBid();
		}else if (bestAgent.equals("TFT")) {
			nextBid = bramAction.getBid();
		}
		
		
		simulator.receiveAction(new Offer(new AgentID("simu"), nextBid));
		return new BidDetails(nextBid, negotiationSession.getUtilitySpace().getUtility(nextBid));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AdaptiveOS";
	}

}
