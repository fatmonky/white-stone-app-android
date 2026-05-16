package com.whitestone.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoneTagHelpersTest {

    @Test
    fun allowedRootsFollowStoneColor() {
        assertEquals(
            listOf(StoneRoot.RENUNCIATION, StoneRoot.KINDNESS, StoneRoot.HARMLESSNESS),
            StoneRoot.allowedFor(StoneType.WHITE)
        )
        assertEquals(
            listOf(StoneRoot.SENSUAL, StoneRoot.ILL_WILL, StoneRoot.HARMING),
            StoneRoot.allowedFor(StoneType.BLACK)
        )
    }

    @Test
    fun rootsIgnoreEmptyAndMalformedCsvValues() {
        val stone = Stone(
            type = StoneType.BLACK,
            timestamp = 100L,
            dayKey = "2026-05-17",
            rootTagsCsv = "sensual,,missing,illWill"
        )

        assertEquals(listOf(StoneRoot.SENSUAL, StoneRoot.ILL_WILL), stone.roots)
    }

    @Test
    fun customDescriptorsTrimBlankLines() {
        val stone = Stone(
            type = StoneType.WHITE,
            timestamp = 100L,
            dayKey = "2026-05-17",
            rootDescriptor = " concern \n\npatience "
        )

        assertEquals(listOf("concern", "patience"), stone.customRootDescriptors)
    }

    @Test
    fun tagSummaryCombinesRootsCustomDescriptorsAndIntensity() {
        val stone = Stone(
            type = StoneType.WHITE,
            timestamp = 100L,
            dayKey = "2026-05-17",
            rootTagsCsv = "kindness,harmlessness",
            rootDescriptor = "patience",
            intensity = "strong"
        )

        assertEquals("kindness · harmlessness · patience · strong", stone.tagSummaryText)
    }

    @Test
    fun emptyTagsReturnNullSummaryAndStorageStrings() {
        val stone = Stone(
            type = StoneType.WHITE,
            timestamp = 100L,
            dayKey = "2026-05-17",
            rootTagsCsv = "",
            rootDescriptor = " \n "
        )

        assertNull(stone.tagSummaryText)
        assertNull(emptyList<StoneRoot>().toRootTagsCsv())
        assertNull(listOf(" ", "").toRootDescriptorString())
    }
}
