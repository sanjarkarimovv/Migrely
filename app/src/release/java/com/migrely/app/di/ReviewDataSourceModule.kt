package com.migrely.app.di

import android.content.Context
import com.migrely.app.activity.CurrentActivityHolder
import com.migrely.app.datasource.review.datasource.PlayReviewDataSourceImpl
import com.migrely.app.datasource.review.datasource.ReviewDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Release builds always ask Google Play for its review flow (BRD D129, D132). */
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
        currentActivity = currentActivityHolder::resumed
    )
}
