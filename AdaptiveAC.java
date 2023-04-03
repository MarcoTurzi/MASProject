import java.util.Map;

import genius.core.actions.Offer;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.Actions;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;

public class AdaptiveAC extends AcceptanceStrategy {
	
	Simulator simulator;
	private HHAAgent HHagent;
	private BRAMAgent bramAgent;
	
	public AdaptiveAC() {
		
	}
	
	public AdaptiveAC(Simulator simulator) {
		
		this.simulator = simulator;
	}
	
	
	public void setHHagent(HHAAgent hHagent) {
		HHagent = hHagent;
	}

	public void setBramAgent(BRAMAgent bramAgent) {
		this.bramAgent = bramAgent;
	}

	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, offeringStrategy, opponentModel, parameters);
	}



	@Override
	public Actions determineAcceptability() {
		if (simulator.getBestAgent().equals("HH")) {
			
			if (this.HHagent.getLastAction() instanceof Offer) {
				return Actions.Reject;
			}
			
		} else if (simulator.getBestAgent().equals("Bram")) {
			
			if (this.bramAgent.getLastAction() instanceof Offer) {
				return Actions.Reject;
			}
		}
		return Actions.Accept;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
