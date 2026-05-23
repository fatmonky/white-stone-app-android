package com.whitestone.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import com.whitestone.app.data.StoneType
import com.whitestone.app.util.DateHelpers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val stoneDao: StoneDao
) : ViewModel() {

    val allStones: Flow<List<Stone>> = stoneDao.getAllStones()

    private val _stoneSavedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val stoneSavedEvents: SharedFlow<Unit> = _stoneSavedEvents

    fun insertStone(
        type: StoneType,
        timestampMillis: Long,
        note: String,
        rootTagsCsv: String? = null,
        rootDescriptor: String? = null,
        intensity: String? = null
    ) {
        viewModelScope.launch {
            stoneDao.insert(
                Stone(
                    type = type,
                    timestamp = timestampMillis,
                    note = note,
                    dayKey = DateHelpers.dayKey(timestampMillis),
                    rootTagsCsv = rootTagsCsv,
                    rootDescriptor = rootDescriptor,
                    intensity = intensity
                )
            )
            _stoneSavedEvents.emit(Unit)
        }
    }
}
