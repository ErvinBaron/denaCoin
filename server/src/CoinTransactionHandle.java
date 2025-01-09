
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CoinTransactionHandle implements HttpHandler {

	private Connection dbConnection;

    public CoinTransactionHandle(Connection dbConnection) {
    	
        this.dbConnection = dbConnection;
    }
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        OutputStream os = exchange.getResponseBody();

        if (exchange.getRequestMethod().equals("POST")) {
            handlePostBuy(exchange, path);
        } else {
        	sendResponse(exchange, 404, "Method Not Allowed");
        }
		
	}

	public void handlePostBuy(HttpExchange exchange, String path) throws IOException {
	    String body = new String(exchange.getRequestBody().readAllBytes());

	    String id = parseField(body, "userId");
	    String amountString = parseField(body, "amount");

	    if (id == null || id.trim().isEmpty()) {
	        sendResponse(exchange, 400, "Invalid or missing userId");
	        return;
	    }

	    double amount;
	    try {
	        amount = Double.parseDouble(amountString);
	        if (amount == 0) {
	            sendResponse(exchange, 400, "Amount must be changed some how");
	            return;
	        }
	    } catch (NumberFormatException e) {
	        sendResponse(exchange, 400, "Invalid amount format");
	        return;
	    }

	    try {
	        boolean success = DBTemplate.change_wallet_coin_balance(id, amount);
	        if (success) {
	            sendResponse(exchange, 200, "Wallet updated.");
	        } else {
	            sendResponse(exchange, 400, "Transaction failed: Check balance, IDs, or amount");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error during transaction: " + e.getMessage());
	        sendResponse(exchange, 500, "Internal Server Error");
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
	        try (OutputStream os = exchange.getResponseBody()) {
	            os.write(response.getBytes());
	        }
	    }


}
