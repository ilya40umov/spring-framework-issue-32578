package me.ilya40umov.issue32578.filters

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.server.CoWebFilter.Companion.COROUTINE_CONTEXT_ATTRIBUTE
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

@Component
class FeatureFlagsContextFilter : CoRouterFilter {

    override suspend fun invoke(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        val ffContext = FeatureFlagsContext(attributes = mapOf("username" to "Gandalf"))
        val lastSavedContext = request.attributes()[COROUTINE_CONTEXT_ATTRIBUTE] as CoroutineContext
        request.attributes()[COROUTINE_CONTEXT_ATTRIBUTE] = lastSavedContext + ffContext
        return next(request)
    }

    class FeatureFlagsContext(
        val attributes: Map<String, Any>,
    ) : AbstractCoroutineContextElement(Key) {
        companion object Key : CoroutineContext.Key<FeatureFlagsContext>
    }
}