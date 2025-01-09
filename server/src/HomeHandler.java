import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Connection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class HomeHandler implements HttpHandler {
	
	private Connection dbConnection;

	public HomeHandler (Connection dbConnection) {
    	
        this.dbConnection = dbConnection;
    }
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		String path = exchange.getRequestURI().getPath();
		OutputStream os = exchange.getResponseBody();
		
		if(exchange.getRequestMethod().equals("GET")) handleGetFirstName(exchange, path);
	}
		
		private void handleGetFirstName(HttpExchange exchange, String path) throws IOException {
			try {
				StringBuilder sb = new StringBuilder(Users.getFirstName());
				if (sb.equals(null)) {
			        sendResponse(exchange, 400, "No user found."); 
				} else {					
					sendResponse(exchange, 200, sb.toString()); 
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
			exchange.sendResponseHeaders(statusCode, response.getBytes().length);
			try (OutputStream os = exchange.getResponseBody()){
				os.write(response.getBytes());
			}
		}
}
