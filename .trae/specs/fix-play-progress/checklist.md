# 播放进度保存与恢复修复检查清单

## 代码修改检查

### PlayHistoryDataStore.kt
- [ ] `getPlayHistoryItemByUrl()` 不再使用 `edit` 进行只读操作
- [ ] 使用 `data.map().first()` 进行只读访问
- [ ] 代码编译无错误

### VideoPlayerViewModel.kt
- [ ] 添加了 `currentVideoTitle` 变量
- [ ] 添加了 `lastSavedPosition` 变量
- [ ] 添加了 `SAVE_INTERVAL_MS` 常量
- [ ] `startProgressUpdate()` 包含定期保存逻辑
- [ ] `saveCurrentProgress()` 方法已添加
- [ ] `recordPlayHistory()` 使用正确的视频标题
- [ ] `restorePlayProgress()` 方法已添加
- [ ] 进度恢复检查是否接近末尾
- [ ] `initializePlayer()` 调用新的恢复逻辑
- [ ] 代码编译无错误

### VideoPlayerScreen.kt
- [ ] 添加了 LifecycleEventObserver
- [ ] ON_PAUSE 时保存进度
- [ ] ON_STOP 时保存进度
- [ ] onDispose 正确清理观察者
- [ ] 代码编译无错误

## 功能测试检查

### 基本功能
- [ ] 播放视频正常
- [ ] 进度条显示正常
- [ ] 播放历史列表显示正常

### 进度保存测试
- [ ] 播放一段时间后按 Home 键，重新打开进度正确
- [ ] 播放一段时间后切换应用，重新打开进度正确
- [ ] 播放一段时间后锁屏，重新打开进度正确
- [ ] PiP 模式下退出，进度正确保存

### 进度恢复测试
- [ ] 播放到 50% 退出，重新打开从 50% 继续
- [ ] 播放到 95% 以上退出，重新打开从头开始
- [ ] 新视频从头开始播放

### 边界情况测试
- [ ] 网络断开后重连，进度保持
- [ ] 应用被系统杀死后重启，进度尽可能恢复
- [ ] 后台播放时进度保存正常

## 性能检查

- [ ] 定期保存不会导致卡顿
- [ ] DataStore 操作不会阻塞主线程
- [ ] 内存使用正常

## 代码质量检查

- [ ] 无 lint 警告
- [ ] 无类型错误
- [ ] 代码风格一致
