package com.tdull.webdavviewer.app.data.repository

import com.tdull.webdavviewer.app.data.model.Tag
import com.tdull.webdavviewer.app.data.model.VideoTag
import kotlinx.coroutines.flow.Flow

/**
 * 标签仓库接口
 */
interface TagRepository {
    /**
     * 获取所有标签
     */
    val tags: Flow<List<Tag>>

    /**
     * 获取指定视频的标签
     */
    suspend fun getTagsForVideo(videoUrl: String): List<Tag>

    /**
     * 创建标签
     */
    suspend fun createTag(name: String, color: String = "#3B82F6"): Tag

    /**
     * 添加标签到视频
     */
    suspend fun addTagToVideo(videoUrl: String, tagId: String)

    /**
     * 从视频移除标签
     */
    suspend fun removeTagFromVideo(videoUrl: String, tagId: String)

    /**
     * 删除标签
     */
    suspend fun deleteTag(tagId: String)

    /**
     * 获取带有指定标签的所有视频
     */
    suspend fun getVideosWithTag(tagId: String): List<String> // 返回视频URL列表

    /**
     * 重命名标签
     */
    suspend fun renameTag(tagId: String, newName: String)

    /**
     * 更新标签颜色
     */
    suspend fun updateTagColor(tagId: String, newColor: String)
}