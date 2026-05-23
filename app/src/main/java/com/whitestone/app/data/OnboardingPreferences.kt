package com.whitestone.app.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class OnboardingStep {
    WELCOME,
    TODAY_COACH,
    FIRST_LOG,
    REVIEW_TOUR,
    REFLECTIONS_TOUR,
    COMPLETED
}

@Singleton
class OnboardingPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    fun getStep(): OnboardingStep? =
        preferences.getString(KEY_STEP, null)?.let { stored ->
            OnboardingStep.entries.firstOrNull { it.name == stored }
        }

    fun setStep(step: OnboardingStep, commit: Boolean = false) {
        val editor = preferences.edit().putString(KEY_STEP, step.name)
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    private companion object {
        const val KEY_STEP = "onboarding.step"
    }
}
