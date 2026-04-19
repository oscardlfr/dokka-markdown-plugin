package com.androidcommondoc.dokka.markdown

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerBinding
import org.jetbrains.dokka.gradle.engine.plugins.DokkaPluginParametersBaseSpec
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi

@OptIn(InternalDokkaGradlePluginApi::class)
class StructuredMarkdownGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create(
            "structuredMarkdown",
            StructuredMarkdownExtension::class.java
        )
        project.plugins.withId("org.jetbrains.dokka") {
            project.afterEvaluate {
                val dokkaAny = project.extensions.findByName("dokka")
                    ?: run {
                        project.logger.warn("structuredMarkdown: Dokka extension 'dokka' not found — is Dokka plugin applied?")
                        return@afterEvaluate
                    }
                val dokkaExtensions = dokkaAny.javaClass
                    .getMethod("getExtensions")
                    .invoke(dokkaAny) as? ExtensionContainer
                    ?: return@afterEvaluate
                @Suppress("UNCHECKED_CAST")
                val pluginsConfig = dokkaExtensions.findByName("pluginsConfiguration")
                    as? ExtensiblePolymorphicDomainObjectContainer<DokkaPluginParametersBaseSpec>
                    ?: return@afterEvaluate
                with(pluginsConfig) {
                    registerBinding(
                        MarkdownPluginConfiguration::class,
                        MarkdownPluginConfiguration::class
                    )
                    register<MarkdownPluginConfiguration>(
                        "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin"
                    ) {
                        layer.convention(ext.layer)
                        category.convention(ext.category)
                        hubParent.convention(ext.hubParent)
                        targets.convention(ext.targets)
                        status.convention(ext.status)
                        generatedFrom.convention(ext.generatedFrom)
                        schemaVersion.convention(ext.schemaVersion)
                        hashFormat.convention(ext.hashFormat)
                        filenameConvention.convention(ext.filenameConvention)
                        slugSeparator.convention(ext.slugSeparator)
                        kdocStatePath.convention(ext.kdocStatePath)
                        frontmatterMode.convention(ext.frontmatterMode)
                        customFields.convention(ext.customFields)
                    }
                }
            }
        }
    }
}
