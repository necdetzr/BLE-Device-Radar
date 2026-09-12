package com.necdetzr.onboarding.model

import androidx.annotation.StringRes

internal data class OnboardingContent(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val visual: OnboardingVisual,
)

internal enum class OnboardingVisual {
    RADAR,
    DEVICE_DETAILS,
    HISTORY,
}
