package Hakaton;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpServer;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpExchange;
//import java.io.OutputStream;
import java.io.IOException;

public class Main {
	
	String url = "jdbc:sqlite:C:\\Users\\david\\eclipse-workspace\\Hakaton\\users.db1";
	
    private static Connection dbConnection;
	private static final int PORT = 8000;
	
	public static void main(String[] args) {
		
		
		try {
			 initializeDatabase();
			
			HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
			server.createContext("/pages/home", new CorsHandle(new HomeHandler(dbConnection)));
			server.createContext("/pages/login", new CorsHandle(new LoginHandler(dbConnection)));
			server.createContext("/pages/register", new CorsHandle(new RegisterHandler(dbConnection)));
			server.createContext("/pages/coin", new CorsHandle(new CoinHandler(dbConnection)));
			server.createContext("/pages/coin/buy", new CorsHandle(new CoinBuyHandle(dbConnection)));
			server.createContext("/pages/coin/sell", new CorsHandle(new CoinSellHandle(dbConnection)));
			server.createContext("/pages/wallet", new CorsHandle(new WalletHandler(dbConnection)));
			
			server.setExecutor(null);
			server.start();
			
			System.out.println("Server is running on port " + PORT);
			
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}

	}
	
	private static void initializeDatabase() throws SQLException {
		String url = "jdbc:sqlite:C:\\Users\\david\\eclipse-workspace\\Hakaton\\users.db1";
		String username = "root";
		String password = "password";
		
		dbConnection = DriverManager.getConnection(url, username, password);
		System.out.println("Connected to the database successfully.");
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
