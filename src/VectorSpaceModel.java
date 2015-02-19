import java.util.*;
import java.io.*;

public class VectorSpaceModel {
	private final double b = 0.8;
	private final double c = -0.1;
	public String query;
	public ArrayList<ArrayList<String>> docs;
	public int docNum;
	public HashMap<String, Integer> dict = new HashMap<String, Integer>();
	public ArrayList<HashMap<String, Double>> relVecs = new ArrayList<HashMap<String, Double>>();
	public ArrayList<HashMap<String, Double>> unrVecs = new ArrayList<HashMap<String, Double>>();

	VectorSpaceModel(String query, ArrayList<ArrayList<String>> docs) {
		this.query = query;
		this.docs = docs;
		this.docNum = docs.get(0).size() + docs.get(1).size();
	}

	public String reviseQuery() {
		createDict();
		HashMap<String, Double> newQueryVec = getNewQueryVec();
        ValueComparator vc = new ValueComparator(newQueryVec);
        TreeMap<String, Double> sortedMap = new TreeMap<String, Double>(vc);
        sortedMap.putAll(newQueryVec);

        List<String> newQueryList = new ArrayList<String>();
        List<String> originQueryList = Arrays.asList(query.toLowerCase().split(" "));
        int count = 0;
        Iterator<String> newQueryIter = sortedMap.keySet().iterator();
        HashSet<String> stopWords = StopWordsHelper.getInstance().getStopWords();
        while(count < 2 && newQueryIter.hasNext()){
            String word = newQueryIter.next().toLowerCase();
            if(stopWords.contains(word))
                continue;
            if(!originQueryList.contains(word))
                count++;
            newQueryList.add(word);
        }
        for(String word : originQueryList){
            if(!newQueryList.contains(word))
                newQueryList.add(word);
        }
        StringBuilder newQuery = new StringBuilder();
        for(String word : newQueryList) {
            newQuery.append(word);
            newQuery.append(" ");
        }
        return newQuery.toString().trim();

	}

	private void createDict() {
		String[] queryArray = query.toLowerCase().split(" ");
		for (String word : queryArray) {
			if (!word.equals("")) {
				if (!dict.containsKey(word))
					dict.put(word, 0);
			}
		}

		for (ArrayList<String> docClass : docs) {
			for (String doc : docClass) {
				String[] docArray = doc.toLowerCase().split(" ");
				for (String word : docArray) {
					if (!word.equals("")) {
						if (!dict.containsKey(word))
							dict.put(word, 1);
						else
							dict.put(word, dict.get(word) + 1);
					}
				}
			}
		}
	}

	private HashMap<String, Double> calculateVec(String doc) {
		HashMap<String, Integer> tf = new HashMap<String, Integer>();
		String[] docArray = doc.toLowerCase().split(" ");
		for (String word : docArray) {
			if (!word.equals("")) {
				if (!tf.containsKey(word))
					tf.put(word, 1);
				else
					tf.put(word, tf.get(word) + 1);
			}
		}

		HashMap<String, Double> vector = new HashMap<String, Double>();
		for (Map.Entry<String, Integer> entry : tf.entrySet()) {
			double tf_idf = entry.getValue()
					* (1 + Math.log((double) docNum / dict.get(entry.getKey())));
			vector.put(entry.getKey(), tf_idf);
		}
		return vector;
	}
	
	private HashMap<String, Double> getNewQueryVec(){
		for (String doc : docs.get(0)) {
			unrVecs.add(calculateVec(doc));
		}
		for (String doc : docs.get(1)) {
			relVecs.add(calculateVec(doc));
		}

		HashMap<String, Double> newQueryVec = new HashMap<String, Double>();
		for (HashMap<String, Double> doc : relVecs) {
			for (Map.Entry<String, Double> entry : doc.entrySet()) {
				if (!newQueryVec.containsKey(entry.getKey()))
					newQueryVec.put(entry.getKey(), entry.getValue() * b
							/ docs.get(1).size());
				else
					newQueryVec.put(entry.getKey(),
							newQueryVec.get(entry.getKey()) + entry.getValue()
									* b / docs.get(1).size());
			}
		}
		for (HashMap<String, Double> doc : unrVecs) {
			for (Map.Entry<String, Double> entry : doc.entrySet()) {
				if (!newQueryVec.containsKey(entry.getKey()))
					newQueryVec.put(entry.getKey(), entry.getValue() * c
							/ docs.get(0).size());
				else
					newQueryVec.put(entry.getKey(),
							newQueryVec.get(entry.getKey()) + entry.getValue()
									* c / docs.get(0).size());
			}
		}
		for (String word : query.toLowerCase().split(" ")) {
			if (!word.equals("")) {
				if (!newQueryVec.containsKey(word))
					newQueryVec.put(word, 1.0);
				else
					newQueryVec.put(word, newQueryVec.get(word) + 1);
			}
		}
		
		return newQueryVec;
	}
	
}

class ValueComparator implements Comparator<String> {
	HashMap<String, Double> base;
	public ValueComparator(HashMap<String, Double> base){
        this.base = base;
    }

    public int compare(String a, String b){
        if(base.get(a) > base.get(b))
            return -1;
        else
            return 1;
    }
}
