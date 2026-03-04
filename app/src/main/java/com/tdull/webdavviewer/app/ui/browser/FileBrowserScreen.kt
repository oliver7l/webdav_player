package com.tdull.webdavviewer.app.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import android.util.Log
import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.data.model.WebDAVResource
import com.tdull.webdavviewer.app.navigation.Screen
import com.tdull.webdavviewer.app.ui.common.MainScreenContainer
import com.tdull.webdavviewer.app.viewmodel.FileBrowserViewModel

/**
 * 文件浏览器页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel = hiltViewModel(),
    navController: NavHostController,
    serverId: String? = null,
    onVideoClick: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val videoPreviews by viewModel.videoPreviews.collectAsState()
    val favoriteStates by viewModel.favoriteStates.collectAsState()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    
    // 全屏预览图状态
    var previewState by remember { mutableStateOf<PreviewState?>(null) }
    
    // 批量操作对话框状态
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    
    // 初始化服务器连接
    LaunchedEffect(serverId) {
        serverId?.let { viewModel.selectServerById(it) }
    }
    
    // 文件列表变化时加载收藏状态
    LaunchedEffect(uiState.files) {
        if (uiState.files.isNotEmpty()) {
            viewModel.loadFavoriteStates(uiState.files.map { it.path })
        }
    }
    
    MainScreenContainer(
        navController = navController,
        currentRoute = Screen.Browser.route,
        title = "文件浏览器"
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 面包屑导航
            if (uiState.isConnected && currentPath.isNotEmpty()) {
                Breadcrumb(
                    path = currentPath,
                    onNavigate = { path -> viewModel.navigateTo(path) }
                )
                HorizontalDivider()
            }
            
            // 内容区域
            when {
                !uiState.isConnected -> {
                    // 未连接状态
                    NotConnectedState(
                        onRetry = { serverId?.let { viewModel.selectServerById(it) } }
                    )
                }
                uiState.isLoading -> {
                    // 加载中
                    LoadingState()
                }
                uiState.error != null -> {
                    // 错误状态
                    ErrorState(
                        error = uiState.error ?: "未知错误",
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.files.isEmpty() -> {
                    // 空目录
                    EmptyDirectoryState()
                }
                else -> {
                    // 文件列表
                    FileList(
                        files = uiState.files,
                        videoPreviews = videoPreviews,
                        favoriteStates = favoriteStates,
                        isMultiSelectMode = isMultiSelectMode,
                        selectedFiles = selectedFiles,
                        onFileClick = { resource ->
                            if (isMultiSelectMode) {
                                viewModel.toggleFileSelection(resource)
                            } else {
                                handleFileClick(
                                    resource = resource,
                                    viewModel = viewModel,
                                    onVideoClick = onVideoClick,
                                    onImageClick = onImageClick
                                )
                            }
                        },
                        onPreviewClick = { images, index ->
                            previewState = PreviewState(images, index)
                        },
                        onLoadPreviews = { path ->
                            viewModel.loadVideoPreviews(path)
                        },
                        onToggleFavorite = { resource ->
                            viewModel.toggleFavorite(resource)
                        },
                        onLongClick = { resource ->
                            if (!isMultiSelectMode) {
                                viewModel.enterMultiSelectMode()
                                viewModel.toggleFileSelection(resource)
                            }
                        }
                    )
                }
            }
        }
    }
    
    // 全屏预览图对话框
    previewState?.let { state ->
        ImagePreviewDialog(
            images = state.images,
            initialIndex = state.initialIndex,
            onDismiss = { previewState = null }
        )
    }
    
    // 添加到播放列表对话框
    if (showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = { Text("添加到播放列表") },
            text = {
                Column {
                    Text("选择一个播放列表或创建新的播放列表")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 现有播放列表
                    if (playlists.isEmpty()) {
                        Text("暂无播放列表", color = MaterialTheme.colorScheme.outline)
                    } else {
                        playlists.forEach { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSelectedToPlaylist(playlist.id)
                                        showAddToPlaylistDialog = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = false,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(playlist.name)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 创建新播放列表按钮
                    Button(
                        onClick = { 
                            showAddToPlaylistDialog = false
                            showCreatePlaylistDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "创建新播放列表")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("创建新播放列表")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 创建新播放列表对话框
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("创建新播放列表") },
            text = {
                Column {
                    Text("请输入播放列表名称")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("播放列表名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            viewModel.createPlaylistAndAddSelected(newPlaylistName)
                            showCreatePlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCreatePlaylistDialog = false
                    newPlaylistName = ""
                }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 预览状态
 */
private data class PreviewState(
    val images: List<String>,
    val initialIndex: Int
)

/**
 * 文件列表
 */
@Composable
private fun FileList(
    files: List<WebDAVResource>,
    videoPreviews: Map<String, List<String>>,
    favoriteStates: Map<String, Boolean>,
    isMultiSelectMode: Boolean,
    selectedFiles: List<WebDAVResource>,
    onFileClick: (WebDAVResource) -> Unit,
    onPreviewClick: (List<String>, Int) -> Unit,
    onLoadPreviews: (String) -> Unit,
    onToggleFavorite: (WebDAVResource) -> Unit,
    onLongClick: (WebDAVResource) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = files,
            key = { it.path }
        ) { resource ->
            // 加载视频预览图
            val previews = if (resource.isVideo) {
                videoPreviews[resource.path] ?: emptyList()
            } else {
                emptyList()
            }
            
            FileItem(
                resource = resource,
                onClick = { onFileClick(resource) },
                previewImages = previews,
                onPreviewClick = onPreviewClick,
                onLoadPreviews = { onLoadPreviews(resource.path) },
                isFavorite = favoriteStates[resource.path] ?: false,
                onFavoriteClick = { onToggleFavorite(resource) },
                isMultiSelectMode = isMultiSelectMode,
                isSelected = selectedFiles.contains(resource)
            )
        }
    }
}

/**
 * 加载状态
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 未连接状态
 */
@Composable
private fun NotConnectedState(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未连接到服务器",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请先在设置中选择并连接服务器",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

/**
 * 错误状态
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "加载失败",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

/**
 * 空目录状态
 */
@Composable
private fun EmptyDirectoryState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "空目录",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "当前目录没有文件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * 处理文件点击事件
 */
private fun handleFileClick(
    resource: WebDAVResource,
    viewModel: FileBrowserViewModel,
    onVideoClick: (String) -> Unit,
    onImageClick: (String) -> Unit
) {
    // 日志打印资源URL
    Log.d("FileBrowserScreen", "File clicked: ${resource.path}")

    when {
        resource.isDirectory -> {
            // 进入目录
            viewModel.navigateTo(resource.path)
        }
        resource.isVideo -> {
            // 播放视频
            val streamUrl = viewModel.getStreamUrl(resource.path)
            Log.d("FileBrowserScreen", "Video clicked: ${streamUrl}")
            onVideoClick(streamUrl)
        }
        resource.isImage -> {
            // 查看图片
            val streamUrl = viewModel.getStreamUrl(resource.path)
            Log.d("FileBrowserScreen", "Image clicked: ${streamUrl}")
            onImageClick(streamUrl)
        }
        else -> {
            // 其他类型文件，暂不处理
        }
    }
}
