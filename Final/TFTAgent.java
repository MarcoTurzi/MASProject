import genius.core.AgentID;
import genius.core.actions.Action;
import genius.core.boaframework.NegotiationSession;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

import java.util.List;
import java.util.Random;

import agents.rlboa.AverageTitForTat1;


@SuppressWarnings("deprecation")
public class TFTAgent extends AverageTitForTat1 {

	//Implementation of TitForTat Agent
	private static final long serialVersionUID = 1L;

	private Action lastAction;
	
	public Action getLastAction() {
		return lastAction;
	}



	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}
	//Constructor for the agents used to make decisions
	public TFTAgent(NegotiationSession negoSess, UtilitySpace utilSpace) {
		
		this.negotiationSession = negoSess;
		this.utilitySpace = (AdditiveUtilitySpace) utilSpace;
		
		super.agentSetup();
		
	}
	//Constructor used by simulator to initialize simulated agents
	public TFTAgent(NegotiationSession negoSess) {
		
		this.negotiationSession = negoSess;
		this.utilitySpace = estimateUtilitySpace();
		
		super.agentSetup();
		
	}
	
	public AbstractUtilitySpace estimateUtilitySpace() 
	{
		Random rand = new Random();
		AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory = new AdditiveUtilitySpaceFactory(this.negotiationSession.getDomain());
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
	
	@Override
	public String getName() {
		return "Tit-for-Tat agent with gamma = 1";
	}
	
	
	public void setUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}

	@Override
	public AgentID getAgentID() {
		return new AgentID(getUniqueIdentifier());
	}
	
	

}