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
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.kover)
    `maven-publish`
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}

val configureAppleTargets = rootProject.extra["configureAppleTargets"] as Boolean

kover {
    reports {
        filters {
            includes {
                classes("com.moriafly.salt.ui.JustifiedRowKt*")
            }
        }
        verify {
            rule("JustifiedRow line coverage") {
                minBound(80)
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.moriafly.salt.ui.generated.resources"
    publicResClass = true
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = project.group.toString(),
        artifactId = "salt-ui",
        version = project.version.toString()
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Salt UI")
        description.set("UI Components for Compose Multiplatform (Android/Desktop/iOS)")
        inceptionYear.set("2024")
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
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-Xallow-holdsin-contract"
        )
    }

    withSourcesJar(false)

    android {
        namespace = "com.moriafly.salt.ui"
        compileSdk = 37
        minSdk = 23

        androidResources {
            enable = true
        }

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
                baseName = "SaltUI"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.ui.test)
        }

        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.material)
            implementation(libs.compose.material3)
            implementation(libs.activity.compose)
            implementation(libs.hiddenapibypass)
        }

        val commonMain by getting
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.components.resources)
            implementation(libs.haze)
            implementation(libs.haze.blur)
            implementation(libs.haze.blur.materials)
            implementation(libs.collection)
            implementation(libs.graphics.shapes)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
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
