plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

// Robolectric helpers that read the accessibility tree as TalkBack does (#255), shared by every ui module's tests
// through testDebugImplementation, so no module keeps its own copy.
android {
    namespace = "com.migrely.app.test.talkback"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    testOptions {
        targetSdk = libs.versions.targetSdk.get().toInt()
        unitTests.isIncludeAndroidResources = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("$projectDir/../../detekt.yml")
}

dependencies {
    api(platform(libs.compose.bom))
    api(libs.compose.ui)
    api(libs.test.compose.ui.junit4)
    implementation(libs.androidx.core.ktx)
    implementation(libs.test.robolectric)

    testImplementation(libs.test.junit)
    // Debug only: the Compose test rule needs the debug test manifest.
    testDebugImplementation(libs.test.robolectric)
    testDebugImplementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.test.manifest)
}
