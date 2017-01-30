/*
 * SimilarResult holds the response to Get request for similarities and contains all similar words to some requested seed word
 */

import java.util.ArrayList;

public class SimilarResult{
	
	private ArrayList<String> similar;
	
	public SimilarResult(ArrayList<String> similaritiesList){
		this.similar = similaritiesList;
	}

	public ArrayList<String> getSimilar() {
		return this.similar;
	}
}
