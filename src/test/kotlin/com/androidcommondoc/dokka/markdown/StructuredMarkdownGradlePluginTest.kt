package com.androidcommondoc.dokka.markdown

import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(InternalDokkaGradlePluginApi::class)
class StructuredMarkdownGradlePluginTest {

    @Test
    fun `apply_withoutDokka_noException_extensionRegistered`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.androidcommondoc.dokka-markdown-config")

        val ext = project.extensions.findByType(StructuredMarkdownExtension::class.java)
        assertNotNull(ext, "StructuredMarkdownExtension must be registered after plugin apply")
        assertEquals("L1", ext.layer.get())
    }

    @Test
    fun `apply_withoutDokka_dokkaExtensionAbsent_noPluginsConfigurationEntry`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.androidcommondoc.dokka-markdown-config")

        val dokkaExt = project.extensions.findByType(DokkaExtension::class.java)
        assertNull(dokkaExt, "DokkaExtension must not be present when Dokka plugin is absent")
        assertNotNull(project.extensions.findByType(StructuredMarkdownExtension::class.java))
    }

    @Test
    fun `apply_twice_isIdempotent_noException`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.androidcommondoc.dokka-markdown-config")
        project.plugins.apply("com.androidcommondoc.dokka-markdown-config")

        val ext = project.extensions.findByType(StructuredMarkdownExtension::class.java)
        assertNotNull(ext, "StructuredMarkdownExtension must be present after double apply")
        assertEquals("L1", ext.layer.get())
    }
}
