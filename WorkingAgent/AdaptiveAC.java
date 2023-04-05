import java.util.Map;

import genius.core.actions.Accept;
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
	private final double MIN_UTIL_OPEN = 0.8;
	
	public AdaptiveAC() {
		
	}
	
	public AdaptiveAC(Simulator simulator) {
		
		this.simulator = simulator;
	}
	
	
	public void setHHagent(HHAgent hHagent) {
		HHagent = hHagent;
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
		
		if(simulator.getBestAgent() == null) {
			
			if(negotiationSession.getUtilitySpace().getUtility(negotiationSession.getOpponentBidHistory().getLastBid()) > MIN_UTIL_OPEN) {
				return Actions.Accept;
			}else return Actions.Reject;
			
		}
		
		if (simulator.getBestAgent().equals("Bram")) {
			
			if (this.HHagent.getLastAction() instanceof Accept) {
				return Actions.Accept;
			}
			
		} else if (simulator.getBestAgent().equals("HH")) {
			
			if (this.bramAgent.getLastAction() instanceof Accept) {
				return Actions.Accept;
			}
		} else if (simulator.getBestAgent().equals("TFT")) {
			
			if (this.bramAgent.getLastAction() instanceof Accept) {
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
