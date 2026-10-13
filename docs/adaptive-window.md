# 自适应窗口

`ui2` 已通过 API 依赖提供 `salt-adaptive-window`，使用 SaltUI 时不需要额外添加模块依赖。窗口信息由 Compose 的当前原生窗口自动派生，应用入口、AppTheme 和每个窗口都不需要手动写 Provider。

## 读取当前窗口

```kotlin
import com.moriafly.salt.adaptive.window.LocalSaltAdaptiveWindow
import com.moriafly.salt.adaptive.window.UnstableSaltAdaptiveWindowApi
import com.moriafly.salt.adaptive.window.WindowSizeClass

@OptIn(UnstableSaltAdaptiveWindowApi::class)
@Composable
fun Example() {
    val window = LocalSaltAdaptiveWindow.current
    if (window.sizeClass == WindowSizeClass.Compact) {
        CompactContent()
    } else {
        WideContent()
    }
}
```

`windowSize` 是所属窗口的 dp 尺寸；`sizeClass` 按窗口宽度分为 Compact（小于 600 dp）、Medium（600 dp 至小于 840 dp）和 Expanded（至少 840 dp）。窗口旋转、调整大小或进入分屏后，读取这些信息的组件会更新。Desktop 的多个窗口分别使用各自的 `LocalWindowInfo`，iOS 同样使用窗口尺寸而非屏幕尺寸。

`formFactor` 描述设备形态，不替代窗口尺寸。Android 按当前显示器的最大窗口范围估算手机与平板，不使用随分屏缩小的资源限定尺寸；Android 11 之前使用显示器尺寸兼容处理。汽车与折叠设备另按平台特征识别，这些判断仍属于设备形态估算。平板上的窄分屏也可以是 Compact；一个横向的 Compact 窗口也未必适合底部面板。系统栏、切口和键盘 inset 仍由布局组件处理，不从窗口尺寸中统一扣除。

## 主题与 Sheet

`SaltTheme` 和 Sample 的 `AppTheme` 只管理主题。自适应信息直接来自当前窗口，忘记添加包装不会影响默认行为。Sample 的宽布局和 Sheet 的默认呈现方式共用 `WindowSizeClass`，没有各自重复定义的宽度阈值。

Sheet 在手机横竖屏均默认从底部呈现，平板与桌面默认浮动居中。折叠设备及其他设备按窗口空间适配：Compact 竖向窗口使用 Bottom，其余使用 Floating。面板的实际宽度、高度与安全区仍受所属窗口空间约束。它通过内部宿主参数保存所属窗口的自适应信息，避免 Dialog 或 Popup 的尺寸影响自身呈现判断，不覆盖正文中的窗口 Local。在 Sheet 正文中创建新的原生窗口时，新窗口仍自动读取自己的尺寸。系统安全区、键盘和实际绘制范围仍由 Sheet 的平台宿主处理。显式指定 `SheetDisplayMode.Bottom` 或 `SheetDisplayMode.Floating` 可以覆盖自动策略。

## 可选覆盖

只有需要受控值时才使用 `ProvideSaltAdaptiveWindow`，例如预览或测试夹具：

```kotlin
ProvideSaltAdaptiveWindow(
    adaptiveWindow = SaltAdaptiveWindow(
        formFactor = DeviceFormFactor.Phone,
        sizeClass = WindowSizeClass.Compact,
        windowSize = DpSize(390.dp, 844.dp)
    )
) {
    PreviewContent()
}
```

`LocalSaltAdaptiveWindow.current` 读取自动值或最近的显式覆盖值；既有的 `currentSaltAdaptiveWindow()` 保持直接读取当前平台窗口的含义，不读取覆盖值。不要为了正常接入而给每个窗口添加覆盖。

旧版双参数 `SaltAdaptiveWindow` 构造、copy 和 Local 入口保留二进制兼容。双参数创建的值使用 `DpSize.Unspecified`；Sheet 遇到这种值时使用宿主尺寸判断方向。

## 验证范围

真实窗口回归测试源码覆盖手机与平板的横竖屏呈现策略、Desktop 无 Provider 时的尺寸变化、独立多窗口、从 Sheet 内创建新窗口，以及模态中的显式覆盖。Android 测试源码检查真机手机横屏时的底部呈现、540 dp 宽度上限、水平居中和导航栏图标样式，并覆盖平板使用窄窗口资源配置时的浮动呈现；使用原生输入检查内容操作与关闭后的背景操作。本轮按用户要求只编译测试源码，不运行 UI 测试；平台运行验收由用户执行。
