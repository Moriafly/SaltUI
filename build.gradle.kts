plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.plugin.compose) apply false
    alias(libs.plugins.kotlin.plugin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
    alias(libs.plugins.kotlinx.atomicfu) apply false
}

val isPublishingToMavenLocal = gradle.startParameter.taskNames.any {
    val taskName = it.substringAfterLast(':')
    taskName == "publishToMavenLocal" ||
        taskName.endsWith("PublicationToMavenLocal")
}

val defaultPublicationVersion = if (isPublishingToMavenLocal) {
    libs.versions.mavenLocalVersion.get()
} else {
    libs.versions.version.get()
}

val publicationVersion = providers.gradleProperty("publicationVersion")
    .getOrElse(defaultPublicationVersion)

allprojects {
    group = "io.github.moriafly"
    version = publicationVersion
}
