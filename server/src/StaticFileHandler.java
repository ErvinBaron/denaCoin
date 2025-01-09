import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {
    private final String baseDirectory;

    public StaticFileHandler(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String filePath = exchange.getRequestURI().getPath();
        if (filePath.equals("/")) {
            filePath = "/index.html"; // Serve default file
        }

        Path path = Path.of(baseDirectory, filePath);
        if (Files.exists(path)) {
            byte[] content = Files.readAllBytes(path);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        } else {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
