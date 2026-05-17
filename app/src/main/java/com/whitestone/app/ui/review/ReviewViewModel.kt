package com.whitestone.app.ui.review

import androidx.lifecycle.ViewModel
import com.whitestone.app.data.Reflection
import com.whitestone.app.data.ReflectionDao
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    stoneDao: StoneDao,
    reflectionDao: ReflectionDao
) : ViewModel() {
    val allStones: Flow<List<Stone>> = stoneDao.getAllStones()
    val reflectionDayKeys: Flow<List<String>> = reflectionDao.getAllDayKeys()
    val allReflections: Flow<List<Reflection>> = reflectionDao.getAllNewestFirst()
}
