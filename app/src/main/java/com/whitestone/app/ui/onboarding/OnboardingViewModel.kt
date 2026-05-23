package com.whitestone.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitestone.app.data.OnboardingPreferences
import com.whitestone.app.data.OnboardingStep
import com.whitestone.app.data.StoneDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val stoneDao: StoneDao,
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(step = onboardingPreferences.getStep())
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState

    init {
        viewModelScope.launch {
            stoneDao.getAllStones().collect { stones ->
                _uiState.update { state ->
                    val loadedState = StonesLoadState.Loaded(stones)
                    val nextStep = bootstrapOnboardingStep(
                        currentStep = state.step,
                        stonesLoadState = loadedState,
                        showingPostFirstEntry = state.showPostFirstEntry
                    )
                    persistIfChanged(state.step, nextStep)
                    state.copy(step = nextStep, stonesLoadState = loadedState)
                }
            }
        }
    }

    fun startTour() {
        applyTransition(startTourTransition())
    }

    fun completeTodayCoach() {
        applyTransition(completeTodayCoachTransition())
    }

    fun dismissOnboarding() {
        applyTransition(completeOnboardingTransition())
    }

    fun onStoneSaved() {
        _uiState.update { state ->
            if (firstStoneSavedTransition(state.step)) {
                state.copy(showPostFirstEntry = true)
            } else {
                state
            }
        }
    }

    fun continueToReview() {
        applyTransition(continueToReviewTransition())
    }

    fun continueToReflections() {
        applyTransition(continueToReflectionsTransition())
    }

    private fun applyTransition(transition: OnboardingTransition) {
        onboardingPreferences.setStep(transition.step, transition.commit)
        _uiState.update {
            it.copy(
                step = transition.step,
                showPostFirstEntry = transition.showPostFirstEntry
            )
        }
    }

    private fun persistIfChanged(previousStep: OnboardingStep?, nextStep: OnboardingStep?) {
        if (nextStep != null && nextStep != previousStep) {
            onboardingPreferences.setStep(nextStep, commit = nextStep == OnboardingStep.COMPLETED)
        }
    }
}
