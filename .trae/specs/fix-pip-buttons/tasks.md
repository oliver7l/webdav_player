# Tasks

- [x] Task 1: 修复画中画按钮不生效问题
  - [x] SubTask 1.1: 在 MainActivity 中注册 PiP 控制广播接收器
  - [x] SubTask 1.2: 从 VideoPlayerScreen.kt 移除广播接收器注册
  - [x] SubTask 1.3: 确保广播接收器能正确调用 ViewModel 方法

- [x] Task 2: 验证修复
  - [x] SubTask 2.1: 测试画中画播放/暂停按钮
  - [x] SubTask 2.2: 测试画中画上一个/下一个按钮

# Task Dependencies
- [Task 2] depends on [Task 1]
