import genius.core.AgentID;
import genius.core.actions.Action;
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

	private Action lastAction;
	
	public Action getLastAction() {
		return lastAction;
	}



	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}
	
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
	
	
	public void setUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}

	@Override
	public AgentID getAgentID() {
		// TODO Auto-generated method stub
		return new AgentID(getUniqueIdentifier());
	}
	
	

}