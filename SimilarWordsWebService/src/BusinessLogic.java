/*
 * BusinessLogic responsible for processing the input and accessing words data base
 * BusinessLogic is singelton.
 */

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class BusinessLogic {
	
	private static BusinessLogic businessLogic = null;
	private WordsDataBase db;
	private AtomicLong totalRequestsCount;
	private AtomicLong totalRequestsProcessingTimePeriod;
	
	public static BusinessLogic GetInstance() {
		if (businessLogic == null) {
			businessLogic = new BusinessLogic();
		}
		
		return businessLogic;
	}
	
	private BusinessLogic() {
		this.db = new WordsDataBase();
		this.totalRequestsCount = new AtomicLong(0);
		this.totalRequestsProcessingTimePeriod = new AtomicLong(0);
	}
	
	public long GetWordsCount() {
		return this.db.GetWordsCount();
	}
	
	public long GetAverageRequestHandlingTimeNS() {
		
		long avgHandlingTime = 0;
		
		if (this.GetTotalRequestsCount() > 0) {
			avgHandlingTime = this.GetTotalRequestsHandleTimePeriod() / this.GetTotalRequestsCount();
		}
		
		return avgHandlingTime;
	}
	
	public void IncrementTotalRequestsCount(){
		this.totalRequestsCount.incrementAndGet();
	}
	
	public long GetTotalRequestsCount(){
		return this.totalRequestsCount.longValue();
	}
	
	public void UpdateTotalRequestsHandleTimePeriod(long requestHandleTimePeriod) {
		this.totalRequestsProcessingTimePeriod.addAndGet(requestHandleTimePeriod);
	}
	
	public long GetTotalRequestsHandleTimePeriod() {
		return this.totalRequestsProcessingTimePeriod.longValue();
	}
	
	public StatsResult GetStatistics() {	
		return new StatsResult(this.GetWordsCount(), this.GetTotalRequestsCount(), this.GetAverageRequestHandlingTimeNS());
	}
	
	/*
	 * Returns all similarities 'originalWord' that is present in data base.
	 * The 'originalWord' will not be listed in the result.
	 *  @pre this.IsValidInput(seed) == true
	 */
	public SimilarResult GetAllSimilarities(String seed) {
		
		ArrayList<String> similarWords = new ArrayList<String>();
        for (String w : db.GetPermutationsByKey(WordsUtilities.GetWordRepresentationKey(seed))) {
        	if (!w.toLowerCase().equals(seed)) {
    	 		   similarWords.add(w);
    	    }
        }
        
        return new SimilarResult(similarWords);
    }
	
	/*
	 * Validates whether the 'originalWord' represent a valid input to all business logic processing
	 */
	public static boolean IsValidInput(String originalWord) {
		return  originalWord != null 
				&& originalWord != "" 
				&& originalWord.length() > 0 
				&& WordsUtilities.IsValidWord(originalWord);
	}
}
