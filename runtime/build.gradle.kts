import org.apache.tools.ant.taskdefs.condition.*
import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
    kotlin("jvm")
    java
}

val javassistVersion = "3.18.1-GA"
val jupiterVersion = "5.4.2"
val gsonVersion = "2.8.5"
val restAssuredVersion = "4.0.0"

group = "com.epam"
version = ""

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api("org.javassist:javassist:$javassistVersion")
    testImplementation("com.google.code.gson:gson:$gsonVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation(kotlin("stdlib-jdk8"))
}

tasks.named<Test>("test") {
    val suffix = if (Os.isFamily(Os.FAMILY_WINDOWS)) "dll" else "so"
    val agentPath = "${rootDir.absolutePath}/build/bin/nativeCore/testDebugShared/test.$suffix"
    val runtimePath = "${rootDir.absolutePath}/runtime/build/libs"
    useJUnitPlatform()
    jvmArgs = listOf(
        "-agentpath:$agentPath=" +
                "runtimePath=$runtimePath," +
                "adminHost=ecse0050029e.epam.com," +
                "adminPort=8090," +
                "agentId=petclinic-standalone," +
                "pluginId=test-to-code-mapping," +
                //"serviceGroupId=petclinic-services" +
                "trace=false," +
                "debug=true," +
                "info=true," +
                "warn=true"
    )
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}