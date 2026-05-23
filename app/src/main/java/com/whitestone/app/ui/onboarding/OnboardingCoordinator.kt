package com.whitestone.app.ui.onboarding

import com.whitestone.app.data.OnboardingStep
import com.whitestone.app.data.Stone

sealed interface StonesLoadState {
    data object Loading : StonesLoadState
    data class Loaded(val stones: List<Stone>) : StonesLoadState
}

data class OnboardingUiState(
    val step: OnboardingStep?,
    val stonesLoadState: StonesLoadState = StonesLoadState.Loading,
    val showPostFirstEntry: Boolean = false
)

data class OnboardingTransition(
    val step: OnboardingStep,
    val showPostFirstEntry: Boolean = false,
    val commit: Boolean = false
)

sealed interface TodayCoachAdvance {
    data class ShowStep(val step: Int) : TodayCoachAdvance
    data object Complete : TodayCoachAdvance
}

fun bootstrapOnboardingStep(
    currentStep: OnboardingStep?,
    stonesLoadState: StonesLoadState,
    showingPostFirstEntry: Boolean
): OnboardingStep? {
    val loaded = stonesLoadState as? StonesLoadState.Loaded ?: return currentStep
    return if (loaded.stones.isEmpty()) {
        when (currentStep) {
            null -> OnboardingStep.WELCOME
            OnboardingStep.COMPLETED -> OnboardingStep.COMPLETED
            else -> currentStep
        }
    } else {
        val preserveFlow = currentStep == OnboardingStep.FIRST_LOG ||
            currentStep == OnboardingStep.REVIEW_TOUR ||
            currentStep == OnboardingStep.REFLECTIONS_TOUR ||
            showingPostFirstEntry
        if (!preserveFlow && currentStep != OnboardingStep.COMPLETED) {
            OnboardingStep.COMPLETED
        } else {
            currentStep
        }
    }
}

fun nextTodayCoachAdvance(currentStep: Int): TodayCoachAdvance =
    if (currentStep <= 0) {
        TodayCoachAdvance.ShowStep(1)
    } else {
        TodayCoachAdvance.Complete
    }

fun startTourTransition(): OnboardingTransition =
    OnboardingTransition(step = OnboardingStep.TODAY_COACH)

fun completeTodayCoachTransition(): OnboardingTransition =
    OnboardingTransition(step = OnboardingStep.FIRST_LOG)

fun completeOnboardingTransition(): OnboardingTransition =
    OnboardingTransition(step = OnboardingStep.COMPLETED, commit = true)

fun firstStoneSavedTransition(currentStep: OnboardingStep?): Boolean =
    currentStep == OnboardingStep.FIRST_LOG

fun continueToReviewTransition(): OnboardingTransition =
    OnboardingTransition(
        step = OnboardingStep.REVIEW_TOUR,
        showPostFirstEntry = false,
        commit = true
    )

fun continueToReflectionsTransition(): OnboardingTransition =
    OnboardingTransition(
        step = OnboardingStep.REFLECTIONS_TOUR,
        showPostFirstEntry = false,
        commit = true
    )
