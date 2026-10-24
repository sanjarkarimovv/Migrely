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
