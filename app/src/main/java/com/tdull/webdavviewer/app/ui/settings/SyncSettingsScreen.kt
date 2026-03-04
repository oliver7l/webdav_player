package com.tdull.webdavviewer.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tdull.webdavviewer.app.data.model.SyncStatus
import com.tdull.webdavviewer.app.viewmodel.SyncViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * 同步设置页面
 */
@Composable
fun SyncSettingsScreen(
    syncViewModel: SyncViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val syncManager = syncViewModel.syncManager
    val syncData by syncManager.syncData.collectAsState()
    val isSyncEnabled by syncViewModel.isSyncEnabled.collectAsState(initial = false)
    val isAutoSyncEnabled by syncViewModel.isAutoSyncEnabled.collectAsState(initial = false)
    val syncInterval by syncViewModel.syncInterval.collectAsState(initial = 60)
    val coroutineScope = rememberCoroutineScope()
    
    // 同步间隔选项（分钟）
    val syncIntervalOptions = listOf(15, 30, 60, 120, 180, 360, 720, 1440)
    val syncIntervalText = when (syncInterval) {
        15 -> "15分钟"
        30 -> "30分钟"
        60 -> "1小时"
        120 -> "2小时"
        180 -> "3小时"
        360 -> "6小时"
        720 -> "12小时"
        1440 -> "1天"
        else -> "${syncInterval}分钟"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "云同步设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        // 同步开关卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "云同步",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isSyncEnabled) "已启用" else "已禁用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = isSyncEnabled,
                    onCheckedChange = {
                        coroutineScope.launch {
                            syncViewModel.setSyncEnabled(it)
                            // 暂时注释掉，因为WorkManager依赖未添加
                            /*if (it && isAutoSyncEnabled) {
                                // 启用同步且自动同步已开启，启动定时同步任务
                                SyncWorker.scheduleSyncWorker(context, syncInterval)
                            } else {
                                // 禁用同步，取消定时同步任务
                                SyncWorker.cancelSyncWorker(context)
                            }*/
                        }
                    }
                )
            }
        }
        
        // 自动同步设置卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "自动同步设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // 自动同步开关
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "自动同步",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "根据设定的间隔自动执行同步",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = isAutoSyncEnabled,
                        onCheckedChange = {
                            coroutineScope.launch {
                                syncViewModel.setAutoSyncEnabled(it)
                                // 暂时注释掉，因为WorkManager依赖未添加
                                /*if (it && isSyncEnabled) {
                                    // 启用自动同步
                                    SyncWorker.scheduleSyncWorker(context, syncInterval)
                                } else {
                                    // 禁用自动同步
                                    SyncWorker.cancelSyncWorker(context)
                                }*/
                            }
                        },
                        enabled = isSyncEnabled
                    )
                }
                
                // 同步间隔设置
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "同步间隔",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = syncIntervalText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { expanded = true },
                            enabled = isSyncEnabled && isAutoSyncEnabled
                        ) {
                            Text(text = "设置")
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            syncIntervalOptions.forEach { interval ->
                                val intervalText = when (interval) {
                                    15 -> "15分钟"
                                    30 -> "30分钟"
                                    60 -> "1小时"
                                    120 -> "2小时"
                                    180 -> "3小时"
                                    360 -> "6小时"
                                    720 -> "12小时"
                                    1440 -> "1天"
                                    else -> "${interval}分钟"
                                }
                                DropdownMenuItem(
                                    text = { Text(text = intervalText) },
                                    onClick = {
                                        coroutineScope.launch {
                                            syncViewModel.setSyncInterval(interval)
                                            // 如果自动同步已启用，更新定时同步任务
                                            if (isAutoSyncEnabled && isSyncEnabled) {
                                                // 暂时注释掉，因为WorkManager依赖未添加
                                                // SyncWorker.scheduleSyncWorker(context, interval)
                                            }
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 同步状态卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "同步状态",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (syncData.status) {
                            SyncStatus.NOT_SYNCED -> "未同步"
                            SyncStatus.SYNCING -> "同步中"
                            SyncStatus.SYNCED -> "已同步"
                            SyncStatus.SYNC_FAILED -> "同步失败"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (syncData.status) {
                            SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                            SyncStatus.SYNC_FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (syncData.lastSyncTime > 0) {
                        Text(
                            text = formatSyncTime(syncData.lastSyncTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (syncData.errorMessage != null) {
                    Text(
                        text = syncData.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
        
        // 同步操作按钮
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "同步操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        syncManager.sync(uploadFirst = true)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = syncData.status != SyncStatus.SYNCING
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "同步",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "同步")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        syncManager.upload()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = syncData.status != SyncStatus.SYNCING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = "上传",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "上传到云端")
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        syncManager.download()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = syncData.status != SyncStatus.SYNCING,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "下载",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "从云端下载")
            }
        }
        
        // 同步说明
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "同步说明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• 同步数据存储在WebDAV服务器的/_sync/目录中",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• 同步内容包括：收藏、播放列表、标签",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• 同步操作会覆盖目标端的数据，请谨慎操作",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• 请确保WebDAV服务器有写入权限",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * 格式化同步时间
 */
private fun formatSyncTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
