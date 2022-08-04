import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 64;
    private static final ExecutorService pool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public void listen(int port) {
        try {
            var server = new ServerSocket(port);
            while (true) {
                var client = server.accept();
                handle(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(Socket client) {
        pool.execute(() -> Handler.handleRequest(client));
    }
}
