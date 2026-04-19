package com.androidcommondoc.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ContentHasherTest {

    @Nested
    inner class CrlfNormalization {

        @Test
        fun `hash_crlfEquivalentToLf_sameHash`() {
            assertEquals(ContentHasher.hash("hello\nworld"), ContentHasher.hash("hello\r\nworld"))
        }

        @Test
        fun `hash_crEquivalentToLf_sameHash`() {
            assertEquals(ContentHasher.hash("hello\nworld"), ContentHasher.hash("hello\rworld"))
        }
    }

    @Nested
    inner class TrailingWhitespace {

        @Test
        fun `hash_trailingSpacePerLineEquivalentToClean_sameHash`() {
            assertEquals(ContentHasher.hash("line\nother"), ContentHasher.hash("line  \nother"))
        }
    }

    @Nested
    inner class Format {

        @Test
        fun `hash_length_is12`() {
            assertEquals(12, ContentHasher.hash("abc").length)
        }

        @Test
        fun `hash_emptyInput_length12`() {
            assertEquals(12, ContentHasher.hash("").length)
        }

        @Test
        fun `hash_outputIsLowercaseHex`() {
            val result = ContentHasher.hash("abc")
            assertTrue(result.matches(Regex("[0-9a-f]{12}")), "Expected 12 lowercase hex chars, got: $result")
        }

        @Test
        fun `hash_noSha256Prefix`() {
            // contract: hash() returns raw 12-char hex, no "sha256:" prefix
            val result = ContentHasher.hash("abc")
            assertFalse(result.startsWith("sha256:"), "hash must not include 'sha256:' prefix, got: $result")
        }
    }

    @Nested
    inner class Determinism {

        @Test
        fun `hash_sameInput_sameHash`() {
            assertEquals(ContentHasher.hash("same"), ContentHasher.hash("same"))
        }

        @Test
        fun `hash_differentInputs_differentHashes`() {
            assertNotEquals(ContentHasher.hash("a"), ContentHasher.hash("b"))
        }
    }

    @Nested
    inner class HashFull {

        @Test
        fun `hashFull_nistAbcVector_startsWithSha256Prefix`() {
            // NIST SHA-256 of "abc" = ba7816bf8f01cfea414140de5dae2ec73b00361bbef0469340d9a9ca48cfa10a...
            val result = ContentHasher.hashFull("abc")
            assertTrue(result.startsWith("sha256:ba7816bf"), "Expected sha256:ba7816bf... got: $result")
        }

        @Test
        fun `hashFull_length_is71`() {
            // "sha256:" (7) + 64 hex chars = 71
            assertEquals(71, ContentHasher.hashFull("abc").length)
        }

        @Test
        fun `hashFull_emptyInput_length71`() {
            assertEquals(71, ContentHasher.hashFull("").length)
        }

        @Test
        fun `hashFull_unicodeInput_deterministic`() {
            val result1 = ContentHasher.hashFull("\u00e9l\u00e8ve")
            val result2 = ContentHasher.hashFull("\u00e9l\u00e8ve")
            assertEquals(result1, result2)
        }

        @Test
        fun `hashFull_differentFromHash_longerAndPrefixed`() {
            val short = ContentHasher.hash("abc")
            val full = ContentHasher.hashFull("abc")
            assertFalse(short == full, "hash() and hashFull() must not be equal")
            assertTrue(full.startsWith("sha256:"), "hashFull must start with sha256:")
            assertFalse(short.startsWith("sha256:"), "hash must not start with sha256:")
        }

        @Test
        fun `hashFull_outputIsLowercaseHexAfterPrefix`() {
            val hex = ContentHasher.hashFull("test").removePrefix("sha256:")
            assertTrue(hex.matches(Regex("[0-9a-f]{64}")), "Expected 64 lowercase hex chars after prefix, got: $hex")
        }
    }
}
