import org.gradle.api.publish.PublishingExtension

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
    alias(libs.plugins.kover) apply false
}

val isPublishingToMavenLocal = gradle.startParameter.taskNames.any {
    val taskName = it.substringAfterLast(':')
    taskName == "publishToMavenLocal" ||
        taskName.endsWith("PublicationToMavenLocal")
}
val isPublishingSnapshot = providers.gradleProperty("snapshotPublication")
    .map { it.toBooleanStrict() }
    .getOrElse(false)

val isWindowsOrLinux = providers.systemProperty("os.name")
    .map { osName ->
        osName.startsWith("Windows", ignoreCase = true) ||
            osName.startsWith("Linux", ignoreCase = true)
    }
    .get()

extra["configureAppleTargets"] =
    !(isPublishingToMavenLocal || isPublishingSnapshot) || !isWindowsOrLinux
extra["signPublications"] = !isPublishingSnapshot

val defaultPublicationVersion = if (isPublishingToMavenLocal || isPublishingSnapshot) {
    libs.versions.mavenLocalVersion.get()
} else {
    libs.versions.version.get()
}

val publicationVersion = providers.gradleProperty("publicationVersion")
    .getOrElse(defaultPublicationVersion)

allprojects {
    group = "io.github.moriafly"
    version = publicationVersion

    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            repositories {
                providers.environmentVariable("CI_MAVEN_REPOSITORY").orNull?.let { repositoryUrl ->
                    maven {
                        name = "Ci"
                        url = uri(repositoryUrl)
                    }
                }
                if (isPublishingSnapshot) {
                    maven {
                        name = "GitHubPackages"
                        url = uri(
                            "https://maven.pkg.github.com/" +
                                providers.environmentVariable("GITHUB_REPOSITORY")
                                    .getOrElse("Moriafly/SaltUI")
                        )
                        credentials {
                            username = providers.environmentVariable("GITHUB_ACTOR")
                                .getOrElse("github-actions")
                            password = providers.environmentVariable("GITHUB_TOKEN").orNull
                        }
                    }
                }
            }
        }
    }
}
