package io.github.oscardlfr.dokka.markdown

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MarkdownPluginConfigSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(config: MarkdownPluginConfig): String = json.encodeToString(config)

    fun deserialize(jsonString: String): MarkdownPluginConfig {
        require(jsonString.isNotBlank()) { "JSON string must not be blank" }
        try {
            return json.decodeFromString<MarkdownPluginConfig>(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed JSON for MarkdownPluginConfig: ${e.message}", e)
        }
    }
}
