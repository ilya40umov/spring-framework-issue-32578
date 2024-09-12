package me.ilya40umov.issue32578.filters

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

fun interface CoRouterFilter : suspend (ServerRequest, suspend (ServerRequest) -> ServerResponse) -> ServerResponse {
    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse,
    ): ServerResponse
}