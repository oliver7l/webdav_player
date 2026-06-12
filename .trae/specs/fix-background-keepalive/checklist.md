# 后台保活问题修复检查清单

## WakeLock 修复检查
- [x] WakeLock 使用无限期获取
- [x] 暂停时正确释放 WakeLock
- [x] 停止时正确释放 WakeLock
- [x] 服务销毁时正确释放 WakeLock

## 关闭提示状态检查
- [x] PlayerSettingsDataStore 包含 BATTERY_OPTIMIZATION_HINT_DISMISSED 键
- [x] setBatteryOptimizationHintDismissed() 方法正确实现
- [x] isBatteryOptimizationHintDismissed() Flow 方法正确实现
- [x] PlayerSettingsRepository 包含相应接口

## 提示横幅组件检查
- [x] BatteryOptimizationBanner 包含关闭按钮
- [x] 点击关闭按钮后横幅隐藏
- [x] 关闭后下次不再显示
- [x] 关闭后台播放后重新启用时提示重新显示

## 代码质量检查
- [x] 无编译错误
- [x] 无 lint 警告（只有弃用警告）
- [x] WakeLock 正确释放，无泄漏
