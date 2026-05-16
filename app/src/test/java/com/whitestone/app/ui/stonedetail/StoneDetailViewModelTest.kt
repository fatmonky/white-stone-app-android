package com.whitestone.app.ui.stonedetail

import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneType
import com.whitestone.app.testutil.FakeStoneDao
import com.whitestone.app.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoneDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadStonePublishesRequestedStone() = runTest {
        val existingStone = Stone(
            id = 7L,
            type = StoneType.WHITE,
            timestamp = 1_741_392_000_000L,
            note = "Patience",
            dayKey = "2025-03-08"
        )
        val viewModel = StoneDetailViewModel(FakeStoneDao(listOf(existingStone)))

        viewModel.loadStone(7L)
        advanceUntilIdle()

        assertEquals(existingStone, viewModel.stone.value)
    }

    @Test
    fun updateStoneWritesThroughAndRefreshesState() = runTest {
        val existingStone = Stone(
            id = 8L,
            type = StoneType.BLACK,
            timestamp = 1_741_392_000_000L,
            note = "Before",
            dayKey = "2025-03-08"
        )
        val dao = FakeStoneDao(listOf(existingStone))
        val viewModel = StoneDetailViewModel(dao)
        val updatedStone = existingStone.copy(note = "After")

        viewModel.updateStone(updatedStone)
        advanceUntilIdle()

        assertEquals(updatedStone, viewModel.stone.value)
        assertEquals(updatedStone, dao.getStoneById(8L))
    }

    @Test
    fun loadStoneLeavesNullWhenStoneDoesNotExist() = runTest {
        val viewModel = StoneDetailViewModel(FakeStoneDao())

        viewModel.loadStone(99L)
        advanceUntilIdle()

        assertNull(viewModel.stone.value)
    }
}
