
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CoinSellHandle implements HttpHandler {

	private Connection dbConnection;

    public CoinSellHandle(Connection dbConnection) {
    	
        this.dbConnection = dbConnection;
    }
    

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
	}

}
