import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

dependencies{
    implementation(kotlin("stdlib-jdk8"))
    api("org.javassist:javassist:$javassistVersion")
    testImplementation("com.google.code.gson:gson:$gsonVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation(kotlin("stdlib-jdk8"))
}

tasks.named<Test>("test") {
    val suffix =
        if(org.apache.tools.ant.taskdefs.condition.Os.isFamily(org.apache.tools.ant.taskdefs.condition.Os.FAMILY_WINDOWS)) "dll" else "so"
    val agentPath = "${rootDir.absolutePath}/build/bin/nativeCore/testDebugShared/test.$suffix"
    val runtimePath = "${rootDir.absolutePath}/runtime/build/libs"
    useJUnitPlatform()
    systemProperty("petclinic.url", "localhost:8080")
    jvmArgs = listOf(
        "-agentpath:$agentPath=" +
                "runtimePath=$runtimePath," +
                "adminHost=localhost," +
                "adminPort=8090," +
                "agentId=Petclinic," +
                "pluginId=test-to-code-mapping," +
                "trace=false," +
                "debug=true," +
                "info=true," +
                "warn=true"
    )
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}