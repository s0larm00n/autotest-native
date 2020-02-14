@file:Suppress("UNUSED_PARAMETER")

package com.epam.drill.auto.test.agent.instrumenting

import com.epam.drill.auto.test.agent.*
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*

@CName("jvmtiEventClassFileLoadHookEvent")
fun classFileLoadHookEvent(
    jvmtiEnv: CPointer<jvmtiEnvVar>?,
    jniEnv: CPointer<JNIEnvVar>?,
    classBeingRedefined: jclass?,
    loader: jobject?,
    kClassName: CPointer<ByteVar>?,
    protection_domain: jobject?,
    classDataLen: jint,
    classData: CPointer<UByteVar>?,
    newClassDataLen: CPointer<jintVar>?,
    newData: CPointer<CPointerVar<UByteVar>>?
) {
    val className = kClassName?.toKString()
    if (notSuitableClass(loader, protection_domain, className, classData)) return
    mainLogger.debug { "Scanning class: $className" }
    val instrumentedBytes =
        transform(loader, className!!) ?: return
    val instrumentedSize = instrumentedBytes.size
    mainLogger.debug { "Applying instrumenting (old: $classDataLen to new: $instrumentedSize)" }
    Allocate(instrumentedSize.toLong(), newData)
    val newBytes = newData!!.pointed.value!!
    instrumentedBytes.forEachIndexed { index, byte ->
        newBytes[index] = byte.toUByte()
    }
    newClassDataLen!!.pointed.value = instrumentedSize
    mainLogger.info { "Successfully instrumented class $className" }
}

private fun notSuitableClass(
    loader: jobject?,
    protection_domain: jobject?,
    className: String?,
    classData: CPointer<UByteVar>?
): Boolean =
    loader == null || protection_domain == null || className == null || classData == null


val transformerClass: jclass
    get() = FindClass("com/epam/drill/auto/test/agent/AgentClassTransformer")
        ?: error("No AgentClassTransformer class!")

fun transform(classLoader: jobject?, className: String): ByteArray? {
    val transform: jmethodID? = GetStaticMethodID(
        transformerClass,
        "transform",
        "(Ljava/lang/String;Ljava/lang/ClassLoader;)[B"
    )
    return CallStaticObjectMethod(transformerClass, transform, NewStringUTF(className), classLoader).toByteArray()
}

fun jobject?.toByteArray(): ByteArray? = this?.run {
    val size = GetArrayLength(this)
    val getByteArrayElements: CPointer<ByteVarOf<jbyte>>? = GetByteArrayElements(this, null)
    return@run getByteArrayElements?.readBytes(size)
}
