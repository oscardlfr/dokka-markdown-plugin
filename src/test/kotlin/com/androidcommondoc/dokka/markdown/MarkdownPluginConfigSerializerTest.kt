package io.github.oscardlfr.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MarkdownPluginConfigSerializerTest {

    @Nested
    inner class RoundTrip {

        @Test
        fun `roundTrip_defaultConfig_isIdentical`() {
            val original = MarkdownPluginConfig()
            val restored = MarkdownPluginConfigSerializer.deserialize(MarkdownPluginConfigSerializer.serialize(original))
            assertEquals(original, restored)
        }

        @Test
        fun `roundTrip_allFieldsNonDefault_isIdentical`() {
            val original = MarkdownPluginConfig(
                layer = "L0",
                category = "guide",
                hubParent = "guide-hub",
                targets = listOf("android", "ios"),
                status = "draft",
                generatedFrom = "manual",
                schemaVersion = 3,
                hashFormat = HashFormat.FULL_SHA256_WITH_PREFIX,
                filenameConvention = FilenameConvention.PLAIN,
                slugSeparator = "__",
                kdocStatePath = "/custom/kdoc-state.json",
                frontmatterMode = FrontmatterMode.MINIMAL,
                customFields = mapOf("author" to "test", "team" to "platform"),
            )
            assertEquals(original, MarkdownPluginConfigSerializer.deserialize(MarkdownPluginConfigSerializer.serialize(original)))
        }

        @Test
        fun `roundTrip_customFieldsMap_preservesEntries`() {
            val original = MarkdownPluginConfig(customFields = mapOf("k1" to "v1", "k2" to "v2"))
            val restored = MarkdownPluginConfigSerializer.deserialize(MarkdownPluginConfigSerializer.serialize(original))
            assertEquals(mapOf("k1" to "v1", "k2" to "v2"), restored.customFields)
        }
    }

    @Nested
    inner class EnumSerialization {

        @Test
        fun `serialize_hashFormatCompact12Hex_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(hashFormat = HashFormat.COMPACT_12_HEX))
            assert(json.contains("COMPACT_12_HEX")) { "Expected COMPACT_12_HEX in: $json" }
            assertEquals(HashFormat.COMPACT_12_HEX, MarkdownPluginConfigSerializer.deserialize(json).hashFormat)
        }

        @Test
        fun `serialize_hashFormatFullSha256WithPrefix_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(hashFormat = HashFormat.FULL_SHA256_WITH_PREFIX))
            assert(json.contains("FULL_SHA256_WITH_PREFIX")) { "Expected FULL_SHA256_WITH_PREFIX in: $json" }
            assertEquals(HashFormat.FULL_SHA256_WITH_PREFIX, MarkdownPluginConfigSerializer.deserialize(json).hashFormat)
        }

        @Test
        fun `serialize_hashFormatNone_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(hashFormat = HashFormat.NONE))
            assertEquals(HashFormat.NONE, MarkdownPluginConfigSerializer.deserialize(json).hashFormat)
        }

        @Test
        fun `serialize_filenameConventionLeadingDash_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(filenameConvention = FilenameConvention.LEADING_DASH))
            assert(json.contains("LEADING_DASH")) { "Expected LEADING_DASH in: $json" }
            assertEquals(FilenameConvention.LEADING_DASH, MarkdownPluginConfigSerializer.deserialize(json).filenameConvention)
        }

        @Test
        fun `serialize_filenameConventionPlain_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(filenameConvention = FilenameConvention.PLAIN))
            assert(json.contains("PLAIN")) { "Expected PLAIN in: $json" }
            assertEquals(FilenameConvention.PLAIN, MarkdownPluginConfigSerializer.deserialize(json).filenameConvention)
        }

        @Test
        fun `serialize_frontmatterModeStructured_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(frontmatterMode = FrontmatterMode.STRUCTURED))
            assert(json.contains("STRUCTURED")) { "Expected STRUCTURED in: $json" }
            assertEquals(FrontmatterMode.STRUCTURED, MarkdownPluginConfigSerializer.deserialize(json).frontmatterMode)
        }

        @Test
        fun `serialize_frontmatterModeMinimal_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(frontmatterMode = FrontmatterMode.MINIMAL))
            assert(json.contains("MINIMAL")) { "Expected MINIMAL in: $json" }
            assertEquals(FrontmatterMode.MINIMAL, MarkdownPluginConfigSerializer.deserialize(json).frontmatterMode)
        }

        @Test
        fun `serialize_frontmatterModeNone_encodedByName`() {
            val json = MarkdownPluginConfigSerializer.serialize(MarkdownPluginConfig(frontmatterMode = FrontmatterMode.NONE))
            assertEquals(FrontmatterMode.NONE, MarkdownPluginConfigSerializer.deserialize(json).frontmatterMode)
        }
    }

    @Nested
    inner class Tolerance {

        @Test
        fun `deserialize_unknownKey_isIgnored`() {
            val json = """{"layer":"L0","unknownFutureField":"value","category":"api","hubParent":"api-hub","targets":["all"],"status":"active","generatedFrom":"dokka","schemaVersion":1,"hashFormat":"COMPACT_12_HEX","filenameConvention":"LEADING_DASH","slugSeparator":"--","kdocStatePath":"","frontmatterMode":"STRUCTURED","customFields":{}}"""
            val config = MarkdownPluginConfigSerializer.deserialize(json)
            assertEquals("L0", config.layer)
            assertEquals("api", config.category)
        }

        @Test
        fun `deserialize_emptyJsonObject_allDefaults`() {
            val config = MarkdownPluginConfigSerializer.deserialize("{}")
            assertEquals(MarkdownPluginConfig(), config)
        }

        @Test
        fun `deserialize_missingField_fallsBackToDefault`() {
            val config = MarkdownPluginConfigSerializer.deserialize("""{"layer":"L2"}""")
            assertEquals("L2", config.layer)
            assertEquals("api", config.category)
            assertEquals(HashFormat.COMPACT_12_HEX, config.hashFormat)
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `deserialize_malformedJson_throwsIllegalArgumentException`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfigSerializer.deserialize("{not valid json")
            }
            assert(ex.message!!.isNotEmpty()) { "Expected non-empty exception message" }
        }

        @Test
        fun `deserialize_blankString_throwsIllegalArgumentException`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfigSerializer.deserialize("   ")
            }
            assert(ex.message!!.contains("blank")) { "Expected 'blank' in message, got: ${ex.message}" }
        }
    }
}
