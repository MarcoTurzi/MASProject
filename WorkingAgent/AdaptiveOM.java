import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import genius.core.Bid;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OpponentModel;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Objective;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
import genius.core.utility.UtilitySpace;

public class AdaptiveOM extends OpponentModel{
	
	Simulator simulator;
	private Map<Integer, Hypotesis> hypothesis;
	private Map<Hypotesis, Double> distribution;
	
	public AdaptiveOM(Simulator simulator) {
		
		this.simulator = simulator;
		hypothesis = new HashMap<>();
		distribution = new HashMap<>();
		
		
	}
	
	private ArrayList<ArrayList<Integer>> valueCombinations(int n) {
        ArrayList<ArrayList<Integer>> combs = new ArrayList<>();
        if (n == 0) {
        	combs.add(new ArrayList<>());
            return combs;
        }
        for (int i = 1; i <= 5; i++) {
            ArrayList<ArrayList<Integer>> combinazioniParziali = valueCombinations(n - 1);
            for (ArrayList<Integer> partialCombo : combinazioniParziali) {
                if (!partialCombo.contains(i)) {
                    ArrayList<Integer> comb = new ArrayList<>(partialCombo);
                    comb.add(i);
                    combs.add(comb);
                }
            }
        }
        return combs;
    }
	
	private void computeHypothesis(int n){
		
		ArrayList<ArrayList<Integer>> combinations = generate(n);
		ArrayList<ArrayList<ArrayList<Integer>>> combos = new ArrayList<>();
		
		for(Issue i : negotiationSession.getDomain().getIssues()) {
			
			IssueDiscrete issue = (IssueDiscrete) i;
			combos.add(valueCombinations(issue.getValues().size()));
			
		}
		
		
		int counter = 0;
		//combination weights
		for (int i = 0; i < combinations.size(); i++) {
			//combination of issue values

			Hypotesis hp = new Hypotesis(combinations.get(i));
			
			for(int j=0; j < combos.size(); j++) {
				int p = 0;
				for(Issue is : negotiationSession.getDomain().getIssues()) {
					
					IssueDiscrete issue = (IssueDiscrete) is;
					int k = 0;
					for (Value val: issue.getValues()) {
						
						try {
							hp.putValue(issue, val, combos.get(p).get(j).get(k++));
						} catch (Exception e) {
							
							System.out.println(val.toString());
							
						}
											
					}
					p++;
				}
				
			}
		
		this.hypothesis.put(counter++, hp);
			
		}
		
	}
	
	private void initializeDistribution() {
		
		int n = this.hypothesis.size();
		
		for (Hypotesis hp : hypothesis.values()) {
			
			double b = 1D/n;
			this.distribution.put(hp, b);
			
		}
		
	}
	
	
	
	public ArrayList<ArrayList<Integer>> generate(int n) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		ArrayList<Integer> current = new ArrayList<>();
        generateHelper(n, current, result);
        return result;
    }

    private void generateHelper(int n, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> result) {
        if (current.size() == n) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = 1; i <= n; i++) {
            if (!current.contains(i)) {
                current.add(i);
                generateHelper(n, current, result);
                current.remove(current.size() - 1);
            }
        }
    }
	
	@Override
	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		super.init(negotiationSession, parameters);
		computeHypothesis(negotiationSession.getDomain().getIssues().size());
		initializeDistribution();
		
	}
	
	public double utilityTime(double time) {
		
		return 1- 0.05*time;
		
	}

	private double computeConditionalProb(Bid bid, Hypotesis hp, double time) {
		
		
		double hpUtil = 0;
		double o = 0.8;
		int j = 0;
		for (Issue i : bid.getIssues()) {
			
			IssueDiscrete issue = (IssueDiscrete)i;
			
			hpUtil += hp.getWeights().get(j++)*hp.getValue(issue, bid.getValue(i.getNumber()));
		}
		return (1D/(o*Math.sqrt(2*Math.PI))*Math.pow(Math.E, -Math.pow(hpUtil - utilityTime(time),2D)/2*o*o));
	}

	@Override
	protected void updateModel(Bid bid, double time) {
		
		double deno = 0;
		
		for (Hypotesis hp : distribution.keySet()) {
			
			deno += (1D/distribution.size())*computeConditionalProb(bid, hp, time);
			
		}
		
		for (Hypotesis hp : distribution.keySet()) {
			
			distribution.put(hp, (1D/distribution.size())*computeConditionalProb(bid, hp, time)/deno);
			
		}
	
		Set<Hypotesis> keys = distribution.keySet();
		Iterator<Hypotesis> iter = keys.iterator();
		Hypotesis bestHP = null;
		float bestHPVal = 0;
		
		for(Entry<Hypotesis, Double> entry: distribution.entrySet()) {
			
			Hypotesis hp = entry.getKey();
			double hpVal = entry.getValue();
			if (hpVal >= bestHPVal){
				bestHP = hp;
			}
			
		}
		
		
		for(Entry<Objective, Evaluator> e : opponentUtilitySpace.getEvaluators()) {
			
			EvaluatorDiscrete value = (EvaluatorDiscrete) e.getValue();
			IssueDiscrete issue = ((IssueDiscrete) e.getKey());
			
			opponentUtilitySpace.setWeight(e.getKey(), bestHP.getWeights().get(issue.getNumber() - 1));
			
			
			for (ValueDiscrete vd : issue.getValues()) {
				
				value.setEvaluation(vd, bestHP.getValue(issue, vd));
				
			}
			
			
		}
		
	}
	
	public UtilitySpace getUtilitySpace() {
		return this.opponentUtilitySpace;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AdaptiveOM";
	}
	

}
