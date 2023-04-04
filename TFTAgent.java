package OurAgent;

import genius.core.boaframework.NegotiationSession;

import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

import agents.rlboa.AverageTitForTat1;


@SuppressWarnings("deprecation")
public class TFTAgent extends AverageTitForTat1 {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TFTAgent(NegotiationSession negoSess, UtilitySpace utilSpace) {
		
		// it does reach here BREAK POINT
		
		
		this.negotiationSession = negoSess;
		// it doesn't reach here BREAK POINT
		this.utilitySpace = (AdditiveUtilitySpace) utilSpace;
		
		super.agentSetup();
		
	}
	
	@Override
	public String getName() {
		return "Tit-for-Tat agent with gamma = 1";
	}
	
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}
	
	

}
