import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            var serverSocket = Server.start(9999);
            while (true) {
                var socket = serverSocket.accept();
                Server.handle(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
