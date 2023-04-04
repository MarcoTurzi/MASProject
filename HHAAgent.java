package OurAgent;

import agents.anac.y2011.HardHeaded.KLH;
import genius.core.actions.Action;
import genius.core.boaframework.NegotiationSession;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

public class HHAAgent extends KLH {
	
	public HHAAgent(NegotiationSession negoSession, UtilitySpace utilSpace) {
		
		this.utilitySpace = (AbstractUtilitySpace) utilSpace;
		
		this.timeline = negoSession.getTimeline();
		init();
		
	}
	
	//public void setUtilitySpace(AdditiveUtilitySpace oppUtilSpace) {
	//	
//		this.utilitySpace = oppUtilSpace;
		
	//
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilitySpace = (AdditiveUtilitySpace)utilSpace;
		
	}
	
}
