package com.epam.drill.auto.test.agent.actions

import com.epam.drill.auto.test.agent.Logger
import com.epam.drill.auto.test.agent.config.AgentConfig
import com.epam.drill.auto.test.agent.config.parse
import com.epam.drill.auto.test.agent.config.stringify
import com.epam.drill.auto.test.agent.http.Sender
import com.epam.drill.jvmapi.gen.JNIEnvVar
import com.epam.drill.jvmapi.gen.jobject
import com.epam.drill.jvmapi.gen.jstring
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

class SessionController {

    var testName: String? = null
    lateinit var sessionId: String
    lateinit var agentConfig: AgentConfig

    fun startSession() {
        val dispatchActionPath = "/api/agents/${agentConfig.agentId}/${agentConfig.pluginId}/dispatch-action"
        val startSession = StartSession.serializer() stringify StartSession()
        val token = getToken()
        val response = Sender.post(
            agentConfig.adminHost,
            agentConfig.adminPort,
            dispatchActionPath,
            mapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            ),
            startSession
        )
        Logger.logInfo("Recieved response: ${response.raw}")
        val startSessionResponse = StartSessionResponse.serializer() parse response.body
        sessionId = startSessionResponse.payload.sessionId
        Logger.logInfo("Started a test session with ID $sessionId")
    }

    fun stopSession() {
        val dispatchActionPath = "/api/agents/${agentConfig.agentId}/${agentConfig.pluginId}/dispatch-action"
        val stopSession = StopSession.serializer() stringify stopAction(sessionId)
        val token = getToken()
        val response = Sender.post(
            agentConfig.adminHost,
            agentConfig.adminPort,
            dispatchActionPath,
            mapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            ),
            stopSession
        )
        Logger.logInfo("Recieved response: ${response.raw}")
        Logger.logInfo("Stopped a test session with ID $sessionId")
    }

    private fun getToken(): String = Sender.post(
        agentConfig.adminHost,
        agentConfig.adminPort,
        "/api/login"
    ).headers["Authorization"] ?: error("No token recieved during login")

    private fun stopAction(sessionId: String) = StopSession(
        payload = StopPayload(sessionId)
    )

}

inline fun <reified T> sessionController(noinline what: SessionController.() -> T) =
    sessionControllerWorker.execute(TransferMode.UNSAFE, { what }) {
        it(sessionController)
    }.result

@SharedImmutable
val sessionControllerWorker = Worker.start(true)

@ThreadLocal
val sessionController = SessionController()

@Suppress("UNUSED", "UNUSED_PARAMETER")
@CName("Java_com_epam_drill_auto_test_agent_GlobalSpy_memorizeTestName")
fun memorizeTestName(env: CPointer<JNIEnvVar>?, thisObj: jobject, inJNIStr: jstring) {
    val testNameFromJava = env?.pointed?.pointed?.GetStringUTFChars?.invoke(env, inJNIStr, null)?.toKString()
    println(testNameFromJava)
    if (testNameFromJava != null) {
        sessionController { testName = testNameFromJava }
    }
}