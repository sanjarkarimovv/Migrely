package com.migrely.app.di

import android.content.Context
import androidx.work.WorkManager
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.ui.notification.mapper.NoNotificationUiMapper
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.doctorvisit.datasource.DoctorVisitDataSource
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.notification.datasource.NotificationDataSourceImpl
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSourceImpl
import com.migrely.app.navigation.mapper.RemindersNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.VisitCardNavigationEventDestinationMapper
import com.migrely.app.reminders.data.mapper.DoctorVisitDomainMapper
import com.migrely.app.reminders.data.mapper.ReminderSettingsDomainMapper
import com.migrely.app.reminders.data.mapper.ReminderZoneDomainMapper
import com.migrely.app.reminders.data.repository.RemindersRepository
import com.migrely.app.reminders.data.repository.VisitReminderRepository
import com.migrely.app.reminders.domain.usecase.CloseVisitCardUseCase
import com.migrely.app.reminders.domain.usecase.DismissDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.GetVisitCardUseCase
import com.migrely.app.reminders.domain.usecase.PlanRemindersAfterImportUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.RemoveDoctorVisitUseCase
import com.migrely.app.reminders.domain.usecase.RescheduleRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RestoreDoctorVisitUseCase
import com.migrely.app.reminders.domain.usecase.RunDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.RunVisitReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetDoctorVisitUseCase
import com.migrely.app.reminders.domain.usecase.SetEffectReminderUseCase
import com.migrely.app.reminders.domain.usecase.ShowAttackLoggedTodayUseCase
import com.migrely.app.reminders.presentation.mapper.DoctorVisitPresentationMapper
import com.migrely.app.reminders.presentation.mapper.RemindersPresentationMapper
import com.migrely.app.reminders.presentation.viewmodel.DailyReminderOfferViewModel
import com.migrely.app.reminders.presentation.viewmodel.RemindersViewModel
import com.migrely.app.reminders.presentation.viewmodel.VisitCardViewModel
import com.migrely.app.reminders.ui.di.DailyReminderOfferDependencies
import com.migrely.app.reminders.ui.di.RemindersDependencies
import com.migrely.app.reminders.ui.di.VisitCardDependencies
import com.migrely.app.reminders.ui.mapper.DailyReminderOfferEventUiMapper
import com.migrely.app.reminders.ui.mapper.DailyReminderOfferUiMapper
import com.migrely.app.reminders.ui.mapper.DoctorVisitUiToPresentationMapper
import com.migrely.app.reminders.ui.mapper.ReminderKindUiToPresentationMapper
import com.migrely.app.reminders.ui.mapper.RemindersEventPresenter
import com.migrely.app.reminders.ui.mapper.RemindersMessageBehaviourUiMapper
import com.migrely.app.reminders.ui.mapper.RemindersNotificationUiMapper
import com.migrely.app.reminders.ui.mapper.RemindersViewStateUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * The reminders feature (change add-reminders, BRD D65–D68). One repository serves every single-function repository
 * interface, so the use cases take it directly.
 */
@Suppress("TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
internal object RemindersModule {
    @Provides
    fun providesReminderScheduleDataSource(
        @ApplicationContext context: Context,
        clockDataSource: ClockDataSource
    ): ReminderScheduleDataSource =
        ReminderScheduleDataSourceImpl(WorkManager.getInstance(context), clockDataSource)

    @Provides
    fun providesNotificationDataSource(
        @ApplicationContext context: Context
    ): NotificationDataSource = NotificationDataSourceImpl(context)

    @Suppress("LongParameterList")
    @Provides
    fun providesRemindersRepository(
        reminderSettingsDataSource: ReminderSettingsDataSource,
        reminderScheduleDataSource: ReminderScheduleDataSource,
        notificationDataSource: NotificationDataSource,
        attackDataSource: AttackDataSource,
        dayMarkDataSource: DayMarkDataSource,
        clockDataSource: ClockDataSource
    ) = RemindersRepository(
        reminderSettingsDataSource,
        reminderScheduleDataSource,
        notificationDataSource,
        attackDataSource,
        dayMarkDataSource,
        clockDataSource,
        ReminderSettingsDomainMapper(),
        ReminderZoneDomainMapper()
    )

    @Suppress("LongParameterList")
    @Provides
    fun providesVisitReminderRepository(
        doctorVisitDataSource: DoctorVisitDataSource,
        reminderSettingsDataSource: ReminderSettingsDataSource,
        reminderScheduleDataSource: ReminderScheduleDataSource,
        notificationDataSource: NotificationDataSource,
        attackDataSource: AttackDataSource
    ) = VisitReminderRepository(
        doctorVisitDataSource,
        reminderSettingsDataSource,
        reminderScheduleDataSource,
        notificationDataSource,
        attackDataSource,
        DoctorVisitDomainMapper()
    )

    @Provides
    fun providesGetRemindersUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = GetRemindersUseCase(repository, repository, repository, visits, visits, coroutines)

    @Provides
    fun providesSetDoctorVisitUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = SetDoctorVisitUseCase(
        repository,
        repository,
        visits,
        repository,
        visits,
        visits,
        visits,
        visits,
        visits,
        coroutines
    )

    @Provides
    fun providesRemoveDoctorVisitUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = RemoveDoctorVisitUseCase(repository, visits, visits, visits, coroutines)

    @Provides
    fun providesRestoreDoctorVisitUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = RestoreDoctorVisitUseCase(
        repository,
        repository,
        visits,
        visits,
        visits,
        visits,
        visits,
        coroutines
    )

    @Provides
    fun providesRunVisitReminderUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = RunVisitReminderUseCase(
        repository,
        repository,
        visits,
        visits,
        visits,
        visits,
        visits,
        visits,
        visits,
        coroutines
    )

    @Provides
    fun providesPlanRemindersAfterImportUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = PlanRemindersAfterImportUseCase(
        repository,
        repository,
        visits,
        visits,
        visits,
        visits,
        coroutines
    )

    @Provides
    fun providesGetVisitCardUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = GetVisitCardUseCase(repository, repository, visits, visits, coroutines)

    @Provides
    fun providesCloseVisitCardUseCase(
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = CloseVisitCardUseCase(visits, coroutines)

    @Provides
    fun providesSetDailyReminderUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = SetDailyReminderUseCase(
        repository,
        repository,
        repository,
        repository,
        coroutines
    )

    @Provides
    fun providesSetEffectReminderUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = SetEffectReminderUseCase(repository, coroutines)

    @Provides
    fun providesRecordPermissionAskedUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = RecordPermissionAskedUseCase(repository, repository, coroutines)

    @Provides
    fun providesRescheduleRemindersUseCase(
        repository: RemindersRepository,
        visits: VisitReminderRepository,
        coroutines: CoroutineContextProvider
    ) = RescheduleRemindersUseCase(
        repository,
        repository,
        repository,
        repository,
        visits,
        visits,
        visits,
        coroutines
    )

    @Provides
    fun providesRunDailyReminderUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = RunDailyReminderUseCase(
        repository,
        repository,
        repository,
        repository,
        repository,
        repository,
        repository,
        repository,
        coroutines
    )

    @Provides
    fun providesShowAttackLoggedTodayUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = ShowAttackLoggedTodayUseCase(repository, coroutines)

    @Provides
    fun providesDismissDailyReminderUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = DismissDailyReminderUseCase(repository, coroutines)

    @Suppress("LongParameterList")
    @Provides
    fun providesRemindersViewModel(
        getRemindersUseCase: GetRemindersUseCase,
        setDailyReminderUseCase: SetDailyReminderUseCase,
        setEffectReminderUseCase: SetEffectReminderUseCase,
        recordPermissionAskedUseCase: RecordPermissionAskedUseCase,
        setDoctorVisitUseCase: SetDoctorVisitUseCase,
        removeDoctorVisitUseCase: RemoveDoctorVisitUseCase,
        restoreDoctorVisitUseCase: RestoreDoctorVisitUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = RemindersViewModel(
        getRemindersUseCase,
        setDailyReminderUseCase,
        setEffectReminderUseCase,
        recordPermissionAskedUseCase,
        setDoctorVisitUseCase,
        removeDoctorVisitUseCase,
        restoreDoctorVisitUseCase,
        RemindersPresentationMapper(),
        DoctorVisitPresentationMapper(),
        useCaseExecutor
    )

    @Provides
    fun providesVisitCardDependencies(
        getVisitCardUseCase: GetVisitCardUseCase,
        closeVisitCardUseCase: CloseVisitCardUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = VisitCardDependencies(
        visitCardViewModel = VisitCardViewModel(
            getVisitCardUseCase,
            closeVisitCardUseCase,
            useCaseExecutor
        ),
        navigationMapper = VisitCardNavigationEventDestinationMapper(),
        notificationMapper = NoNotificationUiMapper()
    )

    @Provides
    fun providesDailyReminderOfferDependencies(
        getRemindersUseCase: GetRemindersUseCase,
        setDailyReminderUseCase: SetDailyReminderUseCase,
        recordPermissionAskedUseCase: RecordPermissionAskedUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = DailyReminderOfferDependencies(
        dailyReminderOfferViewModel = DailyReminderOfferViewModel(
            getRemindersUseCase,
            setDailyReminderUseCase,
            recordPermissionAskedUseCase,
            useCaseExecutor
        ),
        dailyReminderOfferUiMapper = DailyReminderOfferUiMapper(),
        dailyReminderOfferEventUiMapper = DailyReminderOfferEventUiMapper()
    )

    @Provides
    fun providesRemindersDependencies(
        remindersViewModel: RemindersViewModel
    ): RemindersDependencies {
        val presenter = RemindersEventPresenter()
        return RemindersDependencies(
            remindersViewModel = remindersViewModel,
            remindersViewStateUiMapper = RemindersViewStateUiMapper(),
            remindersEventPresenter = presenter,
            remindersMessageBehaviourUiMapper = RemindersMessageBehaviourUiMapper(),
            reminderKindUiToPresentationMapper = ReminderKindUiToPresentationMapper(),
            doctorVisitUiToPresentationMapper = DoctorVisitUiToPresentationMapper(),
            visitNotificationChannelId = NotificationDataSourceImpl.CHANNEL_VISIT,
            navigationMapper = RemindersNavigationEventDestinationMapper(),
            notificationMapper = RemindersNotificationUiMapper(presenter)
        )
    }
}
