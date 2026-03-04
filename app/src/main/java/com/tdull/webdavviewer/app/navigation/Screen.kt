package com.tdull.webdavviewer.app.navigation

/**
 * 应用导航路由定义
 */
sealed class Screen(val route: String) {
    /**
     * 设置页面 - 服务器配置管理
     */
    object Settings : Screen("settings")

    /**
     * 文件浏览器页面
     * @param serverId 服务器ID
     * @param path 初始路径
     */
    object Browser : Screen("browser?serverId={serverId}&path={path}") {
        fun createRoute(serverId: String, path: String = "/") = "browser?serverId=$serverId&path=$path"
    }

    /**
     * 视频播放器页面
     * @param url 视频URL（需编码）
     * @param title 视频标题（需编码）
     * @param playlistId 播放列表ID（可选）
     * @param playlistIndex 播放列表索引（可选）
     */
    object VideoPlayer : Screen("video?url={url}&title={title}&playlistId={playlistId}&playlistIndex={playlistIndex}") {
        fun createRoute(url: String, title: String = "", playlistId: String? = null, playlistIndex: Int? = null): String {
            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val playlistIdParam = playlistId?.let { "&playlistId=$it" } ?: "&playlistId="
            val playlistIndexParam = playlistIndex?.let { "&playlistIndex=$it" } ?: "&playlistIndex=-1"
            return "video?url=$encodedUrl&title=$encodedTitle$playlistIdParam$playlistIndexParam"
        }
    }

    /**
     * 图片查看器页面
     * @param url 图片URL（需编码）
     * @param title 图片标题（需编码）
     */
    object ImageViewer : Screen("image?url={url}&title={title}") {
        fun createRoute(url: String, title: String = ""): String {
            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            return "image?url=$encodedUrl&title=$encodedTitle"
        }
    }

    /**
     * 收藏列表页面
     */
    object Favorites : Screen("favorites")

    /**
     * 播放列表管理页面
     */
    object PlaylistManager : Screen("playlist_manager")

    /**
     * 标签管理页面
     */
    object TagManager : Screen("tag_manager")
    
    /**
     * 快速访问页面
     */
    object QuickAccess : Screen("quick_access")
    
    /**
     * 云同步设置页面
     */
    object SyncSettings : Screen("sync_settings")
    
    /**
     * 播放历史页面
     */
    object PlayHistory : Screen("play_history")
}
