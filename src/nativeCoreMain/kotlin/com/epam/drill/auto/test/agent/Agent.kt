@file:Suppress("UNUSED_PARAMETER", "UNUSED")

package com.epam.drill.auto.test.agent

import com.epam.drill.auto.test.agent.actions.*
import com.epam.drill.auto.test.agent.config.*
import com.epam.drill.hook.http.*
import com.epam.drill.jvmapi.gen.*
import com.epam.drill.logger.*
import kotlinx.cinterop.*
import mu.*
import kotlin.native.concurrent.*

@SharedImmutable
val mainLogger = KotlinLogging.logger("AutoTestAgentLogger")

@CName("Agent_OnLoad")
fun agentOnLoad(vmPointer: CPointer<JavaVMVar>, options: String, reservedPtr: Long): jint = memScoped {
    try {
        val config = options.toAgentParams()
        initAgentGlobals(vmPointer)
        initAgent(config.runtimePath)
        logConfig.value = LoggerConfig(config.trace, config.debug, config.info, config.warn).freeze()
        sessionController.agentConfig.value = config.freeze()
        sessionController.startSession()
    } catch (ex: Throwable) {
        mainLogger.error { "Can't load the agent. Reason: ${ex.message}" }
    }
    JNI_OK
}

@CName("Agent_OnUnload")
fun agentOnUnload(vmPointer: CPointer<JavaVMVar>) {
    try {
        mainLogger.info { "Shutting the agent down" }
        removeHttpHook()
        sessionController.stopSession()
    } catch (ex: Throwable) {
        mainLogger.error { "Failed unloading the agent properly. Ex: ${ex.message}" }
    }
}
