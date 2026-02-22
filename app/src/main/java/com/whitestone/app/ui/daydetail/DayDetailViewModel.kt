package com.whitestone.app.ui.daydetail

import androidx.lifecycle.ViewModel
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val stoneDao: StoneDao
) : ViewModel() {

    fun getStonesForDay(dayKey: String): Flow<List<Stone>> {
        return stoneDao.getStonesForDay(dayKey)
    }
}
