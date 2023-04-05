import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bilateralexamples.boacomponents.BestBid;
import genius.core.boaframework.AcceptanceStrategy;
import genius.core.boaframework.BoaParty;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OfferingStrategy;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;

public class MixedAgent extends BoaParty {

	@Override
	public void init(NegotiationInfo info) 
	{
		
		Simulator simulator = new Simulator();
		// The choice for each component is made here
		AcceptanceStrategy 	ac  = new AdaptiveAC(simulator);
		OfferingStrategy 	os  = new AdaptiveOS(simulator, (AdaptiveAC) ac);
		OpponentModel 		om  = new AdaptiveOM(simulator);
		OMStrategy			oms = new BestBid();
		
		// All component parameters can be set below.
		Map<String, Double> noparams = Collections.emptyMap();
		
		
		// Initialize all the components of this party to the choices defined above
		configure(ac, noparams, 
				os,	noparams, 
				om, noparams,
				oms, noparams);
		super.init(info);
	}

	/**
	 * Specific functionality, such as the estimate of the utility space in the
	 * face of preference uncertainty, can be specified by overriding the
	 * default behavior.
	 * 
	 * This example estimator sets all weights and all evaluator values randomly.
	 */
	@Override
	public AbstractUtilitySpace estimateUtilitySpace() 
	{
		AdditiveUtilitySpaceFactory additiveUtilitySpaceFactory = new AdditiveUtilitySpaceFactory(getDomain());
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
	public String getDescription() {
		// TODO Auto-generated method stub
		return "MixedStategy";
	}

}
