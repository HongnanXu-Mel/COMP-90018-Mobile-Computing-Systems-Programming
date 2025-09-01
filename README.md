# Food Review App - 美食评价应用

## 项目简介

Food Review App 是一个基于Android平台的美食评价社交应用，用户可以分享餐厅体验、查看他人评价，并通过地图功能发现附近的美食店铺。应用采用现代化的Material Design设计，提供流畅的用户体验。

## 技术架构

- **开发语言**: Java + Kotlin
- **UI框架**: Jetpack Compose + XML Layouts
- **后端服务**: Firebase (Authentication, Firestore, Storage)
- **地图服务**: Google Maps API
- **位置服务**: Google Play Services Location
- **最低SDK版本**: API 24 (Android 7.0)
- **目标SDK版本**: API 34 (Android 14)

## 核心功能

### 用户认证系统
- **用户注册**: 支持邮箱注册，创建个人账户
- **用户登录**: 安全的Firebase身份验证
- **个人资料管理**: 修改用户名、邮箱和密码
- **安全登出**: 支持账户安全退出

### 首页 (Home)
- **帖子浏览**: 类似小红书的图文展示界面
- **搜索功能**: 顶部搜索栏，支持关键词搜索
- **帖子详情**: 点击帖子查看完整内容和评价
- **评价系统**: 支持用餐前和用餐后两种评价模式
- **内容展示**: 图文并茂的帖子内容展示

### 地图功能 (Map)
- **餐厅定位**: 显示附近餐厅的地理坐标
- **交互式地图**: 基于Google Maps的交互体验
- **位置权限**: 智能的位置权限管理
- **餐厅信息**: 点击坐标查看相关评价帖子
- **评价筛选**: 按用餐前/用餐后分类显示评价

### 个人中心 (Profile)
- **个人信息**: 显示和编辑用户基本信息
- **账户设置**: 修改密码、更新邮箱
- **数据同步**: 与Firebase实时数据同步
- **安全管理**: 安全的账户操作

### 内容创作
- **帖子发布**: 创建包含图片和文字的美食评价
- **多媒体支持**: 
  - 拍照功能：直接使用相机拍摄
  - 图库选择：从本地相册选择照片
- **语音输入**: 语音转文字功能，提升输入效率
- **评价关联**: 帖子自动关联到对应餐厅

## 安装和运行

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- Android SDK 34
- Java 17 或更高版本
- Google Play Services

### 构建步骤
1. 克隆项目到本地
2. 在Android Studio中打开项目
3. 确保已安装必要的SDK组件
4. 配置Firebase项目（如需要）
5. 运行应用

### 权限配置
应用需要以下权限：
- 网络访问
- 位置信息
- 相机权限
- 存储权限

## 特色亮点

- **现代化UI**: 采用Material Design 3设计语言
- **响应式设计**: 支持不同屏幕尺寸和分辨率
- **实时数据**: Firebase实时数据库同步
- **智能定位**: 基于Google Maps的精准位置服务
- **多媒体支持**: 丰富的图片和语音输入功能
- **用户体验**: 流畅的导航和交互体验

## 开发状态

当前版本为开发阶段，核心功能框架已搭建完成，包括：
- 用户认证系统
- 基础UI框架
- 地图集成
- Firebase后端集成
- 帖子系统开发中
- 评价功能开发中
- 搜索功能开发中

## 贡献指南

欢迎开发者参与项目贡献，请遵循以下规范：
- 代码风格遵循Android开发规范
- 提交前进行充分测试
- 遵循Git提交信息规范

## 许可证

本项目采用开源许可证，具体条款请查看LICENSE文件。

---

## 项目结构

```
Food/
├── app/
│   ├── build/
│   │   ├── generated/
│   │   ├── gmpAppId/
│   │   ├── intermediates/
│   │   ├── kotlin/
│   │   ├── outputs/
│   │   └── tmp/
│   ├── build.gradle.kts
│   ├── google-services.json
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/
│       │   └── java/com/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/example/food/
│       │   │       ├── MainActivity.java
│       │   │       ├── LoginActivity.java
│       │   │       ├── RegisterActivity.java
│       │   │       ├── SuccessActivity.java
│       │   │       ├── HomeFragment.java
│       │   │       ├── MapFragment.java
│       │   │       ├── ProfileFragment.java
│       │   │       └── ui/
│       │   ├── res/
│       │   │   ├── color/
│       │   │   ├── drawable/
│       │   │   ├── layout/
│       │   │   ├── menu/
│       │   │   ├── mipmap-anydpi-v26/
│       │   │   ├── mipmap-hdpi/
│       │   │   ├── mipmap-mdpi/
│       │   │   ├── mipmap-xhdpi/
│       │   │   ├── mipmap-xxhdpi/
│       │   │   ├── mipmap-xxxhdpi/
│       │   │   ├── values/
│       │   │   └── xml/
│       │   └── test/
│       │       └── java/com/
│       └── test/
│           └── java/com/
├── build/
│   └── reports/
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── settings.gradle.kts
└── README.md
```

*Food Review App - 让美食评价更简单，让美食发现更有趣*
