@file:Suppress("UNUSED_PARAMETER", "UNUSED")

package com.epam.drill.auto.test.agent

import com.epam.drill.auto.test.agent.actions.*
import com.epam.drill.auto.test.agent.config.*
import com.epam.drill.hook.http.*
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*

@CName("Agent_OnLoad")
fun agentOnLoad(vmPointer: CPointer<JavaVMVar>, options: String, reservedPtr: Long): jint = memScoped {
    try {
        val config = options.toAgentParams()
        initAgentGlobals(vmPointer)
        initAgent(config.runtimePath)
        log {
            debugMode = config.debugLog
            terminalOutput = config.terminalOutput
            loggerDirPath = config.loggerPath
            init()
        }
        sessionController {
            agentConfig = config
            startSession()
        }
    } catch (ex: Throwable) {
        logError("Can't load the agent. Reason: ${ex.message}")
    }
    JNI_OK
}

@CName("Agent_OnUnload")
fun agentOnUnload(vmPointer: CPointer<JavaVMVar>) {
    try {
        logInfo("Shutting the agent down")
        removeHttpHook()
        sessionController { stopSession() }
    } catch (ex: Throwable) {
        logError("Failed unloading the agent properly. Ex: ${ex.message}")
    }
}
