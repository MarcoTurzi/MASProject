import agents.anac.y2011.BramAgent.BRAMAgent;
import genius.core.AgentID;
import genius.core.actions.Action;
import genius.core.boaframework.NegotiationSession;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

public class OurBRAMAgent extends BRAMAgent {

	private Action lastAction;
	
	public Action getLastAction() {
		return lastAction;
	}



	public void setLastAction(Action bestAction) {
		this.lastAction = bestAction;
	}



	@Override
	public AgentID getAgentID() {
		// TODO Auto-generated method stub
		return new AgentID(getUniqueIdentifier());
	}



	public OurBRAMAgent(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.utilitySpace = (AbstractUtilitySpace) utilSpace;
		
		this.timeline = negoSession.getTimeline();
		init();
		
	}
	
	
	
	public void setUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}
}