import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Parser {
    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST");
    private static final int COUNT_REQUEST_LINE_ELEMENTS = 3;
    private static final int METHOD_INDEX = 0;
    private static final int PATH_INDEX = 1;

    public static Request parse(BufferedInputStream in, BufferedOutputStream out) throws IOException {
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        checkValidness(in, out, requestLineEnd == -1);

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        checkValidness(in, out, requestLine.length != COUNT_REQUEST_LINE_ELEMENTS);

        final var method = requestLine[METHOD_INDEX];
        checkValidness(in, out, !ALLOWED_METHODS.contains(method));

        final var path = requestLine[PATH_INDEX];
        checkValidness(in, out, !path.startsWith("/"));

        final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        checkValidness(in, out, headersEnd == -1);

        in.reset();
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        if (!method.equals(ALLOWED_METHODS.get(0))) {
            in.skip(headersDelimiter.length);
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);

                final var body = new String(bodyBytes);
                return new Request(method, path, headers, body);
            }
        }

        return new Request(method, path, headers);
    }

    private static void checkValidness(BufferedInputStream in, BufferedOutputStream out, boolean isConditionTrue)
            throws IOException {
        if (isConditionTrue) {
            badRequest(out);
            in.close();
        }
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
