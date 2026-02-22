package com.whitestone.app.ui.calendar

import androidx.lifecycle.ViewModel
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val stoneDao: StoneDao
) : ViewModel() {
    val allStones: Flow<List<Stone>> = stoneDao.getAllStones()
}
