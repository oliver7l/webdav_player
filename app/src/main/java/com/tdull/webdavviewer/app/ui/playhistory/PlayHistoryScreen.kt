package com.tdull.webdavviewer.app.ui.playhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tdull.webdavviewer.app.data.model.PlayHistoryItem
import com.tdull.webdavviewer.app.navigation.Screen
import com.tdull.webdavviewer.app.ui.common.MainScreenContainer
import com.tdull.webdavviewer.app.viewmodel.PlayHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)

/**
 * 播放历史页面
 * 显示用户的视频播放历史记录
 */
@Composable
fun PlayHistoryScreen(
    navController: NavHostController,
    onPlayVideo: (String, String) -> Unit,
    viewModel: PlayHistoryViewModel = hiltViewModel()
) {
    val playHistoryItems by viewModel.playHistoryItems.collectAsStateWithLifecycle()
    val showClearDialog by viewModel.showClearDialog.collectAsStateWithLifecycle()

    // 格式化时间
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // 格式化播放时长
    fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    MainScreenContainer(
        navController = navController,
        currentRoute = Screen.PlayHistory.route,
        title = "播放历史"
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (playHistoryItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无播放历史",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playHistoryItems) { item ->
                        PlayHistoryItemCard(
                            item = item,
                            onPlay = { onPlayVideo(item.videoUrl, item.videoTitle) },
                            onDelete = { viewModel.removePlayHistoryItem(item.id) }
                        )
                    }
                }
            }

            // 清空历史对话框
            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideClearDialog() },
                    title = { Text("清空播放历史") },
                    text = { Text("确定要清空所有播放历史记录吗？") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.clearPlayHistory()
                                viewModel.hideClearDialog()
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.hideClearDialog() }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}

/**
 * 播放历史项卡片
 */
@Composable
fun PlayHistoryItemCard(
    item: PlayHistoryItem,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        elevation = CardDefaults.cardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 播放图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = Color(0xFFE0E0E0), shape = MaterialTheme.shapes.small)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "播放",
                    tint = Color(0xFF3B82F6)
                )
            }

            // 视频信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.videoTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(item.playedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "${formatDuration(item.position)} / ${formatDuration(item.duration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}

/**
 * 格式化时间戳为日期时间字符串
 */
private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * 格式化毫秒为分:秒格式
 */
private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}