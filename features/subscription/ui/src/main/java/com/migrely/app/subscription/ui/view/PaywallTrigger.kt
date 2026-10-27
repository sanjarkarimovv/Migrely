package com.migrely.app.subscription.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import com.migrely.app.subscription.ui.R
import com.migrely.app.subscription.ui.di.PaywallGateDependencies
import com.migrely.app.subscription.ui.di.PaywallTriggerDependencies

/**
 * The paywall after the 5th migraine (BRD §7.3, D110), in the slot home leaves to `:app` in
 * today's mode only, so it never runs in attack mode. It draws nothing; every time home loads,
 * [refreshKey] changes and it asks whether the paywall is due.
 */
@Composable
fun PaywallTriggerDependencies.PaywallTrigger(navController: NavController, refreshKey: Int) {
    ViewModelObserver(navController)
    LaunchedEffect(refreshKey) { paywallTriggerViewModel.onEnter() }
}

/**
 * The step between onboarding and home (design D4): the background, while it asks whether the
 * paywall shows itself. It asks again whenever it is shown, so a rebuilt screen never waits.
 */
@Composable
fun PaywallGateDependencies.PaywallGate(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    ViewModelObserver(navController)
    LaunchedEffect(paywallGateViewModel) { paywallGateViewModel.onEnter() }
    val description = stringResource(R.string.subscription_gate)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .semantics { contentDescription = description }
    )
}
