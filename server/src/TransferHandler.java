import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TransferHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                InputStream inputStream = exchange.getRequestBody();
                String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Received transfer request with data: " + requestBody);
                JSONObject requestJson = new JSONObject(requestBody);
                String senderName = requestJson.getString("senderName");
                String receiverID = requestJson.getString("receiverID");
                Double amount = requestJson.getDouble("amount");
                String senderID = DB_Template.getUserIdByName(senderName);
                JSONObject responseJson = new JSONObject();

                if(DB_Template.change_wallet_coin_balance(senderID,amount)){
                    DB_Template.new_transaction(senderID, receiverID, amount);
                    System.out.println("Transfer successful!");
                    responseJson.put("message", "Transaction successful!");
                }else{
                    System.out.println("Insufficient funds!");
                    responseJson.put("message", "Insufficient funds or ReciverID dosent exsist!");
                }
                String response = responseJson.toString();
                exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(response.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                exchange.sendResponseHeaders(500, -1); // Internal Server Error
            } catch (Exception ignored) {
            }
        }
    }
}