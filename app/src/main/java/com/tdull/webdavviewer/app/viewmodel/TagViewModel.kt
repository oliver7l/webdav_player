package com.tdull.webdavviewer.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tdull.webdavviewer.app.data.model.Tag
import com.tdull.webdavviewer.app.data.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 标签管理ViewModel
 */
@HiltViewModel
class TagViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    init {
        // 加载所有标签
        viewModelScope.launch {
            tagRepository.tags.collect {
                _tags.value = it
            }
        }
    }

    /**
     * 创建新标签
     */
    fun createTag(name: String, color: String = "#3B82F6") {
        viewModelScope.launch {
            tagRepository.createTag(name, color)
        }
    }

    /**
     * 添加标签到视频
     */
    fun addTagToVideo(videoUrl: String, tagId: String) {
        viewModelScope.launch {
            tagRepository.addTagToVideo(videoUrl, tagId)
        }
    }

    /**
     * 从视频移除标签
     */
    fun removeTagFromVideo(videoUrl: String, tagId: String) {
        viewModelScope.launch {
            tagRepository.removeTagFromVideo(videoUrl, tagId)
        }
    }

    /**
     * 删除标签
     */
    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            tagRepository.deleteTag(tagId)
        }
    }

    /**
     * 重命名标签
     */
    fun renameTag(tagId: String, newName: String) {
        viewModelScope.launch {
            tagRepository.renameTag(tagId, newName)
        }
    }

    /**
     * 更新标签颜色
     */
    fun updateTagColor(tagId: String, newColor: String) {
        viewModelScope.launch {
            tagRepository.updateTagColor(tagId, newColor)
        }
    }

    /**
     * 获取指定视频的标签
     */
    suspend fun getTagsForVideo(videoUrl: String): List<Tag> {
        return tagRepository.getTagsForVideo(videoUrl)
    }

    /**
     * 获取带有指定标签的所有视频
     */
    suspend fun getVideosWithTag(tagId: String): List<String> {
        return tagRepository.getVideosWithTag(tagId)
    }
}