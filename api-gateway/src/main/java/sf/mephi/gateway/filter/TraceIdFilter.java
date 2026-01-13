package sf.mephi.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Global filter that adds correlation/trace ID to all requests.
 * TraceId is propagated to downstream services via X-Correlation-ID header.
 *
 * Order: Highest priority (runs first)
 */
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Get or create correlation ID
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        final String traceId = correlationId;

        // Add to MDC for logging
        MDC.put(TRACE_ID_MDC_KEY, traceId);

        log.info("Request received: {} {} [traceId={}]",
                request.getMethod(),
                request.getPath(),
                traceId);

        // Add correlation ID to request headers for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CORRELATION_ID_HEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange)
                .doFinally(signalType -> {
                    log.info("Request completed: {} {} [traceId={}, signal={}]",
                            request.getMethod(),
                            request.getPath(),
                            traceId,
                            signalType);
                    MDC.remove(TRACE_ID_MDC_KEY);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}