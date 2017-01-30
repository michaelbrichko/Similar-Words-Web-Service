/*
 * Utilities that can be applied on some word 
 */

public class WordsUtilities {
    
	/*
	 * Validates that the word consist of a - z characters only
	 */
	public static boolean IsValidWord(String word) {
    	
		boolean isValid = true;    	
    	char[] chars = word.toLowerCase().toCharArray();
		
    	for (int i = 0; i < chars.length; i++) {
    		if (chars[i] < 'a'  || chars[i] > 'z'){
    			isValid = false;
    			break;
    		}
    	}
    	
    	return isValid;
    }

	/*
	 *  Return word representation. All word permutations has exactly the same word representation.
	 *  Each word can be viewed as an array of size 50 where there is 26 counters that count occurrences of every char in word.
	 *  The word representation is the string of those counters.
	 *  For example: abc = "1-1-1-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0"
	 *  			 xyz = "0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-1-1-1"
	 */
	public static String GetWordRepresentationKey(String word) {

		int[] wordRepresentation = new int['z' - 'a' + 1];
		
		for (char c : word.toLowerCase().toCharArray()) {
			wordRepresentation[c - 'a']++;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < wordRepresentation.length; i++) {
			sb.append(wordRepresentation[i]);
			sb.append('-'); // delimiter between counters
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}
