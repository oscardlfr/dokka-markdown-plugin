package com.androidcommondoc.dokka.markdown

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerBinding
import org.jetbrains.dokka.gradle.DokkaExtension
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
                val dokka = project.extensions.getByType(DokkaExtension::class.java)
                with(dokka.pluginsConfiguration) {
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
