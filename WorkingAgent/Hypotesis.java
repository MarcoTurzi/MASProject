import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import genius.core.issue.Issue;
import genius.core.issue.Value;

public class Hypotesis {
	
	private ArrayList<Double> weights;
	private Map<Issue, Map<Value, Integer>> issues;
	
	public Hypotesis(List<Integer> ranking) {
		
		this.weights = new ArrayList<>();
		
		for (int i : ranking) {
			
			int n = ranking.size();
			this.weights.add( (2D*i/(n*(n+1))));
			
		}
		issues = new HashMap<Issue, Map<Value, Integer>>();
	}

	public void put(Issue issue, Map<Value, Integer> entry) {
		
		issues.put(issue, entry);
		
	}
	
	public void putValue(Issue issue, Value value, int intValue) {
		
		if (!issues.containsKey(issue))
			issues.put(issue, new HashMap<Value, Integer>());
		issues.get(issue).put(value, intValue);
		
	}
	
	public Integer getValue(Issue issue, Value val) {
		
		return issues.get(issue).get(val);
		
	}
	
	public ArrayList<Double> getWeights(){
		return this.weights;
	}
	
	
	
	
	
	
}
