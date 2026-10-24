import com.android.build.api.artifact.SingleArtifact
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${rootProject.projectDir}/detekt.yml")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.migrely.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.migrely.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.pickFirsts += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.room.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    implementation(projects.designsystem)
    implementation(projects.coroutine)
    implementation(projects.architecture.ui)
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)
    implementation(projects.datasource.source)
    implementation(projects.datasource.implementation)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.konsist)
    testImplementation(libs.test.mockito.kotlin)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso.core)
    implementation(projects.features.home.ui)
    implementation(projects.features.home.presentation)
    implementation(projects.features.home.domain)
    implementation(projects.features.home.data)
    implementation(projects.features.attack.ui)
    implementation(projects.features.attack.presentation)
    implementation(projects.features.attack.domain)
    implementation(projects.features.attack.data)
    implementation(projects.features.history.ui)
    implementation(projects.features.history.presentation)
    implementation(projects.features.history.domain)
    implementation(projects.features.history.data)
    implementation(projects.features.medication.ui)
    implementation(projects.features.medication.presentation)
    implementation(projects.features.medication.domain)
    implementation(projects.features.medication.data)
    implementation(projects.features.reminders.ui)
    implementation(projects.features.reminders.presentation)
    implementation(projects.features.reminders.domain)
    implementation(projects.features.reminders.data)
    implementation(projects.features.backup.ui)
    implementation(projects.features.backup.presentation)
    implementation(projects.features.backup.domain)
    implementation(projects.features.backup.data)
    implementation(projects.features.settings.ui)
    implementation(projects.features.settings.presentation)
    implementation(projects.features.settings.domain)
    implementation(projects.features.settings.data)
    implementation(projects.features.onboarding.ui)
    implementation(projects.features.onboarding.presentation)
    implementation(projects.features.onboarding.domain)
    implementation(projects.features.onboarding.data)
    implementation(libs.androidx.work.runtime)
}

/**
 * Permissions Migrely must never ship, whichever library declares them (issue #36): no network of any kind, no exact
 * alarms (BRD EC-REM-03), no foreground work, no storage, location, contacts, camera, microphone or advertising ID.
 */
val forbiddenPermissions = listOf(
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
    "android.permission.ACCESS_WIFI_STATE",
    "android.permission.CHANGE_NETWORK_STATE",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.SCHEDULE_EXACT_ALARM",
    "android.permission.USE_EXACT_ALARM",
    "android.permission.READ_EXTERNAL_STORAGE",
    "android.permission.WRITE_EXTERNAL_STORAGE",
    "android.permission.MANAGE_EXTERNAL_STORAGE",
    "android.permission.ACCESS_FINE_LOCATION",
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_CONTACTS",
    "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO",
    "com.google.android.gms.permission.AD_ID"
)

/** Fails the build when the merged manifest asks for a forbidden permission. */
abstract class CheckPermissionsTask : DefaultTask() {
    @get:InputFile
    abstract val mergedManifest: RegularFileProperty

    @get:Input
    abstract val forbidden: ListProperty<String>

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun check() {
        val requested = Regex("""<uses-permission(?:-sdk-23)?\s[^>]*android:name="([^"]+)"""")
            .findAll(mergedManifest.get().asFile.readText())
            .map { it.groupValues[1] }
            .toSet()
        val found = forbidden.get().filter { it in requested }
        if (found.isNotEmpty()) {
            throw GradleException(
                "The merged manifest asks for forbidden permissions: $found. Remove them with tools:node=\"remove\"."
            )
        }
        report.get().asFile.writeText(requested.sorted().joinToString("\n"))
    }
}

androidComponents {
    onVariants { variant ->
        val name = variant.name.replaceFirstChar(Char::uppercase)
        val check = tasks.register<CheckPermissionsTask>("check${name}Permissions") {
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            forbidden.set(forbiddenPermissions)
            report.set(layout.buildDirectory.file("reports/permissions/${variant.name}.txt"))
        }
        tasks.matching { it.name == "assemble$name" }.configureEach { dependsOn(check) }
    }
}
