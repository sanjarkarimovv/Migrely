package com.migrely.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.functions
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withPublicOrDefaultModifier
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withTopLevel
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

private val publicFunctionNameRegex = "^on[A-Z0-9].*$".toRegex()

class ArchitecturePresentationTest {
    @Test
    fun `ViewModel public function names describe events`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .functions()
            .withPublicOrDefaultModifier()
            .assertTrue { it.hasNameMatching(publicFunctionNameRegex) }
    }

    @Test
    fun `ViewModels reside in a presentation-viewmodel package`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.resideInPackage("..presentation.viewmodel") }
    }

    @Test
    fun `Presentation models are suffixed with PresentationModel`() {
        // Production only: tests for a model live in the model's package (docs/ARCHITECTURE.md section 13).
        Konsist.scopeFromProduction()
            .classes()
            .withPackage("..presentation..model")
            .withTopLevel()
            .assertTrue { it.hasNameEndingWith("PresentationModel") }
    }
}
