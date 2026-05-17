package com.whitestone.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ReflectionQuestionsTest {
    @Test
    fun questionForDateUsesDayOfYearModuloQuestionCount() {
        val question = ReflectionQuestions.questionForDate(LocalDate.of(2026, 1, 1))

        assertEquals(1, question.first)
        assertEquals(ReflectionQuestions.questions[1], question.second)
    }

    @Test
    fun questionListMatchesIosOrder() {
        assertEquals(
            listOf(
                "Am I often covetous or not?",
                "Am I often malicious or not?",
                "Am I often overcome with dullness and drowsiness or not?",
                "Am I often restless or not?",
                "Am I often doubtful or not?",
                "Am I often irritable or not?",
                "Am I often corrupted in mind or not?",
                "Am I often disturbed in body or not?",
                "Am I often energetic or not?",
                "Am I often immersed in samādhi or not?",
            ),
            ReflectionQuestions.questions
        )
    }
}
