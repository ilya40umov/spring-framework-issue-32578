package me.ilya40umov.issue32578.filters

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class RequestLoggingFilter : CoRouterFilter {

    private val logger = LoggerFactory.getLogger(RequestLoggingFilter::class.java)

    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        logger.info("EP called: ${request.method()} ${request.path()}")
        return next(request)
    }
}