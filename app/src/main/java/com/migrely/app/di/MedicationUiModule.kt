package com.migrely.app.di

import android.content.Context
import android.text.format.DateFormat
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.medication.presentation.navigation.EditMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.navigation.MedicationsPresentationNavigationEvent
import com.migrely.app.medication.presentation.viewmodel.EditMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.LogMedicationViewModel
import com.migrely.app.medication.presentation.viewmodel.MedicationPromptViewModel
import com.migrely.app.medication.presentation.viewmodel.MedicationsViewModel
import com.migrely.app.medication.presentation.viewmodel.OveruseIndicatorViewModel
import com.migrely.app.medication.ui.di.EditMedicationDependencies
import com.migrely.app.medication.ui.di.LogMedicationDependencies
import com.migrely.app.medication.ui.di.MedicationPromptDependencies
import com.migrely.app.medication.ui.di.MedicationsDependencies
import com.migrely.app.medication.ui.di.OveruseIndicatorDependencies
import com.migrely.app.medication.ui.mapper.EditMedicationNotificationUiMapper
import com.migrely.app.medication.ui.mapper.EditMedicationViewStateUiMapper
import com.migrely.app.medication.ui.mapper.IntakeUiMapper
import com.migrely.app.medication.ui.mapper.LogMedicationNotificationUiMapper
import com.migrely.app.medication.ui.mapper.LogMedicationViewStateUiMapper
import com.migrely.app.medication.ui.mapper.MedicationCategoryUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationKindUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.mapper.MedicationPromptUiMapper
import com.migrely.app.medication.ui.mapper.MedicationPromptUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.mapper.MedicationUiMapper
import com.migrely.app.medication.ui.mapper.MedicationUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationsNotificationUiMapper
import com.migrely.app.medication.ui.mapper.MedicationsViewStateUiMapper
import com.migrely.app.medication.ui.mapper.OveruseCategoryUiMapper
import com.migrely.app.medication.ui.mapper.OveruseIndicatorUiMapper
import com.migrely.app.medication.ui.mapper.SaveProblemUiMapper
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import com.migrely.app.navigation.mapper.EditMedicationNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.LogMedicationNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.MedicationsNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.ZoneId

private typealias LogMedicationNavigationMapper =
    NavigationEventDestinationMapper<LogMedicationPresentationNavigationEvent>
private typealias MedicationsNavigationMapper =
    NavigationEventDestinationMapper<MedicationsPresentationNavigationEvent>
private typealias EditMedicationNavigationMapper =
    NavigationEventDestinationMapper<EditMedicationPresentationNavigationEvent>

@Module
@InstallIn(SingletonComponent::class)
internal object MedicationUiModule {
    @Provides
    fun providesLogMedicationNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<LogMedicationPresentationNavigationEvent> =
        LogMedicationNavigationEventDestinationMapper()

    @Provides
    fun providesMedicationsNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<MedicationsPresentationNavigationEvent> =
        MedicationsNavigationEventDestinationMapper()

    @Provides
    fun providesEditMedicationNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<EditMedicationPresentationNavigationEvent> =
        EditMedicationNavigationEventDestinationMapper()

    @Provides
    fun providesMedicationTimeUiFormatter(@ApplicationContext context: Context) =
        MedicationTimeUiFormatter(
            locale = { context.resources.configuration.locales[0] },
            is24HourFormat = { DateFormat.is24HourFormat(context) },
            deviceZone = { ZoneId.systemDefault() },
            bestDateTimePattern = { locale, skeleton ->
                DateFormat.getBestDateTimePattern(locale, skeleton)
            }
        )

    // Each screen gets its own presenter, shared by its notification mapper and its composable.
    @Provides
    fun providesLogMedicationDependencies(
        logMedicationViewModel: LogMedicationViewModel,
        medicationTimeUiFormatter: MedicationTimeUiFormatter,
        navigationMapper: @JvmSuppressWildcards LogMedicationNavigationMapper
    ): LogMedicationDependencies {
        val presenter = MedicationEventPresenter()
        return LogMedicationDependencies(
            logMedicationViewModel = logMedicationViewModel,
            logMedicationViewStateUiMapper = LogMedicationViewStateUiMapper(MedicationUiMapper()),
            medicationUiToPresentationMapper = MedicationUiToPresentationMapper(
                MedicationKindUiToPresentationMapper(),
                MedicationCategoryUiToPresentationMapper()
            ),
            medicationCategoryUiToPresentationMapper = MedicationCategoryUiToPresentationMapper(),
            medicationTimeUiFormatter = medicationTimeUiFormatter,
            medicationEventPresenter = presenter,
            medicationMessageBehaviourUiMapper = MedicationMessageBehaviourUiMapper(),
            navigationMapper = navigationMapper,
            notificationMapper = LogMedicationNotificationUiMapper(
                presenter,
                SaveProblemUiMapper(),
                IntakeUiMapper()
            )
        )
    }

    @Provides
    fun providesMedicationPromptDependencies(
        medicationPromptViewModel: MedicationPromptViewModel,
        medicationTimeUiFormatter: MedicationTimeUiFormatter
    ) = MedicationPromptDependencies(
        medicationPromptViewModel = medicationPromptViewModel,
        medicationPromptUiMapper = MedicationPromptUiMapper(OveruseCategoryUiMapper()),
        medicationPromptUiToPresentationMapper = MedicationPromptUiToPresentationMapper(),
        medicationTimeUiFormatter = medicationTimeUiFormatter
    )

    @Provides
    fun providesOveruseIndicatorDependencies(overuseIndicatorViewModel: OveruseIndicatorViewModel) =
        OveruseIndicatorDependencies(
            overuseIndicatorViewModel = overuseIndicatorViewModel,
            overuseIndicatorUiMapper = OveruseIndicatorUiMapper(OveruseCategoryUiMapper())
        )

    @Provides
    fun providesMedicationsDependencies(
        medicationsViewModel: MedicationsViewModel,
        navigationMapper: @JvmSuppressWildcards MedicationsNavigationMapper
    ): MedicationsDependencies {
        val presenter = MedicationEventPresenter()
        return MedicationsDependencies(
            medicationsViewModel = medicationsViewModel,
            medicationsViewStateUiMapper = MedicationsViewStateUiMapper(MedicationUiMapper()),
            medicationEventPresenter = presenter,
            medicationMessageBehaviourUiMapper = MedicationMessageBehaviourUiMapper(),
            navigationMapper = navigationMapper,
            notificationMapper = MedicationsNotificationUiMapper(presenter)
        )
    }

    @Provides
    fun providesEditMedicationDependencies(
        editMedicationViewModel: EditMedicationViewModel,
        navigationMapper: @JvmSuppressWildcards EditMedicationNavigationMapper
    ): EditMedicationDependencies {
        val presenter = MedicationEventPresenter()
        return EditMedicationDependencies(
            editMedicationViewModel = editMedicationViewModel,
            editMedicationViewStateUiMapper = EditMedicationViewStateUiMapper(MedicationUiMapper()),
            medicationKindUiToPresentationMapper = MedicationKindUiToPresentationMapper(),
            medicationCategoryUiToPresentationMapper = MedicationCategoryUiToPresentationMapper(),
            medicationEventPresenter = presenter,
            medicationMessageBehaviourUiMapper = MedicationMessageBehaviourUiMapper(),
            navigationMapper = navigationMapper,
            notificationMapper = EditMedicationNotificationUiMapper(
                presenter,
                SaveProblemUiMapper()
            )
        )
    }
}
