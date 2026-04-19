# Changelog

## [Unreleased]

## [0.2.0] - 2026-04-19

### Added
- `structuredMarkdown {}` Gradle DSL with 13 configurable settings (layer, category, hubParent, targets, status, generatedFrom, schemaVersion, hashFormat, filenameConvention, slugSeparator, kdocStatePath, frontmatterMode, customFields)
- `FrontmatterMode` enum: `STRUCTURED` (14-field YAML, default), `MINIMAL` (slug + content_hash only), `NONE` (no frontmatter block)
- `HashFormat` enum: `COMPACT_12_HEX` (default), `FULL_SHA256_WITH_PREFIX` (`sha256:<64-hex>`), `NONE`
- `FilenameConvention` enum: `LEADING_DASH` (default, e.g. `-network-result.md`), `PLAIN` (e.g. `network-result.md`)
- `MarkdownPluginConfig` data class with validation (`validate()` throws `IllegalArgumentException` on invalid combos)
- `StructuredMarkdownExtension` Gradle extension — configure via `structuredMarkdown {}` block
- `StructuredMarkdownGradlePlugin` companion Gradle plugin (`io.github.oscardlfr.dokka-markdown-config`)
- JSON serializer/deserializer (`MarkdownPluginConfigSerializer`) — unknown keys ignored, malformed JSON throws with clear message
- Kover coverage enforcement: 85% line / 80% branch thresholds

### Breaking Changes
- groupId changed from `com.androidcommondoc` to `io.github.oscardlfr`
- Package renamed from `com.androidcommondoc.dokka.markdown` to `io.github.oscardlfr.dokka.markdown`
- SPI plugin ID changed from `com.androidcommondoc.dokka-markdown` to `io.github.oscardlfr.dokka-markdown-plugin`
- Companion Gradle plugin ID: `io.github.oscardlfr.dokka-markdown-config`
- Consumers on v0.1.0 with no `structuredMarkdown {}` block: update coordinates only — no behavioral changes required

## [0.1.0] - 2026-04-18

### Added
- Dokka 2.2.x custom renderer (`StructuredMarkdownRenderer` via `CoreExtensions.renderer` override)
- 14-field YAML frontmatter per doc + optional `platforms:` for multi-target KMP declarations
- Three file types: Type A class-index (leading-dash filename, double-dash slug), Type B member (signature + KDoc tags), per-module hub (markdown table)
- `.androidcommondoc/kdoc-state.json` central run state with ISO 8601 timestamp and content-hash drift detection
- Structured platform labels — no more `androidapplecommondesktop` concatenation
- Single-pass deterministic output — no more two-timestamp script bug
- First-class KMP expect/actual merging — one body, platform list separate

### Dependencies
- `org.jetbrains.dokka:dokka-core:2.2.0` (compileOnly)
- `org.jetbrains.dokka:dokka-base:2.2.0` (compileOnly)

[Unreleased]: https://github.com/oscardlfr/dokka-markdown-plugin/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/oscardlfr/dokka-markdown-plugin/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/oscardlfr/dokka-markdown-plugin/releases/tag/v0.1.0
