import agents.anac.y2011.HardHeaded.KLH;
import genius.core.AgentID;
import genius.core.actions.Action;
import genius.core.boaframework.NegotiationSession;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

public class HHAgent extends KLH {

	private Action lastAction;
	
	public HHAgent(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.utilitySpace =  (AbstractUtilitySpace) utilSpace;
		this.timeline = negoSession.getTimeline();
		this.init();
		
	}
	
	@Override
	public AgentID getAgentID() {
		// TODO Auto-generated method stub
		return new AgentID(getUniqueIdentifier());
	}

	public Action getLastAction() {
		return lastAction;
	}



	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}



	public void setUtilitySpace(AdditiveUtilitySpace oppUtilSpace) {
		
		this.utilitySpace = oppUtilSpace;
		
	}
	
}
