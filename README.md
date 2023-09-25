# SaltUI

SaltUI（UI for Salt Player） 是提取自[椒盐音乐](https://github.com/Moriafly/SaltPlayerSource)的 UI 风格组件，用以快速生成椒盐音乐风格用户界面。

本库将会广泛用以椒盐系列 App 开发，以达到快速开发目的。

<img src="img/app.jpg" width="200px"/>

## 使用

### 1. 项目 Gradle 添加 JitPack 依赖

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. 要使用的模块下添加 SaltUI 依赖

最新版本⬇️⬇️⬇️

[![](https://jitpack.io/v/Moriafly/SaltUI.svg)](https://jitpack.io/#Moriafly/SaltUI)

```groovy
dependencies {
    // ...
    // 将 <VERSION> 替换为具体的版本号，如 0.1.0-dev04 
    // 即 implementation 'com.github.Moriafly:SaltUI:0.1.0-dev04'
    // 推荐使用上方最新版本或稳定版本（若有）
    implementation 'com.github.Moriafly:SaltUI:<VERSION>'
}
```

## 设计规范和使用介绍

组件位于 com.moriafly.salt.ui 包下，下面分为主题、页面、文本、按钮和点击。

演示项目（DEMO）在 app 模块。

### 主题

由 SaltTheme 下 SaltColors、SaltTextStyles 和 SaltDimens 组成。

#### Theme

DynamicSaltTheme - Material You 配色

SaltTheme - 椒盐经典

#### SaltColors

| 颜色值           | 说明                          |
|---------------|-----------------------------|
| highlight     | 软件强调色                       |
| text          | 主文本颜色                       |
| subText       | 次要文本颜色                      |
| background    | 用于整个 App 最底层颜色，默认背景色（底层背景色） |
| subBackground | 次要背景色（上层背景色）                |

```kotlin
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colors = if (isSystemInDarkTheme()) {
        darkSaltColors()
    } else {
        lightSaltColors()
    }
    SaltTheme(
        colors = colors
    ) {
        content()
    }
}
```

#### SaltTextStyles

| 字体        | 说明     |
|-----------|--------|
| main      | 主文本样式  |
| sub       | 次要文本样式 |
| paragraph | 段落文本样式 |

#### SaltDimens

| 度量     | 说明      |
|--------|---------|
| corner | 一些元素的圆角 |

### 页面（基础）

| 名称            | 用途                               |
|---------------|----------------------------------|
| TitleBar      | 标题栏                              |
| BottomBar     | 底部栏                              |
| BottomBarItem | 底部栏子项                            |
| RoundedColumn | 以 subBackground 为底色构建圆角内容 Column |

### 页面（内部）

以下的元素要使用在 RoundedColumn 中。

| 名称            | 用途                                  |
|---------------|-------------------------------------|
| ItemTitle     | 构建内容界面标题                            |
| ItemText      | 构建内容界面说明文本                          |
| Item          | 默认列表项目（可设置图标、标题和副标题）                |
| ItemSwitcher  | 默认开关项目（可设置图标、标题和副标题）                |
| ItemCheck     | 选中项目                                |
| ItemValue     | 类似 Key - Value                      |
| ItemEdit      | 默认文本框                               |
| ItemSpacer    | 默认内部的竖向间隔                           |
| ItemContainer | 在内容界面构建拥有内部边距的容器，方便使用在内部添加如按钮等自定义元素 |

### 页面（外部）

| 名称                  | 用途                     |
|---------------------|------------------------|
| ItemOuterLargeTitle | 外部大标题附带说明（适用于如椒盐音乐实验室） |
| ItemOuterTextButton | 适用于 Item 的外部文本按钮       |

### 文本

| 名称      | 用途          |
|---------|-------------|
| .textDp | 文本单位使用 dp 值 |

### 按钮

| 名称             | 用途               |
|----------------|------------------|
| BasicButton    | 基本按钮             |
| TextButton     | 默认文本按钮           |

### 点击

| 名称                         | 用途              |
|----------------------------|-----------------|
| Modifier.noRippleClickable | 没有涟漪扩散的点击效果     |
| Modifier.fadeClickable     | 减淡点击效果（添加透明度效果） |

### 对话框 Dialog

YesDialog 和 YesNoDialog 最新版本对各部分边距进行了调优，达到更好的视觉效果

| 名称                | 用途            |
|-------------------|---------------|
| BottomSheetDialog | 默认底部对话框       |
| YesDialog         | 确认对话框（类似默认通知） |
| YesNoDialog       | 请求确认对话框       |

## 贡献

[贡献者行为准则](CODE_OF_CONDUCT.md)

## 版权

LGPL-2.1 License，详见 [LICENSE](LICENSE) 。

使用开源库：AndroidX、Kotlin 等。

```
SaltUI
Copyright (C) 2023 Moriafly

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
```

## 星星历史

[![Star History Chart](https://api.star-history.com/svg?repos=Moriafly/SaltUI&type=Date)](https://star-history.com/#Moriafly/SaltUI&Date)