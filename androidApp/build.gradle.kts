import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.PathSensitivity
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.compose.multiplatform)
}

abstract class BuildVersionCode : ValueSource<Int, ValueSourceParameters.None> {
    override fun obtain(): Int = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyyMMddHH"))
        .toInt()
}

abstract class NameApk : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val apkDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val transformationRequest: Property<ArtifactTransformationRequest<NameApk>>

    @get:Internal
    abstract val artifactsLoader: Property<BuiltArtifactsLoader>

    @TaskAction
    fun nameApk() {
        val outputDir = outputDirectory.get().asFile.canonicalFile
        val previousApks = artifactsLoader.get().load(outputDirectory.get())
            ?.elements
            ?.map { File(it.outputFile).canonicalFile }
            .orEmpty()
        val currentApks = mutableSetOf<File>()

        transformationRequest.get().submit(this) { artifact ->
            val version = requireNotNull(artifact.versionName) { "APK version name is missing" }
            val versionCode = requireNotNull(artifact.versionCode) { "APK version code is missing" }
            val filters = artifact.filters.joinToString("") { "-${it.identifier}" }
            val fileName = "saltui-$version-$versionCode$filters.apk"
            val destination = outputDirectory.get().file(fileName).asFile
            destination.parentFile.mkdirs()
            File(artifact.outputFile).copyTo(destination, overwrite = true)
            currentApks.add(destination.canonicalFile)
            destination
        }

        previousApks.forEach { previousApk ->
            if (previousApk.parentFile == outputDir &&
                previousApk.name.startsWith("saltui-") &&
                previousApk.name.endsWith(".apk") &&
                previousApk !in currentApks &&
                previousApk.exists()
            ) {
                check(previousApk.delete()) { "Cannot remove previous APK: $previousApk" }
            }
        }
    }
}

android {
    namespace = "com.moriafly.salt.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.moriafly.salt.ui"
        minSdk = 24
        targetSdk = 37
        versionName = project.version.toString()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants { variant ->
        val buildVersionCode = providers.of(BuildVersionCode::class) {}
        variant.outputs.forEach { output ->
            output.versionCode.set(buildVersionCode)
        }
        val taskName = "name${variant.name.replaceFirstChar { it.uppercase() }}Apk"
        val nameTask = tasks.register<NameApk>(taskName) {
            artifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
        }
        val request = variant.artifacts
            .use(nameTask)
            .wiredWithDirectories(NameApk::apkDirectory, NameApk::outputDirectory)
            .toTransformMany(SingleArtifact.APK)
        nameTask.configure {
            transformationRequest.set(request)
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)

    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
}
