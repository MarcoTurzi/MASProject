package OurAgent;


import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.boaframework.NegotiationSession;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.utility.AbstractUtilitySpace;


public class mixedAgent extends AbstractNegotiationParty  {

	
	private Simulator sim;
	/**
	 * Initializes a new instance of the agent.
	 */
	@Override
	public void init(NegotiationInfo info) 
	{
		super.init(info);
		
		NegotiationSession sess = new NegotiationSession(null, utilitySpace, timeline, null, userModel, user); 
		/// we should initialize the right value, idk how yet. We can probably get tehse values from info.getUser(), etc. idk but gtg
		
		this.sim = new Simulator(sess, estimateUtilitySpace());
		
		
	}

	/**
	 * Makes a random offer above the minimum utility target
	 * Accepts everything above the reservation value at the end of the negotiation; or breaks off otherwise. 
	 */
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) 
	{
		// Choose action based on sim.bestAgent
		
		
		Action eventuallyChosenAction = (new Action()).getBid(); // you can't instantiate an Action like this but you get the point
		// We need to let the simulator simulate based on the action we choose. We do this in receiveAction:
		this.sim.receiveAction(eventuallyChosenAction);
		
	}

	private Bid generateRandomBidAboveTarget() 
	{
		Bid randomBid;
		double util;
		int i = 0;
		// try 100 times to find a bid under the target utility
		do 
		{
			randomBid = generateRandomBid();
			util = utilitySpace.getUtility(randomBid);
		} 
		while (util < MINIMUM_TARGET && i++ < 100);		
		return randomBid;
	}

	/**
	 * Remembers the offers received by the opponent.
	 */
	@Override
	public void receiveMessage(AgentID sender, Action action) 
	{
		if (action instanceof Offer) 
		{
			Bid lastOffer = ((Offer) action).getBid();
			
			// Receive action, so evaluate our predictions made in simulator
			this.sim.evaluatePredictions(lastOffer);
		}
	}

	@Override
	public String getDescription() 
	{
		return "----";
	}

	/**
	 * This stub can be expanded to deal with preference uncertainty in a more sophisticated way than the default behavior.
	 */
	@Override
	public AbstractUtilitySpace estimateUtilitySpace() 
	{
		return super.estimateUtilitySpace();
	}
}
