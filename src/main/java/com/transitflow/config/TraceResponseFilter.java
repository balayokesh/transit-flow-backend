package com.transitflow.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that attaches the Micrometer trace ID to the response header (X-Request-ID)
 * and ensures SLF4J MDC contains the requestId key for end-to-end request tracing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceResponseFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_REQUEST_ID_KEY = "requestId";
    public static final String MDC_TRACE_ID_KEY = "traceId";

    private final Tracer tracer;

    public TraceResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Span currentSpan = tracer.currentSpan();
        String traceId = (currentSpan != null && currentSpan.context() != null)
                ? currentSpan.context().traceId()
                : null;

        if (traceId != null) {
            MDC.put(MDC_REQUEST_ID_KEY, traceId);
            MDC.put(MDC_TRACE_ID_KEY, traceId);
            response.setHeader(REQUEST_ID_HEADER, traceId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (response.getHeader(REQUEST_ID_HEADER) == null) {
                Span finalSpan = tracer.currentSpan();
                if (finalSpan != null && finalSpan.context() != null) {
                    String finalTraceId = finalSpan.context().traceId();
                    response.setHeader(REQUEST_ID_HEADER, finalTraceId);
                }
            }
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }
}
