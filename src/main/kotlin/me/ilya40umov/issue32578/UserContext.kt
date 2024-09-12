package me.ilya40umov.issue32578

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class UserContext(
    val attributes: Map<String, Any>,
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<UserContext>
}

fun UserContext?.username() = this?.attributes?.get("username") ?: "n/a"