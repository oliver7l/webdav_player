package com.tdull.webdavviewer.app.ui.quickaccess

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.tdull.webdavviewer.app.data.model.QuickAccessItem
import com.tdull.webdavviewer.app.navigation.Screen
import com.tdull.webdavviewer.app.ui.common.MainScreenContainer
import com.tdull.webdavviewer.app.viewmodel.QuickAccessViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 快速访问页面
 * 用于展示和管理用户添加的快速访问目录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessScreen(
    navController: NavHostController,
    viewModel: QuickAccessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val quickAccessItems = uiState.quickAccessItems
    val showEmptyState = uiState.showEmptyState
    
    MainScreenContainer(
        navController = navController,
        currentRoute = Screen.QuickAccess.route,
        title = "快速访问"
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                showEmptyState -> {
                    // 空状态
                    EmptyQuickAccessState()
                }
                else -> {
                    // 快速访问列表
                    QuickAccessList(
                        quickAccessItems = quickAccessItems,
                        onQuickAccessClick = { item ->
                            // 导航到文件浏览器页面
                            navController.navigate(Screen.Browser.createRoute(item.serverId, item.path))
                        },
                        onDelete = { id ->
                            viewModel.removeQuickAccessItem(id)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 快速访问列表
 */
@Composable
private fun QuickAccessList(
    quickAccessItems: List<QuickAccessItem>,
    onQuickAccessClick: (QuickAccessItem) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = quickAccessItems,
            key = { it.id }
        ) { item ->
            QuickAccessItem(
                item = item,
                onClick = { onQuickAccessClick(item) },
                onDelete = { onDelete(item.id) }
            )
        }
    }
}

/**
 * 快速访问项
 */
@Composable
private fun QuickAccessItem(
    item: QuickAccessItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件夹图标
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "文件夹",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 目录信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "添加于 ${formatDate(item.addedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // 删除按钮
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 空快速访问状态
 */
@Composable
private fun EmptyQuickAccessState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无快速访问",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "在文件浏览器中点击添加到快速访问按钮",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 格式化日期
 */
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
