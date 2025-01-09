
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;


public class LoginHandler implements HttpHandler {
    
	private Connection dbConnection;

    public LoginHandler(Connection dbConnection) {
    	
        this.dbConnection = dbConnection;
    }
    
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		String path = exchange.getRequestURI().getPath();
		OutputStream os = exchange.getResponseBody();
		
		if (exchange.getRequestMethod().equals("POST")) {
			handlePostLogin(exchange, path); 
		}
	}
	
	private void handlePostLogin(HttpExchange exchange, String path) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes());
		String email = parseField(body, "email");
		String password = parseField(body, "password");
		
		boolean isAuthenticated = false;
		
		Users currentUser = null;
		
		try {
			
			isAuthenticated = DB_Template.userLogin(email, password);
			
//			if (isAuthenticated) {
//				currentUser = DB_Template.getUserByEmail(email);
//			}
		
		} catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error");
            return;
		}
		
		if (isAuthenticated && currentUser != null) {
            String response = "Login successful! Welcome, " + currentUser.getEmail();
            sendResponse(exchange, 200, response);
		} else {
            sendResponse(exchange, 401, "Invalid email or password");
		}
	}

	private String parseField(String body, String field) {
		int index = body.indexOf(field + "\":\"");
		if (index == -1) return null;
		int start = index + field.length() + 3;
		int end = body.indexOf("\"", start);
		return body.substring(start, end);
	}
	
	private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
		exchange.sendResponseHeaders(statusCode, response.getBytes().length);
		try (OutputStream os = exchange.getResponseBody()){
			os.write(response.getBytes());
		}
	}
}
