import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

fun getLatestGitTag() = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
    isIgnoreExitValue = true
}.standardOutput
    .asText?.get()?.trim()?.takeIf { it.isNotBlank() } ?: "0.0.0"

val libraryVersion = getLatestGitTag()

println("Library version: $libraryVersion")

version = libraryVersion

kotlin {
    jvmToolchain(21)

    androidTarget { publishLibraryVariants("release") }
    jvm()
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "Axer"
            isStatic = false
        }
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
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

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

            implementation(libs.kodeview)


            implementation(libs.composable.table)
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
            }
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain {
            dependsOn(jvmAndAndroid)
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.koin.android)
            }
        }

        jvmMain {
            dependsOn(jvmAndAndroid)
            dependencies {
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
            }
        }

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

}

android {
    namespace = "io.github.orioneee.axer"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

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
//    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

val generateReadmeDocsWithLatestVersion by tasks.registering(Exec::class) {
    group = "documentation"
    description = "Generates README.md with the latest version of the library"

    // Use inputs to declare the template file, so Gradle can track it
    inputs.file("template_README.MD")
    outputs.file("README.md")

    // Use commandLine with lambda to defer evaluation
    commandLine(
        "bash",
        "-c",
        "sed 's/{{AXER_VERSION}}/${libraryVersion}/' template_README.MD > README.md"
    )

    doLast {
        println("README.md generated with the latest version: $libraryVersion")
    }
}

