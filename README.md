<div align="center">
    <img style="width: 200px;" src="ic_salt_ui_500.png">
    <p style="font-size: 48px">Moriafly/<b>SaltUI</b></p>
</div>

SaltUI（UI for Salt Player） 是提取自[椒盐音乐](https://github.com/Moriafly/SaltPlayerSource)的 UI 风格组件，用以快速生成椒盐音乐风格用户界面。

本库将会广泛用以椒盐系列 App 开发，以达到快速开发目的。

## 使用

⚠️项目还在 dev 开发中。

[![](https://jitpack.io/v/Moriafly/SaltUI.svg)](https://jitpack.io/#Moriafly/SaltUI)

项目 Gradle 添加 jitpack 依赖：

```groovy
allprojects {
    repositories {
        // ...
        maven { url 'https://jitpack.io' }
    }
}
```

要使用的模块下添加 SaltUI 依赖：

```groovy
dependencies {
    // ...
    // 将 <VERSION> 替换为具体的版本号，如 0.1.0-dev04 ，即 implementation 'com.github.Moriafly:SaltUI:0.1.0-dev04'
    implementation 'com.github.Moriafly:SaltUI:<VERSION>'
}
```

## 设计规范和使用介绍

组件位于 com.moriafly.salt.ui 包下。

### 主题

由 SaltTheme 和 SaltColors 组成，将颜色分为软件强调色 highlight ，主背景颜色（background ，用于整个 App 最底层颜色，默认背景色）



### 页面

### 文本

## 版权

LGPL-2.1 License，详见 [LICENSE](LICENSE) 。

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
