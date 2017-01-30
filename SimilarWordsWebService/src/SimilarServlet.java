/*
 * SimilarServlet - Http Servlet
 */

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

public class SimilarServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String c_similarWordKeyName = "word";
	private ConcurrentHashMap<String, String> cache;
	private static final int c_maxCacheSize = 100000;
	private BusinessLogic businessLogic;
	
    public SimilarServlet(){
    	businessLogic = BusinessLogic.GetInstance();
		this.cache = new ConcurrentHashMap<String, String>();
    }
    
    /*
     * This method responds to Get Request for all similar words to some specific word - 'the original word\seed'.
     * The original word will not appear in the result.
     * For example: Get request to 'http://localhost:8000/api/v1/similar?word=apple' will return {"similar":["appel","pepla"]}
     *  provided that 'appel' and 'pepla' is present in words database
     * 
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
    	businessLogic.IncrementTotalRequestsCount();	
    	long startRequestHandleTime = System.nanoTime();
    	
    	boolean isKeyFound = false;
    	String keyValue = "";
    	
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            
            if(key.toLowerCase().equals(c_similarWordKeyName)){
            	isKeyFound = true;
            	keyValue = request.getParameter(key);
            	break;
            }
        }
        
    	if (isKeyFound && BusinessLogic.IsValidInput(keyValue)) {
    		ProcessGetRequest(keyValue, response);
    	}
        else {
        	HandleBadRequest(response);
        }
    	
    	long requestHandleTimePeriod = System.nanoTime() - startRequestHandleTime;
    	businessLogic.UpdateTotalRequestsHandleTimePeriod(requestHandleTimePeriod);    	
    	System.out.println("Request for similarities was handled in:'" + requestHandleTimePeriod + "' nano seconds");
    }
    
    /*
     * Handles good request for similarities.
     * A good request is request to 'http://localhost:8000/api/v1/similar' which contains parameter 'word' and some value for word.
     * For example: word=apple
     */
    private void ProcessGetRequest(String wordToProcess, HttpServletResponse response) {
    	String seed = wordToProcess.toLowerCase();
    	String similaritiesStr;
    	
		// check if those similarities were calculated previously
		if (this.cache.containsKey(seed)) {
			System.out.println("Cached item was found:'" + seed + "'");
			similaritiesStr = this.cache.get(seed);
		}
		else {
			similaritiesStr = new Gson().toJson(businessLogic.GetAllSimilarities(seed));
	        
			// Update cache
	        this.cache.put(seed, similaritiesStr);
	        
	        // Clear the cache when it is full
	        if (this.cache.size() > c_maxCacheSize) {
	        	this.cache.clear();
	        }
		}
        
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        
        try {
     	   	response.getWriter().println(similaritiesStr);
        } 
        catch (IOException e) {
    	    System.err.println("Error occured while trying to process correct request");
    	    e.printStackTrace();
    	    return;
        }
        
        System.out.println("'" + seed + "' similarities were found:'" + similaritiesStr + "'");
    }
    
    /*
     * Handles bad request
     */
    private void HandleBadRequest(HttpServletResponse response) {
    	
    	String errorMessage = "Bad input was detected. Please provide 'word' parameter with some value";
    	
    	response.setContentType("text/html");
    	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	
    	try {
    		response.getWriter().println("<h1>" + errorMessage + "</h1>");
		} 
    	catch (IOException e) {
    		System.err.println("Error occured while trying handle bad request");
	    	e.printStackTrace();
		}
    	
    	System.out.println("Request with bad input was handled");
    } 
}