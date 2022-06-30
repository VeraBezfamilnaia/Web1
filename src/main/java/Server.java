import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final ExecutorService pool = Executors.newFixedThreadPool(64);

    public static ServerSocket start(int port) throws IOException {
        return new ServerSocket(port);
    }

    public static void handle(Socket socket) {
        pool.execute(() -> Handler.handleRequest(socket));
    }
}
