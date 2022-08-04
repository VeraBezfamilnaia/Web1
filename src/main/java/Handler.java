import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Handler {
    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private final static int PARTS_INDEX = 1;

    public static void handleRequest(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");
            checkRequestParams(parts, socket);

            final var path = parts[PARTS_INDEX];
            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (!validPaths.contains(path)) {
                handleNotFoundCase(out, socket);
            } else if (path.equals("/classic.html")) {
                handleClassicCase(filePath, out, mimeType, socket);
            } else {
                final var length = Files.size(filePath);
                handleSuccessfulCase(filePath, length, out, mimeType, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkRequestParams(String[] parts, Socket socket) throws IOException {
        if (parts.length != 3) {
            socket.close();
        }
    }

    private static void handleNotFoundCase(BufferedOutputStream out, Socket socket) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
        socket.close();
    }

    private static void handleClassicCase(Path filePath, BufferedOutputStream out, String mimeType
            , Socket socket) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
        socket.close();
    }

    private static void handleSuccessfulCase(Path filePath, long length, BufferedOutputStream out, String mimeType,
                                             Socket socket)
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
        socket.close();
    }
}
