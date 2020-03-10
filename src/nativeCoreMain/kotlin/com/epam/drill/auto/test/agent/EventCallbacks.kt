@file:Suppress("UNUSED_PARAMETER", "UNUSED")

package com.epam.drill.auto.test.agent

import com.epam.drill.auto.test.agent.actions.*
import com.epam.drill.auto.test.agent.instrumenting.*
import com.epam.drill.interceptor.*
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.*

@CName("enableJvmtiEventVmDeath")
fun enableJvmtiEventVmDeath(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, thread)
}

@CName("enableJvmtiEventVmInit")
fun enableJvmtiEventVmInit(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, thread)
}

@CName("enableJvmtiEventClassFileLoadHook")
fun enableJvmtiEventClassFileLoadHook(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, thread)
}

@Suppress("UNUSED_PARAMETER")
fun vmDeathEvent(jvmtiEnv: CPointer<jvmtiEnvVar>?, jniEnv: CPointer<JNIEnvVar>?) {
    mainLogger.debug { "vmDeathEvent" }
}

fun callbackRegister() = memScoped {
    val eventCallbacks = alloc<jvmtiEventCallbacks>()
    eventCallbacks.VMInit = staticCFunction(::jvmtiEventVMInitEvent)
    eventCallbacks.VMDeath = staticCFunction(::vmDeathEvent)
    eventCallbacks.ClassFileLoadHook = staticCFunction(::classFileLoadHookEvent)
    SetEventCallbacks(eventCallbacks.ptr, sizeOf<jvmtiEventCallbacks>().toInt())
    enableJvmtiEventVmInit()
    enableJvmtiEventVmDeath()
}

@CName("jvmtiEventVMInitEvent")
fun jvmtiEventVMInitEvent(env: CPointer<jvmtiEnvVar>?, jniEnv: CPointer<JNIEnvVar>?, thread: jthread?) {
    mainLogger.debug { "Init event" }
    initRuntimeIfNeeded()
    initializeStrategyManager(sessionController.agentConfig.value.rawFrameworkPlugins)
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, null)
    configureHooks()
}

fun configureHooks() {
    configureHttpInterceptor()
    mainLogger.debug { "Interceptor configured" }
    headersForInject.value = {
        mainLogger.debug { "Injecting headers" }
        val lastTestName = sessionController.testName.value
        val sessionId = sessionController.sessionId.value
        mainLogger.debug { "Adding headers: $lastTestName to $sessionId" }
        mapOf(
            "drill-test-name" to lastTestName,
            "drill-session-id" to sessionId
        )
    }.freeze()
}

@CName("jvmtiEventNativeMethodBindEvent")
fun nativeMethodBind(
    jvmtiEnv: jvmtiEnv,
    jniEnv: JNIEnv,
    thread: jthread,
    method: jmethodID,
    address: COpaquePointer,
    newAddressPtr: CPointer<COpaquePointerVar>
) {
    mainLogger.debug { "Method bind event" }
}