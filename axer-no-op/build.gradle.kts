import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)

    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

val libraryVersion: String by project

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
            implementation(libs.sqlite.bundled)

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
        artifactId = "axer-no-op",
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
