package com.tdull.webdavviewer.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.tdull.webdavviewer.app.ui.browser.FileBrowserScreen
import com.tdull.webdavviewer.app.ui.player.VideoPlayerScreen
import com.tdull.webdavviewer.app.ui.playlist.PlaylistManagerScreen
import com.tdull.webdavviewer.app.ui.quickaccess.QuickAccessScreen
import com.tdull.webdavviewer.app.ui.playhistory.PlayHistoryScreen
import com.tdull.webdavviewer.app.ui.settings.SettingsScreen
import com.tdull.webdavviewer.app.ui.settings.SyncSettingsScreen
import com.tdull.webdavviewer.app.ui.tag.TagManagerScreen
import com.tdull.webdavviewer.app.ui.viewer.ImageViewerScreen
import com.tdull.webdavviewer.app.ui.favorites.FavoritesScreen
import java.net.URLDecoder

/**
 * 应用导航图
 * 定义所有页面之间的导航关系
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Settings.route,
        modifier = modifier
    ) {
        // 设置页面
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }

        // 文件浏览器页面
        composable(
            route = Screen.Browser.route,
            arguments = listOf(
                navArgument("serverId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("path") {
                    type = NavType.StringType
                    defaultValue = "/"
                }
            )
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getString("serverId")
            val path = backStackEntry.arguments?.getString("path")
            FileBrowserScreen(
                navController = navController,
                serverId = serverId,
                path = path,
                onVideoClick = { url ->
                    navController.navigate(Screen.VideoPlayer.createRoute(url))
                },
                onImageClick = { url ->
                    navController.navigate(Screen.ImageViewer.createRoute(url))
                }
            )
        }

        // 视频播放器页面
        composable(
            route = Screen.VideoPlayer.route,
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("playlistId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("playlistIndex") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
            val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
            val playlistIndex = backStackEntry.arguments?.getInt("playlistIndex") ?: -1

            // 解码URL参数
            val videoUrl = try {
                URLDecoder.decode(encodedUrl, "UTF-8")
            } catch (e: Exception) {
                Uri.decode(encodedUrl)
            }
            val videoTitle = try {
                URLDecoder.decode(encodedTitle, "UTF-8")
            } catch (e: Exception) {
                Uri.decode(encodedTitle)
            }

            VideoPlayerScreen(
                videoUrl = videoUrl,
                videoTitle = videoTitle,
                playlistId = if (playlistId.isNotEmpty()) playlistId else null,
                playlistIndex = if (playlistIndex >= 0) playlistIndex else null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 图片查看器页面
        composable(
            route = Screen.ImageViewer.route,
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("url") ?: ""
            val imageTitle = backStackEntry.arguments?.getString("title") ?: ""

            ImageViewerScreen(
                imageUrl = imageUrl,
                imageTitle = imageTitle,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 收藏列表页面
        composable(route = Screen.Favorites.route) {
            FavoritesScreen(
                navController = navController,
                onVideoClick = { url ->
                    navController.navigate(Screen.VideoPlayer.createRoute(url))
                }
            )
        }

        // 播放列表管理页面
        composable(route = Screen.PlaylistManager.route) {
            PlaylistManagerScreen(
                navController = navController,
                onPlaylistItemClick = { videoUrl, videoTitle, playlistId, playlistIndex ->
                    navController.navigate(Screen.VideoPlayer.createRoute(videoUrl, videoTitle, playlistId, playlistIndex))
                }
            )
        }

        // 标签管理页面
        composable(route = Screen.TagManager.route) {
            TagManagerScreen(
                navController = navController,
                onVideoClick = { videoUrl, videoTitle ->
                    navController.navigate(Screen.VideoPlayer.createRoute(videoUrl, videoTitle))
                }
            )
        }
        
        // 快速访问页面
        composable(route = Screen.QuickAccess.route) {
            QuickAccessScreen(
                navController = navController
            )
        }
        
        // 云同步设置页面
        composable(route = Screen.SyncSettings.route) {
            val syncViewModel = hiltViewModel<com.tdull.webdavviewer.app.viewmodel.SyncViewModel>()
            SyncSettingsScreen(
                syncViewModel = syncViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 播放历史页面
        composable(route = Screen.PlayHistory.route) {
            PlayHistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPlayVideo = { url, title ->
                    navController.navigate(Screen.VideoPlayer.createRoute(url, title))
                }
            )
        }
    }
}
