plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
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

dependencies {
    // Flow is part of the datasources' API (PreferencesDataSource.themeChanges).
    api(libs.kotlinx.coroutines.core)
}
