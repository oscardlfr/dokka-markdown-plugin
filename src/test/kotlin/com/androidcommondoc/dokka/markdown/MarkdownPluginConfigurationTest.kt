package io.github.oscardlfr.dokka.markdown

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(InternalDokkaGradlePluginApi::class)
class MarkdownPluginConfigurationTest {

    private lateinit var config: MarkdownPluginConfiguration

    @BeforeEach
    fun setUp() {
        val project = ProjectBuilder.builder().build()
        config = project.objects.newInstance(MarkdownPluginConfiguration::class.java, "test-plugin")
        val defaults = MarkdownPluginConfig()
        config.layer.set(defaults.layer)
        config.category.set(defaults.category)
        config.hubParent.set(defaults.hubParent)
        config.targets.set(defaults.targets)
        config.status.set(defaults.status)
        config.generatedFrom.set(defaults.generatedFrom)
        config.schemaVersion.set(defaults.schemaVersion)
        config.hashFormat.set(defaults.hashFormat)
        config.filenameConvention.set(defaults.filenameConvention)
        config.slugSeparator.set(defaults.slugSeparator)
        config.kdocStatePath.set(defaults.kdocStatePath)
        config.frontmatterMode.set(defaults.frontmatterMode)
        config.customFields.set(defaults.customFields)
    }

    @Test
    fun `pluginFqn_isStructuredMarkdownPluginClassFqn`() {
        assertEquals("io.github.oscardlfr.dokka.markdown.StructuredMarkdownPlugin", config.pluginFqn)
    }

    @Nested
    inner class JsonEncode {

        @Test
        fun `jsonEncode_allDefaults_containsExpectedFieldValues`() {
            val json = config.jsonEncode()
            assert(json.contains("\"layer\":\"L1\"")) { "Expected layer:L1 in: $json" }
            assert(json.contains("\"category\":\"api\"")) { "Expected category:api in: $json" }
            assert(json.contains("\"hubParent\":\"api-hub\"")) { "Expected hubParent:api-hub in: $json" }
            assert(json.contains("\"hashFormat\":\"COMPACT_12_HEX\"")) { "Expected COMPACT_12_HEX in: $json" }
            assert(json.contains("\"frontmatterMode\":\"STRUCTURED\"")) { "Expected STRUCTURED in: $json" }
        }

        @Test
        fun `jsonEncode_nonDefaultValues_containsOverriddenValues`() {
            config.layer.set("L0")
            config.category.set("guide")
            config.hubParent.set("guide-hub")
            config.hashFormat.set(HashFormat.FULL_SHA256_WITH_PREFIX)
            config.filenameConvention.set(FilenameConvention.PLAIN)
            config.frontmatterMode.set(FrontmatterMode.MINIMAL)
            config.schemaVersion.set(2)

            val json = config.jsonEncode()
            assert(json.contains("\"layer\":\"L0\"")) { "Expected layer:L0 in: $json" }
            assert(json.contains("\"category\":\"guide\"")) { "Expected category:guide in: $json" }
            assert(json.contains("FULL_SHA256_WITH_PREFIX")) { "Expected FULL_SHA256_WITH_PREFIX in: $json" }
            assert(json.contains("PLAIN")) { "Expected PLAIN in: $json" }
            assert(json.contains("MINIMAL")) { "Expected MINIMAL in: $json" }
            assert(json.contains("\"schemaVersion\":2")) { "Expected schemaVersion:2 in: $json" }
        }

        @Test
        fun `jsonEncode_emptyLayer_throwsIllegalArgumentException`() {
            config.layer.set("")
            assertFailsWith<IllegalArgumentException> { config.jsonEncode() }
        }

        @Test
        fun `jsonEncode_roundTripsViaSerializer`() {
            config.layer.set("L2")
            config.hashFormat.set(HashFormat.FULL_SHA256_WITH_PREFIX)
            val json = config.jsonEncode()
            val restored = MarkdownPluginConfigSerializer.deserialize(json)
            assertEquals("L2", restored.layer)
            assertEquals(HashFormat.FULL_SHA256_WITH_PREFIX, restored.hashFormat)
        }
    }
}
