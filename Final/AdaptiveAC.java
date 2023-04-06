import java.util.Map;

import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.ActionWithBid;
import genius.core.actions.Offer;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

public class AdaptiveAC extends AcceptanceStrategy {
	
	Simulator simulator;
	private HHAgent HHagent;
	private OurBRAMAgent bramAgent;
	private TFTAgent tftAgent;
	private final double MIN_UTIL_OPEN = 0.9;
	
	public AdaptiveAC() {
		
	}
	
	public AdaptiveAC(Simulator simulator) {
		
		this.simulator = simulator;
	}
	
	
	public void setHHagent(HHAgent hHagent2) {
		HHagent = hHagent2;
	}

	public void setBramAgent(OurBRAMAgent bramAgent) {
		this.bramAgent = bramAgent;
	}
	
	public void setTFTAgent(TFTAgent tftAgent) {
		this.tftAgent = tftAgent;
	}

	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, offeringStrategy, opponentModel, parameters);
	}



	@Override
	public Actions determineAcceptability() {
		//When our agent is the second to make a bid, simulator has no data to compute the most likely opponent agent. In this situation our model accept 
		//the bid only if its utility is higher or equal to MIN_UTIL_OPEN
		if(simulator.getBestAgent() == null) {
			
			if(negotiationSession.getUtilitySpace().getUtility(negotiationSession.getOpponentBidHistory().getLastBid()) >= MIN_UTIL_OPEN) {
				return Actions.Accept;
			}else return Actions.Reject;
			
		}
		
		//Simulator returns the most likely opponent's agent type and then acts as the agent type that
		//performs best against the opponent's type
		
		if (simulator.getBestAgent().equals("Bram")) {
			
			if (this.HHagent.getLastAction() instanceof Accept) {
				
				return Actions.Accept;
			}
			
		} else if (simulator.getBestAgent().equals("HH")) {
			
			if (this.tftAgent.getLastAction() instanceof Accept) {
				
				return Actions.Accept;
			}
		} else if (simulator.getBestAgent().equals("TFT")) {
			
			if (this.HHagent.getLastAction() instanceof Accept) {
				
				return Actions.Accept;
			}
		}
		return Actions.Reject;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AdaptiveAC";
	}
	
}
