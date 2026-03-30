package com.tdull.webdavviewer.app.ui.directoryhistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
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
import com.tdull.webdavviewer.app.data.model.DirectoryHistoryItem
import com.tdull.webdavviewer.app.navigation.Screen
import com.tdull.webdavviewer.app.ui.common.MainScreenContainer
import com.tdull.webdavviewer.app.viewmodel.DirectoryHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)

/**
 * 目录历史页面
 * 显示用户的目录访问历史记录
 */
@Composable
fun DirectoryHistoryScreen(
    navController: NavHostController,
    onNavigateToDirectory: (String, String) -> Unit, // (serverId, directoryPath)
    viewModel: DirectoryHistoryViewModel = hiltViewModel()
) {
    val directoryHistoryItems by viewModel.directoryHistoryItems.collectAsStateWithLifecycle()
    val showClearDialog by viewModel.showClearDialog.collectAsStateWithLifecycle()

    MainScreenContainer(
        navController = navController,
        currentRoute = Screen.DirectoryHistory.route,
        title = "目录历史",
        topBar = {
            TopAppBar(
                title = { Text("目录历史") },
                actions = {
                    IconButton(onClick = { viewModel.showClearDialog() }) {
                        Icon(Icons.Default.Delete, contentDescription = "清空历史")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (directoryHistoryItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无目录访问历史",
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
                    items(directoryHistoryItems) {
                        DirectoryHistoryItemCard(
                            item = it,
                            onNavigate = { onNavigateToDirectory(it.serverId, it.directoryPath) },
                            onDelete = { viewModel.removeDirectoryHistoryItem(it.id) }
                        )
                    }
                }
            }

            // 清空历史对话框
            if (showClearDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.hideClearDialog() },
                    title = { Text("清空目录历史") },
                    text = { Text("确定要清空所有目录访问历史吗？") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearDirectoryHistory()
                            viewModel.hideClearDialog()
                        }) {
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
 * 目录历史项卡片
 */
@Composable
fun DirectoryHistoryItemCard(
    item: DirectoryHistoryItem,
    onNavigate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() },
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
            // 文件夹图标
            Surface(
                    modifier = Modifier
                        .size(48.dp),
                    color = Color(0xFFE0E0E0),
                    shape = MaterialTheme.shapes.small
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Folder,
                            contentDescription = "文件夹",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                }

            // 目录信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.directoryName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.directoryPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(item.accessedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "访问 ${item.accessCount} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // 删除按钮
            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

/**
 * 格式化时间
 */
private fun formatTime(timestamp: Long): String {
    return timeFormat.format(Date(timestamp))
}
