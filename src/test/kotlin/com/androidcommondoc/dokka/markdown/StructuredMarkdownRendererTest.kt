package com.androidcommondoc.dokka.markdown

import org.jetbrains.dokka.DokkaConfiguration
import org.jetbrains.dokka.DokkaConfigurationImpl
import org.jetbrains.dokka.PluginConfigurationImpl
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.plugability.DokkaPlugin
import org.jetbrains.dokka.plugability.ExtensionPoint
import org.jetbrains.dokka.utilities.DokkaLogger
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun fakeContext(
    outputDir: File,
    pluginConfigs: List<PluginConfigurationImpl> = emptyList(),
): DokkaContext {
    val config = DokkaConfigurationImpl(
        outputDir = outputDir,
        pluginsConfiguration = pluginConfigs,
    )
    return object : DokkaContext {
        override val configuration: DokkaConfiguration = config
        override val logger: DokkaLogger = object : DokkaLogger {
            override var warningsCount: Int = 0
            override var errorsCount: Int = 0
            override fun debug(message: String) {}
            override fun info(message: String) {}
            override fun progress(message: String) {}
            override fun warn(message: String) {}
            override fun error(message: String) {}
        }
        override val unusedPoints: Collection<ExtensionPoint<*>> = emptyList()
        override fun <T : DokkaPlugin> plugin(kclass: KClass<T>): T? = null
        override fun <T, E> get(point: E): List<T> where T : Any, E : ExtensionPoint<T> = emptyList()
        override fun <T, E> single(point: E): T where T : Any, E : ExtensionPoint<T> =
            throw UnsupportedOperationException()
    }
}

class StructuredMarkdownRendererTest {

    @TempDir
    lateinit var tempDir: File

    @Nested
    inner class ConfigResolution {

        @Test
        fun `config_emptyPluginsConfiguration_resolvesToDefaults`() {
            val ctx = fakeContext(outputDir = tempDir)
            val renderer = StructuredMarkdownRenderer(ctx)
            val config = renderer.config
            assertEquals("L1", config.layer)
            assertEquals("api", config.category)
            assertEquals("api-hub", config.hubParent)
            assertEquals(FrontmatterMode.STRUCTURED, config.frontmatterMode)
            assertEquals(HashFormat.COMPACT_12_HEX, config.hashFormat)
            assertEquals(FilenameConvention.LEADING_DASH, config.filenameConvention)
        }

        @Test
        fun `config_matchingFqPluginName_deserializesCustomValues`() {
            val customConfig = MarkdownPluginConfig(layer = "L2", category = "domain")
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals("L2", renderer.config.layer)
            assertEquals("domain", renderer.config.category)
        }

        @Test
        fun `config_nonMatchingFqPluginName_resolvesToDefaults`() {
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "some.other.plugin.FQN",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(layer = "WRONG")),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals("L1", renderer.config.layer)
        }

        @Test
        fun `config_hashFormatFullSha256_deserialized`() {
            val customConfig = MarkdownPluginConfig(hashFormat = HashFormat.FULL_SHA256_WITH_PREFIX)
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(HashFormat.FULL_SHA256_WITH_PREFIX, renderer.config.hashFormat)
        }

        @Test
        fun `config_filenameConventionPlain_deserialized`() {
            val customConfig = MarkdownPluginConfig(filenameConvention = FilenameConvention.PLAIN)
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(FilenameConvention.PLAIN, renderer.config.filenameConvention)
        }

        @Test
        fun `config_frontmatterModeMinimal_deserialized`() {
            val customConfig = MarkdownPluginConfig(frontmatterMode = FrontmatterMode.MINIMAL)
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(FrontmatterMode.MINIMAL, renderer.config.frontmatterMode)
        }

        @Test
        fun `config_customKdocStatePath_deserialized`() {
            val customConfig = MarkdownPluginConfig(kdocStatePath = "custom/path/kdoc.json")
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals("custom/path/kdoc.json", renderer.config.kdocStatePath)
        }

        @Test
        fun `config_customTargets_deserialized`() {
            val customConfig = MarkdownPluginConfig(targets = listOf("android", "desktop"))
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(listOf("android", "desktop"), renderer.config.targets)
        }

        @Test
        fun `config_multiplePluginEntries_picksMatchingFqn`() {
            val wrongEntry = PluginConfigurationImpl(
                fqPluginName = "some.other.Plugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(layer = "WRONG")),
            )
            val rightEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(layer = "L0")),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(wrongEntry, rightEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals("L0", renderer.config.layer)
        }

        @Test
        fun `config_frontmatterModeMinimal_wiredThrough`() {
            val customConfig = MarkdownPluginConfig(frontmatterMode = FrontmatterMode.MINIMAL)
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(FrontmatterMode.MINIMAL, renderer.config.frontmatterMode)
        }

        @Test
        fun `config_frontmatterModeNone_wiredThrough`() {
            val customConfig = MarkdownPluginConfig(frontmatterMode = FrontmatterMode.NONE)
            val pluginEntry = PluginConfigurationImpl(
                fqPluginName = "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin",
                serializationFormat = DokkaConfiguration.SerializationFormat.JSON,
                values = MarkdownPluginConfigSerializer.serialize(customConfig),
            )
            val ctx = fakeContext(outputDir = tempDir, pluginConfigs = listOf(pluginEntry))
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(FrontmatterMode.NONE, renderer.config.frontmatterMode)
        }

        @Test
        fun `config_emptyPluginsConfiguration_frontmatterModeDefaultsToStructured`() {
            val ctx = fakeContext(outputDir = tempDir)
            val renderer = StructuredMarkdownRenderer(ctx)
            assertEquals(FrontmatterMode.STRUCTURED, renderer.config.frontmatterMode)
        }
    }

    @Nested
    inner class HashFormatDispatch {

        @Test
        fun `hashFormat_compact12Hex_produces12CharHex`() {
            val body = "# MyClass\n"
            val result = when (HashFormat.COMPACT_12_HEX) {
                HashFormat.COMPACT_12_HEX -> ContentHasher.hash(body)
                HashFormat.FULL_SHA256_WITH_PREFIX -> ContentHasher.hashFull(body)
                HashFormat.NONE -> ""
            }
            assertTrue(result.matches(Regex("[0-9a-f]{12}")))
        }

        @Test
        fun `hashFormat_fullSha256_produces71CharPrefixed`() {
            val body = "# MyClass\n"
            val result = when (HashFormat.FULL_SHA256_WITH_PREFIX) {
                HashFormat.COMPACT_12_HEX -> ContentHasher.hash(body)
                HashFormat.FULL_SHA256_WITH_PREFIX -> ContentHasher.hashFull(body)
                HashFormat.NONE -> ""
            }
            assertEquals(71, result.length)
            assertTrue(result.startsWith("sha256:"))
        }

        @Test
        fun `hashFormat_none_producesEmptyString`() {
            val body = "# MyClass\n"
            val result = when (HashFormat.NONE) {
                HashFormat.COMPACT_12_HEX -> ContentHasher.hash(body)
                HashFormat.FULL_SHA256_WITH_PREFIX -> ContentHasher.hashFull(body)
                HashFormat.NONE -> ""
            }
            assertEquals("", result)
        }
    }

    @Nested
    inner class FilenameConventionDispatch {

        @Test
        fun `filenameConvention_leadingDash_matchesFileBasename`() {
            val className = "TrackRepository"
            assertEquals(
                SlugDeriver.fileBasename(className),
                SlugDeriver.fileBasenameFor(className, FilenameConvention.LEADING_DASH),
            )
        }

        @Test
        fun `filenameConvention_plain_noLeadingDash`() {
            val result = SlugDeriver.fileBasenameFor("TrackRepository", FilenameConvention.PLAIN)
            assertFalse(result.startsWith("-"))
            assertEquals("track-repository", result)
        }
    }
}
