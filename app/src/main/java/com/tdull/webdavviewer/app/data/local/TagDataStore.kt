package com.tdull.webdavviewer.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tdull.webdavviewer.app.data.model.Tag
import com.tdull.webdavviewer.app.data.model.VideoTag
import com.tdull.webdavviewer.app.data.repository.TagRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "webdav_tags")

/**
 * 使用DataStore存储标签数据
 */
@Singleton
class TagDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : TagRepository {
    companion object {
        private val TAGS_KEY = stringPreferencesKey("tags")
        private val VIDEO_TAGS_KEY = stringPreferencesKey("video_tags")
    }

    /**
     * 获取所有标签
     */
    override val tags: Flow<List<Tag>> = context.dataStore.data.map { preferences ->
        val tagsJson = preferences[TAGS_KEY] ?: "[]"
        parseTags(tagsJson)
    }

    /**
     * 获取指定视频的标签
     */
    override suspend fun getTagsForVideo(videoUrl: String): List<Tag> {
        val preferences = context.dataStore.data.first()
        val allTags = preferences[TAGS_KEY]?.let {
            parseTags(it)
        } ?: emptyList()

        val videoTags = preferences[VIDEO_TAGS_KEY]?.let {
            parseVideoTags(it)
        } ?: emptyList()

        val tagIds = videoTags.filter { it.videoUrl == videoUrl }.map { it.tagId }
        return allTags.filter { tagIds.contains(it.id) }
    }

    /**
     * 创建标签
     */
    override suspend fun createTag(name: String, color: String): Tag {
        val newTag = Tag(
            id = UUID.randomUUID().toString(),
            name = name,
            color = color
        )

        context.dataStore.edit { preferences ->
            val tagsJson = preferences[TAGS_KEY] ?: "[]"
            val tags = parseTags(tagsJson).toMutableList()
            tags.add(newTag)
            preferences[TAGS_KEY] = serializeTags(tags)
        }

        return newTag
    }

    /**
     * 添加标签到视频
     */
    override suspend fun addTagToVideo(videoUrl: String, tagId: String) {
        context.dataStore.edit { preferences ->
            val videoTagsJson = preferences[VIDEO_TAGS_KEY] ?: "[]"
            val videoTags = parseVideoTags(videoTagsJson).toMutableList()

            // 检查是否已存在
            val exists = videoTags.any { it.videoUrl == videoUrl && it.tagId == tagId }
            if (!exists) {
                val videoTag = VideoTag(
                    videoUrl = videoUrl,
                    tagId = tagId
                )
                videoTags.add(videoTag)
                preferences[VIDEO_TAGS_KEY] = serializeVideoTags(videoTags)
            }
        }
    }

    /**
     * 从视频移除标签
     */
    override suspend fun removeTagFromVideo(videoUrl: String, tagId: String) {
        context.dataStore.edit { preferences ->
            val videoTagsJson = preferences[VIDEO_TAGS_KEY] ?: "[]"
            val videoTags = parseVideoTags(videoTagsJson)
                .filter { !(it.videoUrl == videoUrl && it.tagId == tagId) }
            preferences[VIDEO_TAGS_KEY] = serializeVideoTags(videoTags)
        }
    }

    /**
     * 删除标签
     */
    override suspend fun deleteTag(tagId: String) {
        context.dataStore.edit { preferences ->
            // 删除标签
            val tagsJson = preferences[TAGS_KEY] ?: "[]"
            val tags = parseTags(tagsJson).filter { it.id != tagId }
            preferences[TAGS_KEY] = serializeTags(tags)

            // 删除相关的视频标签关联
            val videoTagsJson = preferences[VIDEO_TAGS_KEY] ?: "[]"
            val videoTags = parseVideoTags(videoTagsJson).filter { it.tagId != tagId }
            preferences[VIDEO_TAGS_KEY] = serializeVideoTags(videoTags)
        }
    }

    /**
     * 获取带有指定标签的所有视频
     */
    override suspend fun getVideosWithTag(tagId: String): List<String> {
        val preferences = context.dataStore.data.first()
        val videoTags = preferences[VIDEO_TAGS_KEY]?.let {
            parseVideoTags(it)
        } ?: emptyList()

        return videoTags.filter { item -> item.tagId == tagId }.map { item -> item.videoUrl }
    }

    /**
     * 重命名标签
     */
    override suspend fun renameTag(tagId: String, newName: String) {
        context.dataStore.edit { preferences ->
            val tagsJson = preferences[TAGS_KEY] ?: "[]"
            val tags = parseTags(tagsJson).toMutableList()
            val tagIndex = tags.indexOfFirst { it.id == tagId }
            if (tagIndex >= 0) {
                tags[tagIndex] = tags[tagIndex].copy(name = newName)
                preferences[TAGS_KEY] = serializeTags(tags)
            }
        }
    }

    /**
     * 更新标签颜色
     */
    override suspend fun updateTagColor(tagId: String, newColor: String) {
        context.dataStore.edit { preferences ->
            val tagsJson = preferences[TAGS_KEY] ?: "[]"
            val tags = parseTags(tagsJson).toMutableList()
            val tagIndex = tags.indexOfFirst { it.id == tagId }
            if (tagIndex >= 0) {
                tags[tagIndex] = tags[tagIndex].copy(color = newColor)
                preferences[TAGS_KEY] = serializeTags(tags)
            }
        }
    }

    /**
     * 解析标签JSON
     */
    private fun parseTags(json: String): List<Tag> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                Tag(
                    id = jsonObject.optString("id", UUID.randomUUID().toString()),
                    name = jsonObject.optString("name", ""),
                    color = jsonObject.optString("color", "#3B82F6"),
                    createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 解析视频标签关联JSON
     */
    private fun parseVideoTags(json: String): List<VideoTag> {
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { index ->
                val jsonObject = jsonArray.getJSONObject(index)
                VideoTag(
                    id = jsonObject.optString("id", UUID.randomUUID().toString()),
                    videoUrl = jsonObject.optString("videoUrl", ""),
                    tagId = jsonObject.optString("tagId", ""),
                    createdAt = jsonObject.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 序列化标签为JSON
     */
    private fun serializeTags(tags: List<Tag>): String {
        val jsonArray = JSONArray()
        tags.forEach { tag ->
            val jsonObject = JSONObject().apply {
                put("id", tag.id)
                put("name", tag.name)
                put("color", tag.color)
                put("createdAt", tag.createdAt)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    /**
     * 序列化视频标签关联为JSON
     */
    private fun serializeVideoTags(videoTags: List<VideoTag>): String {
        val jsonArray = JSONArray()
        videoTags.forEach { videoTag ->
            val jsonObject = JSONObject().apply {
                put("id", videoTag.id)
                put("videoUrl", videoTag.videoUrl)
                put("tagId", videoTag.tagId)
                put("createdAt", videoTag.createdAt)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }
}