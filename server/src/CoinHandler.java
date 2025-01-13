import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class CoinHandler implements HttpHandler {

    private final Connection dbConnection;

    public CoinHandler(Connection dbConnection) {    
        this.dbConnection = dbConnection;
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePostTransaction(exchange);
        } else {
            sendResponse(exchange, 405, new JSONObject().put("error", "Method Not Allowed"));
        }
    }

    public void handlePostTransaction(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        
        JSONObject requestJson;
        try {
            requestJson = new JSONObject(body);
        } catch (Exception e) {
            sendResponse(exchange, 400, new JSONObject().put("error", "Invalid JSON format"));
            return;
        }
        String userId = requestJson.optString("userId", null);
        int amount = requestJson.optInt("amount", -1);
        String action = requestJson.optString("action", null);  // buy or sell
        
        if (userId == null || userId.isEmpty() || amount <= 0 || action == null || (!action.equals("buy") && !action.equals("sell"))) {
            sendResponse(exchange, 400, new JSONObject().put("error", "Invalid userId, amount, or action"));
            return;
        }
        
        try {
            DB_Template dbTemplate = new DB_Template(dbConnection);
            
            boolean success;
            if ("buy".equals(action)) {
                success = dbTemplate.change_wallet_coin_balance(userId, amount);  // buy
            } else { 
                success = dbTemplate.change_wallet_coin_balance(userId, -amount);  // sell if this negative
            }
            
            if (success) {
                sendResponse(exchange, 200, new JSONObject().put("message", action.equals("buy") ? "Coins purchased successfully" : "Coins sold successfully"));
            } else {
                sendResponse(exchange, 500, new JSONObject().put("error", "Failed to update wallet balance"));
            }
        } catch (SQLException e) {
            sendResponse(exchange, 500, new JSONObject().put("error", "Database error"));
        }
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, JSONObject responseJson) throws IOException {
        String response = responseJson.toString();
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
