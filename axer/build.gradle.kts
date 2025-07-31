import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.compose.internal.utils.getLocalProperty
import org.jetbrains.dokka.gradle.DokkaTask

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
    alias(libs.plugins.dokka)
}

val axerVersion: String by project
println("Axer version: $axerVersion")

version = axerVersion

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

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.server.websockets)

            implementation("com.sebastianneubauer.jsontree:jsontree:2.5.0")
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
                implementation("androidx.lifecycle:lifecycle-process:2.9.2")


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
        version = axerVersion,
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
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", axerVersion)
    }
}


fun generateDocumentations() {
    val versions = mapOf(
        "AXER_VERSION" to axerVersion,
        "KOTLIN_VERSION" to libs.versions.kotlin.get(),
        "COMPOSE_VERSION" to libs.versions.compose.get(),
        "KTOR_VERSION" to libs.versions.ktor.get(),
        "KOIN_VERSION" to libs.versions.koin.get(),
        "ROOM_VERSION" to libs.versions.room.get(),
        "OKHTTP_VERSION" to libs.versions.okhttp.get(),
        "NAPIER_VERSION" to libs.versions.napier.get(),
        "KOTLINX_COROUTINES_VERSION" to libs.versions.kotlinx.coroutines.get(),
        "KOTLINX_SERIALIZATION_VERSION" to libs.versions.kotlinx.serialization.get(),
        "KOTLINX_DATETIME_VERSION" to libs.versions.kotlinx.datetime.get(),
        "ACCOMPANIST_PERMISSIONS_VERSION" to libs.versions.accompanistPermissions.get(),
        "COIL_COMPOSE_VERSION" to libs.versions.coilCompose.get(),
        "NAVIGATION_COMPOSE_VERSION" to libs.versions.navigationCompose.get(),
        "MIN_SDK" to libs.versions.minSdk.get()
    )


    val templateFile = File(rootDir, "axer/template/template_README.MD")
    println("Template exists: ${templateFile.exists()}")

    if (!templateFile.exists()) {
        println("Template file not found!")
        return
    }

    var template = templateFile.readText()

    // Replace all placeholders
    versions.forEach { (key, value) ->
        template = template.replace("{{${key}}}", value)
    }

    val outputFile = File(rootDir, "README.md")
    println("Writing to: ${outputFile.absolutePath}")
    outputFile.writeText(template)

    println("README.md updated with versions.")
}

generateDocumentations()



dokka {
    moduleName.set("Axer")
    basePublicationsDirectory = layout.settingsDirectory.dir("docs")
}