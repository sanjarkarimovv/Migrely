package com.migrely.app.attack.ui.mapper

import androidx.annotation.StringRes
import com.migrely.app.attack.ui.R
import com.migrely.app.attack.ui.model.BuiltInTriggerUiModel

class BuiltInTriggerLabelUiMapper {
    @StringRes
    fun toUi(builtInTrigger: BuiltInTriggerUiModel): Int = when (builtInTrigger) {
        BuiltInTriggerUiModel.Stress -> R.string.trigger_stress
        BuiltInTriggerUiModel.TooLittleSleep -> R.string.trigger_too_little_sleep
        BuiltInTriggerUiModel.TooMuchSleep -> R.string.trigger_too_much_sleep
        BuiltInTriggerUiModel.SkippedMeal -> R.string.trigger_skipped_meal
        BuiltInTriggerUiModel.Dehydration -> R.string.trigger_dehydration
        BuiltInTriggerUiModel.Alcohol -> R.string.trigger_alcohol
        BuiltInTriggerUiModel.Caffeine -> R.string.trigger_caffeine
        BuiltInTriggerUiModel.Period -> R.string.trigger_period
        BuiltInTriggerUiModel.Weather -> R.string.trigger_weather
        BuiltInTriggerUiModel.BrightLightOrScreens -> R.string.trigger_bright_light_or_screens
        BuiltInTriggerUiModel.Smell -> R.string.trigger_smell
        BuiltInTriggerUiModel.PhysicalEffort -> R.string.trigger_physical_effort
        BuiltInTriggerUiModel.Travel -> R.string.trigger_travel
    }
}
