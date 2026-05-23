package com.whitestone.app.ui.onboarding

import com.whitestone.app.data.OnboardingStep
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneType
import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingCoordinatorTest {

    @Test
    fun emptyDbWithNoSavedStepShowsWelcome() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = null,
            stonesLoadState = StonesLoadState.Loaded(emptyList()),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.WELCOME, nextStep)
    }

    @Test
    fun existingStonesWithNoSavedStepCompletesOnboarding() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = null,
            stonesLoadState = StonesLoadState.Loaded(listOf(stone())),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.COMPLETED, nextStep)
    }

    @Test
    fun loadingStateDoesNotBootstrap() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = null,
            stonesLoadState = StonesLoadState.Loading,
            showingPostFirstEntry = false
        )

        assertEquals(null, nextStep)
    }

    @Test
    fun completedWithEmptyDbRemainsCompleted() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = OnboardingStep.COMPLETED,
            stonesLoadState = StonesLoadState.Loaded(emptyList()),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.COMPLETED, nextStep)
    }

    @Test
    fun completedUserDeletesAllStonesRemainsCompleted() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = OnboardingStep.COMPLETED,
            stonesLoadState = StonesLoadState.Loaded(emptyList()),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.COMPLETED, nextStep)
    }

    @Test
    fun firstLogWithExistingStonePreservesFlow() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = OnboardingStep.FIRST_LOG,
            stonesLoadState = StonesLoadState.Loaded(listOf(stone())),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.FIRST_LOG, nextStep)
    }

    @Test
    fun reviewTourWithExistingStonePreservesFlow() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = OnboardingStep.REVIEW_TOUR,
            stonesLoadState = StonesLoadState.Loaded(listOf(stone())),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.REVIEW_TOUR, nextStep)
    }

    @Test
    fun reflectionsTourWithExistingStonePreservesFlow() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = OnboardingStep.REFLECTIONS_TOUR,
            stonesLoadState = StonesLoadState.Loaded(listOf(stone())),
            showingPostFirstEntry = false
        )

        assertEquals(OnboardingStep.REFLECTIONS_TOUR, nextStep)
    }

    @Test
    fun activePostFirstEntrySheetPreservesFlow() {
        val nextStep = bootstrapOnboardingStep(
            currentStep = OnboardingStep.FIRST_LOG,
            stonesLoadState = StonesLoadState.Loaded(listOf(stone())),
            showingPostFirstEntry = true
        )

        assertEquals(OnboardingStep.FIRST_LOG, nextStep)
    }

    @Test
    fun todayCoachStepZeroAdvancesToStepOne() {
        val advance = nextTodayCoachAdvance(0)

        assertEquals(TodayCoachAdvance.ShowStep(1), advance)
    }

    @Test
    fun todayCoachStepOneCompletesCoach() {
        val advance = nextTodayCoachAdvance(1)

        assertEquals(TodayCoachAdvance.Complete, advance)
    }

    @Test
    fun welcomeStartTourMovesToTodayCoach() {
        assertEquals(
            OnboardingTransition(step = OnboardingStep.TODAY_COACH),
            startTourTransition()
        )
    }

    @Test
    fun todayCoachTryItNowMovesToFirstLog() {
        assertEquals(
            OnboardingTransition(step = OnboardingStep.FIRST_LOG),
            completeTodayCoachTransition()
        )
    }

    @Test
    fun firstStoneSavedDuringFirstLogShowsPostFirstEntry() {
        assertEquals(true, firstStoneSavedTransition(OnboardingStep.FIRST_LOG))
    }

    @Test
    fun firstStoneSavedOutsideFirstLogDoesNotShowPostFirstEntry() {
        assertEquals(false, firstStoneSavedTransition(OnboardingStep.COMPLETED))
    }

    @Test
    fun continueToReviewMovesToReviewTourAndCommits() {
        assertEquals(
            OnboardingTransition(
                step = OnboardingStep.REVIEW_TOUR,
                showPostFirstEntry = false,
                commit = true
            ),
            continueToReviewTransition()
        )
    }

    @Test
    fun continueToReflectionsMovesToReflectionsTourAndCommits() {
        assertEquals(
            OnboardingTransition(
                step = OnboardingStep.REFLECTIONS_TOUR,
                showPostFirstEntry = false,
                commit = true
            ),
            continueToReflectionsTransition()
        )
    }

    @Test
    fun reviewSkipOrReflectionsDoneOrSkipCompletesAndCommits() {
        assertEquals(
            OnboardingTransition(step = OnboardingStep.COMPLETED, commit = true),
            completeOnboardingTransition()
        )
    }

    private fun stone() = Stone(
        id = 1L,
        type = StoneType.WHITE,
        timestamp = 1_741_392_000_000L,
        note = "",
        dayKey = "2025-03-08"
    )
}
