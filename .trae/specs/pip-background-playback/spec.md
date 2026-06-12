# 画中画和后台播放功能 Spec

## Why
用户希望在离开播放器界面时能够继续播放视频，包括画中画模式（小窗口悬浮播放）和后台播放模式（仅音频）。这可以提升用户体验，允许用户在使用其他应用时继续观看/收听视频内容。

## What Changes
- 添加画中画（Picture-in-Picture, PiP）模式支持
- 添加后台播放（Background Playback）支持
- 添加媒体通知控制（Media Notification Controls）
- 添加前台服务（Foreground Service）用于后台播放

## Impact
- Affected specs: 视频播放器功能
- Affected code:
  - `VideoPlayerScreen.kt` - 添加 PiP 模式 UI 处理
  - `VideoPlayerViewModel.kt` - 添加 PiP 和后台播放状态管理
  - `AndroidManifest.xml` - 添加权限和服务声明
  - 新增 `PlaybackService.kt` - 前台服务用于后台播放
  - 新增 `MediaNotificationManager.kt` - 媒体通知管理

## ADDED Requirements

### Requirement: 画中画模式
系统应支持画中画模式，允许用户在离开播放器界面时以小窗口继续观看视频。

#### Scenario: 用户按下 Home 键或切换应用
- **WHEN** 用户在播放视频时按下 Home 键或切换到其他应用
- **THEN** 系统应自动进入画中画模式，视频以小窗口继续播放

#### Scenario: 用户点击画中画窗口
- **WHEN** 用户点击画中画窗口
- **THEN** 系统应显示播放/暂停、上一个、下一个控制按钮

#### Scenario: 用户点击画中画窗口返回全屏
- **WHEN** 用户点击画中画窗口并选择返回全屏
- **THEN** 系统应返回播放器全屏界面

### Requirement: 后台播放模式
系统应支持后台播放模式，允许用户在锁屏或关闭屏幕时继续收听视频音频。

#### Scenario: 用户锁屏时继续播放
- **WHEN** 用户在播放视频时锁屏
- **THEN** 系统应继续播放音频，并显示媒体通知控制

#### Scenario: 用户通过通知控制播放
- **WHEN** 用户在通知栏看到媒体控制
- **THEN** 用户可以播放/暂停、切换上一个/下一个视频

### Requirement: 媒体通知控制
系统应在通知栏显示媒体控制，允许用户在后台控制播放。

#### Scenario: 显示媒体通知
- **WHEN** 视频正在播放或暂停
- **THEN** 系统应在通知栏显示媒体通知，包含视频标题、播放/暂停按钮、上一个/下一个按钮

### Requirement: 设置选项
系统应提供设置选项，允许用户控制画中画和后台播放行为。

#### Scenario: 用户禁用画中画
- **WHEN** 用户在设置中禁用画中画模式
- **THEN** 系统不应自动进入画中画模式

#### Scenario: 用户禁用后台播放
- **WHEN** 用户在设置中禁用后台播放
- **THEN** 系统在锁屏时应停止播放

## MODIFIED Requirements

### Requirement: 播放器生命周期管理
播放器应正确处理画中画和后台播放模式下的生命周期。

#### Scenario: 进入画中画模式
- **WHEN** 播放器进入画中画模式
- **THEN** 播放器应继续播放，UI 控件应隐藏

#### Scenario: 退出画中画模式
- **WHEN** 播放器退出画中画模式返回全屏
- **THEN** 播放器应恢复 UI 控件显示
