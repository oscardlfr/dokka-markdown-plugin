package com.androidcommondoc.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MarkdownPluginConfigTest {

    @Nested
    inner class Defaults {

        @Test
        fun `default_layer_isL1`() {
            assertEquals("L1", MarkdownPluginConfig().layer)
        }

        @Test
        fun `default_category_isApi`() {
            assertEquals("api", MarkdownPluginConfig().category)
        }

        @Test
        fun `default_hubParent_isApiHub`() {
            assertEquals("api-hub", MarkdownPluginConfig().hubParent)
        }

        @Test
        fun `default_targets_isListOfAll`() {
            assertEquals(listOf("all"), MarkdownPluginConfig().targets)
        }

        @Test
        fun `default_status_isActive`() {
            assertEquals("active", MarkdownPluginConfig().status)
        }

        @Test
        fun `default_generatedFrom_isDokka`() {
            assertEquals("dokka", MarkdownPluginConfig().generatedFrom)
        }

        @Test
        fun `default_schemaVersion_is1`() {
            assertEquals(1, MarkdownPluginConfig().schemaVersion)
        }

        @Test
        fun `default_hashFormat_isCompact12Hex`() {
            assertEquals(HashFormat.COMPACT_12_HEX, MarkdownPluginConfig().hashFormat)
        }

        @Test
        fun `default_filenameConvention_isLeadingDash`() {
            assertEquals(FilenameConvention.LEADING_DASH, MarkdownPluginConfig().filenameConvention)
        }

        @Test
        fun `default_slugSeparator_isDoubleDash`() {
            assertEquals("--", MarkdownPluginConfig().slugSeparator)
        }

        @Test
        fun `default_kdocStatePath_isEmpty`() {
            assertEquals("", MarkdownPluginConfig().kdocStatePath)
        }

        @Test
        fun `default_frontmatterMode_isStructured`() {
            assertEquals(FrontmatterMode.STRUCTURED, MarkdownPluginConfig().frontmatterMode)
        }

        @Test
        fun `default_customFields_isEmpty`() {
            assertEquals(emptyMap<String, String>(), MarkdownPluginConfig().customFields)
        }
    }

    @Nested
    inner class DataClassContract {

        @Test
        fun `equals_sameValues_areEqual`() {
            assertEquals(MarkdownPluginConfig(layer = "L0"), MarkdownPluginConfig(layer = "L0"))
        }

        @Test
        fun `hashCode_sameValues_sameHash`() {
            assertEquals(
                MarkdownPluginConfig(layer = "L0").hashCode(),
                MarkdownPluginConfig(layer = "L0").hashCode()
            )
        }

        @Test
        fun `copy_changesLayer_otherFieldsUnchanged`() {
            val original = MarkdownPluginConfig()
            val copy = original.copy(layer = "L2")
            assertEquals("L2", copy.layer)
            assertEquals(original.category, copy.category)
            assertEquals(original.hashFormat, copy.hashFormat)
        }
    }

    @Nested
    inner class Validate {

        @Test
        fun `validate_defaults_noException`() {
            MarkdownPluginConfig().validate()
        }

        @Test
        fun `validate_emptyLayer_throwsWithMessage`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(layer = "").validate()
            }
            assert(ex.message!!.contains("layer must not be blank")) {
                "Expected 'layer must not be blank' in message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_blankLayer_throwsWithMessage`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(layer = "   ").validate()
            }
            assert(ex.message!!.contains("layer must not be blank")) {
                "Expected 'layer must not be blank' in message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_emptyCategory_throwsWithMessage`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(category = "").validate()
            }
            assert(ex.message!!.contains("category must not be blank")) {
                "Expected 'category must not be blank' in message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_emptyHubParent_throwsWithMessage`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(hubParent = "").validate()
            }
            assert(ex.message!!.contains("hubParent must not be blank")) {
                "Expected 'hubParent must not be blank' in message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_zeroSchemaVersion_throwsWithMessage`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(schemaVersion = 0).validate()
            }
            assert(ex.message!!.contains("schemaVersion must be positive")) {
                "Expected 'schemaVersion must be positive' in message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_negativeSchemaVersion_messageContainsActualValue`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(schemaVersion = -5).validate()
            }
            assert(ex.message!!.contains("schemaVersion must be positive")) {
                "Expected 'schemaVersion must be positive' in message, got: ${ex.message}"
            }
            assert(ex.message!!.contains("-5")) {
                "Expected actual value -5 in message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_frontmatterModeNoneWithCustomFields_throwsWithMessage`() {
            val ex = assertFailsWith<IllegalArgumentException> {
                MarkdownPluginConfig(
                    frontmatterMode = FrontmatterMode.NONE,
                    customFields = mapOf("key" to "value"),
                ).validate()
            }
            assert(ex.message!!.contains("customFields cannot be set when frontmatterMode is NONE")) {
                "Expected NONE+customFields message, got: ${ex.message}"
            }
        }

        @Test
        fun `validate_frontmatterModeNoneEmptyCustomFields_noException`() {
            MarkdownPluginConfig(frontmatterMode = FrontmatterMode.NONE, customFields = emptyMap()).validate()
        }

        @Test
        fun `validate_isIdempotent`() {
            val config = MarkdownPluginConfig()
            config.validate()
            config.validate()
        }
    }
}
