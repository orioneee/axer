import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val noOpJarTask = project(":axer-no-op").tasks.named<Jar>("jvmJar")

val relocatedNoOpJar = tasks.register<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("relocateNoOpJar") {
    archiveClassifier.set("no-op-relocated")
    from(noOpJarTask.map { zipTree(it.archiveFile) })
    relocate("io.github.orioneee", "io.github.orioneee_no_op")
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    jvm()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
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
            implementation(libs.napier)


            implementation(project(":axer"))

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("io.github.classgraph:classgraph:4.8.181")
            implementation(kotlin("reflect"))
            implementation(project(":axer"))
            implementation(files(relocatedNoOpJar.get().archiveFile))
        }

       val  androidUnitTest by getting {
           dependsOn(commonTest.get())
            dependencies {
                implementation(kotlin("test"))
                implementation("io.github.classgraph:classgraph:4.8.181")
                implementation(kotlin("reflect"))
                implementation(project(":axer"))
                implementation(files(relocatedNoOpJar.get().archiveFile))
            }
        }


        val androidAndJvm by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val androidMain by getting {
            dependsOn(androidAndJvm)
            dependencies {
                implementation(libs.androidx.activityCompose)
                implementation(libs.kotlinx.coroutines.android)

                implementation(libs.accompanist.permissions)
                implementation(libs.koin.android)
                implementation("com.github.chuckerteam.chucker:library:4.1.0")

            }
        }

        val jvmMain by getting {
            dependsOn(androidAndJvm)
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.permissions)
                implementation(libs.permissions.notifications)
                implementation(libs.permissions.compose)
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
    }
}

android {
    namespace = "sample.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()

        applicationId = "sample.app.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isMinifyEnabled = false
            isDebuggable = false
        }
    }
}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "sample"
            packageVersion = "1.0.0"
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
