package OurAgent;

import java.util.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import bilateralexamples.boacomponents.BestBid;
import genius.core.Bid;
import genius.core.bidding.BidDetails;
import genius.core.bidding.BidDetailsSorterUtility;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

/**
 * VARIATION OF BestBid and NiceTitForTat 
 * 
 * When t < 0.8: use BestBid
 * When t >= 0.8: use a variation of NiceTitForTAt (which uses a version of PickBestN)
 * 
 * Variation of NiceTitForTat:
 * - We alternate every round after t = 0.8 between BestBid and PickBestN
 * 		The reason for this is that only wish to use BestN when we are not sure about if our opponentModel is a good representation
 * 		However, in case it is a good representation and the opponent is still working towards a good strategy, we want to continue helping him
 * 			by also using BestBid
 * - When we use PickBestN, we change the value of N depending on the domain size and the time left
 * 		The further we go into the negotiation, the more likely it is that our opponentModel is wrong, so the more we should incorporate randomness
 * 		To incorporate this, we multiply N with a multiplier 1 < t < 5, which linearly increases as time increases
 * 
 * The time at which the opponent model stops updating is t = 0.8
 * 
 * Inspired by Author: @author Mark Hendrikx
 */
public class BestBidVariation extends OMStrategy {

	private boolean domainIsBig;
	private long possibleBids;
	private Random random;
	private BidDetailsSorterUtility comp = new BidDetailsSorterUtility();
	
	private long domainSize;
	private int n;

	private boolean pickBestBid; // After t = 0.8, we alternate pickBestN: either n = N or n = 1

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel model, Map<String, Double> parameters) {
		initializeAgent(negotiationSession, model);
	}

	private void initializeAgent(NegotiationSession negoSession, OpponentModel model) {
		
		this.domainSize = -1; // initial value to compare
		
		this.negotiationSession = negoSession;
		try {
			super.init(negotiationSession, model, new HashMap<String, Double>());
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.possibleBids = negotiationSession.getUtilitySpace().getDomain().getNumberOfPossibleBids();
		domainIsBig = (possibleBids > 10000);
		random = new Random();
	}

	/**
	 * Selects a random bid from the best N bids, where N depends on the domain AND TIME REMAINING
	 * size.
	 * 
	 * @param set
	 *            of similarly preferred bids.
	 * @return nextBid to be offered
	 */
	@Override
	public BidDetails getBid(List<BidDetails> bidsInRange) {
		
		if(this.domainSize == -1) {
			this.domainSize = bidsInRange.size(); // only needs to be calculated once
		}
		
		// If we're still in first 80% of rounds/time, or want to use n=1 anyway, use BestBid
		// Otherwise, use variation
		if(negotiationSession.getTime() < 0.8 || this.pickBestBid) {
			this.pickBestBid = false;
			return getBestBid(bidsInRange);
		} 
		else {
			
			this.pickBestBid = true;
			
			double t = negotiationSession.getTime();
			
			// Copied: put bids in array
			ArrayList<BidDetails> bidsOM = new ArrayList<BidDetails>();
			for (BidDetails bid : bidsInRange) {
				double utility;
				try {
					utility = model.getBidEvaluation(bid.getBid());
					BidDetails bidDetails = new BidDetails(bid.getBid(), utility);
					bidsOM.add(bidDetails);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			double multiplier = Math.round(1 + (t - 0.8) * 20); // if t = 0.8, then multiplier is 1. if t = 1, then multiplier = 5
					
			this.n = (int) Math.max(this.domainSize, Math.round(this.domainSize * multiplier / 20.0));
			

			Collections.sort(bidsOM, comp);

			int entry = random.nextInt(Math.min(bidsOM.size(), this.n));
			Bid opponentBestBid = bidsOM.get(entry).getBid();
			BidDetails nextBid = null;
			try {
				nextBid = new BidDetails(opponentBestBid, negotiationSession.getUtilitySpace().getUtility(opponentBestBid),
						negotiationSession.getTime());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return nextBid;
			
			
		}
		
		
	}
	
	/*
	 * Methods copied from bilateralexamples/boacomponents/BestBid.java
	 * Most efficient method to return Best Bid
	 */
	public BidDetails getBestBid(List<BidDetails> allBids) {
		// 1. If there is only a single bid, return this bid
			if (allBids.size() == 1) {
				return allBids.get(0);
			}
			double bestUtil = -1;
			BidDetails bestBid = allBids.get(0);

			// 2. Check that not all bids are assigned at utility of 0
			// to ensure that the opponent model works. If the opponent model
			// does not work, offer a random bid.
			boolean allWereZero = true;
			// 3. Determine the best bid
			for (BidDetails bid : allBids) {
				double evaluation = model.getBidEvaluation(bid.getBid());
				if (evaluation > 0.0001) {
					allWereZero = false;
				}
				if (evaluation > bestUtil) {
					bestBid = bid;
					bestUtil = evaluation;
				}
			}
			// 4. The opponent model did not work, therefore, offer a random bid.
			if (allWereZero) {
				Random r = new Random();
				return allBids.get(r.nextInt(allBids.size()));
			}
			return bestBid;

	}
	
	
	/**
	 * Method which specifies when the opponent model may be updated. In small
	 * domains the model may be updated up till 0.99 of the time. In large
	 * domains the updating process stops half way.
	 * 
	 * @return true if the opponent model may be updated
	 */
	@Override
	public boolean canUpdateOM() {
		// in the last seconds we don't want to lose any time
		if (negotiationSession.getTime() > 0.8)
			return false;

		return true;
	}

	@Override
	public String getName() {
		return "Offer Best Bids";
	}
}