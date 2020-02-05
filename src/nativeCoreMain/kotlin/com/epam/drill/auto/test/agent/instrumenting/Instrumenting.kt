@file:Suppress("UNUSED_PARAMETER")

package com.epam.drill.auto.test.agent.instrumenting

import com.epam.drill.auto.test.agent.Logger
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*

val transformerClass: jclass
    get() = FindClass("com/epam/drill/auto/test/agent/AgentClassTransformer")
        ?: error("No AgentClassTransformer class!")

val byteArrayClass: jclass
    get() = FindClass("com/epam/drill/auto/test/agent/JByteArray") ?: error("No AgentClassTransformer class!")

fun transform(classLoader: jobject?, className: String): ByteArray? {
    val transform: jmethodID? = GetStaticMethodID(
        transformerClass,
        "transform",
        "(Ljava/lang/String;Ljava/lang/ClassLoader;)Lcom/epam/drill/auto/test/agent/JByteArray;"
    )
    return CallStaticObjectMethod(transformerClass, transform, NewStringUTF(className), classLoader).toByteArray()

}

fun jobject?.toByteArray(): ByteArray? = this?.run {
    val bytesField = GetFieldID(byteArrayClass, "bytes", "[B")
    val sizeField = GetFieldID(byteArrayClass, "size", "I")
    val bytes: jbyteArray? = GetObjectField(this, bytesField)
    val size = GetIntField(this, sizeField)
    val byteArray: CPointer<jbyteVar> = GetByteArrayElements(bytes, null)!!
    val result = ByteArray(size)
    for (i in 0 until size) {
        result[i] = byteArray[i]
    }
    return result
}

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
    val className = kClassName?.toKString() ?: "NONAME"
    if (className.startsWith("my/test/pack") || className.startsWith("my/awesome/pack")) {  //if (className.startsWith("my/awesome/pack")) {
        Logger.logInfo("Loading class: $className")
        val instrumentedBytes =
            transform(loader, className)
        if (instrumentedBytes != null) {
            val instrumentedSize = instrumentedBytes.size
            Allocate(instrumentedSize.toLong(), newData)
            val newBytes = newData!!.pointed.value!!
            instrumentedBytes.forEachIndexed { index, byte ->
                newBytes[index] = byte.toUByte()
            }
            newClassDataLen!!.pointed.value = instrumentedSize
            Logger.logInfo("Successfully instrumented class $className")
        }
    }
}
