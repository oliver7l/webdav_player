# 后台保活问题修复 Spec

## Why
用户反馈后台保活功能"有点问题"，需要修复。经过代码审查发现以下问题：
1. 电池优化提示横幅没有关闭按钮，用户无法隐藏
2. WakeLock 超时时间太短（10分钟），长时间播放会失效
3. 没有保存用户是否已关闭提示的状态

## What Changes
- 为电池优化提示横幅添加关闭按钮
- 延长 WakeLock 超时时间或使用无限期（在暂停/停止时释放）
- 添加用户关闭提示的状态保存

## Impact
- Affected specs: 后台保活功能
- Affected code:
  - `VideoPlayerScreen.kt` - 修改提示横幅组件
  - `PlaybackService.kt` - 修改 WakeLock 超时
  - `PlayerSettingsDataStore.kt` - 添加关闭提示状态保存

## ADDED Requirements

### Requirement: 电池优化提示可关闭
用户应能够关闭电池优化提示横幅，关闭后不再显示。

#### Scenario: 用户关闭提示
- **WHEN** 用户点击提示横幅的关闭按钮
- **THEN** 提示横幅应隐藏，且下次不再显示

#### Scenario: 用户重新启用后台播放
- **WHEN** 用户关闭后台播放后重新启用
- **THEN** 如果未授权电池优化，提示应重新显示

### Requirement: WakeLock 长时间有效
WakeLock 应在播放期间持续有效，不会因超时而失效。

#### Scenario: 长时间播放
- **WHEN** 用户播放视频超过10分钟
- **THEN** WakeLock 应仍然有效，CPU 不应休眠

## MODIFIED Requirements

### Requirement: WakeLock 管理
WakeLock 应使用无限期获取，在暂停/停止时主动释放。

#### Scenario: 获取 WakeLock
- **WHEN** 开始播放
- **THEN** 获取无限期 WakeLock

#### Scenario: 释放 WakeLock
- **WHEN** 暂停或停止播放
- **THEN** 主动释放 WakeLock

## 技术实现

### 1. 修改 WakeLock 超时

```kotlin
// PlaybackService.kt
private fun acquireWakeLock() {
    if (wakeLock == null) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WebDAVViewer:PlaybackWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
    }
    
    wakeLock?.let {
        if (!it.isHeld) {
            it.acquire() // 无限期，在暂停/停止时释放
        }
    }
}
```

### 2. 添加关闭提示状态

```kotlin
// PlayerSettingsDataStore.kt
private val BATTERY_OPTIMIZATION_HINT_DISMISSED = booleanPreferencesKey("battery_optimization_hint_dismissed")

suspend fun setBatteryOptimizationHintDismissed(dismissed: Boolean)
fun isBatteryOptimizationHintDismissed(): Flow<Boolean>
```

### 3. 修改提示横幅组件

```kotlin
@Composable
private fun BatteryOptimizationBanner(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(...) {
        Row(...) {
            // 图标和文字
            ...
            // 授权按钮
            TextButton(onClick = onRequestPermission) {
                Text("授权")
            }
            // 关闭按钮
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, "关闭")
            }
        }
    }
}
```

## 修改文件清单

1. `PlaybackService.kt` - 修改 WakeLock 为无限期
2. `PlayerSettingsDataStore.kt` - 添加关闭提示状态保存
3. `PlayerSettingsRepository.kt` - 添加关闭提示状态接口
4. `VideoPlayerViewModel.kt` - 添加关闭提示方法
5. `VideoPlayerScreen.kt` - 修改提示横幅组件
