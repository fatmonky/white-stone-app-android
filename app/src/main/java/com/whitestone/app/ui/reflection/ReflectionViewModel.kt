package com.whitestone.app.ui.reflection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitestone.app.data.Reflection
import com.whitestone.app.data.ReflectionDao
import com.whitestone.app.util.DateHelpers
import com.whitestone.app.util.ReflectionQuestions
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ReflectionViewModel @Inject constructor(
    private val reflectionDao: ReflectionDao
) : ViewModel() {
    private val today = LocalDate.now()
    val todayDayKey: String = DateHelpers.dayKey(today)
    val todayQuestion: Pair<Int, String> = ReflectionQuestions.questionForDate(today)

    val todayReflection: Flow<Reflection?> = reflectionDao.getByDayKey(todayDayKey)

    val previousCount: StateFlow<Int> = reflectionDao
        .getAllForQuestion(todayQuestion.first)
        .map { reflections -> reflections.count { it.dayKey != todayDayKey } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val allReflections: Flow<List<Reflection>> = reflectionDao.getAllNewestFirst()

    fun reflectionsForQuestion(index: Int): Flow<List<Reflection>> =
        reflectionDao.getAllForQuestion(index)

    fun reflectionForDay(dayKey: String): Flow<Reflection?> =
        reflectionDao.getByDayKey(dayKey)

    fun saveToday(responseText: String) {
        save(dayKey = todayDayKey, questionIndex = todayQuestion.first, responseText = responseText)
    }

    fun save(dayKey: String, questionIndex: Int, responseText: String) {
        viewModelScope.launch {
            val trimmed = responseText.trim()
            val existing = reflectionDao.getByDayKeyOnce(dayKey)
            if (trimmed.isEmpty()) {
                reflectionDao.deleteByDayKey(dayKey)
                return@launch
            }

            val now = System.currentTimeMillis()
            if (existing == null) {
                reflectionDao.insert(
                    Reflection(
                        dayKey = dayKey,
                        questionIndex = questionIndex,
                        responseText = responseText,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                reflectionDao.update(
                    existing.copy(
                        questionIndex = questionIndex,
                        responseText = responseText,
                        updatedAt = now
                    )
                )
            }
        }
    }
}
