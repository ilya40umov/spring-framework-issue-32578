package me.ilya40umov.issue32578.filters.v2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import me.ilya40umov.issue32578.UserContext
import me.ilya40umov.issue32578.filters.CoRouterFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.CoWebFilter.Companion.COROUTINE_CONTEXT_ATTRIBUTE
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

@Component
class UserContextFilterV2 : CoRouterFilter {

    private val logger = LoggerFactory.getLogger(UserContextFilterV2::class.java)

    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        logger.info("Adding user context to the coroutine context")
        val userContext = UserContext(attributes = mapOf("username" to "Gandalf"))
        val lastSavedContext = request.attributes()[COROUTINE_CONTEXT_ATTRIBUTE] as CoroutineContext?
        val newContext = when (lastSavedContext) {
            null -> Dispatchers.Unconfined + userContext
            else -> lastSavedContext + userContext
        }
        request.attributes()[COROUTINE_CONTEXT_ATTRIBUTE] = newContext
        return next(request)
    }

}