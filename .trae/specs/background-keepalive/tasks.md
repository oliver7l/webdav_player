# 后台保活优化任务清单

## 任务列表

### 任务1：添加权限声明
- [x] 在 AndroidManifest.xml 添加 `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` 权限
- [x] 在 AndroidManifest.xml 添加 `WAKE_LOCK` 权限
- [x] 在 AndroidManifest.xml 添加 `RECEIVE_BOOT_COMPLETED` 权限

### 任务2：创建电池优化辅助类
- [x] 创建 `BatteryOptimizationHelper.kt`
- [x] 实现 `isIgnoringBatteryOptimizations()` 检查是否已忽略电池优化
- [x] 实现 `requestIgnoreBatteryOptimizations()` 请求忽略电池优化
- [x] 实现 `getManufacturer()` 获取设备厂商
- [x] 实现各厂商后台设置页面跳转方法

### 任务3：优化 PlaybackService 添加 WakeLock
- [x] 在 PlaybackService 中添加 WakeLock 变量
- [x] 实现 `acquireWakeLock()` 方法
- [x] 实现 `releaseWakeLock()` 方法
- [x] 在播放时获取 WakeLock
- [x] 在暂停/停止时释放 WakeLock
- [x] 添加服务重启机制（START_STICKY）

### 任务4：添加设置界面权限管理入口
- [x] 在 PlayerSettings 中添加电池优化状态显示
- [x] 在 SettingsScreen 中添加"后台运行权限"设置项
- [x] 显示当前电池优化状态
- [x] 点击后请求忽略电池优化权限
- [x] 添加厂商特定设置引导提示

### 任务5：添加播放时权限检查提示
- [x] 在 VideoPlayerScreen 中检查电池优化状态
- [x] 当启用后台播放但未授权时显示提示
- [x] 提供跳转到设置的快捷入口

## 任务依赖
- [任务3] 依赖 [任务1] - WakeLock 需要先声明权限
- [任务4] 依赖 [任务2] - 设置界面需要使用辅助类
- [任务5] 依赖 [任务2] - 权限检查需要使用辅助类
