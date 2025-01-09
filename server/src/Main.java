import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import java.util.UUID;
public class Main {

	private static Connection dbConnection;
	private static final int PORT = 8000;
	private static Blockchain blockchain;

	public Connection getConnection(){
		return dbConnection;
	}
	public static void main(String[] args) {
		try {
			// Initialize the database and blockchain
			initializeDatabase();
			blockchain = new Blockchain(2); // Set mining difficulty



			// Start the HTTP server
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

			// Register handlers
			server.createContext("/pages/home", new CorsHandle(new HomeHandler(dbConnection)));
			server.createContext("/login", new CorsHandle(new LoginHandler()));
			server.createContext("/pages/coin", new CorsHandle(new CoinHandler(dbConnection)));
			server.createContext("/addTransaction", new CorsHandle(new TransactionHandler()));
			server.createContext("/getBlockchain", new CorsHandle(new BlockchainHandler()));
			server.createContext("/register", new CorsHandle(new RegisterHandler()));

			server.setExecutor(null); // Default executor
			server.start();

			System.out.println("Server is running on port " + PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void initializeDatabase() throws SQLException {
		String url = "jdbc:sqlite:C:\\Users\\Ervin\\Desktop\\code\\denaCoin\\server\\src\\users.db1";
		dbConnection = DriverManager.getConnection(url);
		System.out.println("Connected to the database successfully.");
	}

	// Simplified RegisterHandler without encryption
	private static class RegisterHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Read plain JSON request body
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

					// Parse the JSON
					JSONObject registrationData = new JSONObject(requestBody);

					String fname = registrationData.getString("fname");
					String lname = registrationData.getString("lname");
					String email = registrationData.getString("email");
					String password = registrationData.getString("password");

					System.out.println("Received registration for: " + fname + " " + lname + " (" + email + ")");
					String userId = java.util.UUID.randomUUID().toString();
					boolean registrationSuccess = DB_Template.userRegister(userId, email, password, fname, lname);
					// Mock registration logic

					// Respond to the client
					String response = "Registration successful!";
					exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
					OutputStream outputStream = exchange.getResponseBody();
					outputStream.write(response.getBytes(StandardCharsets.UTF_8));
					outputStream.close();
				} else {
					exchange.sendResponseHeaders(405, -1); // Method Not Allowed
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					exchange.sendResponseHeaders(500, -1); // Internal Server Error
				} catch (Exception ignored) {}
			}
		}
	}
	private static class LoginHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Read JSON request body
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

					// Parse JSON for email and password
					JSONObject loginData = new JSONObject(requestBody);
					String email = loginData.getString("email");
					String password = loginData.getString("password");

					// Call the userLogin method to validate credentials
					boolean loginSuccess = DB_Template.userLogin(email, password);

					// Prepare response
					JSONObject responseJson = new JSONObject();
					if (loginSuccess) {
						responseJson.put("message", "Login successful!");
						exchange.sendResponseHeaders(200, responseJson.toString().getBytes(StandardCharsets.UTF_8).length);
					} else {
						responseJson.put("message", "Invalid email or password.");
						exchange.sendResponseHeaders(401, responseJson.toString().getBytes(StandardCharsets.UTF_8).length); // Unauthorized
					}

					// Send response to client
					OutputStream outputStream = exchange.getResponseBody();
					outputStream.write(responseJson.toString().getBytes(StandardCharsets.UTF_8));
					outputStream.close();
				} else {
					exchange.sendResponseHeaders(405, -1); // Method Not Allowed
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					exchange.sendResponseHeaders(500, -1); // Internal Server Error
				} catch (Exception ignored) {}
			}
		}
	}


	// Simplified TransactionHandler without encryption
	private static class TransactionHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Read plain JSON request body
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

					// Parse the JSON
					JSONObject transactionData = new JSONObject(requestBody);
					String email = transactionData.getString("email");
					String password = transactionData.getString("password");
					String fname = transactionData.getString("fname");
					String lname = transactionData.getString("lname");

					// Generate a unique user ID
					String userId = java.util.UUID.randomUUID().toString();

					// Call the userRegister method from DB_Template
					boolean registrationSuccess = DB_Template.userRegister(userId, email, password, fname, lname);

					// Respond to the client based on registration success
					String response;
					if (registrationSuccess) {
						response = "Registration successful!";
						exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
					} else {
						response = "Registration failed!";
						exchange.sendResponseHeaders(400, response.getBytes(StandardCharsets.UTF_8).length);
					}

					// Send the response
					OutputStream outputStream = exchange.getResponseBody();
					outputStream.write(response.getBytes(StandardCharsets.UTF_8));
					outputStream.close();
				} else {
					exchange.sendResponseHeaders(405, -1); // Method Not Allowed
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					exchange.sendResponseHeaders(500, -1); // Internal Server Error
				} catch (Exception ignored) {}
			}
		}
	}


	// Handler to retrieve the blockchain
	private static class BlockchainHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
					List<Block> chain = blockchain.getChain();
					List<JSONObject> chainJson = new ArrayList<>();

					for (Block block : chain) {
						JSONObject blockJson = new JSONObject();
						blockJson.put("index", block.getIndex());
						blockJson.put("timestamp", block.getTimestamp());
						blockJson.put("transactions", block.getTransactions());
						blockJson.put("previousHash", block.getPreviousHash());
						blockJson.put("hash", block.getHash());
						chainJson.add(blockJson);
					}

					JSONObject responseJson = new JSONObject();
					responseJson.put("blockchain", chainJson);

					String response = "Registration successful!";
					exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
					OutputStream outputStream = exchange.getResponseBody();
					outputStream.write(response.getBytes(StandardCharsets.UTF_8));
					outputStream.close();
				} else {
					exchange.sendResponseHeaders(405, -1); // Method Not Allowed
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					exchange.sendResponseHeaders(500, -1); // Internal Server Error
				} catch (Exception ignored) {}
			}
		}
	}

	public static void shutdown() {
		if (dbConnection != null) {
			try {
				dbConnection.close();
				System.out.println("Database connection closed.");
			} catch (SQLException e) {
				System.err.println("Error while closing the database connection: " + e.getMessage());
			}
		}
	}
}
