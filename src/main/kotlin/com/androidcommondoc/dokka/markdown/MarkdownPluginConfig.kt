package io.github.oscardlfr.dokka.markdown

import kotlinx.serialization.Serializable

@Serializable
data class MarkdownPluginConfig(
    val layer: String = "L1",
    val category: String = "api",
    val hubParent: String = "api-hub",
    val targets: List<String> = listOf("all"),
    val status: String = "active",
    val generatedFrom: String = "dokka",
    val schemaVersion: Int = 1,
    val hashFormat: HashFormat = HashFormat.COMPACT_12_HEX,
    val filenameConvention: FilenameConvention = FilenameConvention.LEADING_DASH,
    val slugSeparator: String = "--",
    val kdocStatePath: String = "",
    val frontmatterMode: FrontmatterMode = FrontmatterMode.STRUCTURED,
    val customFields: Map<String, String> = emptyMap(),
) {
    fun validate() {
        require(layer.isNotBlank()) { "layer must not be blank" }
        require(category.isNotBlank()) { "category must not be blank" }
        require(hubParent.isNotBlank()) { "hubParent must not be blank" }
        require(schemaVersion > 0) { "schemaVersion must be positive, was $schemaVersion" }
        if (frontmatterMode == FrontmatterMode.NONE && customFields.isNotEmpty()) {
            throw IllegalArgumentException(
                "customFields cannot be set when frontmatterMode is NONE — no frontmatter block is emitted"
            )
        }
    }
}
