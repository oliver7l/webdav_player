# 播放进度保存与恢复修复任务清单

## 任务列表

### 任务1：修复 PlayHistoryDataStore 读取方法
- [ ] 修改 `getPlayHistoryItemByUrl()` 方法，使用只读方式读取数据
- [ ] 移除不必要的 `edit` 操作

### 任务2：优化 VideoPlayerViewModel 进度保存逻辑
- [ ] 添加 `currentVideoTitle` 变量维护当前视频标题
- [ ] 添加 `lastSavedPosition` 变量跟踪上次保存位置
- [ ] 添加 `SAVE_INTERVAL_MS` 常量定义保存间隔
- [ ] 修改 `startProgressUpdate()` 添加定期保存逻辑
- [ ] 添加 `saveCurrentProgress()` 公开方法供外部调用
- [ ] 修改 `recordPlayHistory()` 使用保存的视频标题

### 任务3：优化进度恢复逻辑
- [ ] 创建 `restorePlayProgress()` 方法
- [ ] 等待播放器准备好后再恢复进度
- [ ] 检查进度是否接近末尾（>95%），从头开始播放
- [ ] 修改 `initializePlayer()` 使用新的恢复逻辑

### 任务4：添加生命周期感知保存
- [ ] 在 VideoPlayerScreen 中添加 LifecycleEventObserver
- [ ] 在 ON_PAUSE 和 ON_STOP 事件时保存进度
- [ ] 确保在 onDispose 时正确清理

### 任务5：测试验证
- [ ] 验证按 Home 键后进度保存和恢复
- [ ] 验证切换应用后进度保存和恢复
- [ ] 验证播放到末尾后从头开始
- [ ] 验证 PiP 模式下进度保存
