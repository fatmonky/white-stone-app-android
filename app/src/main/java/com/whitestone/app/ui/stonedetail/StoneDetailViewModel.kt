package com.whitestone.app.ui.stonedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoneDetailViewModel @Inject constructor(
    private val stoneDao: StoneDao
) : ViewModel() {

    private val _stone = MutableStateFlow<Stone?>(null)
    val stone: StateFlow<Stone?> = _stone

    fun loadStone(stoneId: Long) {
        viewModelScope.launch {
            _stone.value = stoneDao.getStoneById(stoneId)
        }
    }

    fun updateStone(stone: Stone) {
        viewModelScope.launch {
            stoneDao.update(stone)
            _stone.value = stone
        }
    }
}
