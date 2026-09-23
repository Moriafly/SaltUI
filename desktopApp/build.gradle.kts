import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.plugin.compose)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.compose.ui.test)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        if (System.getProperty("os.name").startsWith("Linux")) {
            jvmArgs += listOf(
                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt=ALL-UNNAMED"
            )
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.moriafly.salt.ui"
            packageVersion = "1.0.0"
        }
    }
}
