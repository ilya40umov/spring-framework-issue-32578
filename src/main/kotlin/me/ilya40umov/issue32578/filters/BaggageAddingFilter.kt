package me.ilya40umov.issue32578.filters

import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class BaggageAddingFilter(
    val tracer: Tracer
) : CoRouterFilter {
    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        val traceContext = tracer.currentSpan()?.context()!!
        val baggageInScope = tracer.createBaggageInScope(traceContext, "username", "Gandalf")
        return baggageInScope.use { _ ->
            next(request)
        }
    }
}