package com.migrely.app.review.ui.di

import com.migrely.app.review.presentation.viewmodel.ReviewPromptViewModel

/**
 * The request for a rating, which `:app` puts on home (BRD D130, D132). Its view model lives as long
 * as the process (change add-in-app-review design D4).
 */
class ReviewPromptDependencies(val reviewPromptViewModel: ReviewPromptViewModel)
