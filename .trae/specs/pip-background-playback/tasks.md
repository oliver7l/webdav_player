# Tasks

- [x] Task 1: 添加画中画（PiP）模式支持
  - [x] SubTask 1.1: 更新 AndroidManifest.xml 添加 PiP 权限和属性
  - [x] SubTask 1.2: 在 VideoPlayerScreen.kt 中添加 PiP 模式处理逻辑
  - [x] SubTask 1.3: 在 VideoPlayerViewModel.kt 中添加 PiP 状态管理

- [x] Task 2: 添加后台播放服务
  - [x] SubTask 2.1: 创建 PlaybackService.kt 前台服务
  - [x] SubTask 2.2: 创建 MediaNotificationManager.kt 媒体通知管理器
  - [x] SubTask 2.3: 更新 AndroidManifest.xml 添加前台服务权限和声明

- [x] Task 3: 集成 Media3 库的媒体会话支持
  - [x] SubTask 3.1: 添加 Media3 media session 依赖
  - [x] SubTask 3.2: 在 VideoPlayerViewModel 中集成 MediaSession
  - [x] SubTask 3.3: 连接 MediaSession 与通知控制

- [x] Task 4: 添加播放设置选项
  - [x] SubTask 4.1: 在 PlayerSettings 中添加 PiP 和后台播放开关
  - [x] SubTask 4.2: 在 PlayerSettingsDataStore 中添加持久化逻辑
  - [x] SubTask 4.3: 在设置界面添加 PiP 和后台播放开关 UI

- [x] Task 5: 测试和验证
  - [x] SubTask 5.1: 测试画中画模式进入和退出
  - [x] SubTask 5.2: 测试后台播放和通知控制
  - [x] SubTask 5.3: 测试设置选项功能

# Task Dependencies
- [Task 2] depends on [Task 3]
- [Task 4] can run in parallel with [Task 1], [Task 2], [Task 3]
- [Task 5] depends on [Task 1], [Task 2], [Task 3], [Task 4]
