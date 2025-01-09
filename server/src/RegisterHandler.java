
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterHandler implements HttpHandler {

    private Connection dbConnection;

    public RegisterHandler(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        OutputStream os = exchange.getResponseBody();

        if (exchange.getRequestMethod().equals("POST")) {
            handlePostRegister(exchange, path);
        }
    }

    public void handlePostRegister(HttpExchange exchange, String path) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes());

        String id = parseField(body, "id");
        id = generateUUID(); // ליצור ID ייחודי

        String firstName = parseField(body, "name");
        String lastName = parseField(body, "lastName");

        String email = parseField(body, "email");
        String password = parseField(body, "password");

        // בדיקת תקינות הסיסמה
        if (!validatePassword(password)) {
            sendResponse(exchange, 400, "Invalid Password: One Uppercase, One Lowercase, One Digit, And Minimum 8 Characters.");
        } else if (!isValidEmail(email)) {
            sendResponse(exchange, 400, "Invalid Email format.");
        } else {
            // כאן תוכל להוסיף קוד לשמירת המשתמש במסד הנתונים
            sendResponse(exchange, 201, "Registration Successful");
        }
    }

    private String parseField(String body, String field) {
        int index = body.indexOf(field + "\":\"");
        if (index == -1) return null;
        int start = index + field.length() + 3;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            System.out.println("Email cannot be empty.");
            return false;
        }

        if (!Pattern.matches(EMAIL_REGEX, email)) {
            System.out.println("Invalid Email format.");
            return false;
        }

        return true;
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString(); // ייצור ID ייחודי
    }

    public boolean validatePassword(String password) {
        // בדיקה שהסיסמה מכילה לפחות 8 תווים
        if (password.length() < 8) {
            return false;
        }

        // ביטוי רגולרי לבדיקת:
        // - לפחות אות רישית
        // - לפחות אות קטנה
        // - לפחות ספרה
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$";
        return password.matches(passwordRegex); // בדיקת התאמה לביטוי
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
