/*
 * Tests For SimilarWebService 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

class Tests {
	
	private static final String c_wordsFileName = "words_clean.txt";
	private static final String c_userAgent = "Mozilla/5.0";
	private static final String c_similaritiesUrl = "http://localhost:8000/api/v1/similar?word=";
	private static int c_requestsToPerformCount = 300000;
	private static int sampleWordsMaxCount = 10000;
	private static int threadPoolSize = 100;
	private static ArrayList<String> sampleWords;
	
	public static void main(String[] args) {		
		
		if (args.length == 0) {
			sampleWords = GetSampleWords(Paths.get(c_wordsFileName).toAbsolutePath().toString());
			RunSequentialExecutionTest(c_requestsToPerformCount);
			RunParallelExecutionTest(c_requestsToPerformCount);
		}
		else if (args.length == 4){
			
			// Custom user configuration for test
			String sequentialOrParallel = args[0];
			int requestsCount = Integer.parseInt(args[1]);
			threadPoolSize = Integer.parseInt(args[2]);
			sampleWordsMaxCount = Integer.parseInt(args[3]);
			
			sampleWords = GetSampleWords(Paths.get(c_wordsFileName).toAbsolutePath().toString());
			if (sequentialOrParallel.equals("s")) {
				RunSequentialExecutionTest(requestsCount);				
			}
			else if (sequentialOrParallel.equals("p")){
				RunParallelExecutionTest(requestsCount);
			}
		}
		else {
			System.err.println("Wrong input. Please don't provide any input or provide: 's\\p' (sequential or parallel) 'requestCount' 'threadPoolSize'(<100) 'wordsPoolSize'");
		}
	}

	public static String GetRandomWord() {
		return sampleWords.get(new Random().nextInt(sampleWords.size()));
	}
	
	/*
	 * Executes sequential requests for similarities
	 */
	public static void RunSequentialExecutionTest(int requestsToPerformCount) {
		
		int errorsCount = 0;
		
		for (int i = 0; i < requestsToPerformCount; i++) {	
			
			try {
				String randomWord= GetRandomWord();
				String response = Tests.SendGET(c_similaritiesUrl + randomWord);
				System.out.println(" Requested word:'" + randomWord +  "'. Rsponse Content:'" + response  + "'");
				
				// Count errors
				if (response == "") {
					errorsCount++;
				}
				
			} catch (IOException e) {
				System.err.println("An error occured during sequential execution");
				e.printStackTrace();
				errorsCount++;
			}
		}
		
		System.out.println("Sequential test was finished. Errors count:'" + errorsCount + "'");
	}
	
	/*
	 * Sends Http Get request to URL
	 * Return response string. If response = "" it means that an error occurred during some request
	 */
	public static String SendGET(String url) throws IOException {
		
		String errorMessage = "GET request not worked";
		
		// Create request
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", c_userAgent);
		int responseCode = con.getResponseCode();
		System.out.print("Response Code:'" + responseCode + "'.");
		
		// Read response
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			
			in.close();
			
			// Return successful result
			return response.toString();
		} 
		else {
			System.err.println(errorMessage);
		}
		
		// Should not be reached
		System.err.println(errorMessage);
		return "";
	}

	/*
	 * Parallel test 
	 */
	public static void RunParallelExecutionTest(int requestsToPerformCount) {
		String errorMessage = "An error occured during parallel test execution";
		
		try {
			try {
				Tests.RunTest(requestsToPerformCount);
			} 
			catch (ExecutionException e) {
				System.err.println(errorMessage);
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			System.err.println(errorMessage);
			e.printStackTrace();
		}
	}
	
	/*
	 * Runs parallel test
	 */
	public static void RunTest(int requestsToPerformCount) throws InterruptedException, ExecutionException{
		
		AtomicInteger countOfErrors = new AtomicInteger(0);
		ConcurrentHashMap<String, String> requestToResponse = new ConcurrentHashMap<String, String>();
		
		ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
		
		List<Callable<ConcurrentHashMap<String, String>>> callableList = new ArrayList<Callable<ConcurrentHashMap<String, String>>>();
		
		for (int i = 0; i < requestsToPerformCount; i++) {
			callableList.add(GetRequestToPerform(requestToResponse, countOfErrors));	
		}
		
		// Returns only after all tasks are complete
		List<Future<ConcurrentHashMap<String, String>>> resultFuture = executorService.invokeAll(callableList);
		
		for (Future<ConcurrentHashMap<String, String>> future : resultFuture) {
			if (!future.isDone() || future.isCancelled()) {
				System.err.println("There was an unsuccessful request which is either was cancelled or wasn't finished for some reason");
			}
		}
			
		executorService.shutdown();
		
		if (countOfErrors.intValue() == 0) {
			System.out.println("Parallel requests execution test finished successfully. No errors were found");
		}
		else {
			System.err.println("Parallel requests execution test was finished. Count of errors:'" + countOfErrors + "'");
		}
	}
	
	/*
	 * This method represent actions that will be executed concurrently
	 * 'countOfErrors' counts of failed requests
	 * 'requestToResponse' dictionary of requests and their responses such that if the same requests happens
	 * more than one time, their responses will be matched
	 */
	public static Callable<ConcurrentHashMap<String, String>> GetRequestToPerform(
			final ConcurrentHashMap<String, String> requestToResponse,
			final AtomicInteger countOfErrors) {
		
		Callable<ConcurrentHashMap<String, String>> similarRequestCall = new Callable<ConcurrentHashMap<String, String>>() {
			public ConcurrentHashMap<String, String> call() {
				try {
					String randomWord = GetRandomWord();
					String response = Tests.SendGET(c_similaritiesUrl + randomWord);
					System.out.println(" Requested word:'" + randomWord +  "'. Response Content:'" + response  + "'");
					
					// Count errors
					if (response == "") {
						countOfErrors.addAndGet(1);
					}
					else {
						if (requestToResponse.containsKey(randomWord)) {
							
							// Check if previous exactly the same request had exactly the same answer.
							if (!requestToResponse.get(randomWord).equals(response)) {
								
								// Update errors counters for inconsistent responses
								countOfErrors.addAndGet(1);
								System.err.println(
										"Wrong response for:'" + randomWord + "'. Answer:'" + response + 
										"'. Previously received response:'" + requestToResponse.get(randomWord) + "'");
							}
						}
						else {
							// Aggregate requests and their responses for future comparisons with future requests
							requestToResponse.put(randomWord, response);
						}
					}
				} 
				catch (IOException e) {
					System.err.println("An error occured during parallel execution");
					e.printStackTrace();
					countOfErrors.addAndGet(1);
				}
				
				return requestToResponse;
			}
		};
		
		return similarRequestCall;
	}
	
	/*
	 * Gets sample words from sample file
	 */
    public static ArrayList<String> GetSampleWords(String pathToFile) {
    	ArrayList<String> sampleWords = new ArrayList<String>();
    	
    	try{
	    	// Open the file
	    	FileInputStream fstream = new FileInputStream(pathToFile);
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

	    	int wordsCount = 0;
	    	String strLine;
	    	
	    	// Read File Line By Line
	    	while ((strLine = br.readLine()) != null) {

	    		sampleWords.add(strLine);
    			wordsCount++;
    			
    			// Report the progress
    			if (wordsCount == sampleWordsMaxCount) {
    				break;
    			}
	    	}
	    	
	    	// Close the input stream
	    	br.close();
    	}
    	catch(IOException e){
    		System.err.println("Error occured while reading sample words from file");
    		e.printStackTrace();
    	}
    	
    	System.out.println("'" + sampleWords.size() + "' sample words were loaded to sample list");
    	
    	return sampleWords;
    }
}
