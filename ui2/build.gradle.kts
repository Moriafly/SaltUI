plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.vanniktech.maven.publish)
    `maven-publish`
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.moriafly",
        artifactId = "salt-ui",
        version = libs.versions.version.get()
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

    // Enable GPG signing for all publications
    signAllPublications()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-Xallow-holdsin-contract"
        )
    }

    withSourcesJar(publish = false)

    androidTarget {
        publishLibraryVariants("release")
        // publishLibraryVariantsGroupedByFlavor = true
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SaltUI"
            isStatic = true
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(libs.haze)
            implementation(libs.haze.materials)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jna)
            implementation(libs.jna.platform)
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

android {
    namespace = "com.moriafly.salt.ui"
    compileSdk = 36

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 23
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
    }
}
