package com.tdull.webdavviewer.app.ui.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.tdull.webdavviewer.app.data.model.Playlist
import com.tdull.webdavviewer.app.navigation.Screen
import com.tdull.webdavviewer.app.ui.common.MainScreenContainer
import com.tdull.webdavviewer.app.viewmodel.PlaylistViewModel

/**
 * 播放列表管理页面
 * 用于查看和管理所有播放列表及播放列表内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManagerScreen(
    navController: NavHostController,
    onPlaylistItemClick: (String, String) -> Unit, // (videoUrl, videoTitle)
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
    
    // 状态
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showRenamePlaylistDialog by remember { mutableStateOf(false) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var renamePlaylistName by remember { mutableStateOf("") }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var showDeletePlaylistItemDialog by remember { mutableStateOf(false) }
    var playlistItemToDelete by remember { mutableStateOf<String?>(null) }
    
    MainScreenContainer(
        navController = navController,
        currentRoute = Screen.PlaylistManager.route,
        title = if (selectedPlaylist != null) "播放列表内容" else "播放列表管理"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (selectedPlaylist == null) {
                // 播放列表列表
                if (playlists.isEmpty()) {
                    EmptyPlaylistsState(onCreatePlaylist = { showCreatePlaylistDialog = true })
                } else {
                    PlaylistList(
                        playlists = playlists,
                        onPlaylistClick = { playlist -> viewModel.selectPlaylist(playlist) },
                        onPlaylistEdit = { playlist ->
                            playlistToRename = playlist
                            renamePlaylistName = playlist.name
                            showRenamePlaylistDialog = true
                        },
                        onPlaylistDelete = { playlist ->
                            playlistToDelete = playlist
                            showDeletePlaylistDialog = true
                        }
                    )
                }
            } else {
                // 播放列表内容
                PlaylistContent(
                    playlist = selectedPlaylist!!,
                    onPlaylistItemClick = onPlaylistItemClick,
                    onPlaylistItemDelete = { itemId ->
                        playlistItemToDelete = itemId
                        showDeletePlaylistItemDialog = true
                    }
                )
            }
        }
    }
    
    // 创建播放列表对话框
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
                            viewModel.createPlaylist(newPlaylistName)
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
    
    // 重命名播放列表对话框
    if (showRenamePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showRenamePlaylistDialog = false },
            title = { Text("重命名播放列表") },
            text = {
                Column {
                    Text("请输入新的播放列表名称")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = renamePlaylistName,
                        onValueChange = { renamePlaylistName = it },
                        label = { Text("播放列表名称") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renamePlaylistName.isNotBlank() && playlistToRename != null) {
                            viewModel.renamePlaylist(playlistToRename!!.id, renamePlaylistName)
                            showRenamePlaylistDialog = false
                            renamePlaylistName = ""
                            playlistToRename = null
                        }
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenamePlaylistDialog = false
                    renamePlaylistName = ""
                    playlistToRename = null
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 删除播放列表对话框
    if (showDeletePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistDialog = false },
            title = { Text("删除播放列表") },
            text = {
                Text("确定要删除播放列表 ${playlistToDelete?.name} 吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistToDelete != null) {
                            viewModel.deletePlaylist(playlistToDelete!!.id)
                            showDeletePlaylistDialog = false
                            playlistToDelete = null
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeletePlaylistDialog = false
                    playlistToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
    
    // 删除播放列表项对话框
    if (showDeletePlaylistItemDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistItemDialog = false },
            title = { Text("删除播放列表项") },
            text = {
                Text("确定要从播放列表中删除此项目吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistItemToDelete != null && selectedPlaylist != null) {
                            viewModel.removeItemFromPlaylist(selectedPlaylist!!.id, playlistItemToDelete!!)
                            showDeletePlaylistItemDialog = false
                            playlistItemToDelete = null
                        }
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeletePlaylistItemDialog = false
                    playlistItemToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 空播放列表状态
 */
@Composable
private fun EmptyPlaylistsState(onCreatePlaylist: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "暂无播放列表",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "点击右上角按钮创建您的第一个播放列表",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onCreatePlaylist) {
                Icon(Icons.Default.Add, contentDescription = "创建播放列表")
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建播放列表")
            }
        }
    }
}

/**
 * 播放列表列表
 */
@Composable
private fun PlaylistList(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaylistEdit: (Playlist) -> Unit,
    onPlaylistDelete: (Playlist) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(playlists) {
            PlaylistCard(
                playlist = it,
                onClick = { onPlaylistClick(it) },
                onEditClick = { onPlaylistEdit(it) },
                onDeleteClick = { onPlaylistDelete(it) }
            )
        }
    }
}

/**
 * 播放列表卡片
 */
@Composable
private fun PlaylistCard(
    playlist: Playlist,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${playlist.items.size} 个项目",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
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
 * 播放列表内容
 */
@Composable
private fun PlaylistContent(
    playlist: Playlist,
    onPlaylistItemClick: (String, String) -> Unit,
    onPlaylistItemDelete: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (playlist.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "播放列表为空",
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
                items(playlist.items) {
                    PlaylistItemCard(
                        item = it,
                        onClick = { onPlaylistItemClick(it.videoUrl, it.videoTitle) },
                        onDeleteClick = { onPlaylistItemDelete(it.id) }
                    )
                }
            }
        }
    }
}

/**
 * 播放列表项卡片
 */
@Composable
private fun PlaylistItemCard(
    item: com.tdull.webdavviewer.app.data.model.PlaylistItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.videoTitle,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}
