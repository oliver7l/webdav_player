package com.tdull.webdavviewer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.data.model.PlaylistItem
import com.tdull.webdavviewer.app.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 播放列表管理ViewModel
 */
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    
    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()
    
    init {
        // 加载所有播放列表
        viewModelScope.launch {
            playlistRepository.playlists.collect {
                _playlists.value = it
                // 打印调试信息
                println("Playlists updated: ${it.size} playlists")
                it.forEach { playlist ->
                    println("  Playlist: ${playlist.name}, items: ${playlist.items.size}")
                }
            }
        }
    }
    
    /**
     * 创建新播放列表
     */
    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createPlaylist(name)
        }
    }
    
    /**
     * 重命名播放列表
     */
    fun renamePlaylist(playlistId: String, newName: String) {
        viewModelScope.launch {
            playlistRepository.updatePlaylistName(playlistId, newName)
        }
    }
    
    /**
     * 删除播放列表
     */
    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            playlistRepository.deletePlaylist(playlistId)
            // 如果删除的是当前选中的播放列表，清除选择
            if (_selectedPlaylist.value?.id == playlistId) {
                _selectedPlaylist.value = null
            }
        }
    }
    
    /**
     * 选择播放列表
     */
    fun selectPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
    }
    
    /**
     * 取消选择播放列表
     */
    fun deselectPlaylist() {
        _selectedPlaylist.value = null
    }
    
    /**
     * 从播放列表中移除项目
     */
    fun removeItemFromPlaylist(playlistId: String, itemId: String) {
        viewModelScope.launch {
            playlistRepository.removeItemFromPlaylist(playlistId, itemId)
            // 更新当前选中的播放列表
            _selectedPlaylist.value?.let {
                if (it.id == playlistId) {
                    val updatedPlaylist = playlistRepository.getPlaylist(playlistId)
                    _selectedPlaylist.value = updatedPlaylist
                }
            }
        }
    }
    
    /**
     * 添加项目到播放列表
     */
    fun addItemToPlaylist(playlistId: String, videoUrl: String, videoTitle: String, serverId: String, resourcePath: String) {
        viewModelScope.launch {
            val playlistItem = PlaylistItem(
                id = UUID.randomUUID().toString(),
                videoUrl = videoUrl,
                videoTitle = videoTitle,
                serverId = serverId,
                resourcePath = resourcePath,
                order = 0
            )
            playlistRepository.addItemToPlaylist(playlistId, playlistItem)
            // 更新当前选中的播放列表
            _selectedPlaylist.value?.let {
                if (it.id == playlistId) {
                    val updatedPlaylist = playlistRepository.getPlaylist(playlistId)
                    _selectedPlaylist.value = updatedPlaylist
                }
            }
        }
    }
    
    /**
     * 获取播放列表
     */
    fun getPlaylist(playlistId: String): Playlist? {
        return _playlists.value.find { it.id == playlistId }
    }
}
