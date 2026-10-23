package com.migrely.app.home.ui.mapper

import com.migrely.app.home.presentation.model.HomeViewState
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class HomeViewStateUiMapperTest(
    private val givenViewState: HomeViewState,
    private val expectedUiModel: HomeViewStateUiModel
) {
    companion object {
        @JvmStatic
        @Parameters(name = "Given {0} then returns {1}")
        fun testCases(): Collection<Array<*>> = listOf(
            arrayOf(HomeViewState.Loading, HomeViewStateUiModel.Loading),
            arrayOf(HomeViewState.Ready, HomeViewStateUiModel.Ready)
        )
    }

    private lateinit var classUnderTest: HomeViewStateUiMapper

    @Before
    fun setUp() {
        classUnderTest = HomeViewStateUiMapper()
    }

    @Test
    fun `When toUi then returns expected UI model`() {
        // When
        val actualUiModel = classUnderTest.toUi(givenViewState)

        // Then
        assertEquals(expectedUiModel, actualUiModel)
    }
}
