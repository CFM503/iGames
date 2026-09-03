# 🎮 iGames 儿童启蒙教育安卓游戏合集

[![Build & Release Android APK](https://github.com/USERNAME/iGames/actions/workflows/build-apk.yml/badge.svg)](https://github.com/USERNAME/iGames/actions/workflows/build-apk.yml)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)

**iGames** 是一款专为儿童早期启蒙教育设计的轻量化原生 Android 游戏合集平台。采用模块化架构，界面采用大圆角、高饱和柔和护眼色彩与灵动的触控动效，支持未来不断扩充各类认知与反应类小游戏。

项目深度集成了 **GitHub Actions**，无需在本地配置繁重的 Android SDK / Java 环境，直接在 GitHub 云端即可一键自动编译、打包并发布安装包（APK）。

---

## 🚦 首发游戏：红绿灯模拟器与互动小交警

为帮助低幼龄儿童认识交通信号灯、树立“红灯停、绿灯行、黄灯等一等”的安全规则意识，首款游戏打造了极具沉浸感的两大核心体验：

### 1. 拟真信号灯模拟器（过家家好搭档）
- **道具角色扮演**：支持**屏幕常亮（Keep Screen On）**，可将手机或平板立在玩具道路旁，作为过家家玩具车、小滑板车游戏的真实红绿灯。
- **4 种专业信号灯外观**：
  - 🟢 **经典三色信号灯**：拟真遮阳帽檐、蜂窝透镜玻璃反光与发光光晕。
  - 🚶 **行人过街信号灯**：静态红灯静止小人、动态绿灯踏步迈步小人（带行走步态动画）。
  - 🔢 **超大数字倒计时灯**：圆环进度条与大号数码管读秒。
  - ⏱️ **复合倒计时信号灯**：机动车三色灯与倒计时并列显示。
- **声音与语音系统**：
  - 内置童声语音口诀：“绿灯亮，向前行！”、“黄灯亮，等一等！”、“红灯亮，快停下！”
  - 拟真提示音、盲道过街提示蜂鸣音、倒计时滴答声。
- **手动小交警模式**：支持一键切换为手动模式，孩子可以化身小交警，轻触红黄绿按钮自主控制信号灯切换。

### 2. 规则互动小游戏（小司机过马路）
- 屏幕下方模拟道路斑马线与卡通小汽车。
- **玩法**：按住大按钮向前开，绿灯安全前进，黄灯小心减速；若在红灯时前进，小车会触发趣味警示音并退回起点。
- 成功穿过斑马线获得金灿灿的“小星星”奖励，培养规则意识与专注力。

### 3. 家长/教师自定义设置（带儿童防误触锁）
- **时长自由配置**：绿灯（3~60s）、黄灯（1~10s）、红灯（3~60s）秒级滑块调节。
- **绿灯末尾闪烁开关**：可配置绿灯最后3秒是否闪烁提醒。
- **儿童锁（Parental Gate）**：进入设置需要完成趣味简易加减法算术验证，防止幼儿误触改乱参数。
- **参数本地记忆**：采用 Jetpack DataStore，重启应用依然保留设置。

---

## 🏗️ 架构设计与合集扩展

```text
iGames/
├── .github/workflows/
│   └── build-apk.yml                # GitHub Actions 在线自动编译与发布流水线
├── app/
│   ├── build.gradle.kts             # 模块构建配置（Compose 依赖与自动化签名）
│   └── src/main/java/com/igames/kids/
│       ├── MainActivity.kt          # 入口 Activity 与导航宿主
│       ├── core/                    # 公共底层库
│       │   ├── audio/               # 统一音频与 TTS 管理 (SoundManager)
│       │   ├── theme/               # 儿童专属调色板与排版规范
│       │   ├── components/          # 儿童防误触锁、可爱按钮、统一标题栏
│       │   └── preferences/         # DataStore 设置持久化
│       ├── navigation/              # 游戏大厅与页面路由 (HubScreen)
│       └── games/
│           └── trafficlight/        # 红绿灯独立游戏模块
│               ├── model/           # 信号灯状态、配置、样式枚举
│               ├── engine/          # 信号灯计时与轮转控制器状态机
│               ├── ui/              # 仿真信号灯 Canvas 绘制与模拟器界面
│               ├── interactive/     # 过马路互动闯关游戏
│               └── settings/        # 家长时长与样式设置
```

后续扩展新游戏时，只需在 `games/` 下创建新的独立游戏目录，并在 `HubScreen.kt` 和 `Screen.kt` 中注册入口卡片即可，完全解耦。

---

## 🚀 GitHub Actions 在线云编译与发布指南

无需在电脑上安装数百兆的 Android Studio 或配置 Java SDK，只需将代码推送到 GitHub：

### 1. 自动触发云端打包
- **推送代码**：每次向 `main` 或 `master` 分支 push 代码，GitHub Actions 会自动触发构建任务。
- **下载测试 APK**：在 GitHub 仓库的 **Actions** 标签页中，点击最近的运行记录，在页面底部的 **Artifacts** 区域即可直接下载 `iGames-Android-APKs`（内含 Release 与 Debug 两个已签名可以直接安装的 APK）。

### 2. 发布正式 Release 版本
当您准备发布一个正式版本供其他人直接在 Release 页面下载时，只需打一个版本 Tag 并推送到 GitHub：
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions 会自动：
1. 编译全套 Release APK。
2. 自动生成 GitHub Release 页面。
3. 挂载好命名清晰的 `iGames-release.apk`。
4. 手机扫描或点击 Release 页面里的 APK 文件即可直接安装畅玩！

### 3. 手动一键编译 (Workflow Dispatch)
您也可以随时在 GitHub 网页端的 **Actions** -> **Build & Release Android APK** -> 点击右侧 **Run workflow** 按钮手动触发打包。

---

## 🛠️ 本地调试（若有本地开发环境）

如需在本地 Android Studio 运行：
1. 使用 Android Studio 打开本项目根目录。
2. 确保 Gradle JDK 选择 Java 17。
3. 连接安卓手机或开启模拟器，点击绿色运行按钮即可。

---

## 📜 开源协议
本项目基于 MIT License 开源，欢迎用于早教启蒙、家庭娱乐与拓展学习。
