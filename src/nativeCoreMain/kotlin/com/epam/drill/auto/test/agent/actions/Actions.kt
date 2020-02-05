package com.epam.drill.auto.test.agent.actions

import kotlinx.serialization.Serializable

enum class Actions {
    START,
    STOP
}

@Serializable
data class StartSession(val type: String = Actions.START.name, val payload: StartPayload = StartPayload())

@Serializable
data class StartPayload(val testType: String = "AUTO", val sessionId: String = "")

@Serializable
data class StartSessionResponse(val type: String, val payload: StartResponsePayload)

@Serializable
data class StartResponsePayload(val sessionId: String, val startPayload: StartPayload)

@Serializable
data class StopSession(val type: String = Actions.STOP.name, val payload: StopPayload)

@Serializable
data class StopPayload(val sessionId: String)
