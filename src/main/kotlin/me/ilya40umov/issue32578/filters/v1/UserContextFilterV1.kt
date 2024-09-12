package me.ilya40umov.issue32578.filters.v1

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import me.ilya40umov.issue32578.UserContext
import me.ilya40umov.issue32578.filters.CoRouterFilter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

@Component
class UserContextFilterV1 : CoRouterFilter {

    private val logger = LoggerFactory.getLogger(UserContextFilterV1::class.java)

    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        logger.info("Adding user context to the coroutine context")
        val userContext = UserContext(attributes = mapOf("username" to "Gandalf"))
        return withContext(currentCoroutineContext() + userContext) {
            next(request)
        }
    }

}