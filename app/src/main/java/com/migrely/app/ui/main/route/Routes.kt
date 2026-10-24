// All navigation routes live in this file (docs/ARCHITECTURE.md section 5). While only one route exists,
// ktlint's single-declaration filename rule would demand "Home.kt"; remove this suppression once a
// second route is added.
@file:Suppress("ktlint:standard:filename")

package com.migrely.app.ui.main.route

import kotlinx.serialization.Serializable

@Serializable
object Home
