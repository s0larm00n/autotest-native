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
    api("org.javassist:javassist:$javassistVersion")
    testImplementation("com.google.code.gson:gson:$gsonVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("petclinic.url", "localhost:8080")
    jvmArgs = listOf(
        "-agentpath:C:\\Users\\Kristina_Smirnova\\Documents\\drill-repos\\auto\\build\\bin\\nativeCore\\testDebugShared\\test.dll=" +
                "runtimePath=C:\\Users\\Kristina_Smirnova\\Documents\\drill-repos\\auto\\runtime\\build\\libs," +
                "adminHost=localhost," +
                "adminPort=8090," +
                "agentId=Petclinic," +
                "pluginId=test-to-code-mapping," +
                "debugLog=true"
    )
}
