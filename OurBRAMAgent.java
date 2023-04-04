package OurAgent;

import agents.anac.y2011.BramAgent.BRAMAgent;
import genius.core.boaframework.NegotiationSession;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

public class OurBRAMAgent extends BRAMAgent {

	public OurBRAMAgent(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.utilitySpace = (AbstractUtilitySpace) utilSpace;
		
		this.timeline = negoSession.getTimeline();
		init();
		
	}
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}
}
