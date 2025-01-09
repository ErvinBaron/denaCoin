
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;






public class CoinHandler implements HttpHandler {
	
	private Connection dbConnection;

    public CoinHandler(Connection dbConnection) {
    	
        this.dbConnection = dbConnection;
    }

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePostTransaction(exchange);
		} else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
			try {
				showCoin(exchange);
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
		} else {
            sendResponse(exchange, 405, "Method Not Allowed");
		}
	}
	
	public void handlePostTransaction(HttpExchange exchange) throws IOException{
		String body = new String(exchange.getRequestBody().readAllBytes());
		String senderId = parseField(body, "senderId");
		String recevierId = parseField(body,"recevierID");
		String amountString = parseField(body, "amount");
		
		if (senderId  == null ||recevierId == null ||amountString == null ) {
			sendResponse(exchange,400 ,"Missing required fields");
			return;
		}
		
		double amount;
		try {
			amount = Double.parseDouble(amountString);
		} catch (NumberFormatException e) {
			sendResponse(exchange, 400, "Invalid amount format");
			return;
		}
		
		try {
			boolean success = DBTemplate.new_transaction(senderId,recevierId,amount);
			if (success) {
                sendResponse(exchange, 200, "Transaction successful");
			} else {
                sendResponse(exchange, 400, "Transaction failed: Check balance, IDs, or amount");

			}
		} catch (SQLException e) {
		      System.err.println("Error during transaction: " + e.getMessage());
	            sendResponse(exchange, 500, "Internal Server Error");		}
		}
	
	
	public void showCoin(HttpExchange exchange) throws IOException, SQLException {
		String body = new String(exchange.getRequestBody().readAllBytes());
		String userId = parseField(body,"userId");
		String amountString = parseField(body,"amount");
		if (userId == null) {
			sendResponse(exchange,400 ,"Missing field");
			return;
		}
		
		double amount;
		amount = Double.parseDouble(amountString);
		boolean success = DBTemplate.checkAmount(userId,amount);
		if (success) {
               sendResponse(exchange, 200, "Your Amount Is: " + amount);
		} else {
               sendResponse(exchange, 400, "Detail failed: IDs, or balance.");
		}
	}
	

    private String parseField(String body, String field) {
        int index = body.indexOf(field + "\":\"");
        if (index == -1) return null;
        int start = index + field.length() + 3;
        int end = body.indexOf("\"", start);
        return end == -1 ? null : body.substring(start, end);
    }
    
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
