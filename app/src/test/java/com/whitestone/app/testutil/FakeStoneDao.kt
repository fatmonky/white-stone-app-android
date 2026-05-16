package com.whitestone.app.testutil

import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStoneDao(initialStones: List<Stone> = emptyList()) : StoneDao {
    private val stonesFlow = MutableStateFlow(initialStones.sortedBy { it.timestamp })
    private var nextId = (initialStones.maxOfOrNull { it.id } ?: 0L) + 1L

    override fun getAllStones(): Flow<List<Stone>> = stonesFlow

    override fun getStonesForDay(dayKey: String): Flow<List<Stone>> = stonesFlow.map { stones ->
        stones.filter { it.dayKey == dayKey }.sortedBy { it.timestamp }
    }

    override suspend fun getStoneById(id: Long): Stone? =
        stonesFlow.value.firstOrNull { it.id == id }

    override suspend fun insert(stone: Stone): Long {
        val assignedId = if (stone.id == 0L) nextId++ else stone.id
        val storedStone = stone.copy(id = assignedId)
        stonesFlow.value = (stonesFlow.value + storedStone).sortedBy { it.timestamp }
        return assignedId
    }

    override suspend fun update(stone: Stone) {
        stonesFlow.value = stonesFlow.value
            .map { if (it.id == stone.id) stone else it }
            .sortedBy { it.timestamp }
    }

    override suspend fun delete(stone: Stone) {
        stonesFlow.value = stonesFlow.value.filterNot { it.id == stone.id }
    }
}
