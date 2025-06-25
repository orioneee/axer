plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)

    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

val libraryVersion = "1.0.10"

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
            implementation(libs.ktor.client.core)
            implementation(compose.material3)
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
            }
        }

        val jvmAndAndroid by creating {
            dependsOn(commonMain.get())
            dependencies {
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
            }
        }

        jvmMain {
            dependsOn(jvmAndAndroid)
            dependencies {
            }
        }

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }

}

android {
    namespace = "com.oriooneee.axer"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
}

publishing {
    publications {
        create<MavenPublication>("kmpLibrary") {
            from(components["kotlin"])

            groupId = "com.oriooneee.axer-no-op"
            artifactId = "axer"
            version = libraryVersion
        }
    }
    repositories {
        mavenLocal()
    }
}
