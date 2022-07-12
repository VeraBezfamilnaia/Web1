import java.util.List;

public class Request {
    private final String method;
    private final String path;
    private final List<String> headers;
    private String body;

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public Request(String method, String path, List<String> headers, String body) {
        this(method, path, headers);
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
