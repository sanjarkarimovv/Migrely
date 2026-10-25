package com.migrely.app.di

import android.content.Context
import android.text.format.DateFormat
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.attack.presentation.navigation.AddPastAttackPresentationNavigationEvent
import com.migrely.app.attack.presentation.navigation.AttackDetailsPresentationNavigationEvent
import com.migrely.app.attack.presentation.viewmodel.AddPastAttackViewModel
import com.migrely.app.attack.presentation.viewmodel.AttackDetailsViewModel
import com.migrely.app.attack.ui.di.AddPastAttackDependencies
import com.migrely.app.attack.ui.di.AttackDetailsDependencies
import com.migrely.app.attack.ui.mapper.AddPastAttackNotificationUiMapper
import com.migrely.app.attack.ui.mapper.AttackDetailsViewStateUiMapper
import com.migrely.app.attack.ui.mapper.AttackIntakeUiMapper
import com.migrely.app.attack.ui.mapper.AttackIntakeUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.AttackMessageBehaviourUiMapper
import com.migrely.app.attack.ui.mapper.AttackNotificationUiMapper
import com.migrely.app.attack.ui.mapper.AttackRangeUiMapper
import com.migrely.app.attack.ui.mapper.AttackTagUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.AttackTimeUiFormatter
import com.migrely.app.attack.ui.mapper.BuiltInSymptomUiMapper
import com.migrely.app.attack.ui.mapper.BuiltInSymptomUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.BuiltInTriggerUiMapper
import com.migrely.app.attack.ui.mapper.BuiltInTriggerUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.CustomTagUiMapper
import com.migrely.app.attack.ui.mapper.CustomTagUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.FunctionalImpactUiMapper
import com.migrely.app.attack.ui.mapper.FunctionalImpactUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.HeadLocationUiMapper
import com.migrely.app.attack.ui.mapper.HeadLocationUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.IntensityPointUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.PastAttackDraftUiMapper
import com.migrely.app.attack.ui.mapper.TagKindUiMapper
import com.migrely.app.attack.ui.mapper.TagKindUiToPresentationMapper
import com.migrely.app.attack.ui.mapper.TimeFieldUiMapper
import com.migrely.app.attack.ui.mapper.TimesUndoUiToPresentationMapper
import com.migrely.app.attack.ui.notification.AddPastAttackEventPresenter
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.attack.ui.view.AttackLabels
import com.migrely.app.navigation.mapper.AddPastAttackNavigationEventDestinationMapper
import com.migrely.app.navigation.mapper.AttackDetailsNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.ZoneId
import java.util.Locale

private typealias AttackDetailsNavigationMapper =
    NavigationEventDestinationMapper<AttackDetailsPresentationNavigationEvent>
private typealias AddPastAttackNavigationMapper =
    NavigationEventDestinationMapper<AddPastAttackPresentationNavigationEvent>

@Module
@InstallIn(SingletonComponent::class)
internal object AttackUiModule {
    @Provides
    fun providesAttackDetailsNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<AttackDetailsPresentationNavigationEvent> =
        AttackDetailsNavigationEventDestinationMapper()

    @Provides
    fun providesAddPastAttackNavigationEventDestinationMapper():
        NavigationEventDestinationMapper<AddPastAttackPresentationNavigationEvent> =
        AddPastAttackNavigationEventDestinationMapper()

    @Provides
    fun providesAttackTimeUiFormatter(@ApplicationContext context: Context) = AttackTimeUiFormatter(
        locale = { Locale.getDefault() },
        is24HourFormat = { DateFormat.is24HourFormat(context) },
        deviceZone = { ZoneId.systemDefault() },
        bestDateTimePattern = { locale, skeleton ->
            DateFormat.getBestDateTimePattern(locale, skeleton)
        }
    )

    @Provides
    fun providesAttackDetailsDependencies(
        attackDetailsViewModel: AttackDetailsViewModel,
        attackTimeUiFormatter: AttackTimeUiFormatter,
        attackDetailsNavigationMapper: @JvmSuppressWildcards AttackDetailsNavigationMapper
    ): AttackDetailsDependencies {
        // The notification mapper and the screen must share one presenter so events reach this screen.
        val eventPresenter = AttackDetailsEventPresenter()
        val tagKindUiMapper = TagKindUiMapper()
        val tagKindUiToPresentationMapper = TagKindUiToPresentationMapper()
        val intensityPointUiToPresentationMapper = IntensityPointUiToPresentationMapper()
        val attackIntakeUiMapper = AttackIntakeUiMapper()
        return AttackDetailsDependencies(
            attackDetailsViewModel = attackDetailsViewModel,
            attackDetailsViewStateUiMapper = AttackDetailsViewStateUiMapper(
                HeadLocationUiMapper(),
                BuiltInSymptomUiMapper(),
                BuiltInTriggerUiMapper(),
                CustomTagUiMapper(tagKindUiMapper),
                FunctionalImpactUiMapper(),
                attackIntakeUiMapper
            ),
            attackTimeUiFormatter = attackTimeUiFormatter,
            attackDetailsEventPresenter = eventPresenter,
            attackMessageBehaviourUiMapper = AttackMessageBehaviourUiMapper(),
            labels = AttackLabels.Default,
            attackTagUiToPresentationMapper = AttackTagUiToPresentationMapper(
                HeadLocationUiToPresentationMapper(),
                BuiltInSymptomUiToPresentationMapper(),
                BuiltInTriggerUiToPresentationMapper(),
                CustomTagUiToPresentationMapper(tagKindUiToPresentationMapper)
            ),
            functionalImpactUiToPresentationMapper = FunctionalImpactUiToPresentationMapper(),
            tagKindUiToPresentationMapper = tagKindUiToPresentationMapper,
            intensityPointUiToPresentationMapper = intensityPointUiToPresentationMapper,
            timesUndoUiToPresentationMapper = TimesUndoUiToPresentationMapper(
                intensityPointUiToPresentationMapper
            ),
            attackIntakeUiToPresentationMapper = AttackIntakeUiToPresentationMapper(),
            attackDetailsNavigationMapper = attackDetailsNavigationMapper,
            attackNotificationMapper = AttackNotificationUiMapper(
                eventPresenter,
                TimeFieldUiMapper(),
                AttackRangeUiMapper(),
                attackIntakeUiMapper
            )
        )
    }

    @Provides
    fun providesAddPastAttackDependencies(
        addPastAttackViewModel: AddPastAttackViewModel,
        attackTimeUiFormatter: AttackTimeUiFormatter,
        addPastAttackNavigationMapper: @JvmSuppressWildcards AddPastAttackNavigationMapper
    ): AddPastAttackDependencies {
        // The notification mapper and the screen must share one presenter so events reach this screen.
        val eventPresenter = AddPastAttackEventPresenter()
        return AddPastAttackDependencies(
            addPastAttackViewModel = addPastAttackViewModel,
            pastAttackDraftUiMapper = PastAttackDraftUiMapper(),
            attackTimeUiFormatter = attackTimeUiFormatter,
            addPastAttackEventPresenter = eventPresenter,
            attackMessageBehaviourUiMapper = AttackMessageBehaviourUiMapper(),
            addPastAttackNavigationMapper = addPastAttackNavigationMapper,
            addPastAttackNotificationMapper = AddPastAttackNotificationUiMapper(
                eventPresenter,
                TimeFieldUiMapper(),
                AttackRangeUiMapper()
            )
        )
    }
}
