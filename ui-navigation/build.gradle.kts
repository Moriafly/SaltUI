/*
 * Salt UI
 * Copyright (C) 2026 Moriafly
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
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
}

val configureAppleTargets = rootProject.extra["configureAppleTargets"] as Boolean

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "salt-ui-navigation",
        version = project.version.toString()
    )

    pom {
        name.set("Salt UI Navigation")
        description.set("Navigation components for Compose Multiplatform (Android/Desktop/iOS)")
        inceptionYear.set("2026")
        url.set("https://github.com/Moriafly/SaltUI")

        licenses {
            license {
                name.set("Apache-2.0 license")
                url.set("https://github.com/Moriafly/SaltUI/blob/main/LICENSE")
            }
        }

        developers {
            developer {
                id.set("Moriafly")
                name.set("Moriafly")
                email.set("moriafly@163.com")
            }
        }

        scm {
            url.set("https://github.com/Moriafly/SaltUI")
        }
    }

    publishToMavenCentral()
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
        namespace = "com.moriafly.salt.ui.navigation"
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
                baseName = "SaltUINavigation"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":ui2"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.navigationevent.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.ui.test)
        }
    }

    targets.withType<KotlinAndroidTarget>().configureEach {
        publishLibraryVariants("release")
    }
}
