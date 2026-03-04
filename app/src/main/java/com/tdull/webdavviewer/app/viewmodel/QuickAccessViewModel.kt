package com.tdull.webdavviewer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdull.webdavviewer.app.data.model.QuickAccessItem
import com.tdull.webdavviewer.app.data.repository.QuickAccessRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 快速访问视图模型
 * 用于管理快速访问目录的增删查操作
 */
@HiltViewModel
class QuickAccessViewModel @Inject constructor(
    private val quickAccessRepository: QuickAccessRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuickAccessUiState())
    val uiState: StateFlow<QuickAccessUiState> = _uiState.asStateFlow()
    
    init {
        loadQuickAccessItems()
    }
    
    /**
     * 加载快速访问项
     */
    private fun loadQuickAccessItems() {
        viewModelScope.launch {
            quickAccessRepository.quickAccessItems.collectLatest {items ->
                _uiState.update {state ->
                    state.copy(
                        quickAccessItems = items,
                        showEmptyState = items.isEmpty()
                    )
                }
            }
        }
    }
    
    /**
     * 添加快速访问项
     */
    fun addQuickAccessItem(serverId: String, path: String, name: String) {
        viewModelScope.launch {
            val quickAccessItem = QuickAccessItem(
                serverId = serverId,
                path = path,
                name = name
            )
            quickAccessRepository.addQuickAccessItem(quickAccessItem)
        }
    }
    
    /**
     * 移除快速访问项
     */
    fun removeQuickAccessItem(id: String) {
        viewModelScope.launch {
            quickAccessRepository.removeQuickAccessItem(id)
        }
    }
    
    /**
     * 检查路径是否已添加到快速访问
     */
    suspend fun isQuickAccess(serverId: String, path: String): Boolean {
        return quickAccessRepository.isQuickAccess(serverId, path).first()
    }
}

/**
 * 快速访问UI状态
 */
data class QuickAccessUiState(
    val quickAccessItems: List<QuickAccessItem> = emptyList(),
    val showEmptyState: Boolean = false
)
