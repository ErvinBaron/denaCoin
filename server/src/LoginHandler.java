import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LoginHandler implements HttpHandler {

    private Connection dbConnection;

    public LoginHandler(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePostLogin(exchange, path);
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handlePostLogin(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());
        String id = parseField(body, "id");
        String email = parseField(body, "email");
        String password = parseField(body, "password");

        if (id == null || email == null || password == null) {
            sendResponse(exchange, 400, "Missing required fields");
            return;
        }

        boolean isAuthenticated = false;
        String[] userInfo = null;

        try {
            // בדיקת אימות
            isAuthenticated = DBTemplate.userLogin(email, password);

            if (isAuthenticated) {
                // קבלת מידע על המשתמש
                userInfo = DBTemplate.getUserInfo(id);
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            sendResponse(exchange, 500, "Internal server error");
            return;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            sendResponse(exchange, 500, "Internal server error");
            return;
        }

        if (isAuthenticated && userInfo != null) {
            // הכנת תגובה למשתמש
            String response = String.format("Login successful! Welcome, %s %s (%s)", 
                userInfo[1], userInfo[2], userInfo[3]);
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
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
