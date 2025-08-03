import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    id("com.codingfeline.buildkonfig") version "+"
}

val axerVersion: String by project

version = axerVersion


kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.material3)
                implementation(libs.kotlin.stdlib)
                implementation(compose.materialIconsExtended)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.serialization)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.okhttp)

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.navigation.compose)
                implementation(compose.components.resources)
                implementation(project(":axer"))
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
            implementation(libs.kotlinx.coroutines.android)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("com.malinskiy.adam:adam:0.5.10")
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Axer Debbugger"
            packageVersion = axerVersion.substringAfter('-').substringBeforeLast("-")


            windows {
                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))

            }
            macOS {
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
            }
            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
            }

            windows.shortcut = true
            windows.upgradeUuid = "904679ed-6445-4d0e-b66e-ca96688e81b3"
        }
    }
}

android {
    namespace = "io.orioneee.axer.debugger"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 26
        targetSdk = libs.versions.compileSdk.get().toInt()

        applicationId = "io.orioneee.axer.debugger"
        versionCode = 1
        versionName = axerVersion
    }

    buildTypes {
        debug {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

buildkonfig {
    packageName = "io.orioneee.axer.debugger"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", axerVersion)
    }
}
