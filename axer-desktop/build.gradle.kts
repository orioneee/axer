import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

fun getLatestGitTag() = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
    isIgnoreExitValue = true
}.standardOutput
    .asText?.get()?.trim()?.takeIf { it.isNotBlank() } ?: "0.0.0"

val libraryVersion = getLatestGitTag()
version = libraryVersion

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {

                implementation(compose.material3)
                implementation(libs.kotlin.stdlib)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.okhttp)

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.ktor.client.content.negotiation)

                implementation(compose.desktop.currentOs)
                implementation(project(":axer"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "axer-desktop"
            packageVersion = libraryVersion
        }
    }
}