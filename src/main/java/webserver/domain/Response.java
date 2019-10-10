package webserver.domain;

import org.slf4j.Logger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class Response {
    private static final Logger LOG = getLogger(Response.class);
    private static final byte[] NEW_LINE = "\r\n".getBytes(StandardCharsets.ISO_8859_1);

    private ResponseHeader header;
    private ResponseBody body;

    private Response(final ResponseHeader header, final ResponseBody body) {
        this.header = header;
        this.body = body;
    }

    public static class Builder {
        private static final String CONTENT_TYPE = "Content-Type";
        private static final String LOCATION = "Location";
        private static final String EMPTY = "";

        private Map<String, String> fields = new HashMap<>();
        private ResponseBody body = new ResponseBody();
        private HttpVersion protocol;
        private HttpStatus httpStatus;
        private Cookies cookies;

        Builder(final HttpVersion protocol, final HttpStatus httpStatus, final Cookies cookies) {
            this.protocol = protocol;
            this.httpStatus = httpStatus;
            this.cookies = cookies;
        }

        public Builder(final HttpVersion protocol, final Request request) {
            this(protocol, HttpStatus.OK, request.getCookies());
        }

        public Builder(final HttpStatus httpStatus, final Request request) {
            this(HttpVersion._1_1, httpStatus, request.getCookies());
        }

        public Builder(final Request request) {
            this(HttpVersion._1_1, HttpStatus.OK, request.getCookies());
        }

        public Builder() {
            this(HttpVersion._1_1, HttpStatus.OK, new Cookies(EMPTY));
        }

        public Builder protocol(final HttpVersion protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder httpStatus(final HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder putField(final String key, final String value) {
            if (!EMPTY.equals(value)) {
                this.fields.put(key, value);
            }
            return this;
        }

        public Builder redirect(final String url) {
            this.httpStatus = HttpStatus.FOUND;
            this.putField(LOCATION, url);
            return this;
        }

        public Builder contentType(final MediaType contentType) {
            this.putField(CONTENT_TYPE, contentType.is());
            return this;
        }

        public Builder addCookies(final Cookies cookies) {
            this.cookies.merge(cookies);
            return this;
        }

        public Builder body(final byte[] body) {
            this.body = new ResponseBody(body);
            return this;
        }

        public Builder body(final String body) {
            this.body = new ResponseBody(body.getBytes(StandardCharsets.UTF_8));
            return this;
        }

        public Builder body(final StaticFile file) {
            this.putField(CONTENT_TYPE, MediaType.of(file.getExtension()).is());
            this.body = new ResponseBody(file.getBody());
            return this;
        }

        public Response build() {
            final ResponseHeader header = new ResponseHeader(this.protocol, this.httpStatus, this.fields, this.cookies);
            return new Response(header, this.body);
        }
    }

    public void sendToClient(final OutputStream outputStream) {
        final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        try {
            writeTo(dataOutputStream, this.header.getBytes(this.body.length()));
            writeTo(dataOutputStream, NEW_LINE);
            writeTo(dataOutputStream, this.body.getBytes());
            dataOutputStream.flush();
        } catch (final IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void writeTo(final OutputStream stream, final byte[] data) throws IOException {
        stream.write(data, 0, data.length);
    }
}
