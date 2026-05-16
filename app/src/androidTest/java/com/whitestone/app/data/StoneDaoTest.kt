package com.whitestone.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StoneDaoTest {
    private lateinit var database: StoneDatabase
    private lateinit var stoneDao: StoneDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoneDatabase::class.java
        ).build()
        stoneDao = database.stoneDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndQueryByDayReturnsTimestampSortedResults() = runBlocking {
        stoneDao.insert(
            Stone(
                type = StoneType.BLACK,
                timestamp = 300L,
                note = "Later",
                dayKey = "2026-03-08"
            )
        )
        val middleId = stoneDao.insert(
            Stone(
                type = StoneType.WHITE,
                timestamp = 200L,
                note = "Middle",
                dayKey = "2026-03-08"
            )
        )
        stoneDao.insert(
            Stone(
                type = StoneType.WHITE,
                timestamp = 100L,
                note = "Earlier",
                dayKey = "2026-03-08"
            )
        )
        stoneDao.insert(
            Stone(
                type = StoneType.BLACK,
                timestamp = 50L,
                note = "Other day",
                dayKey = "2026-03-07"
            )
        )

        val stones = stoneDao.getStonesForDay("2026-03-08").first()

        assertEquals(listOf(100L, 200L, 300L), stones.map { it.timestamp })
        assertEquals(listOf("Earlier", "Middle", "Later"), stones.map { it.note })
        assertNotNull(stoneDao.getStoneById(middleId))
    }

    @Test
    fun updateAndDeleteMutateStoredStone() = runBlocking {
        val stoneId = stoneDao.insert(
            Stone(
                type = StoneType.WHITE,
                timestamp = 100L,
                note = "Original",
                dayKey = "2026-03-08"
            )
        )
        val storedStone = stoneDao.getStoneById(stoneId)!!
        val updatedStone = storedStone.copy(note = "Updated", type = StoneType.BLACK)

        stoneDao.update(updatedStone)

        assertEquals(updatedStone, stoneDao.getStoneById(stoneId))

        stoneDao.delete(updatedStone)

        assertEquals(null, stoneDao.getStoneById(stoneId))
    }

    @Test
    fun insertAndQueryRoundTripsTagFields() = runBlocking {
        val stoneId = stoneDao.insert(
            Stone(
                type = StoneType.WHITE,
                timestamp = 100L,
                note = "Tagged",
                dayKey = "2026-05-17",
                rootTagsCsv = "kindness,harmlessness",
                rootDescriptor = "patience",
                intensity = "weak"
            )
        )

        val stone = stoneDao.getStoneById(stoneId)!!

        assertEquals("kindness,harmlessness", stone.rootTagsCsv)
        assertEquals("patience", stone.rootDescriptor)
        assertEquals("weak", stone.intensity)
    }
}
