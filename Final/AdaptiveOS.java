
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
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.BidHistory;

public class AdaptiveOS extends OfferingStrategy {

	private Simulator simulator;
	private AdaptiveAC acceptanceStrategy;
	private HHAgent HHagent;
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
		//Initialize the agents
		this.HHagent = new HHAgent(negotiationSession, (AdditiveUtilitySpace)negotiationSession.getUtilitySpace());
		this.bramAgent = new OurBRAMAgent(negotiationSession, negotiationSession.getUtilitySpace());
		this.tftAgent = new TFTAgent(negotiationSession, negotiationSession.getUtilitySpace());
		//Set the agents to the acceptanceStategy
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
		
		Bid nextBid = null;
		if (this.negotiationSession.getTime() < 0.95 ) {
			
			
			//update the simulated agents utilityspace using the utilityspace estimated by the opponentmodel
			this.simulator.updateUtilitySpace(opponentModel.getOpponentUtilitySpace());
			
			//every agent receive the opponentbid
			try {
				this.HHagent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOpponentBidHistory().getLastBid()));
			} catch (Exception e) {
				
			}
			this.bramAgent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOpponentBidHistory().getLastBid()));
			this.tftAgent.ReceiveMessage(new Offer(new AgentID("simu"), negotiationSession.getOpponentBidHistory().getLastBid()));
			negotiationSession.getOpponentBidHistory().getHistory().remove(negotiationSession.getOpponentBidHistory().size() - 1);
			//Get best Agent

			//simulator evaluate the prediction based on the opponent last bid to determine the most likely opponent agent type
			simulator.evaluatePredictions(negotiationSession.getOpponentBidHistory().getLastBid());
		    String bestAgent = simulator.getBestAgent();
		    
			
			
			
			//every agent decide which action to perform
			
			ActionWithBid hhAction = (ActionWithBid) this.HHagent.chooseAction();
			ActionWithBid bramAction = (ActionWithBid) this.bramAgent.chooseAction();
			ActionWithBid tftAction = (ActionWithBid) this.tftAgent.chooseAction();
			
			HHagent.setLastAction(hhAction);
			bramAgent.setLastAction(bramAction);
			tftAgent.setLastAction(tftAction);
			
			//our agent decide which action to perform 
			
			if (bestAgent.equals("Bram")) {
				nextBid = hhAction.getBid();
			}else if (bestAgent.equals("HH")) {
				nextBid = bramAction.getBid();
			}else if (bestAgent.equals("TFT")) {
				nextBid = bramAction.getBid();
			}
			
			
			simulator.receiveAction(new Offer(new AgentID("simu"), nextBid));
		}else {
			
			//if the time is almost done our agent start consulting the omStrategy to get Bid whose utility is the average
			// between the opponent average utility and itself average utility.
			double ownAverageUtility = getAvarageUtility(this.negotiationSession.getOwnBidHistory());
			double opponentAverageUtility = getAvarageUtility(this.negotiationSession.getOpponentBidHistory());
			double utilityGoal = (ownAverageUtility + opponentAverageUtility)/2;
			nextBid = this.omStrategy.getBid(new SortedOutcomeSpace(this.negotiationSession.getUtilitySpace()), utilityGoal ).getBid();
			
		}
		
		return new BidDetails(nextBid, negotiationSession.getUtilitySpace().getUtility(nextBid));
	}
	
	private double getAvarageUtility(BidHistory history) {
		
		double sum = 0;
		for (BidDetails bidD : history.getHistory()) {
			
			Bid bid = bidD.getBid();
			sum += this.negotiationSession.getUtilitySpace().getUtility(bid);
			
			
			
		}
		
		return sum/(this.negotiationSession.getOwnBidHistory().getHistory().size());
		
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AdaptiveOS";
	}

}
