package com.migrely.app.home.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.migrely.app.architecture.ui.view.ScreenEnterObserver
import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.ui.R
import com.migrely.app.home.ui.di.HomeDependencies
import com.migrely.app.home.ui.model.HomeViewStateUiModel

@Composable
fun HomeDependencies.Home(navController: NavController, modifier: Modifier = Modifier) {
    ScreenEnterObserver {
        homeViewModel.onEnter()
    }

    ViewModelObserver(navController)

    val viewState by homeViewModel.viewState.collectAsState(HomeViewState.Loading)
    val uiState by rememberSaveable(viewState) {
        mutableStateOf(homeViewStateUiMapper.toUi(viewState))
    }

    HomeContents(viewState = uiState, modifier = modifier)
}

@Composable
fun HomeContents(viewState: HomeViewStateUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (viewState is HomeViewStateUiModel.Ready) {
            Text(
                text = stringResource(R.string.home_subtitle),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Loading")
@Composable
private fun HomeContentsLoadingPreview() {
    HomePreviewTheme {
        HomeContents(viewState = HomeViewStateUiModel.Loading)
    }
}

@Preview(name = "Ready")
@Composable
private fun HomeContentsReadyPreview() {
    HomePreviewTheme {
        HomeContents(viewState = HomeViewStateUiModel.Ready)
    }
}

@Composable
private fun HomePreviewTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}
