import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.28.0"
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.moriafly",
        artifactId = "salt-ui",
        version = "2.0.0-alpha01"
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Salt UI")
        description.set("Moriafly Salt UI")
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
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}

kotlin {
    withSourcesJar(publish = false)

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }

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
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.material)
            implementation(libs.compose.material3)
            implementation(libs.activity.compose)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = "com.moriafly.salt.ui"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}