 

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.json.JSONArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
			server.createContext("/login", new CorsHandle(new LoginHandler()));
			server.createContext("/user-balance", new CorsHandle(new CoinHandler(dbConnection)));
			server.createContext("/transactions", new CorsHandle(new CoinHandler(dbConnection)));
//			server.createContext("/addTransaction", new CorsHandle(new TransactionHandler()));
			server.createContext("/getBlockchain", new CorsHandle(new BlockchainHandler()));
			server.createContext("/register", new CorsHandle(new RegisterHandler()));
			server.createContext("/get-key", new CorsHandle(new KeyHandler()));
			server.createContext("/getBalance", new CorsHandle(new GetBalanceHandler()));
			server.createContext("/transfer", new CorsHandle(new TransferHandler()));
			server.createContext("/getHistory", new CorsHandle(new HistoryHandler()));
			server.setExecutor(null); // Default executor
			server.start();

			System.out.println("Server is running on port " + PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	// initalize the DB
	private static void initializeDatabase() throws SQLException, NoSuchAlgorithmException {
		String url = "jdbc:sqlite:C:\\Users\\david\\denaCoin\\server\\src\\users.db1";
		dbConnection = DriverManager.getConnection(url);
		System.out.println("Connected to the database successfully.");
		DB_Template.createTables();
	}
	//get user history from db by id
	private static class HistoryHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Read and parse JSON request body
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					JSONObject requestJson = new JSONObject(requestBody);

					// Extract user ID from the request
					String id = requestJson.getString("user_id");
					System.out.println("Received user_id: " + id);

					// Fetch transaction history
					String[] history = DB_Template.senderTransactionsInfo(id);
					System.out.println("Fetched history: " + Arrays.toString(history));

					JSONArray responseArray = new JSONArray();

					if (history.length > 0) {
						// Parse each transaction and add it to the response
						for (String transaction : history) {
							String[] details = transaction.split(",");
							JSONObject transactionJson = new JSONObject();
							transactionJson.put("senderID", details[0]);
							transactionJson.put("receiverID", details[1]);
							transactionJson.put("amount", details[2]);
							transactionJson.put("timestamp", details[3]);
							responseArray.put(transactionJson);
						}
					} else {
						System.out.println("No transactions found for user_id: " + id);
					}

					// Send the JSON response
					System.out.println("Sending response: " + responseArray.toString());
					String response = responseArray.toString();
					exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);

					try (OutputStream outputStream = exchange.getResponseBody()) {
						outputStream.write(response.getBytes(StandardCharsets.UTF_8));
					}
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


	// registration with encryption
	private static class RegisterHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

					// Read and decrypt JSON request body
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					JSONObject requestJson = new JSONObject(requestBody);
					String encryptedDataString = requestJson.getString("data");
					System.out.println("Received register request with encrypted data: " + encryptedDataString);
					String decryptedDataJson = AESDecryption.decrypt(encryptedDataString);
					System.out.println("Decrypted data: " + decryptedDataJson);

					// Parse JSON for email and password
					JSONObject registrationData = new JSONObject(decryptedDataJson);
					String fname = registrationData.getString("fname");
					String lname = registrationData.getString("lname");
					String email = registrationData.getString("email");
					String password = registrationData.getString("password");

					System.out.println("Received registration for: " + fname + " " + lname + " (" + email + ")");
					String userId = java.util.UUID.randomUUID().toString();

					//getting confirmation registration has been accomplished in the db
					boolean registrationSuccess = DB_Template.userRegister(userId, email, password, fname, lname);
					JSONObject responseJson = new JSONObject();
					if (registrationSuccess) {
						responseJson.put("message", "Registration successful!");
						responseJson.put("user_id", userId); // Include the UUID in the response
						responseJson.put("fname",fname);
					} else {
						responseJson.put("message", "Registration failed.");
					}

					String response = responseJson.toString();
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
	// login with encryption
	private static class LoginHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {

					// Read and decrypt JSON request body
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					JSONObject requestJson = new JSONObject(requestBody);
					String encryptedDataString = requestJson.getString("data");
					System.out.println("Received login request with encrypted data: " + encryptedDataString);
					String decryptedDataJson = AESDecryption.decrypt(encryptedDataString);
					System.out.println("Decrypted data: " + decryptedDataJson);


					// Parse JSON for email and password
					JSONObject loginData = new JSONObject(decryptedDataJson);
					String email = loginData.getString("email");
					String password = loginData.getString("password");
					System.out.println("email: "+email + " password: "+password);

					// Call the userLogin method to validate credentials
					boolean loginSuccess = DB_Template.userLogin(email, password);


					// Prepare response
					JSONObject responseJson = new JSONObject();
					if (loginSuccess) {
						responseJson.put("message", "Login successful!");
						responseJson.put("fname",DB_Template.getUserFirstNameEmail(email));
						responseJson.put("user_id", DB_Template.getUserIdByName(DB_Template.getUserFirstNameEmail(email)));
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
	// gives the encryption key to the client
	private static class KeyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Step 1: Generate or retrieve the encryption key
					String encryptionKey = "1234567890123456"; // Fixed key (must be 16 bytes for AES-128)

					// Step 2: Send the key to the client
					JSONObject responseJson = new JSONObject();
					responseJson.put("encryptionKey", encryptionKey);
					System.out.println("encryptionKey"+encryptionKey);
					exchange.sendResponseHeaders(200, responseJson.toString().getBytes(StandardCharsets.UTF_8).length);

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
	private static class UserNameHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Parse request to get user ID
					InputStream inputStream = exchange.getRequestBody();
					String requestBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
					JSONObject requestJson = new JSONObject(requestBody);
					String userId = requestJson.getString("user_id");

					// Fetch the user's first name from the database
					String userName = DB_Template.getUserFirstName(userId);

					// Prepare the response
					JSONObject responseJson = new JSONObject();
					if (userName != null && !userName.isEmpty()) {
						responseJson.put("name", userName);
					} else {
						responseJson.put("error", "User not found");
					}

					String response = responseJson.toString();
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
	//closes system resources when program exits
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
