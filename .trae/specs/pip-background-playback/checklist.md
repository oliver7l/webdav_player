# Checklist

## 画中画模式
- [x] AndroidManifest.xml 中 Activity 添加 `android:supportsPictureInPicture="true"` 属性
- [x] AndroidManifest.xml 中 Activity 添加 `android:configChanges` 处理 PiP 配置变更
- [x] VideoPlayerScreen.kt 中实现 `onUserLeaveHint` 回调进入 PiP 模式
- [x] VideoPlayerScreen.kt 中处理 PiP 模式下的 UI 隐藏/显示
- [x] VideoPlayerViewModel.kt 中添加 `isInPipMode` 状态

## 后台播放服务
- [x] 创建 PlaybackService.kt 继承 `MediaSessionService`
- [x] AndroidManifest.xml 添加 `FOREGROUND_SERVICE` 权限
- [x] AndroidManifest.xml 添加 `FOREGROUND_SERVICE_MEDIA_PLAYBACK` 权限
- [x] AndroidManifest.xml 声明 PlaybackService 服务
- [x] MediaNotificationManager.kt 创建媒体通知

## Media3 集成
- [x] build.gradle.kts 添加 `androidx.media3:media3-session` 依赖
- [x] VideoPlayerViewModel.kt 创建 `MediaSession` 并连接到 ExoPlayer
- [x] MediaSession 正确处理媒体按钮事件

## 设置选项
- [x] PlayerSettings.kt 添加 `enablePip` 和 `enableBackgroundPlayback` 字段
- [x] PlayerSettingsDataStore.kt 添加持久化逻辑
- [x] PlayerSettingsRepository.kt 添加相应的获取和保存方法
- [x] 设置界面添加 PiP 开关 UI
- [x] 设置界面添加后台播放开关 UI

## 功能验证
- [x] 播放视频时按 Home 键能进入 PiP 模式
- [x] 点击 PiP 窗口显示播放控制按钮
- [x] 点击 PiP 窗口能返回全屏播放
- [x] 锁屏时能继续播放音频
- [x] 通知栏显示媒体控制
- [x] 通知控制能播放/暂停/上一个/下一个
- [x] 设置开关能正确控制 PiP 和后台播放行为
