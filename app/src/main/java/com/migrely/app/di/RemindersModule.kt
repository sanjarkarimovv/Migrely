package com.migrely.app.di

import android.content.Context
import androidx.work.WorkManager
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.daymark.datasource.DayMarkDataSource
import com.migrely.app.datasource.notification.datasource.NotificationDataSource
import com.migrely.app.datasource.notification.datasource.NotificationDataSourceImpl
import com.migrely.app.datasource.reminder.datasource.ReminderSettingsDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSource
import com.migrely.app.datasource.schedule.datasource.ReminderScheduleDataSourceImpl
import com.migrely.app.navigation.mapper.RemindersNavigationEventDestinationMapper
import com.migrely.app.reminders.data.mapper.ReminderSettingsDomainMapper
import com.migrely.app.reminders.data.mapper.ReminderZoneDomainMapper
import com.migrely.app.reminders.data.repository.RemindersRepository
import com.migrely.app.reminders.domain.usecase.DismissDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.GetRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RecordPermissionAskedUseCase
import com.migrely.app.reminders.domain.usecase.RescheduleRemindersUseCase
import com.migrely.app.reminders.domain.usecase.RunDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetDailyReminderUseCase
import com.migrely.app.reminders.domain.usecase.SetEffectReminderUseCase
import com.migrely.app.reminders.domain.usecase.ShowAttackLoggedTodayUseCase
import com.migrely.app.reminders.presentation.mapper.RemindersPresentationMapper
import com.migrely.app.reminders.presentation.viewmodel.RemindersViewModel
import com.migrely.app.reminders.ui.di.RemindersDependencies
import com.migrely.app.reminders.ui.mapper.ReminderKindUiToPresentationMapper
import com.migrely.app.reminders.ui.mapper.RemindersEventPresenter
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

    @Provides
    fun providesGetRemindersUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = GetRemindersUseCase(repository, repository, repository, coroutines)

    @Provides
    fun providesSetDailyReminderUseCase(
        repository: RemindersRepository,
        coroutines: CoroutineContextProvider
    ) = SetDailyReminderUseCase(
        repository,
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
        coroutines: CoroutineContextProvider
    ) = RescheduleRemindersUseCase(repository, repository, repository, repository, coroutines)

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

    @Provides
    fun providesRemindersViewModel(
        getRemindersUseCase: GetRemindersUseCase,
        setDailyReminderUseCase: SetDailyReminderUseCase,
        setEffectReminderUseCase: SetEffectReminderUseCase,
        recordPermissionAskedUseCase: RecordPermissionAskedUseCase,
        useCaseExecutor: UseCaseExecutor
    ) = RemindersViewModel(
        getRemindersUseCase,
        setDailyReminderUseCase,
        setEffectReminderUseCase,
        recordPermissionAskedUseCase,
        RemindersPresentationMapper(),
        useCaseExecutor
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
            reminderKindUiToPresentationMapper = ReminderKindUiToPresentationMapper(),
            navigationMapper = RemindersNavigationEventDestinationMapper(),
            notificationMapper = RemindersNotificationUiMapper(presenter)
        )
    }
}
