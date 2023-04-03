import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agents.anac.y2010.AgentFSEGA.Hypothesis;
import genius.core.Bid;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OpponentModel;

public class AdaptiveOM extends OpponentModel{
	
	Simulator simulator;
	private Map<Integer, Hypotesis> hypothesis;
	private Map<Hypotesis, Double> distribution;
	private double max_value = 15D;
	
	public AdaptiveOM(Simulator simulator) {
		
		this.simulator = simulator;
		hypothesis = new HashMap<>();
		distribution = new HashMap<>();
		
		
	}
	
	public List<List<Double>> getCombinations(ArrayList<Double> A, int I) {
        List<List<Double>> result = new ArrayList<>();
        if (I == 0) {
            result.add(new ArrayList<>());
            return result;
        }
        if (I > A.size()) {
            return result;
        }
        List<List<Integer>> combinationsWithoutFirst = getCombinations(new ArrayList<Double>(A.subList(1, A.size())), I);
        List<List<Integer>> combinationsWithFirst = getCombinations(new ArrayList<Double>(A.subList(1, A.size())), I - 1);
        for (List<Integer> combination : combinationsWithFirst) {
            combination.add(0, A.get(0));
        }
        result.addAll(combinationsWithoutFirst);
        result.addAll(combinationsWithFirst);
        return result;
    }
	
	private void computeHypothesis(int n){
		
		List<List<Integer>> combinations = numberCombinations(n+ 1);
		int counter = 0;
		ArrayList<Double> sample_list = new ArrayList<>();
		
		for(double k = 0; k <= max_value; k += max_value/20) {
			
			sample_list.add(k);
			
		}
		
		for (int i = 0; i < combinations.size(); i++) {
			
			for(List<Double> list: getCombinations(sample_list, n)) {
				
				Hypotesis temp = new Hypotesis(combinations.get(i));
				temp.setEvaluationFunctionThreshold((ArrayList<Double>) list);
				
			}
			
		}
		
	}
	
	
	
	private List<List<Integer>> numberCombinations(int n) {
        List<List<Integer>> result = new ArrayList<>();
        if (n == 1) {
            result.add(new ArrayList<Integer>());
        } else {
            List<List<Integer>> prev = numberCombinations(n-1);
            for (List<Integer> sublist : prev) {
                for (int i = 0; i < n; i++) {
                    if (!sublist.contains(i)) {
                        List<Integer> combination = new ArrayList<>(sublist);
                        combination.add(i);
                        if (combination.size() == n) {
                            result.add(combination);
                        }
                    }
                }
            }
        }
        return result;
    }
	
	public double evaluationFunction(double threshold, double x) {
		
		
		double slope_before_threshold = (threshold == 0) ? 1 : 1/threshold;
		double slope_after_threshold= (threshold == max_value) ? 0 : (-1)/(max_value - threshold);
		
		if (x < threshold) {
			
			return x*slope_before_threshold;
			
		}if (x == threshold) {
			
			return 1;
			
		}else {
			return x*slope_after_threshold;
		}
	}
	
	@Override
	public void init(NegotiationSession negotiationSession, Map<String, Double> parameters) {
		super.init(negotiationSession, parameters);
		computeHypothesis(negotiationSession.getDomain().getIssues().size());
		
	}


	@Override
	protected void updateModel(Bid bid, double time) {
		// TODO Auto-generated method stub
		
	}
	
	

}
