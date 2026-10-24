package com.migrely.app.datasource.attack.model

/** One stored location, symptom or trigger of an attack. Custom tags are referenced by their tag id. */
sealed interface AttackTagDataModel {
    data class Location(val location: HeadLocationDataModel) : AttackTagDataModel

    data class BuiltInSymptom(val symptom: BuiltInSymptomDataModel) : AttackTagDataModel

    data class BuiltInTrigger(val trigger: BuiltInTriggerDataModel) : AttackTagDataModel

    data class CustomSymptom(val tagId: String) : AttackTagDataModel

    data class CustomTrigger(val tagId: String) : AttackTagDataModel
}
