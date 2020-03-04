import org.apache.tools.ant.taskdefs.condition.Os.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*

plugins {
    kotlin("multiplatform") version "1.3.60"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.60"
}

buildscript {
    val kotlinVersion = "1.3.60"
    repositories { jcenter() }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}

val presetName = when {
    isFamily(FAMILY_MAC) -> "macosX64"
    isFamily(FAMILY_UNIX) -> "linuxX64"
    isFamily(FAMILY_WINDOWS) -> "mingwX64"
    else -> throw RuntimeException("Target ${System.getProperty("os.name")} is not supported")
}

val drillJvmApiLibVersion = "0.4.0"
val serializationRuntimeVersion = "0.14.0"
val drillLogger = "0.1.0"
val drillHookVersion = "1.0.0"

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "https://oss.jfrog.org/artifactory/list/oss-release-local")
}

kotlin {
    targets {
        currentTarget("nativeCore") {
            compilations.getByName("main") {
                binaries {
                    sharedLib("test", setOf(DEBUG)) {
                        if (presetName == "mingwX64") linkerOpts("-lpsapi", "-lwsock32", "-lws2_32", "-lmswsock")
                    }
                }
            }
        }
    }

    sourceSets {
        named("nativeCoreMain") {
            dependencies {
                implementation("com.epam.drill:jvmapi-native:$drillJvmApiLibVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationRuntimeVersion")
                implementation("com.epam.drill.hook:platform:$drillHookVersion")
                implementation("com.epam.drill.logger:logger:$drillLogger")
            }
            kotlin.srcDir("src/${presetName}Main/kotlin")
        }
        named("nativeCoreTest") {
            dependencies {
                implementation("com.epam.drill.hook:platform:$drillHookVersion")
            }
        }
    }
}

fun KotlinMultiplatformExtension.currentTarget(
    name: String,
    config: KotlinNativeTarget.() -> Unit = {}
): KotlinNativeTarget {
    val createdTarget =
        (presets.getByName(presetName) as KotlinNativeTargetWithTestsPreset).createTarget(
            name
        )
    targets.add(createdTarget)
    config(createdTarget)
    return createdTarget
}

tasks.getByName("build") {
    dependsOn(":runtime:shadowJar")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
}
