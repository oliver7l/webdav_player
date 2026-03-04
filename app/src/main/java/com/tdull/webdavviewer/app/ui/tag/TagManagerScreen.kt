package com.tdull.webdavviewer.app.ui.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tdull.webdavviewer.app.data.model.Tag
import com.tdull.webdavviewer.app.navigation.Screen
import com.tdull.webdavviewer.app.ui.common.MainScreenContainer
import com.tdull.webdavviewer.app.viewmodel.TagViewModel

/**
 * 标签管理页面
 * 用于查看和管理所有标签及带有标签的视频
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManagerScreen(
    navController: NavHostController,
    onVideoClick: (String, String) -> Unit, // (videoUrl, videoTitle)
    viewModel: TagViewModel = hiltViewModel()
) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    
    // 状态
    var showRenameTagDialog by remember { mutableStateOf(false) }
    var showDeleteTagDialog by remember { mutableStateOf(false) }
    var renameTagName by remember { mutableStateOf("") }
    var tagToRename by remember { mutableStateOf<Tag?>(null) }
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }
    var selectedTag by remember { mutableStateOf<Tag?>(null) }
    var videosWithTag by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 加载标签视频
    LaunchedEffect(selectedTag) {
        if (selectedTag != null) {
            videosWithTag = viewModel.getVideosWithTag(selectedTag!!.id)
        }
    }

    MainScreenContainer(
        navController = navController,
        currentRoute = Screen.TagManager.route,
        title = if (selectedTag != null) "标签视频: ${selectedTag!!.name}" else "标签管理"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (selectedTag == null) {
                // 标签列表
                if (tags.isEmpty()) {
                    EmptyTagsState()
                } else {
                    TagList(
                        tags = tags,
                        onTagClick = { tag ->
                            selectedTag = tag
                        },
                        onTagEdit = { tag ->
                            tagToRename = tag
                            renameTagName = tag.name
                            showRenameTagDialog = true
                        },
                        onTagDelete = { tag ->
                            tagToDelete = tag
                            showDeleteTagDialog = true
                        }
                    )
                }
            } else {
                // 标签视频列表
                TagVideoList(
                    tag = selectedTag!!,
                    videos = videosWithTag,
                    onVideoClick = onVideoClick
                )
            }
        }
    }
    
    // 重命名标签对话框
    if (showRenameTagDialog) {
        AlertDialog(
            onDismissRequest = { showRenameTagDialog = false },
            title = { Text("重命名标签") },
            text = {
                Column {
                    Text("请输入新的标签名称")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = renameTagName,
                        onValueChange = { renameTagName = it },
                        label = { Text("标签名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameTagName.isNotBlank() && tagToRename != null) {
                            viewModel.renameTag(tagToRename!!.id, renameTagName)
                            showRenameTagDialog = false
                            renameTagName = ""
                            tagToRename = null
                        }
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameTagDialog = false
                    renameTagName = ""
                    tagToRename = null
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 删除标签对话框
    if (showDeleteTagDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTagDialog = false },
            title = { Text("删除标签") },
            text = {
                Text("确定要删除标签 ${tagToDelete?.name} 吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tagToDelete != null) {
                            viewModel.deleteTag(tagToDelete!!.id)
                            showDeleteTagDialog = false
                            tagToDelete = null
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteTagDialog = false
                    tagToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 空标签状态
 */
@Composable
private fun EmptyTagsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无标签",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "在视频播放页面为视频添加标签",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * 标签列表
 */
@Composable
private fun TagList(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    onTagEdit: (Tag) -> Unit,
    onTagDelete: (Tag) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tags) {
            TagCard(
                tag = it,
                onClick = { onTagClick(it) },
                onEditClick = { onTagEdit(it) },
                onDeleteClick = { onTagDelete(it) }
            )
        }
    }
}

/**
 * 标签卡片
 */
@Composable
private fun TagCard(
    tag: Tag,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color(android.graphics.Color.parseColor(tag.color)),
                            shape = MaterialTheme.shapes.small
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

/**
 * 标签视频列表
 */
@Composable
private fun TagVideoList(
    tag: Tag,
    videos: List<String>,
    onVideoClick: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "该标签下暂无视频",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(videos) {
                    VideoItem(
                        videoUrl = it,
                        onClick = { onVideoClick(it, extractVideoTitle(it)) }
                    )
                }
            }
        }
    }
}

/**
 * 视频项
 */
@Composable
private fun VideoItem(
    videoUrl: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = extractVideoTitle(videoUrl),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 从视频URL中提取视频标题
 */
private fun extractVideoTitle(videoUrl: String): String {
    val parts = videoUrl.split("/")
    return parts.lastOrNull() ?: videoUrl
}
