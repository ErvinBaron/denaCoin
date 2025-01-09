import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javax.crypto.Cipher;
import org.json.JSONObject;

public class Main {

	private static Connection dbConnection;
	private static final int PORT = 8000;
	private static KeyPair rsaKeyPair;
	private static Blockchain blockchain;

 public static void main(String[] args) {
     try {
         // Initialize the database and blockchain
         initializeDatabase();
         blockchain = new Blockchain(2); // Set mining difficulty
         rsaKeyPair = generateRSAKeyPair();
 
         // Start the HTTP server
         HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
 
         // Register handlers
         server.createContext("/pages/home", new CorsHandle(new HomeHandler(dbConnection)));
         server.createContext("/pages/login", new CorsHandle(new LoginHandler(dbConnection)));
         server.createContext("/pages/coin", new CorsHandle(new CoinHandler(dbConnection)));
         server.createContext("/addTransaction", new CorsHandle(new TransactionHandler()));
         server.createContext("/getBlockchain", new CorsHandle(new BlockchainHandler()));
         server.createContext("/register", new CorsHandle(new RegisterHandler()));
         server.createContext("/getPublicKey", new CorsHandle(new PublicKeyHandler()));
 
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

	private static KeyPair generateRSAKeyPair() throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048); // RSA 2048-bit key size
		return keyGen.generateKeyPair();
	}

	// Handlers integrated from the provided code

	// Handler to expose the public key
	private static class PublicKeyHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
					String publicKey = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
					byte[] responseBytes = publicKey.getBytes(StandardCharsets.UTF_8);

					exchange.sendResponseHeaders(200, responseBytes.length);
					exchange.getResponseBody().write(responseBytes);
					exchange.getResponseBody().close();
				} else {
					exchange.sendResponseHeaders(405, -1); // Method Not Allowed
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Update the RegisterHandler to handle encrypted data
	private static class RegisterHandler implements HttpHandler {
		private static final String SECRET_KEY = "1234567890123456"; // Must be 16 bytes

		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					// Read encrypted request body
					InputStream inputStream = exchange.getRequestBody();
					String encryptedData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

					// Decrypt the data
					String decryptedMessage = AESUtils.decrypt(encryptedData, SECRET_KEY);

					// Parse the decrypted JSON
					JSONObject registrationData = new JSONObject(decryptedMessage);
					String fname = registrationData.getString("fname");
					String lname = registrationData.getString("lname");
					String email = registrationData.getString("email");
					String password = registrationData.getString("password");

					// Process the registration (mock)
					System.out.println("Received registration for: " + fname + " " + lname + " (" + email + ")");

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

	public class AESUtils {

		private static final String ALGORITHM = "AES";

		public static String decrypt(String encryptedData, String secretKey) throws Exception {
			SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);

			byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
			byte[] decryptedBytes = cipher.doFinal(decodedBytes);

			return new String(decryptedBytes, StandardCharsets.UTF_8);
		}
	}

	// Handler to add transactions
	private static class TransactionHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) {
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					InputStream inputStream = exchange.getRequestBody();
					String encryptedData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

					// Decrypt the data
					Cipher cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
					byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
					String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);

					// Parse the decrypted JSON
					JSONObject json = new JSONObject(decryptedMessage);
					String amount = json.getString("amount");
					String sender = json.getString("sender");
					String receiver = json.getString("receiver");

					// Add the transaction to the blockchain
					List<String> transactions = new ArrayList<>();
					transactions.add(sender + " sent " + amount + " to " + receiver);
					blockchain.addBlock(transactions);

					// Respond to the client
					String response = "Transaction added to the blockchain!";
					exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
					exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
					exchange.getResponseBody().close();
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

					String response = responseJson.toString();
					exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
					exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
					exchange.getResponseBody().close();
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
