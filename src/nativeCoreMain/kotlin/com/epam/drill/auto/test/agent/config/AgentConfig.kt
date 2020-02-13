package com.epam.drill.auto.test.agent.config

import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*

data class AgentConfig(
    val agentId: String,
    val pluginId: String,
    val adminHost: String,
    val adminPort: String,
    val runtimePath: String,
    val debugLog: Boolean,
    val terminalOutput: Boolean,
    val loggerPath: String?
)

const val WRONG_PARAMS = "Agent parameters are not specified correctly."

fun String?.toAgentParams() = this.asParams().let { params ->
    AgentConfig(
        agentId = params["agentId"] ?: error(WRONG_PARAMS),
        pluginId = params["pluginId"] ?: error(WRONG_PARAMS),
        adminHost = params["adminHost"] ?: error(WRONG_PARAMS),
        adminPort = params["adminPort"] ?: error(WRONG_PARAMS),
        runtimePath = params["runtimePath"] ?: error(WRONG_PARAMS),
        debugLog = params["debugLog"]?.toBoolean() ?: false,
        terminalOutput = params["terminalOutput"]?.toBoolean() ?: false,
        loggerPath = params["loggerPath"]
    )
}

fun String?.asParams(): Map<String, String> = try {
    this?.split(",")?.associate {
        val (key, value) = it.split("=")
        key to value
    } ?: emptyMap()
} catch (parseException: Exception) {
    throw IllegalArgumentException(WRONG_PARAMS)
}

fun initAgentGlobals(vmPointer: CPointer<JavaVMVar>) {
    agentSetup(vmPointer.pointed.value)
    saveVmToGlobal(vmPointer)
}

