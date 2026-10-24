package com.migrely.app.datasource.preferences.model

/** Why the user came to Migrely (BRD ONB-F1). [key] is what is stored and must never change. */
enum class GoalDataModel(val key: String) {
    RecordForDoctor("record_for_doctor"),
    FindTriggers("find_triggers"),
    FewerPainkillers("fewer_painkillers")
}
