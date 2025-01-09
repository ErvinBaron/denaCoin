
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class DB_Template {
	private static String url = "jdbc:sqlite:users.db1"; // The database file name
	private static Connection conn;

	public static void main(String[] args) throws SQLException, NoSuchAlgorithmException {

		// Database URL (the database will be created if it doesn't exist)
		conn = DriverManager.getConnection(url);
		
		System.out.println("Database file path: " + new java.io.File(url).getAbsolutePath());
		
		// SQL query to create the user table with email as username
		 String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
            + "id TEXT PRIMARY KEY, "
            + "email TEXT NOT NULL UNIQUE, " 
            + "password TEXT NOT NULL, " 
            + "name TEXT NOT NULL," 
            + "lastName TEXT NOT NULL" 
            + ");";

		 //SQL query to create the wallets table
        String createWalletsTable = "CREATE TABLE IF NOT EXISTS wallets ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "coin_balance REAL, "
            + "user_id TEXT, "
            + "FOREIGN KEY (user_id) REFERENCES users(id)"
            + ");";
        //SQL query to create the transactions history table
        String createTransactionHistoryTable = "CREATE TABLE IF NOT EXISTS transaction_history ("
            + "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "senderID TEXT, "
            + "reciverID TEXT, "
            + "transaction_amount REAL, "
            + "time_of_date DATETIME, "
            + "FOREIGN KEY (senderID) REFERENCES users(id), "
            + "FOREIGN KEY (reciverID) REFERENCES users(id)"
            + ");";

//When the program exits the try block, the JVM automatically calls close() for all resources defined in try-with-resources,
//	including Connection and Statement.
		   
       			
       			 
        try (Statement stmt = conn.createStatement()) {
//       			create the tables
		         if (stmt.execute(createUsersTable)) { 
				        System.out.println("Users table created successfully!");
				   
		         }
		         else {
		        	 System.out.println("Problem occured while trying to create the Users table.");
		         }
		         if (stmt.execute(createWalletsTable)) { 
				        
				        System.out.println("Wallets table created successfully!");
				      
		         }
		         else {
		        	 System.out.println("Problem occured while trying to create the wallets table.");
		         }
		         if(stmt.execute(createTransactionHistoryTable)) {
		        	 System.out.println("Transactions table created successfully!");
		         }else {
		        	 System.out.println("Problem occured while trying to create the transactions table.");
		         }
  
		        
        	
//		        userRegister("user1", "user1@gmailhfuhr", "user1yuhe123", "yacov", "israel"); 
//		        userRegister("user2", "user2@gmailhfuhr", "user2hq7t3", "pol", "lior"); 
//		        userRegister("user3", "user@dor", "dor123", "pol", "lior"); 
//		        userLogin("user1@gmailhfuhr", "hghjgvg");
//		        userLogin("user@dor", "dor123");

//		        userExist("user1");
//		        userExist("user54");
		        getUserInfo("user1");
//		        getUserInfo("ngk4532");
//		        addWallet("user2");
//		        addWallet("user3");
		        
//		        checkAmount("ndjkek", 40.393);
//		        change_wallet_coin_balance("ndjkek",30);
//		        new_transaction("user2", "user3", 10);
//		        new_transaction("user2", "user3", 100);
//		        senderTransactionsInfo("user2");
		        
		        String[] arr = reciverTransactionsInfo("user3");
		        System.out.println(Arrays.toString(arr));
		        System.out.println(getCoinBalance("user2"));
		        System.out.println(getUserFirstName("user3"));
		      
		
       		
       		} catch (SQLException e) {
		    System.out.println("An error occurred while establishing the connection.");
		    e.printStackTrace();
		    }
       		
       		
		}
	
	
	//registering a new user in the users table with input data
	public static boolean userRegister(String id, String email, String password,String name, String lastName) throws SQLException, NoSuchAlgorithmException {
		//SQL query to create a new user
       	String createUser = "INSERT INTO users (id, email, password, name, lastName) VALUES (?, ?, ?, ?, ?)";
       	
       	//encrypting the user password
       	String encryptePassword = hashWith256(password);
       	
        try(PreparedStatement pstmt = conn.prepareStatement(createUser)){ 
       		
       		pstmt.setString(1, id);
       		pstmt.setString(2, email);
       		pstmt.setString(3, encryptePassword);
       		pstmt.setString(4, name);
       		pstmt.setString(5, lastName);
       		
       		
       		int rowsAffected = pstmt.executeUpdate(); 
       		
       	// Check if a row was inserted
            if (rowsAffected > 0) {
//            	addWallet(id);
                System.out.println("User registered successfully.");
                return true;
            } else {
                System.out.println("User registration failed.(DB error)" );
                return false;
            }
        } catch(SQLException e) {
			System.err.println("Error in user registraition. (User wrong input)"+e.getMessage() );
			return false;
			
		}
	}
	
       	
	//password encryption method
	public static String hashWith256(String textToHash) throws NoSuchAlgorithmException{
        byte[] hash = new byte[256];
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            hash = messageDigest.digest(textToHash.getBytes(StandardCharsets.UTF_8));
            System.out.println("Hashing completed for: " + textToHash);

        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        return String.format("%064x", new BigInteger(1, hash));
    }

	//method gets email and password for login and checks if a user with that email exist and if the password is correct
	public static boolean userLogin(String email, String password) throws SQLException, NoSuchAlgorithmException { 
	    String query = "SELECT password FROM users WHERE email = ?"; // Corrected SQL query
	    String encryptedPassword = hashWith256(password); // Hash the input password
	    String storedPassword = null; // Initialize as null to handle no results case

	    try (PreparedStatement pstmt = conn.prepareStatement(query)) { 
	    	System.out.println("DEBUG email: "+email);
	        pstmt.setString(1, email); // Set the email parameter

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) { // Check if a record exists
	                storedPassword = rs.getString("password"); // Get the password
	                System.out.println("Stored password: " + storedPassword);  
	            } else {
	                System.out.println("No user found with email: " + email);
	                return false; // No user found with the given email
	            }
	        }
	        } catch(SQLException e) {
				System.err.println("Error in user login "+e.getMessage() );
				return false;
				
			}
	        // Compare the encrypted input password with the stored password
	        return encryptedPassword.equals(storedPassword);
	    }

	

	//method gets a user id and checks if that user exists
	public static boolean userExist(String userId) throws SQLException {
	    String query = "SELECT name FROM users WHERE id = ?"; // Query to check existence
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, userId); // Set the user ID parameter

	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                System.out.println("User exists with ID: " + userId);
	                return true;  // If a record exists, the user exists
	            } else {
	                System.out.println("No user found with ID: " + userId);
	                return false;
	            }
	        }
	        } catch(SQLException e) {
				System.err.println("Error in cheking ig user exists "+e.getMessage() );
				return false;
				
			}
	    }
	
	
	//method that inserts a new wallet in the wallets table
	public static boolean addWallet(String user_id) throws SQLException { 
		String createwallets = "INSERT INTO wallets (coin_balance, user_id) VALUES (?, ?)";
		
		try (PreparedStatement pstmt = conn.prepareStatement(createwallets)){ 
			
			pstmt.setDouble(1, 20.0);
			pstmt.setString(2, user_id);
			
			
			int rowsAffected = pstmt.executeUpdate(); 
			
			// Check if a row was inserted
			if (rowsAffected > 0) {
				System.out.println("wallet add successfully.");
				return true;
			} else {
				System.out.println("wallet add  failed.");
				return false;
			}
			} catch(SQLException e) {
				System.err.println("Error in adding wallet "+e.getMessage() );
				return false;
				
			}
		}
		

	
	//method gets user id and the amount the user wants to transfer and checks if the user coin balance is sufficient for the wanted transfer
	public static boolean checkAmount(String userId, double amount) throws SQLException{
		String query = "SELECT coin_balance FROM wallets WHERE user_id = ?"; 
		double coin_balance;
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, userId); // Set the user ID parameter

	        try (ResultSet rs = pstmt.executeQuery()) {
	        	if (rs.next()) { // Check if a record exists
	        		coin_balance = rs.getDouble("coin_balance"); 
	        		System.out.println("Current coin balance: " + coin_balance);
	        		if (coin_balance - amount >= 0) {
	        			System.out.println("coin balance sufficient for transfer");
	        			return true;
	        		}
	        		else {
	        			System.out.println("coin balance insufficient");
	        			return false;
	        		}
	        			
	        	} else {
	                System.out.println("No wallet found for user ID: " + userId);
	                return false;  // No user found with the given id
	            }
	        }
	        } catch(SQLException e) {
				System.err.println("Error in cheking amount "+e.getMessage() );
				return false;
				
			}
	    }
		

	//method that gets a user id and prints the user details if exists
	public static String[] getUserInfo(String id) throws SQLException {
		String userInfo = "SELECT email, name, lastName FROM users WHERE id = ?";
		String[] info = new String[4];
		
		try(PreparedStatement pstmt = conn.prepareStatement(userInfo)) {
			pstmt.setString(1, id);
			
			try (ResultSet rs = pstmt.executeQuery()) { 
		        if (rs.next()) { 
		        	info[0] = id;
		        	info[1] = rs.getString("name");
		        	info[2] = rs.getString("lastName");
		        	info[3] = rs.getString("email");
//		            System.out.println("Record found:");
//		            System.out.println("Name: " + rs.getString("name"));
//		            System.out.println("last Name: " + rs.getString("lastName")); 
//		            System.out.println("email: " + rs.getString("email")); 
		        	System.out.println("record found " + Arrays.toString(info));
		        } else { 
		            System.out.println("No record found with ID " + id); 
		        }
		    }
		
			return info;
			
			
		} catch(SQLException e) {
			System.err.println("Error in pulling user information "+e.getMessage() );
			
			
		}
		return info;
		
	}

	
	//method that gets the user id and the amount thats needed to change in the balance, checks and changes if possible
	public static boolean change_wallet_coin_balance(String userId, double amount) throws SQLException {
			String query = "UPDATE wallets SET coin_balance = coin_balance + ? WHERE user_id = ?";
		    
		    // Ensure checkAmount() returns true before updating the balance
		    if (!checkAmount(userId, Math.abs(amount))) {
		        return false; // Return false if the operation isn't allowed
		    }

		    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
		        pstmt.setDouble(1, amount); // Set the amount to add
		        pstmt.setString(2, userId); // Set the user ID
		        
		        int rowsUpdated = pstmt.executeUpdate(); // Execute the update query
		        
		        // Return true if a row was updated, false otherwise
		        return rowsUpdated > 0;
		    } catch(SQLException e) {
				System.err.println("Error in updating coin balance "+e.getMessage() );
				return false;
				
			}
		}
	
	
	//method that gets the ids of sender and receiver and the amount for the transfer, and transfers the money
	public static boolean new_transaction(String senderID ,String reciverID,double amount) throws SQLException
	{	
		 // Get the current date and time
        LocalDateTime currentTime = LocalDateTime.now();
        
        // Format it to the desired format for SQL DATETIME (dd-MM-yyyy HH:mm)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedTime = currentTime.format(formatter);
       
        String createTransaction = "INSERT INTO transaction_history (senderID ,reciverID ,transaction_amount , time_of_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(createTransaction)){ 

        if(userExist(senderID) || userExist(reciverID) || senderID != reciverID || amount > 0)
		{		
		if(checkAmount(senderID,(amount*(1)))) {
			
			change_wallet_coin_balance(senderID,((-1)*amount));
		    change_wallet_coin_balance(reciverID,amount);
		    

       		pstmt.setString(1, senderID);
       		pstmt.setString(2, reciverID);
       		pstmt.setDouble(3, amount);
       		pstmt.setString(4, formattedTime);
       		
       		int rowsAffected = pstmt.executeUpdate(); 
       		
           	// Check if a row was inserted
                if (rowsAffected > 0) {
                    System.out.println("Transaction added successfully.");
                    return true;
                } else {
                    System.out.println("Transaction adding failed.");
                    return false;
                }
		
		}	
		}
		else {
		}
		
        return false;
       	
 	}
 	catch(SQLException e) {
    				
 		System.err.println("Error in Transactions transfering  "+e.getMessage() );
    	return false;
    	}
 	}
       		
	
	
	//method that gets a user id and prints all transactions where he transfered coins
	public static boolean senderTransactionsInfo(String id) throws SQLException {
		String userInfo = "SELECT senderID, reciverID, transaction_amount, time_of_date FROM transaction_history WHERE senderID = ?";
		try(PreparedStatement pstmt = conn.prepareStatement(userInfo)) {
			pstmt.setString(1, id);
			
			try (ResultSet rs = pstmt.executeQuery()) { 
		        while(rs.next()) { 
		        
		            System.out.println("Record found:");
		            System.out.println("Sender: " + rs.getString("senderID"));
		            System.out.println("reciver: " + rs.getString("reciverID")); 
		            System.out.println("transaction amount: " + rs.getString("transaction_amount")); 
		            System.out.println("time of date: " + rs.getString("time_of_date")); 
				       
		       
		    }
		        return true;
		
		}
			
	    } catch(SQLException e) {
			System.err.println("Error in sender transactions info "+e.getMessage() );
			return false;
			
		}	
	}
	
	//method that gets user id and prints all transactions where he received coins
	public static String[] reciverTransactionsInfo(String id) throws SQLException {
			String userInfo = "SELECT senderID, reciverID, transaction_amount, time_of_date FROM transaction_history WHERE reciverID = ?";
			ArrayList<String> resultArrayList = new ArrayList<>();
			String[] resultArray;
			try(PreparedStatement pstmt = conn.prepareStatement(userInfo)) {
				pstmt.setString(1, id);
				
				try (ResultSet rs = pstmt.executeQuery()) { 
			        while(rs.next()) { 
			        	System.out.println(rs);
//			            System.out.println("Record found:");
//			            System.out.println("Sender: " + rs.getString("senderID"));
//			            System.out.println("reciver: " + rs.getString("reciverID")); 
//			            System.out.println("transaction amount: " + rs.getString("transaction_amount")); 
//			            System.out.println("time of date: " + rs.getString("time_of_date"));
			        	resultArrayList.add(rs.getString("senderID") + "," + 
			        						rs.getString("reciverID") + "," +
			        						rs.getString("transaction_amount") + "," +
			        						rs.getString("time_of_date"));
			        }
			        
			        resultArray = resultArrayList.toArray(new String[resultArrayList.size()]);
			        return resultArray;
				}
			} catch(SQLException e) {
				System.err.println("Error in reciver transactions info "+e.getMessage() );
				resultArray = new String[resultArrayList.size()];
				return resultArray;
			}
			
			
		}
	
	//if -1 returned then no user exists with input user id
	public static double getCoinBalance (String user_id) throws SQLException
	{
		String query = "SELECT coin_balance FROM wallets WHERE user_id = ?"; 
		double coin_balance = -1;
	    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, user_id); // Set the user ID parameter

	        try (ResultSet rs = pstmt.executeQuery()) {
	        	if (rs.next()) { // Check if a record exists
	        		coin_balance = rs.getDouble("coin_balance");
	        	
	        	}
	        }
	        return coin_balance;
	    }
	   
	        catch (SQLException e)
	        {
	        	e.printStackTrace();
	        	return coin_balance; 
	        }
	}
	
	public static String getUserFirstName(String user_id) throws SQLException
	{
		String name ="";
		String query = "SELECT name FROM users WHERE id = ?";
		
		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	        pstmt.setString(1, user_id); // Set the user ID parameter

	        try (ResultSet rs = pstmt.executeQuery()) {
	        	if (rs.next()) { // Check if a record exists
	        		name = rs.getString("name");
	        	
	        	}
	        }
	        return name;
	    }
	   
	        catch (SQLException e)
	        {
	        	e.printStackTrace();
	        	return name; 
	        }
		
	}
	
	    
}
