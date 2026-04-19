package io.github.oscardlfr.dokka.markdown

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class StructuredMarkdownExtension @Inject constructor(objects: ObjectFactory) {
    val layer: Property<String> = objects.property(String::class.java).convention("L1")
    val category: Property<String> = objects.property(String::class.java).convention("api")
    val hubParent: Property<String> = objects.property(String::class.java).convention("api-hub")
    val targets: ListProperty<String> = objects.listProperty(String::class.java).convention(listOf("all"))
    val status: Property<String> = objects.property(String::class.java).convention("active")
    val generatedFrom: Property<String> = objects.property(String::class.java).convention("dokka")
    val schemaVersion: Property<Int> = objects.property(Int::class.java).convention(1)
    val hashFormat: Property<HashFormat> = objects.property(HashFormat::class.java).convention(HashFormat.COMPACT_12_HEX)
    val filenameConvention: Property<FilenameConvention> = objects.property(FilenameConvention::class.java).convention(FilenameConvention.LEADING_DASH)
    val slugSeparator: Property<String> = objects.property(String::class.java).convention("--")
    val kdocStatePath: Property<String> = objects.property(String::class.java).convention("")
    val frontmatterMode: Property<FrontmatterMode> = objects.property(FrontmatterMode::class.java).convention(FrontmatterMode.STRUCTURED)
    val customFields: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java).convention(emptyMap())
}
