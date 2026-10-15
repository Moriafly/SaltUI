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

`formFactor` 描述设备形态，不替代窗口尺寸。Android 按当前显示器的最大窗口范围估算手机与平板，不使用随分屏缩小的资源限定尺寸；Android 11 之前使用显示器尺寸兼容处理。汽车与折叠设备另按平台特征识别，这些判断仍属于设备形态估算。平板上的窄分屏也可以是 Compact；组件应依据各自需要的窗口空间选择布局。系统栏、切口和键盘 inset 仍由布局组件处理，不从窗口尺寸中统一扣除。

## 主题与 Sheet

`SaltTheme` 和 Sample 的 `AppTheme` 只管理主题。自适应信息直接来自当前窗口，忘记添加包装不会影响默认行为。Sample 的宽布局使用 `WindowSizeClass`，Sheet 则使用同一窗口信息中的宽度与高度选择呈现方式。

`SheetDisplayMode.Auto` 在所属窗口宽度至少为 600 dp 且高度至少为 480 dp 时采用 Floating，其余使用 Bottom，不依赖设备形态。判断使用系统安全区和键盘 inset 扣除前的窗口尺寸，面板实际宽、高与安全区再由布局处理。因此平板的窄分屏可以使用 Bottom，足够宽高的窗口则使用 Floating。它通过内部宿主参数保存所属窗口的自适应信息，避免 Dialog 或 Popup 的尺寸影响自身呈现判断，不覆盖正文中的窗口 Local。在 Sheet 正文中创建新的原生窗口时，新窗口仍自动读取自己的尺寸。显式指定 `SheetDisplayMode.Bottom` 或 `SheetDisplayMode.Floating` 可以覆盖自动策略。

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

旧版双参数 `SaltAdaptiveWindow` 构造、copy 和 Local 入口保留二进制兼容。双参数创建的值使用 `DpSize.Unspecified`；Sheet 遇到这种值时使用宿主的宽、高选择呈现方式。

## 验证范围

本次文档依据当前实现修正为窗口宽、高策略，Android Sample APK 构建和 Desktop 示例源码编译已通过，不以旧版按设备形态编写的测试或运行记录作为运行验证结论。窗口旋转、分屏、Desktop 多窗口，以及独立原生窗口的实际呈现和输入验收由用户执行，不新增或运行 UI 测试。
