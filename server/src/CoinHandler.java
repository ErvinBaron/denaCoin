
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

public class CoinHandler implements HttpHandler {
	
	private Connection dbConnection;

    public CoinHandler(Connection dbConnection) {
    	
        this.dbConnection = dbConnection;
    }

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		String path = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getQuery();
		OutputStream os = exchange.getResponseBody();
		
		if (exchange.getRequestMethod().equals("POST")) {
			String reciverId;
			String senderId;
			
			//new_transaction(String );
		} else if (exchange.getRequestMethod().equals("GET")){
			
		}
		
	}
	
    public void showCoin(HttpExchange exchange) throws IOException {
        // שליפה ממסד הנתונים של המידע על המטבעות
        // לדוגמה: לשלוף את כל המטבעות של המשתמש
        String response = "Here are your coins";  // צריך להחליף בנתונים מהמסד
        sendResponse(exchange, 200, response);  // שליחה למשתמש
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
