import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform")
}

group = "cn.fj.loli"
version = "1.0.1"

val localIdePath = providers.gradleProperty("localIdePath")
val hexSupportPluginPath = providers.gradleProperty("hexSupportPluginPath")
val adjacentHexSupport = layout.projectDirectory.file(
    "../idea-hex-support/build/distributions/idea-hex-support-3.0.1.zip"
)

dependencies {
    implementation("io.kaitai:kaitai-struct-compiler_2.13:0.11")
    implementation("io.kaitai:kaitai-struct-runtime:0.11")
    implementation("org.yaml:snakeyaml:2.0")

    intellijPlatform {
        if (localIdePath.isPresent) {
            local(localIdePath.get())
        } else {
            intellijIdea("2025.1")
        }

        when {
            hexSupportPluginPath.isPresent -> localPlugin(hexSupportPluginPath.get())
            adjacentHexSupport.asFile.isFile -> localPlugin(adjacentHexSupport.asFile)
            else -> plugin("cn.fj.loli.hexsupport:3.0.0")
        }
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.12.2")
}

intellijPlatform {
    pluginConfiguration {
        id = "cn.fj.loli.kaitaistructsupport"
        name = "Kaitai Struct Support"
        version = project.version.toString()
        description = """
            <p>Language support for Kaitai Struct YAML (.ksy) files in IntelliJ-based IDEs.</p>
            <p>Provides YAML-style syntax highlighting, context-aware KSY completion, and an optional Hex Support structure analysis provider powered by the bundled official Kaitai Struct Compiler.</p>
        """.trimIndent()
        changeNotes = """
            <ul>
                <li>1.0.1: Standardize the shared Hex Support structure analysis phrase in Marketplace metadata so Hex Support can discover this provider without maintaining a plugin ID.</li>
                <li>1.0.0: Add .ksy file recognition, YAML-style syntax highlighting, context-aware completion for KSY keys, primitive types and values, comment and quote support, configurable colors, and an optional Hex Support structure provider.</li>
            </ul>
        """.trimIndent()
        vendor {
            name = "feiju12138"
            url = "https://github.com/feiju12138/idea-kaitai-struct-support"
        }
        ideaVersion {
            sinceBuild = "251"
        }
    }

    pluginVerification {
        externalPrefixes = listOf("cn.fj.loli.hexsupport.structure")
        ides {
            current()
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        from("LICENSE") {
            into("META-INF")
            rename { "LICENSE.txt" }
        }
        from("THIRD_PARTY_NOTICES.md") {
            into("META-INF")
        }
        from("licenses") {
            into("META-INF/licenses")
        }
    }

    named("buildSearchableOptions") {
        enabled = false
    }

    named("prepareJarSearchableOptions") {
        enabled = false
    }

    named("jarSearchableOptions") {
        enabled = false
    }
}
