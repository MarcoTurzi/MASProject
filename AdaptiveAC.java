package OurAgent;

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
	private OurBRAMAgent bramAgent;
	private TFTAgent TFTAgent;
	
	public AdaptiveAC() {
		
	}
	
	public AdaptiveAC(Simulator simulator) {
		
		this.simulator = simulator;
	}
	
	
	public void setHHagent(HHAAgent hHagent) {
		this.HHagent = hHagent;
	}

	public void setBramAgent(OurBRAMAgent bramAgent) {
		this.bramAgent = bramAgent;
	}
	
	public void setTFTAgent(TFTAgent tftAgent) {
		this.TFTAgent = tftAgent;
	}

	@Override
	public void init(NegotiationSession negotiationSession, OfferingStrategy offeringStrategy,
			OpponentModel opponentModel, Map<String, Double> parameters) throws Exception {
		super.init(negotiationSession, offeringStrategy, opponentModel, parameters);
	}



	@Override
	public Actions determineAcceptability() {
		if (simulator.getBestAgent().equals("HH")) {
			
			if (simulator.HHPrediction instanceof Offer) {
				return Actions.Reject;
			}
			
		} else if (simulator.getBestAgent().equals("Bram")) {
			
			if (simulator.BRAMPrediction instanceof Offer) {
				return Actions.Reject;
			}
		} else if (simulator.getBestAgent().equals("TFT")) {
			
			if (simulator.TFTPrediction instanceof Offer) {
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