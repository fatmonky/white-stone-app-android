package com.whitestone.app.ui.today

import com.whitestone.app.data.StoneType
import com.whitestone.app.testutil.FakeStoneDao
import com.whitestone.app.testutil.MainDispatcherRule
import com.whitestone.app.util.DateHelpers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun insertStonePersistsTypeTimestampNoteAndDerivedDayKey() = runTest {
        val dao = FakeStoneDao()
        val viewModel = TodayViewModel(dao)
        val timestamp = 1_741_392_000_000L

        viewModel.insertStone(StoneType.BLACK, timestamp, "Observed irritation")
        advanceUntilIdle()

        val storedStone = dao.getAllStones().first().single()
        assertEquals(StoneType.BLACK, storedStone.type)
        assertEquals(timestamp, storedStone.timestamp)
        assertEquals("Observed irritation", storedStone.note)
        assertEquals(DateHelpers.dayKey(timestamp), storedStone.dayKey)
    }
}
