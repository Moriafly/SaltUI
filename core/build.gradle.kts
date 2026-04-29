/*
 * Salt UI
 * Copyright (C) 2025 Moriafly
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
}

val isPublishingToMavenLocal = gradle.startParameter.taskNames.any {
    it.contains("publishToMavenLocal", ignoreCase = true)
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.moriafly",
        artifactId = "salt-core",
        version = if (isPublishingToMavenLocal) libs.versions.mavenLocalVersion.get() else libs.versions.version.get()
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

    // Enable GPG signing for all publications
    signAllPublications()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    withSourcesJar(false)

    android {
        namespace = "com.moriafly.salt.core"
        compileSdk = 36
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

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SaltCore"
            isStatic = true
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

        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting

        @Suppress("unused")
        val iosMain by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }

    targets.withType<KotlinAndroidTarget>().configureEach {
        publishLibraryVariants("release")
    }
}
