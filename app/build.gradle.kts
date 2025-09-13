import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Locale
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.serialization)
}

android {
    sourceSets["main"].java.srcDir(file("$buildDir/generated/source/locales"))
    signingConfigs {
        create("defaultSignature") {
            storeFile = file(project.findProperty("storeFile") ?: "testkey.jks")
            storePassword = (project.findProperty("storePassword") as String?) ?: "testkey"
            keyPassword = (project.findProperty("keyPassword") as String?) ?: "testkey"
            keyAlias = (project.findProperty("keyAlias") as String?) ?: "testkey"
	    enableV1Signing = true
	    enableV2Signing = true
	    enableV3Signing = true
	    enableV4Signing = true
        }
    }
    namespace = "dev.mr2.dpc"
    compileSdk = 36
    
    lint.checkReleaseBuilds = false
    lint.disable += "All"

    defaultConfig {
        applicationId = "dev.mr2.dpc"
        minSdk = 21
        targetSdk = 36
        versionCode = 4003
        versionName = "7.1.3"
        multiDexEnabled = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("defaultSignature")
        }
        debug {
            signingConfig = signingConfigs.getByName("defaultSignature")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        aidl = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    dependenciesInfo {
        includeInApk = false
    }
    composeCompiler {
        includeSourceInformation = false
        includeTraceMarkers = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

gradle.taskGraph.whenReady {
    project.tasks.findByPath(":app:test")?.enabled = false
    project.tasks.findByPath(":app:lint")?.enabled = false
    project.tasks.findByPath(":app:lintAnalyzeDebug")?.enabled = false
}

val outRoot = layout.buildDirectory.dir("generated/source/locales")
val targetPackageDir = "dev/mr2/dpc"

abstract class GenerateLocalesTask : DefaultTask() {
    @get:InputDirectory
    abstract val resDirInput: org.gradle.api.file.DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: org.gradle.api.file.DirectoryProperty

    @TaskAction
    fun generate() {
        val resDir = resDirInput.get().asFile
        if (!resDir.exists()) return

        val traditional = Regex("^values-([a-z]{2,3})(?:-r([A-Za-z0-9]{2,3}))?$", RegexOption.IGNORE_CASE)
        val bcp47 = Regex("^values-b\\+([a-z]{2,3})(?:\\+([A-Za-z]{4}))?(?:\\+([A-Za-z0-9]{2,3}))?$", RegexOption.IGNORE_CASE)

        data class LangInfo(val lang: String, val region: String, val script: String)

        val langs = mutableListOf<LangInfo>()

        resDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("values") }
            ?.forEach { d ->
                val name = d.name
                if (name == "values") return@forEach
                traditional.matchEntire(name)?.let { m ->
                    val language = m.groupValues[1].lowercase()
                    val region = m.groupValues.getOrNull(2)?.uppercase() ?: ""
                    langs.add(LangInfo(language, region, ""))
                    return@forEach
                }
                bcp47.matchEntire(name)?.let { m ->
                    val language = m.groupValues[1].lowercase()
                    val scriptRaw = m.groupValues.getOrNull(2) ?: ""
                    val regionRaw = m.groupValues.getOrNull(3) ?: ""
                    val script = if (scriptRaw.isNotEmpty()) scriptRaw.replaceFirstChar { it.uppercase() } else ""
                    langs.add(LangInfo(language, regionRaw.uppercase(), script))
                }
            }

        val defaultLocale = Locale.getDefault()
        if (langs.none { it.lang == defaultLocale.language && it.region == defaultLocale.country }) {
            langs.add(LangInfo(defaultLocale.language, defaultLocale.country ?: "", ""))
        }

        val unique = langs.distinctBy { "${it.lang}-${it.region}-${it.script}" }
            .sortedBy { "${it.lang}-${it.region}-${it.script}" }

        fun sanitizeResourcePart(s: String): String {
            return s.lowercase()
                .replace(Regex("[^a-z0-9]"), "_")
                .replace(Regex("_+"), "_")
                .trim('_')
        }

        fun resourceNameFor(l: LangInfo): String {
            val langPart = sanitizeResourcePart(l.lang)
            val regionPart = l.region.takeIf { it.isNotBlank() }?.let { sanitizeResourcePart(it) } ?: ""
            val scriptPart = l.script.takeIf { it.isNotBlank() }?.let { sanitizeResourcePart(it) } ?: ""
            val parts = listOfNotNull(langPart, regionPart.ifEmpty { null }, scriptPart.ifEmpty { null })
            return "lang_" + parts.joinToString("_")
        }

        val outDir = outputDir.get().asFile.apply { mkdirs() }
        val outFile = File(outDir, "GeneratedLocales.kt")
        outFile.bufferedWriter(Charsets.UTF_8).use { w ->
            w.appendLine("package dev.mr2.dpc")
            w.appendLine()
            w.appendLine("import android.content.Context")
            w.appendLine("import dev.mr2.dpc.R")
            w.appendLine()
            w.appendLine("data class LanguageRes(val lang: String, val region: String, val nameRes: Int)")
            w.appendLine()
            w.appendLine("object BuiltInLocales {")
            w.appendLine("    val LANGUAGES = listOf(")
            unique.forEachIndexed { idx, li ->
                var langResName = "${li.lang}"
		if (!li.region.isEmpty()) langResName += "_${li.region}"
                val comma = if (idx < unique.size - 1) "," else ""
                w.appendLine("        LanguageRes(\"${li.lang}\", \"${li.region}\", R.string.lang_${langResName})$comma")
            }
            w.appendLine("    )")
            w.appendLine()
            w.appendLine("    fun toLanguages(context: Context): List<Language> =")
            w.appendLine("        LANGUAGES.map { Language(it.lang, it.region, context.getString(it.nameRes)) }")
            w.appendLine("}")
        }
    }
}

val generateLocales = tasks.register<GenerateLocalesTask>("generateLocales") {
    resDirInput.set(layout.projectDirectory.dir("src/main/res"))
    outputDir.set(outRoot)
}

val stampFile = project.findProperty("stampFile") as String?
val stampAlias = project.findProperty("stampAlias") as String?
val stampStorePass = project.findProperty("stampStorePass") as String?

afterEvaluate {
    if (stampFile != null && stampAlias != null && stampStorePass != null) {
        tasks.register<Exec>("stampReleaseApk") {
            val apkPath = layout.buildDirectory.file("outputs/apk/release/app-release.apk")
            doFirst {
                commandLine(
                    "apksigner", "sign",
		    "--ks", "${project.findProperty("storeFile") ?: "testkey.jks"}",
		    "--ks-key-alias", project.findProperty("keyAlias") ?: "testkey",
        "--ks-pass", "pass:${project.findProperty("storePassword") ?: "testkey"}",
        "--key-pass", "pass:${project.findProperty("keyPassword") ?: "testkey"}",
		    "--force-stamp-overwrite",
		    "--stamp-signer",
                    "--ks", stampFile,
                    "--ks-key-alias", stampAlias,
                    "--ks-pass", "pass:$stampStorePass",
		    "--v1-signing-enabled", "true",
		    "--v2-signing-enabled", "true",
		    "--v3-signing-enabled", "true",
		    "--v4-signing-enabled", "true",
		    "--rotation-min-sdk-version", "33",
                    apkPath.get().asFile.absolutePath
                )
            }
        }

        tasks.named("assembleRelease") {
            finalizedBy("stampReleaseApk")
        }
    }
}

tasks.named("preBuild") {
    dependsOn(generateLocales)
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.api)
    implementation(libs.dhizuku.api)
    implementation(libs.dhizuku.server.api)
    implementation(libs.androidx.fragment)
    implementation(libs.hiddenApiBypass)
    implementation(libs.libsu)
    implementation(libs.serialization)
    implementation(libs.jbcrypt)
    implementation(kotlin("reflect"))
}
