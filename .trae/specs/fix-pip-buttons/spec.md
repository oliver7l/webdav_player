# 修复画中画按钮不生效问题 Spec

## Why
画中画模式的播放/暂停按钮点击不生效，用户无法在画中画窗口中控制播放。

## What Changes
- 将广播接收器从 Composable 移动到 MainActivity 中注册
- 确保广播接收器在 PiP 模式下仍然可以接收广播
- 使用 LocalBroadcastManager 或显式 Intent 确保广播能被正确接收

## Impact
- Affected specs: 画中画功能
- Affected code:
  - `MainActivity.kt` - 添加广播接收器注册
  - `VideoPlayerScreen.kt` - 移除广播接收器注册，改用 ViewModel 回调

## ADDED Requirements

### Requirement: 画中画按钮响应
画中画窗口中的控制按钮应正确响应点击事件。

#### Scenario: 用户点击播放/暂停按钮
- **WHEN** 用户在画中画窗口中点击播放/暂停按钮
- **THEN** 视频应切换播放/暂停状态

#### Scenario: 用户点击下一个按钮
- **WHEN** 用户在画中画窗口中点击下一个按钮
- **THEN** 应播放下一个视频

## MODIFIED Requirements

### Requirement: 广播接收器注册
广播接收器应在 Activity 生命周期中注册，而不是在 Composable 中。

#### Scenario: Activity 创建时注册广播接收器
- **WHEN** Activity 创建时
- **THEN** 注册 PiP 控制广播接收器

#### Scenario: Activity 销毁时注销广播接收器
- **WHEN** Activity 销毁时
- **THEN** 注销 PiP 控制广播接收器
