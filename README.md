# TimeStampApp

给图片添加自定义时间水印的 Android 应用。

## 功能特性

- 📷 从相册选择图片
- ✏️ 自定义水印文字（默认显示当前时间，格式：yyyy.MM.dd HH:mm）
- 🎨 水印样式：
  - 字体：Roboto（系统默认无衬线字体）
  - 颜色：纯白色 (#FFFFFF)
  - 位置：右下角，距边缘约 5%
  - 字号：自动适配为图片宽度的 4.5%
  - 无描边、无阴影、不透明、单行显示
- 👁️ 实时预览水印效果
- 💾 一键保存到相册

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 34 (Android 14)
- **构建工具**: Gradle + AGP 8.2.2
- **架构**: 单 Activity + ViewBinding

## 项目结构

```
TimeStampApp/
├── app/src/main/
│   ├── java/com/timestamp/app/
│   │   └── MainActivity.kt          # 主界面逻辑
│   ├── res/
│   │   ├── layout/activity_main.xml # 布局文件
│   │   ├── values/                  # 资源文件
│   │   └── xml/                     # 配置文件
│   └── AndroidManifest.xml
├── .github/workflows/
│   └── android-build.yml            # CI/CD 构建配置
├── build.gradle.kts                 # 项目级构建配置
├── app/build.gradle.kts             # 应用级构建配置
└── settings.gradle.kts
```

## 使用说明

1. 点击「选择图片」按钮从相册选择图片
2. 在输入框中编辑水印文字（默认自动填充当前时间）
3. 点击「生成水印」按钮预览效果
4. 满意后点击「保存到相册」保存图片

## 构建

### 本地构建

```bash
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/`

### GitHub Actions 自动构建

推送代码到 `master` 或 `main` 分支会自动触发构建，构建产物可在 Actions 页面下载。

## 权限说明

- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES`: 读取相册图片
- `WRITE_EXTERNAL_STORAGE`: 保存水印图片到相册（Android 9 及以下）

## 开源协议

MIT License
