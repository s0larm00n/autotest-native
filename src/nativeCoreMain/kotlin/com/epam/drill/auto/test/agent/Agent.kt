@file:Suppress("UNUSED_PARAMETER", "UNUSED")

package com.epam.drill.auto.test.agent

import com.epam.drill.auto.test.agent.actions.sessionController
import com.epam.drill.auto.test.agent.config.AgentConfig
import com.epam.drill.auto.test.agent.config.toAgentParams
import com.epam.drill.hook.http.removeHttpHook
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value
import kotlin.native.concurrent.freeze

@CName("Agent_OnLoad")
fun agentOnLoad(vmPointer: CPointer<JavaVMVar>, options: String, reservedPtr: Long): jint = memScoped {
    try {
        val config = options.toAgentParams()
        initAgentGlobals(vmPointer, config)

        Logger.logInfo("Initializing agent...")
        setUnhandledExceptionHook({ x: Throwable ->
            println("unhandled event $x")
        }.freeze())
        initAgent(config.runtimePath)
        Logger.logInfo("Agent initialized! Attempt to start a Drill4J test session")
        sessionController {
            agentConfig = config
            startSession()
        }
    } catch (ex: Throwable) {
        Logger.logError("Can't load the agent. Ex: ${ex.message}")
    }
    JNI_OK
}

@CName("Agent_OnUnload")
fun agentOnUnload(vmPointer: CPointer<JavaVMVar>) {
    try {
        Logger.logInfo("Shutting the agent down")
        removeHttpHook()
        sessionController { stopSession() }
    } catch (ex: Throwable) {
        Logger.logError("Failed unloading the agent properly. Ex: ${ex.message}")
    }
}

private fun initAgentGlobals(vmPointer: CPointer<JavaVMVar>, config: AgentConfig) {
    agentSetup(vmPointer.pointed.value)
    saveVmToGlobal(vmPointer)
}
