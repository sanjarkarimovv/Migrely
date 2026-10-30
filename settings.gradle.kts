enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Migrely"
include(":app")
include(":coroutine")
include(":designsystem")
include(":features:attackdays:domain")
include(":features:patterns:domain")

setOf(
    "ui",
    "instrumentation-test", 
    "presentation",
    "presentation-test",
    "domain"
).forEach { module ->
    include(":architecture:$module")
}


setOf(
    "source",
    "implementation"
).forEach { module ->
    include(":datasource:$module")
}
setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:home:$module")
}
setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:attack:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:history:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:medication:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:reminders:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:backup:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:settings:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:onboarding:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:report:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:insights:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:subscription:$module")
}

setOf(
    "ui",
    "presentation",
    "domain",
    "data"
).forEach { module ->
    include(":features:review:$module")
}
