public class Main {
    private final static int PORT = 9999;

    public static void main(String[] args) {
        var server = new Server();
        server.listen(PORT);
    }
}
