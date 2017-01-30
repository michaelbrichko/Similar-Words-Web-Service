/*
 * StatsResult holds the response to Get request for statistics
 */

public class StatsResult {
	
	private long totalWords;
	private long totalRequests;
	private long avgProcessingTimeNs;
	
	public StatsResult(long totalWords, long totalRequests, long avgProcessingTimeNs){
		
		this.totalWords = totalWords;
		this.totalRequests = totalRequests;
		this.avgProcessingTimeNs = avgProcessingTimeNs;
	}

	public long getTotalWords() {
		return this.totalWords;
	}

	public long getTotalRequests() {
		return this.totalRequests;
	}

	public long getAvgProcessingTimeNs() {
		return this.avgProcessingTimeNs;
	}
}