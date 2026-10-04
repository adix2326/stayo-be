package com.stayo.stayo.config;

import com.github.luben.zstd.Zstd;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Compresses JSON responses with zstd when the client sends "Accept-Encoding: zstd".
 * Clients without zstd fall through untouched, and Tomcat's built-in gzip
 * (server.compression.*) handles them. Tomcat skips responses that already
 * carry a Content-Encoding header, so the two never double-compress.
 */
@Component
@ConditionalOnProperty(name = "app.compression.zstd-enabled", havingValue = "true", matchIfMissing = true)
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ZstdCompressionFilter extends OncePerRequestFilter {

    private static final Pattern ZSTD = Pattern.compile("(^|[\\s,])zstd\\s*(;(?!\\s*q\\s*=\\s*0(\\.0+)?\\s*(,|$))[^,]*)?(,|$)");

    private final int minResponseSize;
    private final int level;

    public ZstdCompressionFilter(
            @Value("${server.compression.min-response-size:1024}") int minResponseSize,
            @Value("${app.compression.zstd-level:3}") int level) {
        this.minResponseSize = minResponseSize;
        this.level = level;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        return accept == null || !ZSTD.matcher(accept.toLowerCase()).find();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
        } finally {
            writeResponse(request, response, wrapper);
        }
    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response,
                               ContentCachingResponseWrapper wrapper) throws IOException {
        byte[] body = wrapper.getContentAsByteArray();
        String contentType = wrapper.getContentType();
        boolean compress = body.length >= minResponseSize
                && wrapper.getStatus() == HttpServletResponse.SC_OK
                && !"HEAD".equals(request.getMethod())
                && contentType != null && contentType.startsWith("application/json")
                && !wrapper.containsHeader(HttpHeaders.CONTENT_ENCODING);

        if (!compress) {
            wrapper.copyBodyToResponse();
            return;
        }

        byte[] compressed = Zstd.compress(body, level);
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "zstd");
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_ENCODING);
        response.setContentLength(compressed.length);
        response.getOutputStream().write(compressed);
    }
}
