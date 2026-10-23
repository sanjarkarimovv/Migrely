package com.migrely.app.datasource.attack.model

/** Where it hurts. [key] is what the database stores and must never change (BRD D25). */
enum class HeadLocationDataModel(val key: String) {
    Left("left"),
    Right("right"),
    BothSides("both_sides"),
    Forehead("forehead"),
    BackOfHead("back_of_head"),
    AroundEyes("around_eyes"),
    WholeHead("whole_head")
}
