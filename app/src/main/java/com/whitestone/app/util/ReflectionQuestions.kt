package com.whitestone.app.util

import java.time.LocalDate

object ReflectionQuestions {
    val questions = listOf(
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
    )

    const val attributionPrefix = "Daily questions from "
    const val attributionLinkText = "Sacitta Sutta (AN 10.51)"
    const val attributionSuffix = "Translated by Bhikkhu Sujato, SuttaCentral (CC0)."
    const val sourceUrl =
        "https://suttacentral.net/an10.51/en/sujato?lang=en&layout=linebyline&reference=main&notes=asterisk&highlight=false&script=latin#3.6"

    fun questionForDate(date: LocalDate): Pair<Int, String> {
        val index = date.dayOfYear % questions.size
        return index to questions[index]
    }
}
