package com.migrely.app.di

import com.migrely.app.attack.data.mapper.AttackDetailsDomainResolver
import com.migrely.app.attack.data.mapper.AttackTagDataMapper
import com.migrely.app.attack.data.mapper.BuiltInSymptomDataMapper
import com.migrely.app.attack.data.mapper.BuiltInSymptomDomainMapper
import com.migrely.app.attack.data.mapper.BuiltInTriggerDataMapper
import com.migrely.app.attack.data.mapper.BuiltInTriggerDomainMapper
import com.migrely.app.attack.data.mapper.CustomTagDomainMapper
import com.migrely.app.attack.data.mapper.FunctionalImpactDomainMapper
import com.migrely.app.attack.data.mapper.HeadLocationDataMapper
import com.migrely.app.attack.data.mapper.HeadLocationDomainMapper
import com.migrely.app.attack.data.mapper.TagKindDataMapper
import com.migrely.app.attack.data.repository.AttackDetailsRepository
import com.migrely.app.attack.data.repository.CustomTagRepository
import com.migrely.app.attack.domain.repository.AddIntensityPointRepository
import com.migrely.app.attack.domain.repository.CountActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.CreateCustomTagRepository
import com.migrely.app.attack.domain.repository.DeleteAttackRepository
import com.migrely.app.attack.domain.repository.FindCustomTagRepository
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetActiveCustomTagsRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.GetMaximumRecordedRepository
import com.migrely.app.attack.domain.repository.MergeAttacksRepository
import com.migrely.app.attack.domain.repository.RemoveIntensityPointRepository
import com.migrely.app.attack.domain.repository.RestoreAttackTimesRepository
import com.migrely.app.attack.domain.repository.SelectAttackTagRepository
import com.migrely.app.attack.domain.repository.SetCustomTagArchivedRepository
import com.migrely.app.attack.domain.repository.SetFunctionalImpactRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.attack.domain.repository.UpdateAttackNoteRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.tag.datasource.TagDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions")
internal object AttackDataModule {
    @Provides
    fun providesAttackDetailsRepository(
        attackDataSource: AttackDataSource,
        tagDataSource: TagDataSource,
        clockDataSource: ClockDataSource
    ) = AttackDetailsRepository(
        attackDataSource = attackDataSource,
        tagDataSource = tagDataSource,
        clockDataSource = clockDataSource,
        attackDetailsDomainResolver = AttackDetailsDomainResolver(
            HeadLocationDomainMapper(),
            BuiltInSymptomDomainMapper(),
            BuiltInTriggerDomainMapper(),
            CustomTagDomainMapper(),
            FunctionalImpactDomainMapper()
        ),
        attackTagDataMapper = AttackTagDataMapper(
            HeadLocationDataMapper(),
            BuiltInSymptomDataMapper(),
            BuiltInTriggerDataMapper()
        )
    )

    @Provides
    fun providesCustomTagRepository(tagDataSource: TagDataSource) =
        CustomTagRepository(tagDataSource, CustomTagDomainMapper(), TagKindDataMapper())

    @Provides
    fun providesGetAttackDetailsRepository(
        repository: AttackDetailsRepository
    ): GetAttackDetailsRepository = repository

    @Provides
    fun providesGetCurrentTimeRepository(
        repository: AttackDetailsRepository
    ): GetCurrentTimeRepository = repository

    @Provides
    fun providesFindOverlappingAttacksRepository(
        repository: AttackDetailsRepository
    ): FindOverlappingAttacksRepository = repository

    @Provides
    fun providesStorePastAttackRepository(
        repository: AttackDetailsRepository
    ): StorePastAttackRepository = repository

    @Provides
    fun providesMergeAttacksRepository(
        repository: AttackDetailsRepository
    ): MergeAttacksRepository = repository

    @Provides
    fun providesUpdateAttackTimesRepository(
        repository: AttackDetailsRepository
    ): UpdateAttackTimesRepository = repository

    @Provides
    fun providesRestoreAttackTimesRepository(
        repository: AttackDetailsRepository
    ): RestoreAttackTimesRepository = repository

    @Provides
    fun providesAddIntensityPointRepository(
        repository: AttackDetailsRepository
    ): AddIntensityPointRepository = repository

    @Provides
    fun providesGetMaximumRecordedRepository(
        repository: AttackDetailsRepository
    ): GetMaximumRecordedRepository = repository

    @Provides
    fun providesRemoveIntensityPointRepository(
        repository: AttackDetailsRepository
    ): RemoveIntensityPointRepository = repository

    @Provides
    fun providesSelectAttackTagRepository(
        repository: AttackDetailsRepository
    ): SelectAttackTagRepository = repository

    @Provides
    fun providesSetFunctionalImpactRepository(
        repository: AttackDetailsRepository
    ): SetFunctionalImpactRepository = repository

    @Provides
    fun providesUpdateAttackNoteRepository(
        repository: AttackDetailsRepository
    ): UpdateAttackNoteRepository = repository

    @Provides
    fun providesDeleteAttackRepository(
        repository: AttackDetailsRepository
    ): DeleteAttackRepository = repository

    @Provides
    fun providesGetActiveCustomTagsRepository(
        repository: CustomTagRepository
    ): GetActiveCustomTagsRepository = repository

    @Provides
    fun providesFindCustomTagRepository(repository: CustomTagRepository): FindCustomTagRepository =
        repository

    @Provides
    fun providesCountActiveCustomTagsRepository(
        repository: CustomTagRepository
    ): CountActiveCustomTagsRepository = repository

    @Provides
    fun providesCreateCustomTagRepository(
        repository: CustomTagRepository
    ): CreateCustomTagRepository = repository

    @Provides
    fun providesSetCustomTagArchivedRepository(
        repository: CustomTagRepository
    ): SetCustomTagArchivedRepository = repository
}
