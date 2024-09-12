package me.ilya40umov.issue32578

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import me.ilya40umov.issue32578.filters.BaggageAddingFilter
import me.ilya40umov.issue32578.filters.FeatureFlagsContextFilter
import me.ilya40umov.issue32578.filters.RequestLoggingFilter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.plus
import org.springframework.web.server.CoWebFilter
import kotlin.coroutines.CoroutineContext

@Configuration
class Router(
    private val observationRegistry: ObservationRegistry,
) {
    private val logger = LoggerFactory.getLogger(Router::class.java)

    @Bean
    fun routes(
        baggageAddingFilter: BaggageAddingFilter,
        requestLoggingFilter: RequestLoggingFilter,
        featureFlagsContextFilter: FeatureFlagsContextFilter
    ): RouterFunction<ServerResponse> = coRouter {
        // XXX we don't want any of the filters etc. to apply to these endpoints,
        //  which is why if we were to use CoWebFilter, we would need to make those aware of all endpoints to ignore
        GET("/internal/get-something") { _ ->
            ServerResponse.ok().bodyValueAndAwait(mapOf("some" to "value"))
        }
    } + coRouter {
        context(contextProvider())
        filter(baggageAddingFilter) // this filter adds baggage to the current trace
        filter(requestLoggingFilter) // this filter is logging a request relying on baggage already being there
        filter(featureFlagsContextFilter) // this filter is modifying the coroutine context further by adding FF context
        GET("/api/username") { _ ->
            val ffContext = currentCoroutineContext()[FeatureFlagsContextFilter.FeatureFlagsContext.Key]!!
            ServerResponse.ok().bodyValueAndAwait(mapOf("username" to ffContext.attributes["username"]))
        }
    }

    private fun contextProvider(): suspend (ServerRequest) -> CoroutineContext = { request ->
        logger.info("contextProvider() is called for ${request.uri()}")
        // XXX if context is defined yet, we need to define one here and add observation in there
        if (CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE !in request.attributes()) {
            request.attributes()[CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE] =
                Dispatchers.Unconfined + observationRegistry.asContextElement()
        }
        // XXX and then we need to always return the value stored under CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE
        // to make sure that we have some sort of propagation between different filters etc.
        request.attributes()[CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE] as CoroutineContext
    }
}