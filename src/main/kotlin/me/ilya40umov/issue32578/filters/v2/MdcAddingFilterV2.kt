package me.ilya40umov.issue32578.filters.v2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import me.ilya40umov.issue32578.filters.CoRouterFilter
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.CoWebFilter.Companion.COROUTINE_CONTEXT_ATTRIBUTE
import kotlin.coroutines.CoroutineContext

@Component
class MdcAddingFilterV2 : CoRouterFilter {

    private val logger = LoggerFactory.getLogger(MdcAddingFilterV2::class.java)

    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        val mdcContext = MDCContext(MDC.getCopyOfContextMap() + mapOf("username" to "Gandalf"))
        val lastSavedContext = request.attributes()[COROUTINE_CONTEXT_ATTRIBUTE] as CoroutineContext?
        val newContext = when (lastSavedContext) {
            null -> Dispatchers.Unconfined + mdcContext
            else -> lastSavedContext + mdcContext
        }
        request.attributes()[COROUTINE_CONTEXT_ATTRIBUTE] = newContext
        logger.info("Added username to MDC")
        return next(request)
    }
}