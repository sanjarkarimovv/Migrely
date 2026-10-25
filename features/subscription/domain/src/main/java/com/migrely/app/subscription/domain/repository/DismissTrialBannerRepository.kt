package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.SubscriptionWriteResultDomainModel
import java.time.Instant

interface DismissTrialBannerRepository {
    fun dismissTrialBanner(trialEnd: Instant): SubscriptionWriteResultDomainModel
}
