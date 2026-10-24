package com.migrely.app.architecture.ui.navigation.exception

import com.migrely.app.architecture.presentation.navigation.PresentationNavigationEvent

class UnhandledNavigationException(event: PresentationNavigationEvent) :
    IllegalArgumentException(
        "Navigation event ${event::class.simpleName} was not handled."
    )
