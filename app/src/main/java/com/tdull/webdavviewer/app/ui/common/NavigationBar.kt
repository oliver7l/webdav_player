package com.tdull.webdavviewer.app.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tdull.webdavviewer.app.navigation.Screen

/**
 * 应用导航栏
 * 提供快速导航到主要功能页面的入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationBar(
    navController: NavHostController,
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isNarrowScreen = configuration.screenWidthDp < 360
    
    // 更多菜单状态
    var showMoreMenu by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            modifier = modifier,
            containerColor = Color.White
        ) {
            NavigationBarItem(
                selected = currentRoute == Screen.Settings.route,
                onClick = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Settings.route) {
                            inclusive = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "设置"
                    )
                },
                label = { Text("设置", fontSize = 12.sp) }
            )
            
            NavigationBarItem(
                selected = currentRoute == Screen.Favorites.route,
                onClick = {
                    navController.navigate(Screen.Favorites.route) {
                        popUpTo(Screen.Settings.route) {
                            inclusive = false
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "收藏"
                    )
                },
                label = { Text("收藏", fontSize = 12.sp) }
            )
            
            NavigationBarItem(
                selected = currentRoute == Screen.PlaylistManager.route,
                onClick = {
                    navController.navigate(Screen.PlaylistManager.route) {
                        popUpTo(Screen.Settings.route) {
                            inclusive = false
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "播放列表"
                    )
                },
                label = { Text(if (isNarrowScreen) "列表" else "播放列表", fontSize = 12.sp) }
            )
            
            NavigationBarItem(
                selected = currentRoute == Screen.QuickAccess.route,
                onClick = {
                    navController.navigate(Screen.QuickAccess.route) {
                        popUpTo(Screen.Settings.route) {
                            inclusive = false
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "快速访问"
                    )
                },
                label = { Text(if (isNarrowScreen) "快速" else "快速访问", fontSize = 12.sp) }
            )
            
            NavigationBarItem(
                selected = showMoreMenu,
                onClick = {
                    showMoreMenu = !showMoreMenu
                },
                icon = {
                    Box {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多"
                        )
                        // 更多菜单下拉列表
                        if (showMoreMenu) {
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                                offset = androidx.compose.ui.unit.DpOffset(0.dp, (-48).dp)
                            ) {
                                // 标签管理
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Tag,
                                                contentDescription = "标签管理",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("标签管理")
                                        }
                                    },
                                    onClick = {
                                        navController.navigate(Screen.TagManager.route) {
                                            popUpTo(Screen.Settings.route) {
                                                inclusive = false
                                            }
                                        }
                                        showMoreMenu = false
                                    }
                                )
                                
                                // 播放历史
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "播放历史",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("播放历史")
                                        }
                                    },
                                    onClick = {
                                        navController.navigate(Screen.PlayHistory.route) {
                                            popUpTo(Screen.Settings.route) {
                                                inclusive = false
                                            }
                                        }
                                        showMoreMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                label = { Text("更多", fontSize = 12.sp) }
            )
        }
        

    }
}
