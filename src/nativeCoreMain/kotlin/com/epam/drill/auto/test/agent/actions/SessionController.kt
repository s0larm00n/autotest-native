package com.epam.drill.auto.test.agent.actions

import com.epam.drill.auto.test.agent.*
import com.epam.drill.auto.test.agent.config.*
import com.epam.drill.auto.test.agent.http.*
import com.epam.drill.jvmapi.gen.*
import kotlinx.cinterop.*
import kotlin.native.concurrent.*

class SessionController {

    var testName: String = ""
    var sessionId: String = ""
    lateinit var agentConfig: AgentConfig

    fun startSession() {
        logInfo("Attempting to start a Drill4J test session...")
        val payload = StartSession.serializer() stringify StartSession()
        val response = dispatchAction(payload)
        logInfo("Received response: ${response.raw}")
        val startSessionResponse = StartSessionResponse.serializer() parse response.body
        sessionId = startSessionResponse.payload.sessionId
        logInfo("Started a test session with ID $sessionId")
    }

    fun stopSession() {
        logInfo("Attempting to stop a Drill4J test session...")
        val payload = StopSession.serializer() stringify stopAction(sessionId)
        val response = dispatchAction(payload)
        logInfo("Received response: ${response.raw}")
        logInfo("Stopped a test session with ID $sessionId")
    }

    private fun dispatchAction(payload: String): HttpResponse {
        val dispatchActionPath = "/api/agents/${agentConfig.agentId}/${agentConfig.pluginId}/dispatch-action"
        val token = getToken()
        return Sender.post(
            agentConfig.adminHost,
            agentConfig.adminPort,
            dispatchActionPath,
            mapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            ),
            payload
        )
    }

    private fun getToken(): String = Sender.post(
        agentConfig.adminHost,
        agentConfig.adminPort,
        "/api/login"
    ).headers["Authorization"] ?: error("No token received during login")

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
    val testNameFromJava: String =
        env?.pointed?.pointed?.GetStringUTFChars?.invoke(env, inJNIStr, null)?.toKString() ?: ""
    println(testNameFromJava)
    sessionController { testName = testNameFromJava }
}
