package com.tdull.webdavviewer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdull.webdavviewer.app.data.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * 播放历史ViewModel
 * 管理播放历史页面的状态和操作
 */
@HiltViewModel
class PlayHistoryViewModel @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) : ViewModel() {
    
    // 播放历史列表
    private val _playHistoryItems = MutableStateFlow(emptyList<com.tdull.webdavviewer.app.data.model.PlayHistoryItem>())
    val playHistoryItems = _playHistoryItems.asStateFlow()
    
    // 清空历史对话框显示状态
    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog = _showClearDialog.asStateFlow()
    
    init {
        // 加载播放历史
        loadPlayHistory()
    }
    
    /**
     * 加载播放历史
     */
    private fun loadPlayHistory() {
        viewModelScope.launch {
            playHistoryRepository.playHistoryItems.collect {
                _playHistoryItems.value = it
            }
        }
    }
    
    /**
     * 移除播放历史项
     */
    fun removePlayHistoryItem(id: String) {
        viewModelScope.launch {
            playHistoryRepository.removePlayHistoryItem(id)
        }
    }
    
    /**
     * 清空播放历史
     */
    fun clearPlayHistory() {
        viewModelScope.launch {
            playHistoryRepository.clearPlayHistory()
        }
    }
    
    /**
     * 显示清空历史对话框
     */
    fun showClearDialog() {
        _showClearDialog.value = true
    }
    
    /**
     * 隐藏清空历史对话框
     */
    fun hideClearDialog() {
        _showClearDialog.value = false
    }
}