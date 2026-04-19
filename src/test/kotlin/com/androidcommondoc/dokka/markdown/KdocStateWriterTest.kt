package com.androidcommondoc.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KdocStateWriterTest {

    @TempDir
    lateinit var tempDir: File

    @Nested
    inner class EmptyEntries {

        @Test
        fun `write_emptyList_createsFile`() {
            val out = File(tempDir, "kdoc-state.json")
            KdocStateWriter.write(out, emptyList(), generatedAt = "2026-04-19T00:00:00Z")
            assertTrue(out.exists())
        }

        @Test
        fun `write_emptyList_fileContainsGeneratedAt`() {
            val out = File(tempDir, "kdoc-state.json")
            KdocStateWriter.write(out, emptyList(), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            assertTrue(content.contains("\"generated_at\": \"2026-04-19T00:00:00Z\""), content)
        }

        @Test
        fun `write_emptyList_filesBlockIsEmpty`() {
            val out = File(tempDir, "kdoc-state.json")
            KdocStateWriter.write(out, emptyList(), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            assertTrue(content.contains("\"files\": {"), content)
        }

        @Test
        fun `write_emptyList_validUtf8`() {
            val out = File(tempDir, "kdoc-state.json")
            KdocStateWriter.write(out, emptyList(), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText(Charsets.UTF_8)
            assertFalse(content.isEmpty())
        }
    }

    @Nested
    inner class SingleEntry {

        @Test
        fun `write_singleEntry_containsSlug`() {
            val out = File(tempDir, "kdoc-state.json")
            val entry = KdocStateEntry(
                slug = "core-audio--track-repository",
                sourcePath = "core-audio/-track-repository.md",
                contentHash = "abc123def456",
            )
            KdocStateWriter.write(out, listOf(entry), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            assertTrue(content.contains("\"core-audio--track-repository\""), content)
        }

        @Test
        fun `write_singleEntry_containsSourcePath`() {
            val out = File(tempDir, "kdoc-state.json")
            val entry = KdocStateEntry(
                slug = "core--my-class",
                sourcePath = "core/-my-class.md",
                contentHash = "aabbcc112233",
            )
            KdocStateWriter.write(out, listOf(entry), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            assertTrue(content.contains("\"source\": \"core/-my-class.md\""), content)
        }

        @Test
        fun `write_singleEntry_containsContentHash`() {
            val out = File(tempDir, "kdoc-state.json")
            val entry = KdocStateEntry(
                slug = "core--my-class",
                sourcePath = "core/-my-class.md",
                contentHash = "aabbcc112233",
            )
            KdocStateWriter.write(out, listOf(entry), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            assertTrue(content.contains("\"content_hash\": \"aabbcc112233\""), content)
        }
    }

    @Nested
    inner class MultipleEntries {

        @Test
        fun `write_multipleEntries_sortedBySlug`() {
            val out = File(tempDir, "kdoc-state.json")
            val entries = listOf(
                KdocStateEntry("zzz-slug", "zzz.md", "000000000000"),
                KdocStateEntry("aaa-slug", "aaa.md", "111111111111"),
                KdocStateEntry("mmm-slug", "mmm.md", "222222222222"),
            )
            KdocStateWriter.write(out, entries, generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            val aaaIdx = content.indexOf("aaa-slug")
            val mmmIdx = content.indexOf("mmm-slug")
            val zzzIdx = content.indexOf("zzz-slug")
            assertTrue(aaaIdx < mmmIdx && mmmIdx < zzzIdx, "Expected alphabetical order: $content")
        }

        @Test
        fun `write_multipleEntries_lastEntryHasNoTrailingComma`() {
            val out = File(tempDir, "kdoc-state.json")
            val entries = listOf(
                KdocStateEntry("aaa", "aaa.md", "aaaaaaaaaaaa"),
                KdocStateEntry("bbb", "bbb.md", "bbbbbbbbbbbb"),
            )
            KdocStateWriter.write(out, entries, generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            val lastBrace = content.lastIndexOf("    }")
            val afterLastBrace = content.substring(lastBrace + 5).trimStart()
            assertFalse(afterLastBrace.startsWith(","), "Last entry must not have trailing comma: $content")
        }
    }

    @Nested
    inner class ParentDirCreation {

        @Test
        fun `write_missingParentDir_createsParentAutomatically`() {
            val nested = File(tempDir, "deep/nested/dir/kdoc-state.json")
            assertFalse(nested.parentFile.exists())
            KdocStateWriter.write(nested, emptyList(), generatedAt = "2026-04-19T00:00:00Z")
            assertTrue(nested.exists())
        }
    }

    @Nested
    inner class Overwrite {

        @Test
        fun `write_calledTwiceWithSameInput_secondCallOverwrites`() {
            val out = File(tempDir, "kdoc-state.json")
            val entries = listOf(KdocStateEntry("my-slug", "my/path.md", "aabbccdd1122"))
            KdocStateWriter.write(out, entries, generatedAt = "2026-04-19T00:00:00Z")
            val firstContent = out.readText()
            KdocStateWriter.write(out, entries, generatedAt = "2026-04-19T00:00:00Z")
            val secondContent = out.readText()
            assertEquals(firstContent, secondContent)
        }

        @Test
        fun `write_calledTwiceWithDifferentInput_fileReflectsSecondWrite`() {
            val out = File(tempDir, "kdoc-state.json")
            KdocStateWriter.write(out, listOf(KdocStateEntry("slug-v1", "v1.md", "000000000000")), generatedAt = "2026-04-19T00:00:00Z")
            KdocStateWriter.write(out, listOf(KdocStateEntry("slug-v2", "v2.md", "111111111111")), generatedAt = "2026-04-19T00:00:00Z")
            val content = out.readText()
            assertTrue(content.contains("slug-v2"), content)
            assertFalse(content.contains("slug-v1"), content)
        }
    }

    @Nested
    inner class UnicodeValues {

        @Test
        fun `write_unicodeInDescription_doesNotThrow`() {
            val out = File(tempDir, "kdoc-state.json")
            val entry = KdocStateEntry(
                slug = "core--\u00e9l\u00e8ve",
                sourcePath = "core/\u00e9l\u00e8ve.md",
                contentHash = "aabbcc112233",
            )
            KdocStateWriter.write(out, listOf(entry), generatedAt = "2026-04-19T00:00:00Z")
            assertTrue(out.exists())
            val content = out.readText(Charsets.UTF_8)
            assertTrue(content.contains("\u00e9l\u00e8ve"), content)
        }
    }

    @Nested
    inner class DefaultGeneratedAt {

        @Test
        fun `write_noGeneratedAtParam_fileCreatedWithIso8601Timestamp`() {
            val out = File(tempDir, "kdoc-state.json")
            KdocStateWriter.write(out, emptyList())
            assertTrue(out.exists())
            val content = out.readText()
            assertTrue(content.contains("generated_at"), content)
            // ISO-8601 instant format: contains 'T' and 'Z'
            assertTrue(content.contains("T") && content.contains("Z"), content)
        }
    }
}
