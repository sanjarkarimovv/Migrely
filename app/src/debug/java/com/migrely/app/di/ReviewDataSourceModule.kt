package com.migrely.app.di

import android.content.Context
import com.google.android.play.core.review.testing.FakeReviewManager
import com.migrely.app.activity.CurrentActivityHolder
import com.migrely.app.datasource.review.datasource.PlayReviewDataSourceImpl
import com.migrely.app.datasource.review.datasource.ReviewDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Debug builds ask Google's FakeReviewManager, which shows no card and completes at once, so the
 * walk and QA check the rules through the marks, as with the simulated store (BRD D113, D132). A
 * debug build is never installed from Google Play, where the real manager would show no card either.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object ReviewDataSourceModule {
    @Provides
    @Singleton
    fun providesReviewDataSource(
        @ApplicationContext context: Context,
        currentActivityHolder: CurrentActivityHolder
    ): ReviewDataSource = PlayReviewDataSourceImpl(
        context = context,
        currentActivity = currentActivityHolder::resumed,
        newManager = ::FakeReviewManager
    )
}
