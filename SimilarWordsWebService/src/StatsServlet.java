/*
 * StatsServlet - Http Servlet
 */

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

public class StatsServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
    private BusinessLogic businessLogic;
	
    public StatsServlet(){
    	businessLogic = BusinessLogic.GetInstance();
    }
    
    /*
     * This method response to Get Request by returning next statistics:
     *  1. Total number of words in the data base
     *  2. Total number of requests (not including "stats" requests).
     *  3. Average time for request handling in nano seconds (not including "stats" requests).
     * 
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    {
    	ProcessGetRequest(response);
    }

    /*
     * Handles request for statistics.
     * Sample request: 'http://localhost:8000/api/v1/stats'.
     * Sample Response: '{"totalWords":351075,"totalRequests":9,"avgProcessingTimeNs":45239}'
     */
    private void ProcessGetRequest(HttpServletResponse response) {

        String statsStr = new Gson().toJson(businessLogic.GetStatistics());
    	
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
     	   	response.getWriter().println(statsStr);
        } 
        catch (IOException e) {
    	    System.out.println("Error occured while retrieving statistics");
    	    e.printStackTrace();
    	    return;
        }
        
        System.out.println("Statistics request was processed:'" + statsStr + "'");
    }
}