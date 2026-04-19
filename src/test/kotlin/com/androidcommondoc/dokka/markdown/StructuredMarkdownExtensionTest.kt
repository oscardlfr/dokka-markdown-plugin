package com.androidcommondoc.dokka.markdown

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StructuredMarkdownExtensionTest {

    private lateinit var ext: StructuredMarkdownExtension

    @BeforeEach
    fun setUp() {
        val project = ProjectBuilder.builder().build()
        ext = project.extensions.create("structuredMarkdown", StructuredMarkdownExtension::class.java)
    }

    @Nested
    inner class ConventionDefaults {

        @Test
        fun `default_layer_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().layer, ext.layer.get())
        }

        @Test
        fun `default_category_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().category, ext.category.get())
        }

        @Test
        fun `default_hubParent_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().hubParent, ext.hubParent.get())
        }

        @Test
        fun `default_targets_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().targets, ext.targets.get())
        }

        @Test
        fun `default_status_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().status, ext.status.get())
        }

        @Test
        fun `default_generatedFrom_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().generatedFrom, ext.generatedFrom.get())
        }

        @Test
        fun `default_schemaVersion_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().schemaVersion, ext.schemaVersion.get())
        }

        @Test
        fun `default_hashFormat_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().hashFormat, ext.hashFormat.get())
        }

        @Test
        fun `default_filenameConvention_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().filenameConvention, ext.filenameConvention.get())
        }

        @Test
        fun `default_slugSeparator_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().slugSeparator, ext.slugSeparator.get())
        }

        @Test
        fun `default_kdocStatePath_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().kdocStatePath, ext.kdocStatePath.get())
        }

        @Test
        fun `default_frontmatterMode_matchesMarkdownPluginConfigDefault`() {
            assertEquals(MarkdownPluginConfig().frontmatterMode, ext.frontmatterMode.get())
        }

        @Test
        fun `default_customFields_isEmpty`() {
            assertEquals(emptyMap<String, String>(), ext.customFields.get())
        }
    }

    @Nested
    inner class Overrides {

        @Test
        fun `set_layer_returnsOverriddenValue`() {
            ext.layer.set("L2")
            assertEquals("L2", ext.layer.get())
        }

        @Test
        fun `set_hashFormat_fullSha256WithPrefix_returnsOverriddenValue`() {
            ext.hashFormat.set(HashFormat.FULL_SHA256_WITH_PREFIX)
            assertEquals(HashFormat.FULL_SHA256_WITH_PREFIX, ext.hashFormat.get())
        }

        @Test
        fun `set_filenameConvention_plain_returnsOverriddenValue`() {
            ext.filenameConvention.set(FilenameConvention.PLAIN)
            assertEquals(FilenameConvention.PLAIN, ext.filenameConvention.get())
        }

        @Test
        fun `set_frontmatterMode_minimal_returnsOverriddenValue`() {
            ext.frontmatterMode.set(FrontmatterMode.MINIMAL)
            assertEquals(FrontmatterMode.MINIMAL, ext.frontmatterMode.get())
        }

        @Test
        fun `set_targets_multipleValues_returnsFullList`() {
            ext.targets.set(listOf("android", "ios", "desktop"))
            assertEquals(listOf("android", "ios", "desktop"), ext.targets.get())
        }

        @Test
        fun `put_customFields_entryIsRetrievable`() {
            ext.customFields.put("team", "platform")
            assertEquals("platform", ext.customFields.get()["team"])
        }
    }
}
