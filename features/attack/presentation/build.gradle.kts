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
    config.setFrom("$projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.attack.domain)
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockito.kotlin)
    testImplementation(libs.test.kotlinx.coroutines)
    testImplementation(projects.architecture.presentationTest)
}
