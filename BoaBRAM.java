package OurAgent;

import java.util.List;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import OurAgent.Simulator;
import agents.rlboa.AverageTitForTatOfferingGamma1;
import bilateralexamples.boacomponents.AC_Next;
import bilateralexamples.boacomponents.BestBid;
import bilateralexamples.boacomponents.HardHeadedFrequencyModel;
import bilateralexamples.boacomponents.TimeDependent_Offering;
import genius.core.AgentID;
import genius.core.actions.Action;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.BOAagentBilateral;
import genius.core.boaframework.BoaParty;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.NoModel;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_BRAMAgent;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_HardHeaded;
import negotiator.boaframework.acceptanceconditions.anac2011.AC_NiceTitForTat;
import negotiator.boaframework.offeringstrategy.anac2011.BRAMAgent_Offering;
import negotiator.boaframework.offeringstrategy.anac2011.HardHeaded_Offering;
import negotiator.boaframework.offeringstrategy.anac2011.NiceTitForTat_Offering;
import negotiator.boaframework.omstrategy.NullStrategy;
import negotiator.boaframework.opponentmodel.PerfectModel;

/**
 * This example shows how BOA components can be made into an independent
 * negotiation party and which can handle preference uncertainty.
 * 
 * Note that this is equivalent to adding a BOA party via the GUI by selecting
 * the components and parameters. However, this method gives more control over
 * the implementation, as the agent designer can choose to override behavior
 * (such as handling preference uncertainty).
 * <p>
 * For more information, see: Baarslag T., Hindriks K.V., Hendrikx M.,
 * Dirkzwager A., Jonker C.M. Decoupling Negotiating Agents to Explore the Space
 * of Negotiation Strategies. Proceedings of The Fifth International Workshop on
 * Agent-based Complex Automated Negotiations (ACAN 2012), 2012.
 * https://homepages.cwi.nl/~baarslag/pub/Decoupling_Negotiating_Agents_to_Explore_the_Space_of_Negotiation_Strategies_ACAN_2012.pdf
 * 
 * @author Tim Baarslag
 */
@SuppressWarnings("serial")
public class BoaBRAM extends BOAagentBilateral 
{
	private static final long serialVersionUID = 1L;

	public BoaBRAM(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.utilitySpace =  (AbstractUtilitySpace) utilSpace;
		this.negotiationSession = negoSession;
		this.timeline = negoSession.getTimeline();
		agentSetup();
		
	}
	
	@Override
	public void agentSetup() {

		// AverageTitForTat2 makes decisions based on its own preferences
		opponentModel = new NoModel();
		opponentModel.init(this.negotiationSession, new HashMap<String, Double>());

		// OMS not relevant for NoModel
		omStrategy = new NullStrategy(this.negotiationSession);


		offeringStrategy = new BRAMAgent_Offering();
		
		acceptConditions = new AC_BRAMAgent(); //negotiationSession, offeringStrategy, 1, 0);	
		setDecoupledComponents(acceptConditions, offeringStrategy, opponentModel, omStrategy);
	}
	
	private Action lastAction;
	
	public Action getLastAction() {
		return lastAction;
	}



	public void setLastAction(Action lastAction) {
		this.lastAction = lastAction;
	}
	
	
	
	@Override
	public String getName() {
		return "HH BOA";
	}
	
	
	public void setUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}

	@Override
	public AgentID getAgentID() {
		// TODO Auto-generated method stub
		return new AgentID(getUniqueIdentifier());
	}
	
	
	// All the rest of the agent functionality is defined by the components selected above, using the BOA framework
}