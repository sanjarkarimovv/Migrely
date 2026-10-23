package com.migrely.app.datasource.attack.model

/** Built-in triggers. [key] is what the database stores and must never change (BRD D25). */
enum class BuiltInTriggerDataModel(val key: String) {
    Stress("stress"),
    TooLittleSleep("too_little_sleep"),
    TooMuchSleep("too_much_sleep"),
    SkippedMeal("skipped_meal"),
    Dehydration("dehydration"),
    Alcohol("alcohol"),
    Caffeine("caffeine"),
    Period("period"),
    Weather("weather"),
    BrightLightOrScreens("bright_light_or_screens"),
    Smell("smell"),
    PhysicalEffort("physical_effort"),
    Travel("travel")
}
