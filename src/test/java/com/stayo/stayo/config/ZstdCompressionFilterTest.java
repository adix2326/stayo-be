package com.stayo.stayo.config;

import com.github.luben.zstd.Zstd;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ZstdCompressionFilterTest {

    private final ZstdCompressionFilter filter = new ZstdCompressionFilter(1024, 3);
    private final byte[] json = ("{\"data\":\"" + "x".repeat(2000) + "\"}").getBytes(StandardCharsets.UTF_8);

    private MockHttpServletResponse call(String acceptEncoding, byte[] body, String contentType) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        if (acceptEncoding != null) {
            request.addHeader("Accept-Encoding", acceptEncoding);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            res.setContentType(contentType);
            res.getOutputStream().write(body);
        });
        return response;
    }

    @Test
    void compressesWithZstdWhenAccepted() throws Exception {
        MockHttpServletResponse response = call("gzip, zstd", json, "application/json");

        assertEquals("zstd", response.getHeader("Content-Encoding"));
        assertArrayEquals(json, Zstd.decompress(response.getContentAsByteArray(), json.length));
    }

    @Test
    void passesThroughWhenZstdNotAccepted() throws Exception {
        for (String accept : new String[] {null, "gzip", "zstd;q=0"}) {
            MockHttpServletResponse response = call(accept, json, "application/json");
            assertNull(response.getHeader("Content-Encoding"), accept);
            assertArrayEquals(json, response.getContentAsByteArray(), accept);
        }
    }

    @Test
    void skipsSmallAndNonJsonResponses() throws Exception {
        byte[] small = "{}".getBytes(StandardCharsets.UTF_8);
        assertNull(call("zstd", small, "application/json").getHeader("Content-Encoding"));
        assertNull(call("zstd", json, "text/plain").getHeader("Content-Encoding"));
    }
}
