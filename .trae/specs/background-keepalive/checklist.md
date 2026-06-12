# 后台保活优化检查清单

## 权限声明检查
- [x] AndroidManifest.xml 包含 `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` 权限
- [x] AndroidManifest.xml 包含 `WAKE_LOCK` 权限
- [x] AndroidManifest.xml 包含 `RECEIVE_BOOT_COMPLETED` 权限

## BatteryOptimizationHelper 检查
- [x] `isIgnoringBatteryOptimizations()` 方法正确实现
- [x] `requestIgnoreBatteryOptimizations()` 方法正确实现
- [x] `getManufacturer()` 方法正确识别厂商
- [x] 小米设备设置页面跳转正确
- [x] 华为设备设置页面跳转正确
- [x] OPPO 设备设置页面跳转正确
- [x] vivo 设备设置页面跳转正确

## PlaybackService WakeLock 检查
- [x] WakeLock 在播放时正确获取
- [x] WakeLock 在暂停时正确释放
- [x] WakeLock 在停止时正确释放
- [x] WakeLock 在服务销毁时正确释放
- [x] 服务使用 START_STICKY 返回值

## 设置界面检查
- [x] 后台运行权限设置项显示正确
- [x] 权限状态正确显示（已授权/未授权）
- [x] 点击设置项能跳转到电池优化设置
- [x] 厂商特定提示正确显示

## 播放界面检查
- [x] 后台播放启用时检查电池优化状态
- [x] 未授权时显示提示
- [x] 提示可跳转到设置

## 功能测试检查
- [ ] 锁屏后应用不被杀死
- [ ] 后台播放持续运行
- [ ] 通知控制正常工作
- [ ] 长时间后台播放稳定
- [ ] 低电量时仍能保持运行

## 代码质量检查
- [x] 无 lint 警告（只有弃用警告）
- [x] 无类型错误
- [x] WakeLock 正确释放，无泄漏
