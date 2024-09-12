package me.ilya40umov.issue32578

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import me.ilya40umov.issue32578.filters.v1.MdcAddingFilterV1
import me.ilya40umov.issue32578.filters.v1.UserContextFilterV1
import me.ilya40umov.issue32578.filters.RequestLoggingFilter
import me.ilya40umov.issue32578.filters.v2.MdcAddingFilterV2
import me.ilya40umov.issue32578.filters.v2.UserContextFilterV2
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
        requestLoggingFilter: RequestLoggingFilter,
        mdcAddingFilterV1: MdcAddingFilterV1,
        mdcAddingFilterV2: MdcAddingFilterV2,
        userContextFilterV1: UserContextFilterV1,
        userContextFilterV2: UserContextFilterV2
    ): RouterFunction<ServerResponse> = coRouter {
        // it's possible that some endpoints don't need some / all the filters
        GET("/internal/get-something") { _ ->
            logger.info("Handling a request to /internal/get-something endpoint")
            ServerResponse.ok().bodyValueAndAwait(mapOf("some" to "value"))
        }
    } + coRouter {
        // XXX this does not work
        filter(mdcAddingFilterV1)
        filter(userContextFilterV1)
        filter(requestLoggingFilter)
        GET("/api/v1/username") { _ ->
            logger.info("Handling a request to /api/v1/username endpoint")
            val userContext = currentCoroutineContext()[UserContext.Key]
            ServerResponse.ok()
                .bodyValueAndAwait(mapOf("username" to userContext.username()))
        }
    } + coRouter {
        // XXX this approach works, but is ugly
        context(contextProvider())
        filter(mdcAddingFilterV2)
        filter(userContextFilterV2)
        filter(requestLoggingFilter)
        GET("/api/v2/username") { _ ->
            logger.info("Handling a request to /api/v2/username endpoint")
            val userContext = currentCoroutineContext()[UserContext.Key]
            ServerResponse.ok()
                .bodyValueAndAwait(mapOf("username" to userContext.username()))
        }
    }

    private fun contextProvider(): suspend (ServerRequest) -> CoroutineContext = { request ->
        logger.info("contextProvider() is called for ${request.uri()}")
        if (CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE !in request.attributes()) {
            request.attributes()[CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE] =
                Dispatchers.Unconfined + observationRegistry.asContextElement()
        }
        request.attributes()[CoWebFilter.COROUTINE_CONTEXT_ATTRIBUTE] as CoroutineContext
    }
}