package com.whitestone.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import com.whitestone.app.data.StoneType
import com.whitestone.app.util.DateHelpers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val stoneDao: StoneDao
) : ViewModel() {

    val allStones: Flow<List<Stone>> = stoneDao.getAllStones()

    fun insertStone(type: StoneType, timestampMillis: Long, note: String) {
        viewModelScope.launch {
            stoneDao.insert(
                Stone(
                    type = type,
                    timestamp = timestampMillis,
                    note = note,
                    dayKey = DateHelpers.dayKey(timestampMillis)
                )
            )
        }
    }
}
