# 后台保活优化 Spec

## Why
用户反馈应用锁屏后容易被系统杀死，无法保持后台运行。Android 系统为了省电会 aggressively 杀死后台应用，需要申请相关权限并优化实现来保持后台播放稳定运行。

## What Changes
- 添加忽略电池优化权限请求
- 添加 WakeLock 保持 CPU 运行
- 添加后台运行权限引导（针对国产厂商）
- 优化前台服务实现
- 添加设置界面的权限管理入口

## Impact
- Affected specs: 后台播放功能
- Affected code:
  - `AndroidManifest.xml` - 添加权限声明
  - `PlaybackService.kt` - 添加 WakeLock 和优化保活
  - `VideoPlayerViewModel.kt` - 添加权限请求逻辑
  - `SettingsScreen.kt` - 添加权限管理入口
  - 新增 `BatteryOptimizationHelper.kt` - 电池优化权限辅助类

## ADDED Requirements

### Requirement: 忽略电池优化
系统应请求用户将应用加入电池优化白名单，防止系统杀死后台服务。

#### Scenario: 检查电池优化状态
- **WHEN** 用户进入设置页面
- **THEN** 系统应显示当前电池优化状态

#### Scenario: 请求忽略电池优化
- **WHEN** 用户点击"允许后台运行"选项
- **THEN** 系统应弹出电池优化设置页面，引导用户将应用加入白名单

#### Scenario: 后台播放时检查权限
- **WHEN** 用户启用后台播放且应用未加入电池优化白名单
- **THEN** 系统应提示用户申请忽略电池优化权限

### Requirement: WakeLock 保活
系统应在播放时持有 WakeLock，防止 CPU 休眠。

#### Scenario: 播放时获取 WakeLock
- **WHEN** 视频开始播放
- **THEN** 系统应获取 Partial WakeLock

#### Scenario: 暂停或停止时释放 WakeLock
- **WHEN** 视频暂停或停止
- **THEN** 系统应释放 WakeLock

### Requirement: 国产厂商权限引导
系统应针对国产厂商（小米、华为、OPPO、vivo 等）提供后台运行权限引导。

#### Scenario: 检测厂商并提供引导
- **WHEN** 用户使用国产厂商设备
- **THEN** 系统应提供对应厂商的后台运行设置引导

### Requirement: 前台服务优化
系统应优化前台服务实现，确保服务不被杀死。

#### Scenario: 提升服务优先级
- **WHEN** 后台播放启用
- **THEN** 前台服务应以最高优先级运行

#### Scenario: 服务重启机制
- **WHEN** 服务被系统杀死
- **THEN** 系统应尝试重启服务

## MODIFIED Requirements

### Requirement: 设置界面
设置界面应添加后台运行权限管理入口。

#### Scenario: 显示权限状态
- **WHEN** 用户查看设置
- **THEN** 应显示电池优化权限状态和后台运行权限状态

## 技术实现

### 1. AndroidManifest.xml 添加权限

```xml
<!-- 电池优化 -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
<!-- WakeLock -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<!-- 开机启动（用于服务重启） -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### 2. BatteryOptimizationHelper.kt

```kotlin
object BatteryOptimizationHelper {
    fun isIgnoringBatteryOptimizations(context: Context): Boolean
    
    fun requestIgnoreBatteryOptimizations(activity: Activity)
    
    fun openBatterySettings(context: Context)
    
    fun getManufacturer(): ManufacturerType
}

enum class ManufacturerType {
    XIAOMI, HUAWEI, OPPO, VIVO, SAMSUNG, OTHER
}
```

### 3. PlaybackService.kt 添加 WakeLock

```kotlin
private var wakeLock: PowerManager.WakeLock? = null

private fun acquireWakeLock() {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK,
        "WebDAVViewer:PlaybackWakeLock"
    )
    wakeLock?.acquire(10 * 60 * 1000L) // 10分钟超时
}

private fun releaseWakeLock() {
    wakeLock?.let {
        if (it.isHeld) {
            it.release()
        }
    }
    wakeLock = null
}
```

### 4. 设置界面添加权限管理

```kotlin
// 后台运行权限设置项
SettingsItem(
    title = "后台运行权限",
    subtitle = if (isIgnoringBatteryOptimizations) "已授权" else "点击授权",
    onClick = { requestBatteryOptimization() }
)
```

### 5. 引导用户到厂商设置

针对不同厂商提供特定的设置页面入口：
- 小米：安全中心 -> 应用管理 -> 权限
- 华为：手机管家 -> 应用启动管理
- OPPO：手机管家 -> 应用管理 -> 自启动
- vivo：i管家 -> 应用管理 -> 自启动

## 修改文件清单

1. `AndroidManifest.xml` - 添加权限声明
2. `PlaybackService.kt` - 添加 WakeLock 和优化保活
3. `BatteryOptimizationHelper.kt` - 新建，电池优化辅助类
4. `SettingsScreen.kt` - 添加权限管理入口
5. `PlayerSettings.kt` - 添加权限状态字段
6. `VideoPlayerScreen.kt` - 添加权限检查提示
