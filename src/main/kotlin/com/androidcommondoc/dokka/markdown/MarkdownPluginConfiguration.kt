package com.androidcommondoc.dokka.markdown

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.jetbrains.dokka.gradle.engine.plugins.DokkaPluginParametersBaseSpec
import org.jetbrains.dokka.gradle.internal.InternalDokkaGradlePluginApi
import javax.inject.Inject

@OptIn(InternalDokkaGradlePluginApi::class)
abstract class MarkdownPluginConfiguration @Inject constructor(name: String) :
    DokkaPluginParametersBaseSpec(
        name,
        "com.androidcommondoc.dokka.markdown.StructuredMarkdownPlugin"
    ) {

    @get:Input abstract val layer: Property<String>
    @get:Input abstract val category: Property<String>
    @get:Input abstract val hubParent: Property<String>
    @get:Input abstract val targets: ListProperty<String>
    @get:Input abstract val status: Property<String>
    @get:Input abstract val generatedFrom: Property<String>
    @get:Input abstract val schemaVersion: Property<Int>
    @get:Input abstract val hashFormat: Property<HashFormat>
    @get:Input abstract val filenameConvention: Property<FilenameConvention>
    @get:Input abstract val slugSeparator: Property<String>
    @get:Input abstract val kdocStatePath: Property<String>
    @get:Input abstract val frontmatterMode: Property<FrontmatterMode>
    @get:Input abstract val customFields: MapProperty<String, String>

    override fun jsonEncode(): String {
        val config = MarkdownPluginConfig(
            layer = layer.get(),
            category = category.get(),
            hubParent = hubParent.get(),
            targets = targets.get(),
            status = status.get(),
            generatedFrom = generatedFrom.get(),
            schemaVersion = schemaVersion.get(),
            hashFormat = hashFormat.get(),
            filenameConvention = filenameConvention.get(),
            slugSeparator = slugSeparator.get(),
            kdocStatePath = kdocStatePath.get(),
            frontmatterMode = frontmatterMode.get(),
            customFields = customFields.get(),
        )
        config.validate()
        return MarkdownPluginConfigSerializer.serialize(config)
    }
}
