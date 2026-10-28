import com.android.build.api.artifact.SingleArtifact
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.Properties
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

/** The upload key, from the git-ignored `keystore.properties` at the root; null when the file is absent (BRD D102). */
val keystoreProperties = rootProject.file("keystore.properties").takeIf {
    it.exists()
}?.let { file ->
    Properties().apply { file.inputStream().use(::load) }
}

/**
 * Google Play's licence public key, which checks the signature on every purchase (BRD D107): the
 * base64 key Play Console shows under Monetization setup, set in the project's `gradle.properties`
 * (a public key) or passed as `-Pmigrely.playLicenseKey=…`. Without it a debug build skips the
 * check, a release build unlocks nothing, and the bundle for Play refuses to build.
 */
val playLicenseKey: String = providers.gradleProperty(
    "migrely.playLicenseKey"
).orElse("").get().trim()

android {
    namespace = "com.migrely.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.migrely.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 6
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "PLAY_LICENSE_KEY", "\"$playLicenseKey\"")
    }

    signingConfigs {
        if (keystoreProperties != null) {
            create("release") {
                fun key(name: String) = requireNotNull(keystoreProperties.getProperty(name)) {
                    "keystore.properties lacks $name"
                }
                storeFile = rootProject.file(key("storeFile"))
                storePassword = key("storePassword")
                keyAlias = key("keyAlias")
                keyPassword = key("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig =
                signingConfigs.getByName(if (keystoreProperties != null) "release" else "debug")
            // Play warns that the bundle has native code without debug symbols. The only native
            // code is androidx.graphics.path, which ships its libraries already stripped, so there
            // are no symbols to upload; the warning is expected and harmless.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(projects.features.report.ui)
    implementation(projects.features.report.presentation)
    implementation(projects.features.report.domain)
    implementation(projects.features.report.data)
    implementation(projects.features.insights.ui)
    implementation(projects.features.insights.presentation)
    implementation(projects.features.insights.domain)
    implementation(projects.features.insights.data)
    implementation(projects.features.subscription.ui)
    implementation(projects.features.subscription.presentation)
    implementation(projects.features.subscription.domain)
    implementation(projects.features.subscription.data)
    implementation(libs.androidx.work.runtime)
    implementation(projects.features.review.ui)
    implementation(projects.features.review.presentation)
    implementation(projects.features.review.domain)
    implementation(projects.features.review.data)
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
        // The bundle that goes to Play does not depend on assemble, so it gets the check too (BRD D44).
        tasks.matching { it.name == "assemble$name" || it.name == "bundle$name" }
            .configureEach { dependsOn(check) }
    }
}

/** The bundle for Play must check purchase signatures, so it needs the licence key (BRD D107). */
val checkPlayLicenseKey by tasks.registering {
    val key = playLicenseKey
    doLast {
        if (key.isEmpty()) {
            throw GradleException(
                "migrely.playLicenseKey is empty, so the bundle could not check purchases. " +
                    "Pass Play Console's licence key (Monetization setup) with " +
                    "-Pmigrely.playLicenseKey=… or in ~/.gradle/gradle.properties."
            )
        }
        // The key must be Play's base64 X.509 RSA public key, or every purchase would fail its
        // check and nothing would ever unlock (review M13).
        try {
            KeyFactory.getInstance("RSA")
                .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(key)))
        } catch (exception: GeneralSecurityException) {
            throw malformedKey(exception)
        } catch (exception: IllegalArgumentException) {
            throw malformedKey(exception)
        }
    }
}

fun malformedKey(cause: Exception) = GradleException(
    "migrely.playLicenseKey is not an RSA public key in base64 (${cause.message}). " +
        "Copy the whole licence key from Play Console → Monetization setup → Licensing.",
    cause
)

tasks.matching { it.name == "bundleRelease" }.configureEach { dependsOn(checkPlayLicenseKey) }

if (keystoreProperties == null) {
    tasks.matching { it.name in setOf("packageRelease", "signReleaseBundle") }.configureEach {
        doFirst { logger.warn("Release signed with the debug key: for local smoke only") }
    }
}
