import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.boaframework.NegotiationSession;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Objective;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
import genius.core.utility.UtilitySpace;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import agents.anac.y2011.HardHeaded.*;

public class HHAAgent {
	
	NegotiationSession negoSess;
	AdditiveUtilitySpace utilSpace;
	BidSelector bidSelector;
	BidHistory bidHistory;
	private double MINIMUM_BID_UTILITY = 0.585D;
	private final int TOP_SELECTED_BIDS = 4;
	private final double LEARNING_COEF = 0.2D;
	private final int LEARNING_VALUE_ADDITION = 1;
	private final double UTILITY_TOLORANCE = 0.01D;
	private double Ka = 0.05;
	private double e = 0.05;
	private double discountF = 1D;
	private double lowestYetUtility = 1D;

	private LinkedList<Entry<Double, Bid>> offerQueue = null;
	private Bid opponentLastBid = null;
	private boolean firstRound = true;

	private Domain domain = null;
	private AdditiveUtilitySpace oppUtility = null;
	private int numberOfIssues = 0;

	private double maxUtil = 1;
	private double minUtil = MINIMUM_BID_UTILITY;

	private Bid opponentbestbid = null;
	private Entry<Double, Bid> opponentbestentry;

	private final boolean TEST_EQUIVALENCE = false;
	private Random random100;
	private Random random200;
	int round;


	
	public HHAAgent(NegotiationSession negoSess, UtilitySpace utilSpace) {
		
		this.negoSess = negoSess;
		this.utilSpace = (AdditiveUtilitySpace) utilSpace;
		bidSelector = new BidSelector(this.utilSpace);
		bidHistory = new BidHistory(this.utilSpace);
		oppUtility = (AdditiveUtilitySpace) utilSpace.copy();
		offerQueue = new LinkedList<Entry<Double, Bid>>();
		domain = utilSpace.getDomain();
		numberOfIssues = domain.getIssues().size();

		if (this.utilSpace.getDiscountFactor() <= 1D
				&& this.utilSpace.getDiscountFactor() > 0D)
			discountF = this.utilSpace.getDiscountFactor();
		
		Entry<Double, Bid> highestBid = bidSelector.BidList.lastEntry();

		try {
			maxUtil = this.utilSpace.getUtility(highestBid.getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (TEST_EQUIVALENCE) {
			random100 = new Random(100);
			random200 = new Random(200);
		} else {
			random100 = new Random();
			random200 = new Random();
		}

		double w = 1D / numberOfIssues;
		for (Entry<Objective, Evaluator> e : oppUtility.getEvaluators()) {
			oppUtility.unlock(e.getKey());
			e.getValue().setWeight(w);
			try {
				// set the initial weight for each value of each issue to 1.
				for (ValueDiscrete vd : ((IssueDiscrete) e.getKey())
						.getValues())
					((EvaluatorDiscrete) e.getValue()).setEvaluation(vd, 1);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (this.utilSpace.getReservationValue() != null)
			MINIMUM_BID_UTILITY = this.utilSpace.getReservationValue();

		
	}
	
	public void ReceiveMessage(Action pAction) {
		double opbestvalue;
		if (pAction instanceof Offer) {
			opponentLastBid = ((Offer) pAction).getBid();
			bidHistory.addOpponentBid(opponentLastBid);
			updateLearner();
			try {
				if (opponentbestbid == null)
					opponentbestbid = opponentLastBid;
				else if (this.utilSpace.getUtility(opponentLastBid) > this.utilSpace
						.getUtility(opponentbestbid)) {
					opponentbestbid = opponentLastBid;
				}

				opbestvalue = bidSelector.BidList
						.floorEntry(this.utilSpace.getUtility(opponentbestbid))
						.getKey();

				while (!bidSelector.BidList.floorEntry(opbestvalue).getValue()
						.equals(opponentbestbid)) {
					opbestvalue = bidSelector.BidList.lowerEntry(opbestvalue)
							.getKey();
				}
				opponentbestentry = bidSelector.BidList.floorEntry(opbestvalue);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
	
	private void updateLearner() {

		if (bidHistory.getOpponentBidCount() < 2)
			return;

		int numberOfUnchanged = 0;
		HashMap<Integer, Integer> lastDiffSet = bidHistory
				.BidDifferenceofOpponentsLastTwo();

		// counting the number of unchanged issues
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0)
				numberOfUnchanged++;
		}

		// This is the value to be added to weights of unchanged issues before
		// normalization.
		// Also the value that is taken as the minimum possible weight,
		// (therefore defining the maximum possible also).
		double goldenValue = LEARNING_COEF / numberOfIssues;
		// The total sum of weights before normalization.
		double totalSum = 1D + goldenValue * numberOfUnchanged;
		// The maximum possible weight
		double maximumWeight = 1D - (numberOfIssues) * goldenValue / totalSum;

		// re-weighing issues while making sure that the sum remains 1
		for (Integer i : lastDiffSet.keySet()) {
			if (lastDiffSet.get(i) == 0
					&& oppUtility.getWeight(i) < maximumWeight)
				oppUtility.setWeight(domain.getObjectivesRoot().getObjective(i),
						(oppUtility.getWeight(i) + goldenValue) / totalSum);
			else
				oppUtility.setWeight(domain.getObjectivesRoot().getObjective(i),
						oppUtility.getWeight(i) / totalSum);
		}

		// Then for each issue value that has been offered last time, a constant
		// value is added to its corresponding ValueDiscrete.
		try {
			for (Entry<Objective, Evaluator> e : oppUtility.getEvaluators()) {

				((EvaluatorDiscrete) e.getValue()).setEvaluation(
						opponentLastBid.getValue(
								((IssueDiscrete) e.getKey()).getNumber()),
						(LEARNING_VALUE_ADDITION + ((EvaluatorDiscrete) e
								.getValue()).getEvaluationNotNormalized(
										((ValueDiscrete) opponentLastBid
												.getValue(((IssueDiscrete) e
														.getKey())
																.getNumber())))));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public double get_p() {
		
		double time = this.negoSess.getTime();
		double Fa;
		double p = 1D;
		double step_point = discountF;
		double tempMax = maxUtil;
		double tempMin = minUtil;
		double tempE = e;
		double ignoreDiscountThreshold = 0.9D;

		if (step_point >= ignoreDiscountThreshold) {
			Fa = Ka + (1 - Ka) * Math.pow(time / step_point, 1D / e);
			p = minUtil + (1 - Fa) * (maxUtil - minUtil);
		} else if (time <= step_point) {
			tempE = e / step_point;
			Fa = Ka + (1 - Ka) * Math.pow(time / step_point, 1D / tempE);
			tempMin += Math.abs(tempMax - tempMin) * step_point;
			p = tempMin + (1 - Fa) * (tempMax - tempMin);
		} else {
			// Ka = (maxUtil - (tempMax -
			// tempMin*step_point))/(maxUtil-minUtil);
			tempE = 30D;
			Fa = (Ka + (1 - Ka) * Math
					.pow((time - step_point) / (1 - step_point), 1D / tempE));
			tempMax = tempMin + Math.abs(tempMax - tempMin) * step_point;
			p = tempMin + (1 - Fa) * (tempMax - tempMin);
		}
		return p;
	}

	public Action chooseAction() {
		round++;
		Entry<Double, Bid> newBid = null;
		Action newAction = null;

		double p = get_p();

		try {
			if (firstRound) {
				firstRound = !firstRound;
				newBid = bidSelector.BidList.lastEntry();
				offerQueue.add(newBid);
			}

			// if the offers queue has yet bids to be offered, skip this.
			// otherwise select some new bids to be offered
			else if (offerQueue.isEmpty() || offerQueue == null) {
				// calculations of concession step according to time

				TreeMap<Double, Bid> newBids = new TreeMap<Double, Bid>();
				newBid = bidSelector.BidList
						.lowerEntry(bidHistory.getMyLastBid().getKey());
				newBids.put(newBid.getKey(), newBid.getValue());

				if (newBid.getKey() < p) {
					int indexer = bidHistory.getMyBidCount();
					indexer = (int) Math
							.floor(indexer * random100.nextDouble());
					newBids.remove(newBid.getKey());
					newBids.put(bidHistory.getMyBid(indexer).getKey(),
							bidHistory.getMyBid(indexer).getValue());
				}

				double firstUtil = newBid.getKey();

				Entry<Double, Bid> addBid = bidSelector.BidList
						.lowerEntry(firstUtil);
				double addUtil = addBid.getKey();
				int count = 0;

				while ((firstUtil - addUtil) < UTILITY_TOLORANCE
						&& addUtil >= p) {
					newBids.put(addUtil, addBid.getValue());
					addBid = bidSelector.BidList.lowerEntry(addUtil);
					addUtil = addBid.getKey();
					count = count + 1;
				}

				// adding selected bids to offering queue
				if (newBids.size() <= TOP_SELECTED_BIDS) {
					offerQueue.addAll(newBids.entrySet());
				} else {
					int addedSofar = 0;
					Entry<Double, Bid> bestBid = null;

					while (addedSofar <= TOP_SELECTED_BIDS) {
						bestBid = newBids.lastEntry();
						// selecting the one bid with the most utility for the
						// opponent.
						for (Entry<Double, Bid> e : newBids.entrySet()) {
							if (oppUtility.getUtility(e.getValue()) > oppUtility
									.getUtility(bestBid.getValue())) {
								bestBid = e;
							}
						}
						offerQueue.add(bestBid);
						newBids.remove(bestBid.getKey());
						addedSofar++;
					}
				}
				// if opponentbest entry is better for us then the offer que
				// then replace the top entry

				if (offerQueue.getFirst().getKey() < opponentbestentry
						.getKey()) {
					offerQueue.addFirst(opponentbestentry);
				}
			}

			// if no bids are selected there must be a problem
			if (offerQueue.isEmpty() || offerQueue == null) {
				Bid bestBid1 = domain.getRandomBid(random200);
				if (opponentLastBid != null
						&& this.utilSpace.getUtility(bestBid1) <= this.utilSpace
								.getUtility(opponentLastBid)) {
					newAction = new Accept(new AgentID("HHS"), opponentLastBid);
				} else if (bestBid1 == null) {
					newAction = new Accept(new AgentID("HHS"), opponentLastBid);
				} else {
					newAction = new Offer(new AgentID("HHS"), bestBid1);
					if (this.utilSpace.getUtility(bestBid1) < lowestYetUtility)
						lowestYetUtility = this.utilSpace.getUtility(bestBid1);
				}
			}

			// if opponent's suggested bid is better than the one we just
			// selected, then accept it
			if (opponentLastBid != null && (this.utilSpace
					.getUtility(opponentLastBid) > lowestYetUtility
					|| this.utilSpace.getUtility(
							offerQueue.getFirst().getValue()) <= this.utilSpace
									.getUtility(opponentLastBid))) {
				newAction = new Accept(new AgentID("HHS"), opponentLastBid);
			}
			// else offer a new bid
			else {
				Entry<Double, Bid> offer = offerQueue.remove();
				bidHistory.addMyBid(offer);
				if (offer.getKey() < lowestYetUtility) {

					lowestYetUtility = this.utilSpace
							.getUtility(offer.getValue());
				}
				newAction = new Offer(new AgentID("HHS"), offer.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newAction;
	}
	
	public void updateUtilitySpace(UtilitySpace utilSpace) {
		
		this.utilSpace = (AdditiveUtilitySpace)utilSpace;
		
	}
	
}
