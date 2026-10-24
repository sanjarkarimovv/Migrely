package com.migrely.app.datasource.attack.model

/** Built-in symptoms. [key] is what the database stores and must never change (BRD D25). */
enum class BuiltInSymptomDataModel(val key: String) {
    Aura("aura"),
    Nausea("nausea"),
    Vomiting("vomiting"),
    LightSensitivity("light_sensitivity"),
    SoundSensitivity("sound_sensitivity"),
    SmellSensitivity("smell_sensitivity"),
    Dizziness("dizziness"),
    NeckPain("neck_pain")
}
