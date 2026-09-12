# 🌊 Liquid Music Player

一款基于 Jetpack Compose + Kotlin 打造的现代化 Android 音乐播放器，拥有**真正的液态玻璃材质**与**流动动效**的精美UI设计与强大功能。

## ✨ 核心特性

### 🎨 视觉体验（v6.0 液态玻璃UI）
- **🌊 液态流动边框** - 彩虹渐变边框，8秒周期动态扫描
- **✨ 三层毛玻璃卡片** - 16dp模糊 + 动态光泽扫描 + 径向渐变
- **💫 液态波动背景** - 双层相位波（10s+13s）+ 粒子流动系统
- **🎨 专辑封面取色** - 实时提取主色，1200ms缓动过渡
- **🌙 深色/浅色主题** - 一键切换，立即生效（Sun/Moon图标）
- **🎬 流畅动画系统** - 遵循Emil设计工程规范，所有动画<300ms

### 🎧 播放功能
- **ExoPlayer内核** - 稳定高效的音频播放
- **多种播放模式** - 顺序/列表循环/单曲循环/随机播放
- **智能进度控制** - 拖动进度条精准跳转
- **音量调节** - 实时音量滑块
- **睡眠定时器** - 5/10/15/30/60分钟预设，倒计时可视化

### 📋 资源管理
- **收藏系统** - 持久化收藏，一键过滤收藏曲目
- **播放队列** - 查看/编辑/清空队列，底部Sheet展示
- **播放历史** - 自动记录最近20首播放曲目
- **多维度排序** - 标题/艺术家/时长 升序/降序，添加顺序
- **实时搜索** - 标题/艺术家/专辑 全文搜索

### 🎛️ 用户体验
- **手势交互** - 点击封面播放/暂停，列表项长按菜单
- **空状态友好** - 搜索无结果/收藏为空/队列为空 提示优化
- **状态可视化** - 播放指示器、收藏高亮、队列Badge
- **错误提示** - Snackbar友好提示，不阻断交互

## 📸 截图展示

| 主界面 | 播放器 | 队列管理 |
|:---:|:---:|:---:|
| *列表 + 搜索* | *毛玻璃播放卡* | *播放队列* |

| 排序菜单 | 定时器 | 主题切换 |
|:---:|:---:|:---:|
| *7种排序选项* | *睡眠倒计时* | *深色/浅色* |

## 🏗️ 技术栈

### 架构与设计模式
- **MVVM** - ViewModel + StateFlow 响应式架构
- **Dependency Injection** - Hilt 依赖注入
- **Repository Pattern** - 数据层抽象
- **Clean Architecture** - 清晰分层，易于测试

### 核心技术
- **Jetpack Compose** - 声明式UI框架
- **Material Design 3** - 最新设计规范
- **ExoPlayer (Media3)** - 专业音频播放内核
- **Kotlin Coroutines** - 异步编程
- **StateFlow/SharedFlow** - 数据流管理
- **DataStore** - 轻量级持久化存储
- **Coil** - 高效图片加载库
- **Palette API** - 动态取色

### 开发工具
- **Kotlin 1.9** - 现代化编程语言
- **Gradle 8.2** - 构建系统
- **Android Studio** - IDE
- **Git** - 版本控制

## 📂 项目结构

```
app/src/main/kotlin/com/musicplayer/liquid/
├── data/
│   ├── model/              # 数据模型
│   │   ├── Track.kt        # 曲目数据类
│   │   ├── PlaybackMode.kt # 播放模式枚举
│   │   ├── PlaybackState.kt# 播放状态
│   │   ├── SortOption.kt   # 排序选项
│   │   └── SleepTimer.kt   # 定时器模型
│   ├── repository/         # 数据仓库
│   │   ├── MusicRepository.kt
│   │   └── MusicRepositoryImpl.kt
│   └── player/             # 播放器封装
│       ├── MusicPlayer.kt
│       └── ExoPlayerImpl.kt
├── ui/
│   ├── components/         # 可复用UI组件
│   │   ├── AlbumCover.kt
│   │   ├── DynamicBackground.kt
│   │   ├── GlassCard.kt
│   │   ├── ProgressBar.kt
│   │   ├── VolumeControl.kt
│   │   ├── SearchBar.kt
│   │   ├── EmptyState.kt
│   │   ├── SortMenu.kt
│   │   ├── SleepTimerDialog.kt
│   │   └── PlayQueueSheet.kt
│   ├── player/             # 播放器ViewModel
│   │   └── PlayerViewModel.kt
│   └── theme/              # 主题配置
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/                   # 工具类
│   ├── TimeFormatter.kt
│   └── PaletteCache.kt
├── di/                     # 依赖注入模块
│   └── AppModule.kt
├── MainActivity.kt
└── App.kt
```

## 🚀 快速开始

### 环境要求
- **Android Studio** Hedgehog | 2023.1.1 或更高版本
- **JDK** 17
- **Android SDK** API 26+ (Android 8.0+)
- **Kotlin** 1.9.22

### 克隆项目
```bash
git clone https://github.com/yourusername/MusicPlayerLiquid.git
cd MusicPlayerLiquid
```

### 构建运行
```bash
# 构建Debug版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 或直接在Android Studio中点击Run按钮
```

### 权限说明
应用首次启动时会请求以下权限：
- **READ_MEDIA_AUDIO** (Android 13+) - 读取音频文件
- **READ_EXTERNAL_STORAGE** (Android 12及以下) - 读取存储权限

## 📋 版本历史

### v5.0 (2026-09-12) - **最新版本**
- ✅ 新增深色/浅色主题切换
- ✅ 新增7种排序选项 (标题/艺术家/时长/添加顺序)
- ✅ 新增睡眠定时器 (5/10/15/30/60分钟)
- ✅ 新增播放队列管理
- ✅ 新增播放历史记录 (最近20首)
- ✅ 新增列表项更多菜单
- ✅ 修复3个bug (重复import、缺失import、内存泄漏)
- 代码增量: +746行

### v4.0 (2026-09-12)
- ✅ 新增收藏功能 (DataStore持久化)
- ✅ 新增收藏过滤按钮
- ✅ 新增列表入场瀑布流动画
- ✅ 新增播放指示器圆形Badge + 脉冲动画
- ✅ 新增专辑封面点击播放/暂停
- ✅ 新增曲目时长显示
- ✅ 新增专辑信息两行显示
- ✅ 优化进度更新频率 (500ms)
- ✅ 优化空状态提示
- 代码增量: +246行

### v3.0 (2026-09-11)
- ✅ 修复Shuffle播放历史bug
- ✅ 修复进度条拖动卡顿
- ✅ 修复ViewModel内存泄漏
- ✅ 修复播放模式图标混淆
- ✅ 新增音量控制
- ✅ 新增搜索/过滤功能
- ✅ 新增加载状态指示
- ✅ 优化3处动画效果
- 代码增量: +180行

### v2.0 (2026-09-10)
- ✅ 基础播放功能
- ✅ 液态UI设计
- ✅ 动态背景
- ✅ 播放模式切换

## 🗺️ 未来规划

### v6.0 (计划中)
- [ ] 播放历史页面
- [ ] 通知栏媒体控制 (MediaSession)
- [ ] 歌词显示 (lrc文件)
- [ ] 桌面Widget小部件
- [ ] 均衡器 (EQ)
- [ ] 自定义播放列表
- [ ] 手势操作 (滑动切歌/调音量)
- [ ] 迷你播放器 (底部常驻)

### 性能优化
- [ ] 封面缓存策略优化
- [ ] Paging3分页加载
- [ ] 前台Service后台播放
- [ ] 唤醒锁管理

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发流程
1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范
- 遵循 Kotlin 官方代码风格
- 使用 Compose 声明式UI
- StateFlow 用于状态管理
- 单一职责原则 (SRP)
- 注释清晰简洁

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 📞 联系方式

- **作者**: Kiro AI Assistant
- **项目地址**: https://github.com/yourusername/MusicPlayerLiquid
- **反馈邮箱**: your.email@example.com

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Google官方UI框架
- [ExoPlayer](https://github.com/google/ExoPlayer) - 强大的媒体播放器
- [Coil](https://github.com/coil-kt/coil) - 现代化图片加载库
- [Hilt](https://dagger.dev/hilt/) - 依赖注入框架
- [Material Design 3](https://m3.material.io/) - 设计规范

---

⭐️ 如果这个项目对你有帮助，欢迎Star支持！
