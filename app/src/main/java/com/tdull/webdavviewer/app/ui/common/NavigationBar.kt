package com.tdull.webdavviewer.app.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
            selected = currentRoute == Screen.TagManager.route,
            onClick = {
                navController.navigate(Screen.TagManager.route) {
                    popUpTo(Screen.Settings.route) {
                        inclusive = false
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = "标签管理"
                )
            },
            label = { Text("标签", fontSize = 12.sp) }
        )
        
        NavigationBarItem(
            selected = currentRoute == Screen.PlayHistory.route,
            onClick = {
                navController.navigate(Screen.PlayHistory.route) {
                    popUpTo(Screen.Settings.route) {
                        inclusive = false
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "播放历史"
                )
            },
            label = { Text("历史", fontSize = 12.sp) }
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
    }
}
