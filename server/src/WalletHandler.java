
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class WalletHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		String path = exchange.getRequestURI().getPath();
		String query = exchange.getRequestURI().getQuery();
		OutputStream os = exchange.getResponseBody();
		
		if (exchange.getRequestMethod().equals("GET")) displayWallet(exchange, path);
	}

	public static void displayWallet(HttpExchange exchange, String path) throws IOException { 
		String body = new String(exchange.getRequestBody().readAllBytes());
		StringBuilder sb = new StringBuilder("[");
//		add logic 
		String id = parseField(body, "id");
		String amount = parseField(body, "amount"); 
		sendResponse(exchange, 200, sb.toString());
		
	}
	
	private static String parseField(String body, String field) {
		int index = body.indexOf(field + "\":\"");
		if (index == -1) return null;
		int start = index + field.length() + 3;
		int end = body.indexOf("\"", start);
		return body.substring(start, end);
	}
	
	private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
		exchange.sendResponseHeaders(statusCode, response.getBytes().length);
		try (OutputStream os = exchange.getResponseBody()){
			os.write(response.getBytes());
		}
	}
}
