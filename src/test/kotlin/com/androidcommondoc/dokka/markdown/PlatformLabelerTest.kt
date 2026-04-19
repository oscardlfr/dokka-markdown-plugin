package com.androidcommondoc.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlatformLabelerTest {

    @Nested
    inner class Sorting {

        @Test
        fun `labels_multiplePlatforms_alphabeticallySorted`() {
            assertEquals(listOf("apple", "common", "jvm"), PlatformLabeler.labels(listOf("common", "apple", "jvm")))
        }

        @Test
        fun `labels_singleElement_returnsSingle`() {
            assertEquals(listOf("common"), PlatformLabeler.labels(listOf("common")))
        }

        @Test
        fun `labels_empty_returnsEmpty`() {
            assertEquals(emptyList(), PlatformLabeler.labels(emptyList()))
        }
    }

    @Nested
    inner class MainSuffixStripping {

        @Test
        fun `labels_mainSuffix_stripped`() {
            assertEquals(
                listOf("apple", "common", "jvm"),
                PlatformLabeler.labels(listOf("commonMain", "appleMain", "jvmMain")),
            )
        }

        @Test
        fun `labels_mixedMainAndPlain_normalizedAndSorted`() {
            assertEquals(
                listOf("apple", "jvm"),
                PlatformLabeler.labels(listOf("appleMain", "jvm")),
            )
        }
    }

    @Nested
    inner class Deduplication {

        @Test
        fun `labels_duplicates_deduped`() {
            assertEquals(listOf("apple", "jvm"), PlatformLabeler.labels(listOf("jvm", "jvm", "apple")))
        }

        @Test
        fun `labels_duplicatesAfterNormalization_deduped`() {
            // appleMain + apple both normalize to "apple"
            assertEquals(listOf("apple", "jvm"), PlatformLabeler.labels(listOf("appleMain", "apple", "jvm")))
        }
    }

    @Nested
    inner class PlatformNormalization {

        @Test
        fun `label_commonMain_normalizesToCommon`() {
            val info = PlatformLabeler.labels(listOf("commonMain", "jvmMain"))
            assertEquals(listOf("common", "jvm"), info)
        }

        @Test
        fun `label_jvmMain_normalizesToJvm`() {
            val info = PlatformLabeler.labels(listOf("appleMain", "jvmMain"))
            assertEquals(listOf("apple", "jvm"), info)
        }
    }

    @Nested
    inner class NoConcatenationBug {

        @Test
        fun `labels_noElementContainsConcatenatedNames`() {
            val result = PlatformLabeler.labels(listOf("common", "apple", "desktop"))
            assertTrue(result.none { it.contains("common") && it.length > "common".length },
                "No element should be a concatenation like 'commonapple'")
        }

        @Test
        fun `labels_eachElementIsSingleShortName`() {
            val result = PlatformLabeler.labels(listOf("common", "apple", "jvm", "desktop"))
            result.forEach { label ->
                assertTrue(label.length <= 10, "Each label must be a short single name, got: '$label'")
            }
        }
    }

    @Nested
    inner class Normalize {

        @Test
        fun `normalize_android_returnsAndroid`() {
            assertEquals("android", PlatformLabeler.normalize("android"))
            assertEquals("android", PlatformLabeler.normalize("androidJvm"))
            assertEquals("android", PlatformLabeler.normalize("ANDROID"))
        }

        @Test
        fun `normalize_jvm_returnsJvm`() {
            assertEquals("jvm", PlatformLabeler.normalize("jvm"))
            assertEquals("jvm", PlatformLabeler.normalize("JVM"))
        }

        @Test
        fun `normalize_js_returnsJs`() {
            assertEquals("js", PlatformLabeler.normalize("js"))
            assertEquals("js", PlatformLabeler.normalize("javascript"))
        }

        @Test
        fun `normalize_native_returnsNative`() {
            assertEquals("native", PlatformLabeler.normalize("native"))
        }

        @Test
        fun `normalize_iosVariants_returnApple`() {
            assertEquals("apple", PlatformLabeler.normalize("ios"))
            assertEquals("apple", PlatformLabeler.normalize("iosX64"))
            assertEquals("apple", PlatformLabeler.normalize("iosArm64"))
            assertEquals("apple", PlatformLabeler.normalize("iosSimulatorArm64"))
        }

        @Test
        fun `normalize_macosVariants_returnApple`() {
            assertEquals("apple", PlatformLabeler.normalize("macos"))
            assertEquals("apple", PlatformLabeler.normalize("macosX64"))
            assertEquals("apple", PlatformLabeler.normalize("macosArm64"))
            assertEquals("apple", PlatformLabeler.normalize("apple"))
        }

        @Test
        fun `normalize_tvosAndWatchos_returnApple`() {
            assertEquals("apple", PlatformLabeler.normalize("tvos"))
            assertEquals("apple", PlatformLabeler.normalize("watchos"))
        }

        @Test
        fun `normalize_linuxVariants_returnLinux`() {
            assertEquals("linux", PlatformLabeler.normalize("linuxArm64"))
            assertEquals("linux", PlatformLabeler.normalize("linuxX64"))
        }

        @Test
        fun `normalize_mingwx64_returnsWindows`() {
            assertEquals("windows", PlatformLabeler.normalize("mingwX64"))
        }

        @Test
        fun `normalize_common_returnsCommon`() {
            assertEquals("common", PlatformLabeler.normalize("common"))
        }

        @Test
        fun `normalize_desktop_returnsDesktop`() {
            assertEquals("desktop", PlatformLabeler.normalize("desktop"))
        }

        @Test
        fun `normalize_unknown_returnsLowercased`() {
            assertEquals("wasm", PlatformLabeler.normalize("WASM"))
            assertEquals("wasm32", PlatformLabeler.normalize("wasm32"))
        }
    }

    @Nested
    inner class Label {

        @Test
        fun `label_emptySet_returnsNull`() {
            assertEquals(null, PlatformLabeler.label(emptySet()))
        }

        @Test
        fun `label_singlePlatform_returnsNull`() {
            assertEquals(null, PlatformLabeler.label(setOf("jvm")))
        }

        @Test
        fun `label_twoDistinctPlatforms_returnsPlatformInfo`() {
            val info = PlatformLabeler.label(setOf("jvm", "apple"))
            assertEquals(listOf("apple", "jvm"), info?.frontmatterList)
        }

        @Test
        fun `label_duplicatesAfterNormalization_singleResult_returnsNull`() {
            // iosX64 + iosArm64 both normalize to "apple" → only 1 distinct → null
            assertEquals(null, PlatformLabeler.label(setOf("iosX64", "iosArm64")))
        }

        @Test
        fun `label_multiPlatform_bodyLineContainsPlatformNames`() {
            val info = PlatformLabeler.label(setOf("jvm", "android", "apple"))
            assertTrue(info?.bodyLine?.contains("Platforms:") == true, info?.bodyLine)
        }

        @Test
        fun `label_multiPlatform_frontmatterListIsSorted`() {
            val info = PlatformLabeler.label(setOf("jvm", "common", "apple"))
            assertEquals(listOf("apple", "common", "jvm"), info?.frontmatterList)
        }
    }
}
