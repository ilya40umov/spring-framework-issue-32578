package me.ilya40umov.issue32578.filters.v1

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import me.ilya40umov.issue32578.filters.CoRouterFilter
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class MdcAddingFilterV1 : CoRouterFilter {

    private val logger = LoggerFactory.getLogger(MdcAddingFilterV1::class.java)

    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        return withContext(
            currentCoroutineContext() +
                MDCContext(MDC.getCopyOfContextMap() + mapOf("username" to "Gandalf"))
        ) {
            logger.info("Added username to MDC")
            next(request)
        }
    }
}