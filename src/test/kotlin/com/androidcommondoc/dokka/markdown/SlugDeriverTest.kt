package com.androidcommondoc.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SlugDeriverTest {

    @Nested
    inner class ToKebab {

        @ParameterizedTest(name = "toKebab({0}) == {1}")
        @CsvSource(
            "MyClass,         my-class",
            "doSomething,     do-something",
            "HttpClient,      http-client",
            "URL,             u-r-l",
            "JSON2YAML,       j-s-o-n2-y-a-m-l",
        )
        fun `toKebab_camelAndAcronymCases`(input: String, expected: String) {
            assertEquals(expected, SlugDeriver.toKebab(input))
        }

        @Test
        fun `toKebab_allCapsAcronym_json`() {
            // "JSON" → "j-s-o-n" per contract table (all-uppercase acronym = per-letter dashes)
            assertEquals("j-s-o-n", SlugDeriver.toKebab("JSON"))
        }
    }

    @Nested
    inner class DeriveForMember {

        @Test
        fun `deriveForMember_functionName_moduleAndKebab`() {
            // production: deriveForMember(simpleName, moduleName)
            assertEquals("core-do-something", SlugDeriver.deriveForMember("doSomething", "core"))
        }

        @Test
        fun `deriveForMember_preservesModuleName`() {
            assertEquals("core-common-my-prop", SlugDeriver.deriveForMember("myProp", "core-common"))
        }
    }

    @Nested
    inner class DeriveForClass {

        @Test
        fun `deriveForClass_withModule_prependsNormalizedModule`() {
            assertEquals("core--my-class", SlugDeriver.deriveForClass("MyClass", "core"))
        }

        @Test
        fun `deriveForClass_withHyphenatedModule_preserved`() {
            assertEquals("core-audio--my-class", SlugDeriver.deriveForClass("MyClass", "core-audio"))
        }

        @Test
        fun `deriveForClass_withColonModule_normalized`() {
            // "core:audio" → "core-audio" via normalizeModule
            assertEquals("core-audio--my-class", SlugDeriver.deriveForClass("MyClass", "core:audio"))
        }

        @Test
        fun `deriveForClass_withModule_noLeadingDash`() {
            // two-arg form must NOT produce the leading dash that the one-arg form produces
            assertFalse(SlugDeriver.deriveForClass("MyClass", "core").startsWith("-"))
        }
    }

    @Nested
    inner class FileBasename {

        @Test
        fun `fileBasename_simpleClass_leadingDashKebab`() {
            assertEquals("-my-class", SlugDeriver.fileBasename("MyClass"))
        }

        @Test
        fun `fileBasename_acronymClass_leadingDashPerLetterKebab`() {
            assertEquals("-u-r-l", SlugDeriver.fileBasename("URL"))
        }
    }

    @Nested
    inner class DeriveForHub {

        @Test
        fun `deriveForHub_moduleName_appendsApiHub`() {
            assertEquals("core-common-api-hub", SlugDeriver.deriveForHub("core-common"))
        }

        @Test
        fun `deriveForHub_simpleModule_appendsApiHub`() {
            assertEquals("core-api-hub", SlugDeriver.deriveForHub("core"))
        }
    }

    // RED: normalizeModule doesn't exist yet — compile errors until Bug 1 fix
    @Nested
    inner class DeriveForMemberNormalization {

        @Test
        fun `deriveForMember_colonSeparatedModule_normalized`() {
            // "core:audio" should be normalized to "core-audio" before slug
            assertEquals("core-audio-do-something", SlugDeriver.deriveForMember("doSomething", "core:audio"))
        }

        @Test
        fun `deriveForMember_dotSeparatedModule_normalized`() {
            assertEquals("core-audio-do-something", SlugDeriver.deriveForMember("doSomething", "core.audio"))
        }
    }

    @Nested
    inner class DeriveForHubNormalization {

        @Test
        fun `deriveForHub_colonSeparatedModule_normalized`() {
            assertEquals("core-audio-api-hub", SlugDeriver.deriveForHub("core:audio"))
        }

        @Test
        fun `deriveForHub_dotSeparatedModule_normalized`() {
            assertEquals("core-audio-api-hub", SlugDeriver.deriveForHub("core.audio"))
        }
    }

    @Nested
    inner class NormalizeModule {

        @ParameterizedTest(name = "normalizeModule({0}) == {1}")
        @CsvSource(
            "core,              core",
            "core-audio,        core-audio",
            "core:audio,        core-audio",
            "core.audio,        core-audio",
            "core:audio:engine, core-audio-engine",
            "com.example.core,      com-example-core",
            "core.audio:feature,    core-audio-feature",
        )
        fun `normalizeModule_variousSeparators_allHyphenated`(input: String, expected: String) {
            assertEquals(expected, SlugDeriver.normalizeModule(input))
        }
    }

    @Nested
    inner class FileBasenameFor {

        @Test
        fun `fileBasenameFor_leadingDash_backwardCompatibleWithFileBasename`() {
            val className = "MyClass"
            assertEquals(
                SlugDeriver.fileBasename(className),
                SlugDeriver.fileBasenameFor(className, FilenameConvention.LEADING_DASH),
            )
        }

        @Test
        fun `fileBasenameFor_plain_noLeadingDash`() {
            assertEquals("my-class", SlugDeriver.fileBasenameFor("MyClass", FilenameConvention.PLAIN))
        }

        @Test
        fun `fileBasenameFor_plain_acronym_noLeadingDash`() {
            assertEquals("u-r-l", SlugDeriver.fileBasenameFor("URL", FilenameConvention.PLAIN))
        }

        @Test
        fun `fileBasenameFor_leadingDash_acronym_matchesFileBasename`() {
            assertEquals(
                SlugDeriver.fileBasename("URL"),
                SlugDeriver.fileBasenameFor("URL", FilenameConvention.LEADING_DASH),
            )
        }

        @Test
        fun `fileBasenameFor_plain_emptyString_returnsEmpty`() {
            assertEquals("", SlugDeriver.fileBasenameFor("", FilenameConvention.PLAIN))
        }

        @Test
        fun `fileBasenameFor_leadingDash_emptyString_dashOnly`() {
            assertEquals("-", SlugDeriver.fileBasenameFor("", FilenameConvention.LEADING_DASH))
        }
    }
}
