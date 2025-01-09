
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CorsHandle implements HttpHandler {
	private final HttpHandler wrappedHandler;

	public CorsHandle(HttpHandler handler) {
		this.wrappedHandler = handler;
	}
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		System.out.println("Recevied request: " + exchange.getRequestMethod());
		
//		Add CORS headers
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
	
		if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
			System.out.println("OPTIONS request handled");
			exchange.sendResponseHeaders(204, -1); // no content 
			return;
		}
		
		wrappedHandler.handle(exchange);
	}
	
}
