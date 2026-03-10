package com.tdull.webdavviewer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdull.webdavviewer.app.data.repository.DirectoryHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * 目录历史ViewModel
 * 管理目录历史页面的状态和操作
 */
@HiltViewModel
class DirectoryHistoryViewModel @Inject constructor(
    private val directoryHistoryRepository: DirectoryHistoryRepository
) : ViewModel() {
    
    // 目录历史列表
    private val _directoryHistoryItems = MutableStateFlow(emptyList<com.tdull.webdavviewer.app.data.model.DirectoryHistoryItem>())
    val directoryHistoryItems = _directoryHistoryItems.asStateFlow()
    
    // 清空历史对话框显示状态
    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog = _showClearDialog.asStateFlow()
    
    init {
        // 加载目录历史
        loadDirectoryHistory()
    }
    
    /**
     * 加载目录历史
     */
    private fun loadDirectoryHistory() {
        viewModelScope.launch {
            directoryHistoryRepository.directoryHistoryItems.collect {
                _directoryHistoryItems.value = it
            }
        }
    }
    
    /**
     * 移除目录历史项
     */
    fun removeDirectoryHistoryItem(id: String) {
        viewModelScope.launch {
            directoryHistoryRepository.removeDirectoryHistoryItem(id)
        }
    }
    
    /**
     * 清空目录历史
     */
    fun clearDirectoryHistory() {
        viewModelScope.launch {
            directoryHistoryRepository.clearDirectoryHistory()
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
