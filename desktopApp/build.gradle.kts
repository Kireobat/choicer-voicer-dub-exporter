import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class GenerateAppVersion : DefaultTask() {
    @get:Input
    abstract val appVersion: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package eu.kireobat.choicer_voicer_dub_exporter

            internal const val APP_VERSION = "${appVersion.get()}"
            """.trimIndent()
        )
    }
}

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.ffmpeg)
}

compose.desktop {
    application {
        mainClass = "eu.kireobat.choicer_voicer_dub_exporter.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "eu.kireobat.choicer_voicer_dub_exporter"
            packageVersion = providers.gradleProperty("version").get()
        }
    }
}

val appVersion = providers.gradleProperty("version")
val generatedVersionDir = layout.buildDirectory.dir("generated/sources/appVersion/kotlin")

val generateAppVersion by tasks.registering(GenerateAppVersion::class) {
    appVersion.set(providers.gradleProperty("version"))
    outputFile.set(
        generatedVersionDir.map {
            it.file("eu/kireobat/choicer_voicer_dub_exporter/AppVersion.kt")
        }
    )
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generatedVersionDir)
}

tasks.named("compileKotlin") {
    dependsOn(generateAppVersion)
}