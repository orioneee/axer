import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.internal.utils.getLocalProperty
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)

    id("com.codingfeline.buildkonfig") version "+"
}

fun getLatestGitTag() = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
    isIgnoreExitValue = true
}.standardOutput.asText?.get()?.trim()?.takeIf { it.isNotBlank() } ?: "0.0.0"

val libraryVersion =  getLatestGitTag()

println("Library version: $libraryVersion")

version = libraryVersion

kotlin {

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    jvmToolchain(17)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        iosX64()
    ).forEach {
        it.binaries.framework {
            baseName = "Axer"
            isStatic = false
        }
        it.binaries.all { freeCompilerArgs += "-Xadd-light-debug=enable" }

    }


    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            implementation(compose.material3AdaptiveNavigationSuite)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.datetime)
            api(libs.ktor.client.core)
            api(libs.ktor.client.serialization)
            api(libs.ktor.serialization.json)
            api(libs.ktor.client.logging)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)


            implementation(libs.navigation.compose)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.coil.compose)


            implementation(libs.napier)
            implementation(libs.multiplatform.settings)

            implementation("org.jetbrains.compose.material3.adaptive:adaptive:1.0.0-alpha03")
            implementation("org.jetbrains.compose.material3.adaptive:adaptive-layout:1.0.0-alpha03")
            implementation("org.jetbrains.compose.material3.adaptive:adaptive-navigation:1.0.0-alpha03")

            val ktor_version = libs.versions.ktor.get()

            implementation("io.ktor:ktor-server-core:${ktor_version}")
            implementation("io.ktor:ktor-server-cio:${ktor_version}")
            implementation("io.ktor:ktor-server-content-negotiation:${ktor_version}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")
            implementation("io.ktor:ktor-server-websockets:${ktor_version}")

        }


        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val jvmAndAndroid by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.okhttp)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.default.headers)
            }
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosX64Main by getting {
            dependsOn(iosMain)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain {
            dependsOn(jvmAndAndroid)
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                api(libs.ktor.client.okhttp)
                implementation(libs.koin.android)
                implementation(libs.androidx.startup.runtime)
            }
        }

        jvmMain {
            dependsOn(jvmAndAndroid)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
            }
        }

    }
}

android {
    namespace = "io.github.orioneee.axer"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

val isLocal = project.getLocalProperty("isLocal") == "true"

mavenPublishing {
    if (!isLocal) {
        publishToMavenCentral()
        signAllPublications()
    }

    coordinates(
        groupId = "io.github.orioneee",
        artifactId = "axer",
        version = libraryVersion,
    )

    pom {
        name = "Axer"
        description = "Debugging tool for Kotlin Multiplatform applications"
        inceptionYear = "2025"
        url = "https://github.com/orioneee/Axer"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "orioneee"
                name = "Danylo Perepeluk"
                url = "https://github.com/orioneee"
            }
        }
        scm {
            url = "https://github.com/orioneee/Axer"
            connection = "scm:git:git://github.com/orioneee/Axer.git"
            developerConnection = "scm:git:ssh://git@github.com/orioneee/Axer.git"
        }
    }
}


dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    packageOfResClass = "io.github.orioneee.axer.generated.resources"
    publicResClass = true
}

buildkonfig {
    packageName = "io.github.orioneee.axer.generated.configs"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", libraryVersion)
    }
}


fun generateDocumentations() {
    val version = getLatestGitTag()
    println("Latest version: '$version'")

    val templateFile = File(rootDir, "axer/template/template_README.MD")
    println("Template exists: ${templateFile.exists()}")

    val template = templateFile.readText()
    val updated = template.replace("{{AXER_VERSION}}", version)

    val outputFile = File(rootDir, "README.md")
    println("Writing to: ${outputFile.absolutePath}")
    outputFile.writeText(updated)

    println("README.md updated with version: $version")
}

generateDocumentations()
