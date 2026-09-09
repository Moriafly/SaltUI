/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
}

val configureAppleTargets = rootProject.extra["configureAppleTargets"] as Boolean

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = project.group.toString(),
        artifactId = "salt-core",
        version = project.version.toString()
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Salt Core")
        description.set("UI Components for Compose Multiplatform (Android/Desktop/iOS)")
        inceptionYear.set("2025")
        url.set("https://github.com/Moriafly/SaltUI")

        licenses {
            license {
                name.set("Apache-2.0 license")
                url.set("https://github.com/Moriafly/SaltUI/blob/main/LICENSE")
            }
        }

        // Specify developer information
        developers {
            developer {
                id.set("Moriafly")
                name.set("Moriafly")
                email.set("moriafly@163.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/Moriafly/SaltUI")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral()

    // GitHub/CI snapshots are authenticated by their repository and remain unsigned.
    if (rootProject.extra["signPublications"] as Boolean) {
        signAllPublications()
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    withSourcesJar(false)

    android {
        namespace = "com.moriafly.salt.core"
        compileSdk = 37
        minSdk = 23

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    jvm("desktop")

    if (configureAppleTargets) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "SaltCore"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.hiddenapibypass)
        }

        val commonMain by getting
        commonMain.dependencies {
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }

        if (configureAppleTargets) {
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting

            @Suppress("unused")
            val iosMain by creating {
                dependsOn(commonMain)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }
        }
    }

    targets.withType<KotlinAndroidTarget>().configureEach {
        publishLibraryVariants("release")
    }
}
