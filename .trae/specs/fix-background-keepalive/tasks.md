# 后台保活问题修复任务清单

## 任务列表

### 任务1：修复 WakeLock 超时问题
- [x] 修改 PlaybackService.kt 中 acquireWakeLock() 方法
- [x] 移除 WakeLock 超时参数，使用无限期获取
- [x] 确保在暂停/停止时正确释放 WakeLock

### 任务2：添加关闭提示状态保存
- [x] 在 PlayerSettingsDataStore.kt 添加 BATTERY_OPTIMIZATION_HINT_DISMISSED 键
- [x] 实现 setBatteryOptimizationHintDismissed() 方法
- [x] 实现 isBatteryOptimizationHintDismissed() Flow 方法
- [x] 在 PlayerSettingsRepository.kt 添加相应接口

### 任务3：修改提示横幅组件
- [x] 为 BatteryOptimizationBanner 添加 onDismiss 回调参数
- [x] 添加关闭按钮（IconButton）
- [x] 在 VideoPlayerScreen 中读取关闭状态
- [x] 在 VideoPlayerViewModel 中添加关闭提示方法

## 任务依赖
- [任务3] 依赖 [任务2] - 需要先有状态保存
