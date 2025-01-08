import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.crypto.Cipher;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class CryptoServer {
    private Blockchain blockchain;
    private KeyPair rsaKeyPair;

    public CryptoServer() throws Exception {
        this.blockchain = new Blockchain(2);
        this.rsaKeyPair = generateRSAKeyPair(); // Generate RSA key pair during server initialization
    }

    public void startServer() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/addTransaction", new TransactionHandler());
        server.createContext("/getBlockchain", new BlockchainHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/getPublicKey", new PublicKeyHandler());

        server.setExecutor(null); // Default executor
        server.start();
        System.out.println("Server is listening on port 8000");
    }

    // Generate RSA Key Pair
    private KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); // RSA 2048-bit key size
        return keyGen.generateKeyPair();
    }

    // Handler to expose the public key to clients
    private class PublicKeyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Send the public key as a Base64-encoded string
                    String publicKey = Base64.getEncoder().encodeToString(rsaKeyPair.getPublic().getEncoded());
                    byte[] responseBytes = publicKey.getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(200, responseBytes.length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(responseBytes);
                    outputStream.close();
                } else {
                    exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // Handler to add transactions
    private class TransactionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Read encrypted request body
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

                     //Perform transaction logic
//                    if (new_transaction(sender, receiver, Double.parseDouble(amount))) {
//                        System.out.println("Transaction successful!");
//                    } else {
//                        throw new Exception("Insufficient funds!");
//                    }

                    // Create a transaction string
                    String transaction = sender + " sent " + amount + " to " + receiver;

                    // Add the transaction as a new block
                    List<String> transactions = new ArrayList<>();
                    transactions.add(transaction);
                    blockchain.addBlock(transactions);

                    // Send a success response
                    String response = "Transaction added to the blockchain!";
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

    // Handler to register new users
    private class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Read encrypted request body
                    InputStream inputStream = exchange.getRequestBody();
                    String encryptedData = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                    // Decrypt the data
                    Cipher cipher = Cipher.getInstance("RSA");
                    cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
                    byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
                    String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);

                    // Parse the decrypted JSON
                    JSONObject registrationData = new JSONObject(decryptedMessage);
                    String fname = registrationData.getString("fname");
                    String lname = registrationData.getString("lname");
                    String email = registrationData.getString("email");
                    String password = registrationData.getString("password");

                    // Process the registration (mock)
                    System.out.println("Registered User: " + fname + " " + lname + " (" + email + ")");

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

    // Handler to get the current blockchain
    private class BlockchainHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    // Create a JSON representation of the blockchain
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

                    // Send the blockchain as a response
                    String response = responseJson.toString();
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream outputStream = exchange.getResponseBody();
                    outputStream.write(response.getBytes());
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

    public static void main(String[] args) {
        try {
            CryptoServer server = new CryptoServer();
            server.startServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
