import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final ExecutorService POOL = Executors.newFixedThreadPool(64);
    private static final ConcurrentMap<String, Handler> GET_HANDLERS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Handler> POST_HANDLERS = new ConcurrentHashMap<>();
    private static final String GET = "GET";

    public void addHandler(String method, String path, Handler handler) {
        if (method.equals(GET)) {
            GET_HANDLERS.put(path, handler);
        } else {
            POST_HANDLERS.put(path, handler);
        }
    }

    public void listen(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                var client = server.accept();
                POOL.execute(() -> executeRequest(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void executeRequest(Socket socket) {
        try (
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Parser.parse(in, out);
            System.out.println("REQUEST HERE");
            String method = request.getMethod();
            String path = request.getPath();

            if (method.equals(GET)) {
                handleAppropriateRequest(out, request, path, GET_HANDLERS);
            } else {
                handleAppropriateRequest(out, request, path, POST_HANDLERS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleAppropriateRequest(BufferedOutputStream out, Request request, String path,
                                                 ConcurrentMap<String, Handler> handlers) {
        for (Map.Entry<String, Handler> handlerEntry : handlers.entrySet()) {
            if (handlerEntry.getKey().equals(path)) {
                handlerEntry.getValue().handle(request, out);
            }
        }
    }
}
