@file:Suppress("UNUSED_PARAMETER", "UNUSED")

package com.epam.drill.auto.test.agent

import com.epam.drill.auto.test.agent.actions.sessionController
import com.epam.drill.auto.test.agent.instrumenting.classFileLoadHookEvent
import com.epam.drill.hook.http.addHttpWriteCallback
import com.epam.drill.hook.http.configureHttpHooks
import com.epam.drill.jvmapi.JNIEnvPointer
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.freeze

@CName("enableJvmtiEventVmDeath")
fun enableJvmtiEventVmDeath(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_DEATH = true
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

@Suppress("UNUSED_PARAMETER")
fun vmDeathEvent(jvmtiEnv: CPointer<jvmtiEnvVar>?, jniEnv: CPointer<JNIEnvVar>?) {
    Logger.logInfo("vmDeathEvent")
}

fun initAgent(additionalClassesPath: String) = memScoped {
    setUnhandledExceptionHook({ x: Throwable ->
        println("unhandled event $x")
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

fun callbackRegister() {
    generateDefaultCallbacks().useContents {
        ClassFileLoadHook = staticCFunction(::classFileLoadHookEvent)
        SetEventCallbacks(this.ptr, sizeOf<jvmtiEventCallbacks>().toInt())
        null
    }
    SetEventCallbacks(gjavaVMGlob?.pointed?.callbackss?.ptr, sizeOf<jvmtiEventCallbacks>().toInt())
    gjavaVMGlob?.pointed?.callbackss?.VMDeath = staticCFunction(::vmDeathEvent)
    enableJvmtiEventVmDeath()
    enableJvmtiEventVmInit()
    enableJvmtiEventClassFileLoadHook()
    enableJvmtiEventNativeMethodBind()
}

@CName("enableJvmtiEventVmInit")
fun enableJvmtiEventVmInit(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_VM_INIT = true
}

@CName("enableJvmtiEventNativeMethodBind")
fun enableJvmtiEventNativeMethodBind(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_NATIVE_METHOD_BIND, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_NATIVE_METHOD_BIND = true
}

@CName("enableJvmtiEventClassFileLoadHook")
fun enableJvmtiEventClassFileLoadHook(thread: jthread? = null) {
    SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, thread)
    gdata?.pointed?.IS_JVMTI_EVENT_CLASS_FILE_LOAD_HOOK = true
}

@CName("jvmtiEventVMInitEvent")
fun jvmtiEventVMInitEvent(env: CPointer<jvmtiEnvVar>?, jniEnv: CPointer<JNIEnvVar>?, thread: jthread?) {
    Logger.logInfo("Init event")
    configureHooks()
}

fun configureHooks() {
    configureHttpHooks()
    addHttpWriteCallback {
        val (lastTestName, sessionId) = sessionController {
            (testName ?: "") to sessionId
        }
        println("Adding hooks: $lastTestName to $sessionId")
        mapOf(
            "drill-test-name" to lastTestName,
            "drill-session-id" to sessionId
        )
    }
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
    Logger.logInfo("Method bind event")
}