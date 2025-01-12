import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GetBalanceHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {
        try {
            // Ensure the request method is POST
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            // Step 1: Parse the request body to get the user's name
            InputStream inputStream = exchange.getRequestBody();
            String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject requestJson = new JSONObject(requestBody);

            String userName = requestJson.getString("name");

            // Step 2: Call the database function to get the user's balance
            double balance = DB_Template.getCoinBalanceByName(userName);

            // Step 3: Send the balance back as a JSON response
            JSONObject responseJson = new JSONObject();
            responseJson.put("balance", balance);

            String response = responseJson.toString();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Internal Server Error");
        }
    }  }