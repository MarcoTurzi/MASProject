import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {

	public static void main(String[] args) {

		ArrayList<Integer> rank = new ArrayList<>();
		rank.add(1);
		rank.add(2);
		Hypotesis hp = new Hypotesis(rank);
		Map<Hypotesis, Integer> map = new HashMap<>();
		map.put(hp, 2);
		map.put(hp,3);
		System.out.println();
		
	}

	public static ArrayList<ArrayList<Integer>> generate(int n) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
		ArrayList<Integer> current = new ArrayList<>();
        generateHelper(n, current, result);
        return result;
    }

    private static void generateHelper(int n, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> result) {
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
	private static ArrayList<ArrayList<Integer>> valueCombinations(int n) {
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
	
}
