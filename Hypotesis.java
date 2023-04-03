import java.util.ArrayList;
import java.util.List;

public class Hypotesis {
	
	private ArrayList<Double> weights;
	private ArrayList<Double> evaluationFunctionThreshold;
	
	public Hypotesis(List<Integer> ranking) {
		
		for (int i : ranking) {
			
			int n = ranking.size();
			weights.add((double) (2*i/(n*(n+1))));
			
		}
		this.evaluationFunctionThreshold = new ArrayList<>();
	}

	public ArrayList<Double> getWeights(){
		return this.weights;
	}
	
	public ArrayList<Double> getEvaluationFunctionThreshold() {
		return evaluationFunctionThreshold;
	}

	public void setEvaluationFunctionThreshold(ArrayList<Double> evaluationFunctionThreshold) {
		this.evaluationFunctionThreshold = evaluationFunctionThreshold;
	}
	
	
	
	
}
