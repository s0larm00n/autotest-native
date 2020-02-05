package com.epam.drill.auto.test.agent.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val json = Json(JsonConfiguration.Stable)

infix fun <T> KSerializer<T>.parse(rawData: String) = json.parse(this, rawData)

infix fun <T> KSerializer<T>.stringify(rawData: T) = json.stringify(this, rawData)
