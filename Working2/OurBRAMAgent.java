import java.util.List;
import java.util.Random;

import agents.anac.y2011.BramAgent.BRAMAgent;
import genius.core.AgentID;
import genius.core.actions.Action;
import genius.core.boaframework.NegotiationSession;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

public class OurBRAMAgent extends BRAMAgent {

	private Action lastAction;
	NegotiationSession negoSession;
	
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



	public OurBRAMAgent(NegotiationSession negoSession) {
		
		this.negoSession = negoSession;
		this.utilitySpace = estimateUtilitySpace();
		
		this.timeline = negoSession.getTimeline();
		init();
		
	}
	
	public OurBRAMAgent(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.negoSession = negoSession;
		this.utilitySpace = (AbstractUtilitySpace) utilSpace;
		
		this.timeline = negoSession.getTimeline();
		init();
		
	}
	
	public AbstractUtilitySpace estimateUtilitySpace() 
	{
		Random rand = new Random();
		AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory = new AdditiveUtilitySpaceFactory(this.negoSession.getDomain());
		List<IssueDiscrete> issues = additiveUtilitySpaceFactory.getIssues();
		for (IssueDiscrete i : issues)
		{
			additiveUtilitySpaceFactory.setWeight(i, rand.nextDouble());
			for (ValueDiscrete v : i.getValues())
				additiveUtilitySpaceFactory.setUtility(i, v, rand.nextDouble());
		}
		
		// Normalize the weights, since we picked them randomly in [0, 1]
		additiveUtilitySpaceFactory.normalizeWeights();
		
		// The factory is done with setting all parameters, now return the estimated utility space
		return additiveUtilitySpaceFactory.getUtilitySpace();
	}
	
	
	public void setUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}
}