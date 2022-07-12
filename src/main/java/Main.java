import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var server = new Server();
        server.addHandler("GET", "/messages.html", Main::handleImpl);
        server.addHandler("POST", "/messages.html", Main::handleImpl);
        server.listen(9999);
    }

    private static void handleImpl(Request request, BufferedOutputStream responseStream) {
        final var filePath = Path.of(".", "public", request.getPath());
        try {
            final String mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);
            handleSuccessfulCase(filePath, length, responseStream, mimeType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleSuccessfulCase(Path filePath, long length, BufferedOutputStream out, String mimeType)
            throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
