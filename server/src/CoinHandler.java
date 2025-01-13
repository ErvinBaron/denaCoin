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
        try {
            String path = exchange.getRequestURI().getPath();

            // Handle GET request for user balance
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod()) && path.contains("/user-balance/")) {
                handleGetBalance(exchange);
            }
            // Handle POST request for transactions
            else if ("POST".equalsIgnoreCase(exchange.getRequestMethod()) && path.endsWith("/transactions")) {
                handlePostTransaction(exchange);
            } else {
                sendResponse(exchange, 405, new JSONObject().put("error", "Method Not Allowed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, new JSONObject().put("error", "Internal server error: " + e.getMessage()));
        }
    }

    private void handleGetBalance(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String userId = path.substring(path.lastIndexOf("/") + 1);

        if (userId.isEmpty()) {
            sendResponse(exchange, 400, new JSONObject().put("error", "Invalid userId"));
            return;
        }

        try {
            DB_Template dbTemplate = new DB_Template(dbConnection);
            double balance = dbTemplate.getCoinBalance(userId);

            if (balance == -1) {
                sendResponse(exchange, 404, new JSONObject().put("error", "User not found"));
                return;
            }

            sendResponse(exchange, 200, new JSONObject()
                    .put("coin_balance", balance)
                    .put("user_name", dbTemplate.getUserFirstName(userId)));
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, new JSONObject().put("error", "Unexpected error: " + e.getMessage()));
        }
    }

    private void handlePostTransaction(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        System.out.println("Received POST body: " + body);

        JSONObject requestJson;
        try {
            requestJson = new JSONObject(body);
        } catch (Exception e) {
            sendResponse(exchange, 400, new JSONObject().put("error", "Invalid JSON format"));
            return;
        }

        String userId = requestJson.optString("userId", null);
        int amount = requestJson.optInt("amount", -1);
        String action = requestJson.optString("action", null);

        if (userId == null || userId.isEmpty() || amount <= 0 || action == null || (!action.equals("buy") && !action.equals("sell"))) {
            sendResponse(exchange, 400, new JSONObject().put("error", "Invalid userId, amount, or action"));
            return;
        }

        try {
            DB_Template dbTemplate = new DB_Template(dbConnection);
            // קריאה למתודה `buyAndSell` עם שלושה ארגומנטים (userId, amount, isBuy)
            boolean isBuy = action.equals("buy");
            dbTemplate.buyAndSell(userId, amount, isBuy);

            double newBalance = dbTemplate.getCoinBalance(userId);
            JSONObject response = new JSONObject()
                    .put("message", isBuy ? "Coins purchased successfully" : "Coins sold successfully")
                    .put("amount", amount)
                    .put("new_balance", newBalance);

            sendResponse(exchange, 200, response);
        } catch (SQLException e) {
            e.printStackTrace();
            sendResponse(exchange, 500, new JSONObject().put("error", "Database error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, new JSONObject().put("error", "Unexpected error: " + e.getMessage()));
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, JSONObject responseJson) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://127.0.0.1:5500");

        String response = responseJson.toString();
        System.out.println("Sending response: " + response);

        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

}
