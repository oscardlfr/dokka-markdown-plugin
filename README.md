# Dokka Markdown Plugin

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Dokka](https://img.shields.io/badge/Dokka-2.2.0-blue)](https://kotlinlang.org/docs/dokka-introduction.html)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.20-purple)](https://kotlinlang.org/)

Dokka 2.2.x plugin that transforms KDoc into structured markdown (`docs/api/*.md`) with 14-field YAML frontmatter, content-addressed hashes for CI drift detection, and first-class KMP expect/actual handling.

Designed for AI agent consumption — deterministic output, slug-based cross-references, and structured platform labels.

## Apply

Add to your project's `libs.versions.toml`:

```toml
[versions]
dokka-markdown-plugin = "0.2.0"

[libraries]
dokka-markdown-plugin = { module = "io.github.oscardlfr:dokka-markdown-plugin", version.ref = "dokka-markdown-plugin" }
```

Add the GitHub Packages repository in your root `build.gradle.kts`:

```kotlin
allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/oscardlfr/dokka-markdown-plugin")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Add the Dokka plugin and plugin dependency in the target module's `build.gradle.kts`:

```kotlin
plugins {
    id("org.jetbrains.dokka") version "2.2.0"
}

dependencies {
    dokkaPlugin(libs.dokka.markdown.plugin)
}
```

Store GitHub Packages credentials in `~/.gradle/gradle.properties` (never commit):

```properties
gpr.user=your-github-username
gpr.key=ghp_yourPersonalAccessToken
```

(The token needs `read:packages` scope. [Create one here](https://github.com/settings/tokens/new?scopes=read:packages&description=dokka-markdown-plugin).)

## Task

Run `./gradlew dokkaGenerate` — the plugin intercepts Dokka's rendering phase automatically via the `CoreExtensions.renderer` extension point, overriding `htmlRenderer`. Output lands in `build/dokka/` by default.

```bash
./gradlew dokkaGenerate
# then copy output to your docs tree:
cp -r build/dokka/ docs/api/
```

Or for a single module:

```bash
./gradlew :core-domain:dokkaGenerate
```

## Output format

Three file types are generated per module:

- `docs/api/<module>-hub.md` — navigation hub with a markdown table of all sub-docs (≤100 lines); hub table has a blank separator row after the header
- `docs/api/<module>/-<kebab-class>.md` — one file per top-level class/object/interface (Type A); filename has leading `-`, no module prefix (e.g., `-base64-converter.md`)
- `docs/api/<module>/<kebab-fn>.md` — one file per top-level function/property/typealias (Type B); no leading `-` (e.g., `parse-json.md`)
- `<module>/build/.androidcommondoc/kdoc-state.json` — written at end of every run (per-module): ISO 8601 timestamp + 12-char compact content hash per file, for CI drift detection

Each generated doc includes 14-field YAML frontmatter:

```yaml
---
scope: [api, core-domain]
sources: [core-domain]
targets: [all]
slug: core-domain--base64-converter    # Type A (class): module--kebab-class
                                       # Type B (function): core-domain-parse-json
status: active
layer: L1
category: api
description: One-line KDoc summary.
version: 1                             # frontmatter schema version (always 1)
last_updated: 2026-04
generated: true
generated_from: dokka
content_hash: 7a8836e73f62             # 12-char compact hex (no sha256: prefix)
parent: core-domain-api-hub
---
```

Optional 15th field for multi-target KMP declarations:

```yaml
platforms: [android, common, desktop]
```

## Module name normalization

The plugin normalizes module names to a consistent kebab form, supporting all common Gradle conventions:

| Consumer module | Normalized |
|---|---|
| `core-error` (flat kebab) | `core-error` |
| `:core:error` (Gradle colon path) | `core-error` |
| `core:error` (relative colon) | `core-error` |
| `app:feature:login` (deep nesting) | `app-feature-login` |
| `core/error` (filesystem path) | `core-error` |
| `com.example.core` (dot notation) | `com-example-core` |
| `Core:Error` (mixed case) | `core-error` |

## Compatibility matrix

| Dokka | Kotlin | AGP | JDK | Status |
|-------|--------|-----|-----|--------|
| 2.2.0 | 2.3.20 | 9.0.0+ (KMP) | 17+ | Supported |
| 2.2.0 | 2.3.20 | 8.x (Android-only) | 17+ | Supported |
| 2.1.x | any | any | 17+ | Unsupported — extension point changed |

Dokka 2.1.x used a different renderer extension point (`htmlRenderer` shape differs). Upgrade to 2.2.x before adopting this plugin.

## DSL Configuration

Apply the companion Gradle plugin alongside Dokka, then configure via `structuredMarkdown {}`:

```kotlin
plugins {
    id("org.jetbrains.dokka") version "2.2.0"
    id("io.github.oscardlfr.dokka-markdown-config") version "0.2.0"
}

dependencies {
    dokkaPlugin(libs.dokka.markdown.plugin)
}

structuredMarkdown {
    layer.set("L1")                                           // default: "L1"
    category.set("api")                                       // default: "api"
    hubParent.set("api-hub")                                  // default: "api-hub"
    targets.set(listOf("all"))                                // default: ["all"]
    status.set("active")                                      // default: "active"
    generatedFrom.set("dokka")                                // default: "dokka"
    schemaVersion.set(1)                                      // default: 1
    hashFormat.set(HashFormat.COMPACT_12_HEX)                 // default: COMPACT_12_HEX
    filenameConvention.set(FilenameConvention.LEADING_DASH)   // default: LEADING_DASH
    slugSeparator.set("--")                                   // default: "--"
    kdocStatePath.set("")                                     // default: "" (uses legacy path)
    frontmatterMode.set(FrontmatterMode.STRUCTURED)           // default: STRUCTURED
    customFields.set(mapOf())                                 // default: empty
}
```

### Enum values

**`FrontmatterMode`**
- `STRUCTURED` — full 14-field YAML frontmatter (default, v0.1.0 behavior)
- `MINIMAL` — only `slug:` and `content_hash:` fields
- `NONE` — no frontmatter block emitted; pure markdown body

**`HashFormat`**
- `COMPACT_12_HEX` — 12-char compact hex e.g. `7a8836e73f62` (default, v0.1.0 behavior)
- `FULL_SHA256_WITH_PREFIX` — `sha256:<64-hex-chars>`
- `NONE` — no content hash field

**`FilenameConvention`**
- `LEADING_DASH` — leading dash prefix e.g. `-network-result.md` (default, v0.1.0 behavior)
- `PLAIN` — no leading dash e.g. `network-result.md`

## Upgrading from v0.1.0

**Coordinate change (breaking):**
```toml
# Before
dokka-markdown-plugin = { module = "com.androidcommondoc:dokka-markdown-plugin", version = "0.1.0" }

# After
dokka-markdown-plugin = { module = "io.github.oscardlfr:dokka-markdown-plugin", version = "0.2.0" }
```

**Behavior**: No `structuredMarkdown {}` block required. All defaults in v0.2.0 replicate v0.1.0 output exactly — absent the DSL block, behavior is byte-identical.

**Composite build consumers**: update the `substitute(module(...))` coordinate to `io.github.oscardlfr:dokka-markdown-plugin`.

## Building from source

```bash
git clone https://github.com/oscardlfr/dokka-markdown-plugin.git
cd dokka-markdown-plugin
./gradlew test --no-daemon       # 120 unit + 2 integration tests
./gradlew publishToMavenLocal    # install locally for testing
```

Then in your consumer project, add `mavenLocal()` to repositories instead of GitHub Packages.

## Composite build (no publish required)

During development or when testing pre-release changes, consumers can use Gradle composite build:

```kotlin
// consumer settings.gradle.kts
includeBuild("../dokka-markdown-plugin") {
    dependencySubstitution {
        substitute(module("io.github.oscardlfr:dokka-markdown-plugin"))
            .using(project(":"))
    }
}
```

Any source change in the plugin is picked up by the consumer's next build — no republish step.

## AndroidCommonDoc integration

This plugin is part of the [AndroidCommonDoc](https://github.com/oscardlfr/AndroidCommonDoc) toolkit ecosystem. If you're using AndroidCommonDoc, the `/setup` wizard step **W10** automates the installation:

```bash
cd your-project
/setup --dokka-plugin yes
```

## License

[MIT](LICENSE)
