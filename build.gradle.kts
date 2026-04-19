plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    `maven-publish`
    `java-gradle-plugin`
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
}

// java-gradle-plugin is applied only for TestKit's plugin-under-test-metadata.properties
// generation (GoldenIntegrationTest needs it). We are NOT shipping a Gradle plugin —
// consumers use dokkaPlugin(...) via the DokkaPlugin SPI. Disable the automatic
// plugin-marker publication so publish:
//   - Publishes only our main maven publication
//   - Does NOT publish a io.github.oscardlfr.dokka-markdown-plugin.gradle.plugin marker
gradlePlugin {
    plugins {
        create("dokkaMarkdown") {
            id = "io.github.oscardlfr.dokka-markdown-plugin"
            implementationClass = "io.github.oscardlfr.dokka.markdown.StructuredMarkdownPlugin"
        }
        create("dokkaMarkdownGradle") {
            id = "io.github.oscardlfr.dokka-markdown-config"
            implementationClass = "io.github.oscardlfr.dokka.markdown.StructuredMarkdownGradlePlugin"
        }
    }
    isAutomatedPublishing = false
}

// Maven Central only — no plugins.gradle.org needed (corporate SSL proxy constraint).
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.dokka:dokka-core:2.2.0")
    compileOnly("org.jetbrains.dokka:dokka-base:2.2.0")
    compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")
    compileOnly(gradleKotlinDsl())
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.jetbrains.dokka:dokka-core:2.2.0")
    testImplementation("org.jetbrains.dokka:dokka-base:2.2.0")
    testImplementation("org.jetbrains.dokka:dokka-gradle-plugin:2.2.0")
    testImplementation(gradleTestKit())
    testImplementation(gradleKotlinDsl())
}


kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1
    exclude("**/GoldenIntegrationTest*")
    systemProperty("pluginJarDir", layout.buildDirectory.dir("libs").get().asFile.absolutePath)
}

val integrationTest by tasks.registering(Test::class) {
    useJUnitPlatform()
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/GoldenIntegrationTest*")
    dependsOn(tasks.jar, tasks.pluginUnderTestMetadata, tasks.processTestResources)
    jvmArgs("-Xmx1g")
    maxParallelForks = 1
    systemProperty("pluginJarDir", layout.buildDirectory.dir("libs").get().asFile.absolutePath)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.oscardlfr"
            artifactId = "dokka-markdown-plugin"
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/oscardlfr/dokka-markdown-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

kover {
    currentProject {
        instrumentation {
            disabledForTestTasks.add("integrationTest")
        }
    }
    reports {
        filters {
            excludes {
                // Renderer integration-tested via GradleRunner (Wave 1 byte-identical golden + Wave 4 matrix).
                // Kover cannot instrument TestKit subprocess. Coverage maintained via integration safety gate.
                // DO NOT exclude other classes — unit tests must cover remaining code paths.
                classes("io.github.oscardlfr.dokka.markdown.StructuredMarkdownRenderer")
            }
        }
        verify {
            rule {
                minBound(85, kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE)
                minBound(80, kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
            }
        }
    }
}

tasks.named("check") {
    dependsOn(integrationTest)
    dependsOn("koverVerify")
}
