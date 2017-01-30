/*
 * WordsDataBase holds all the words in data base
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class WordsDataBase {
    
	// Mapping of Word Key to all permutations that match that key
	private HashMap<String, ArrayList<String>> words;
	private long wordsCount;
	private static final String c_wordsFileName = "words_clean.txt";
	
	public WordsDataBase(){
		System.out.println("Initializing words data base");
		
    	this.words = new HashMap<String, ArrayList<String>>();
    	InitializeDB();
    	
    	System.out.println("Words data base was initialized. Count of words: '" + this.GetWordsCount() + "'");
	}
	
	public long GetWordsCount(){
		return wordsCount;
	}
	
	public ArrayList<String> GetPermutationsByKey(String key) {
		
		if (this.words.containsKey(key)) {
			return this.words.get(key);
		}
		else {
			return new ArrayList<String>();
		}
	}
	
    private void InitializeDB() {
    	try{
	    	// Open the file
	    	FileInputStream fstream = new FileInputStream(Paths.get(c_wordsFileName).toAbsolutePath().toString());
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	    	long wordsReadLastStep = 100000; // for reporting purposes
	    	String strLine;
	
	    	// Read File Line By Line
	    	while ((strLine = br.readLine()) != null) {	    		
	    		if (WordsUtilities.IsValidWord(strLine)) {
	    			
	    			String wordKey = WordsUtilities.GetWordRepresentationKey(strLine);
	    			
	    			if (this.words.containsKey(wordKey)) {
	    				this.words.get(wordKey).add(strLine);
	    			}
	    			else {	    				
	    				ArrayList<String> wordsPermutationsGroup = new ArrayList<String>();
	    				wordsPermutationsGroup.add(strLine);
	    				this.words.put(wordKey, wordsPermutationsGroup);
	    			}

	    			wordsCount++;
	    			
	    			// Report the progress
	    			if (wordsCount == wordsReadLastStep) {
	    				System.out.println(wordsCount + " words were loaded.");
	    				wordsReadLastStep += 100000;
	    			}
	    		}
	    		else {
	    			System.out.println("Invalid word was encountered and won't be added to database: '" + strLine + "'");
	    		}
	    	}
	    	
	    	// Close the input stream
	    	br.close();
    	}
    	catch(IOException e){
    		System.err.println("Error occured while loading reading words from file");
    		e.printStackTrace();
    	}
    }
}