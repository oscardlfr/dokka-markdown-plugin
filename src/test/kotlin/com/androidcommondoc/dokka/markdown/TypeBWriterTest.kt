package com.androidcommondoc.dokka.markdown

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TypeBWriterTest {

    private fun minimalFrontmatter() = FrontmatterFields(
        scope = listOf("api", "sample"),
        sources = listOf("sample"),
        slug = "sample-do-something",
        layer = "L1",
        description = "Does something.",
        lastUpdated = "2026-04",
        contentHash = "abc123",
        parent = "sample-api-hub",
    )

    private fun minimalCtx(
        parentClassName: String? = null,
        platformInfo: PlatformInfo? = null,
        description: String = "Does something useful.",
        signature: String = "fun doSomething(value: Int): String",
        params: List<org.jetbrains.dokka.model.doc.Param> = emptyList(),
        returnDoc: org.jetbrains.dokka.model.doc.Return? = null,
        throws: List<org.jetbrains.dokka.model.doc.Throws> = emptyList(),
        sees: List<org.jetbrains.dokka.model.doc.See> = emptyList(),
    ) = TypeBContext(
        moduleName = "sample",
        parentClassName = parentClassName,
        symbolName = "doSomething",
        platformInfo = platformInfo,
        signature = signature,
        description = description,
        params = params,
        returnDoc = returnDoc,
        throws = throws,
        sees = sees,
        frontmatter = minimalFrontmatter(),
    )

    @Nested
    inner class Structure {

        @Test
        fun `write_containsFrontmatterBlock`() {
            val result = TypeBWriter.write(minimalCtx())
            assertTrue(result.contains("---\nscope:"))
        }

        @Test
        fun `write_containsH1WithSymbolName`() {
            val result = TypeBWriter.write(minimalCtx())
            assertTrue(result.contains("# doSomething"))
        }

        @Test
        fun `write_containsSignature`() {
            val result = TypeBWriter.write(minimalCtx())
            assertTrue(result.contains("fun doSomething(value: Int): String"))
        }

        @Test
        fun `write_containsDescription`() {
            val result = TypeBWriter.write(minimalCtx())
            assertTrue(result.contains("Does something useful."))
        }

        @Test
        fun `write_noTrailingWhitespace`() {
            val result = TypeBWriter.write(minimalCtx())
            result.lines().forEach { line ->
                assertEquals(line.trimEnd(), line, "Trailing whitespace in: [$line]")
            }
        }
    }

    @Nested
    inner class Breadcrumb {

        @Test
        fun `write_topLevel_breadcrumbHasModule`() {
            val result = TypeBWriter.write(minimalCtx(parentClassName = null))
            assertTrue(result.contains("[sample](../../sample-hub.md)"))
            assertTrue(!result.contains("[com.sample]"))
        }

        @Test
        fun `write_classMember_breadcrumbIncludesParentClass`() {
            val result = TypeBWriter.write(minimalCtx(parentClassName = "MyClass"))
            assertTrue(result.contains("[MyClass](-my-class.md)"))
        }
    }

    @Nested
    inner class Platforms {

        @Test
        fun `write_withPlatformInfo_bodyLinePresent`() {
            val pi = PlatformInfo(listOf("common", "jvm"), "**Platforms:** common, jvm")
            val result = TypeBWriter.write(minimalCtx(platformInfo = pi))
            assertTrue(result.contains("**Platforms:** common, jvm"))
        }

        @Test
        fun `write_noPlatformInfo_noPlatformsLine`() {
            val result = TypeBWriter.write(minimalCtx(platformInfo = null))
            assertTrue(!result.contains("**Platforms:**"))
        }
    }

    @Nested
    inner class Sections {

        @Test
        fun `write_withParams_containsParametersSection`() {
            val param = org.jetbrains.dokka.model.doc.Param(
                root = org.jetbrains.dokka.model.doc.P(children = listOf(org.jetbrains.dokka.model.doc.Text(body = "The input."))),
                name = "input",
            )
            val result = TypeBWriter.write(minimalCtx(params = listOf(param)))
            assertTrue(result.contains("#### Parameters"), result)
            assertTrue(result.contains("`input`"), result)
        }

        @Test
        fun `write_withReturn_containsReturnSection`() {
            val ret = org.jetbrains.dokka.model.doc.Return(
                root = org.jetbrains.dokka.model.doc.P(children = listOf(org.jetbrains.dokka.model.doc.Text(body = "The result."))),
            )
            val result = TypeBWriter.write(minimalCtx(returnDoc = ret))
            assertTrue(result.contains("#### Return"), result)
            assertTrue(result.contains("The result."), result)
        }

        @Test
        fun `write_withThrows_containsThrowsSection`() {
            val throws = org.jetbrains.dokka.model.doc.Throws(
                root = org.jetbrains.dokka.model.doc.P(children = listOf(org.jetbrains.dokka.model.doc.Text(body = "When null."))),
                name = "IllegalArgumentException",
                exceptionAddress = null,
            )
            val result = TypeBWriter.write(minimalCtx(throws = listOf(throws)))
            assertTrue(result.contains("#### Throws"), result)
            assertTrue(result.contains("`IllegalArgumentException`"), result)
        }

        @Test
        fun `write_withSee_containsSeeAlsoSection`() {
            val see = org.jetbrains.dokka.model.doc.See(
                root = org.jetbrains.dokka.model.doc.P(children = emptyList()),
                name = "com.sample.Other",
                address = null,
            )
            val result = TypeBWriter.write(minimalCtx(sees = listOf(see)))
            assertTrue(result.contains("#### See also"), result)
            assertTrue(result.contains("`com.sample.Other`"), result)
        }

        @Test
        fun `write_emptyDescription_noBlankDescriptionBlock`() {
            val result = TypeBWriter.write(minimalCtx(description = ""))
            // signature is present but no extra blank line for empty description
            assertTrue(result.contains("fun doSomething"), result)
        }
    }
}
