/*
 * Main - starts Jetty Server which includes several servlets
 */

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {

	private static final String apiVersion = "v1";
	
	public static void main(String[] args) {
        Server server = new Server(8000);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);

        context.addServlet(new ServletHolder(new SimilarServlet()), "/api/" + apiVersion + "/similar");
        context.addServlet(new ServletHolder(new StatsServlet()), "/api/" + apiVersion + "/stats");

        try {
			server.start();
	        server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
