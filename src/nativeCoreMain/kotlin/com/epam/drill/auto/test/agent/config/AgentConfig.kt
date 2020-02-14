package com.epam.drill.auto.test.agent.config

import com.epam.drill.auto.test.agent.*
import com.epam.drill.jvmapi.*
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.*

data class AgentConfig(
    val agentId: String = "",
    val pluginId: String = "",
    val adminHost: String = "",
    val adminPort: String = "",
    val runtimePath: String = "",
    val trace: Boolean = false,
    val debug: Boolean = false,
    val info: Boolean = false,
    val warn: Boolean = false
)

const val WRONG_PARAMS = "Agent parameters are not specified correctly."

fun String?.toAgentParams() = this.asParams().let { params ->
    AgentConfig(
        agentId = params["agentId"] ?: error(WRONG_PARAMS),
        pluginId = params["pluginId"] ?: error(WRONG_PARAMS),
        adminHost = params["adminHost"] ?: error(WRONG_PARAMS),
        adminPort = params["adminPort"] ?: error(WRONG_PARAMS),
        runtimePath = params["runtimePath"] ?: error(WRONG_PARAMS),
        trace = params["trace"]?.toBoolean() ?: false,
        debug = params["debug"]?.toBoolean() ?: false,
        info = params["info"]?.toBoolean() ?: false,
        warn = params["warn"]?.toBoolean() ?: false
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

fun initAgent(additionalClassesPath: String) = memScoped {
    setUnhandledExceptionHook({ thr: Throwable ->
        mainLogger.error { "Unhandled event $thr" }
    }.freeze())
    val alloc = alloc<jvmtiCapabilities>()
    alloc.can_retransform_classes = 1.toUInt()
    alloc.can_retransform_any_class = 1.toUInt()
    alloc.can_generate_native_method_bind_events = 1.toUInt()
    alloc.can_maintain_original_method_order = 1.toUInt()
    AddCapabilities(alloc.ptr)
    val cl = "$additionalClassesPath/runtime-all.jar"
    AddToBootstrapClassLoaderSearch(cl)
    callbackRegister()
}

@CName("jvmtii")
fun jvmtii(): CPointer<jvmtiEnvVar>? {
    return com.epam.drill.jvmapi.jvmtii()
}

@CName("checkEx")
fun checkEx(errCode: jvmtiError, funName: String): jvmtiError {
    return com.epam.drill.jvmapi.checkEx(errCode, funName)
}

@CName("currentEnvs")
fun currentEnvs(): JNIEnvPointer {
    return com.epam.drill.jvmapi.currentEnvs()
}