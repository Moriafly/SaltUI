# Sheet 使用说明

`Sheet` 是 SaltUI 的模态面板，公共显隐接口只有 `visible: Boolean` 和 `onDismissRequest`。高度、动画、平台承载、遮罩、返回处理与退出释放由组件管理。接口位于 `com.moriafly.salt.ui.sheet`，目前标记为 `@UnstableSaltUiApi`。

## 最简调用

```kotlin
@OptIn(UnstableSaltUiApi::class)
@Composable
fun Example() {
    var visible by rememberSaveable { mutableStateOf(false) }

    Button(onClick = { visible = true }, text = "打开")

    Sheet(
        visible = visible,
        onDismissRequest = { visible = false },
        title = "选择内容"
    ) {
        Text("业务内容")
    }
}
```

保持 `Sheet` 调用在组合中。`visible = false` 后立即释放背景输入拦截，保留视觉退场并在动画完成后释放承载；不要用 `if (visible) { Sheet(...) }` 包裹它。离开整个页面时会立即清理承载。

同一窗口内接力切换两个 Sheet 时，新 Sheet 在等待旧面板退场期间继续保护背景输入，避免切换中误触背景；其业务内容在获得呈现位置后才组合。没有后续 Sheet 等待时，普通关闭仍立即释放背景输入。

用户点击遮罩、关闭按钮，按 Back / Escape，或向下拖动越过最小高度时，产生关闭请求。回调保持 `visible = true` 就继续显示；拖动请求会回到合法高度。业务主动设置 false 不触发关闭请求。

## 多高度列表

```kotlin
Sheet(
    visible = visible,
    onDismissRequest = { visible = false },
    title = "播放队列",
    properties = SheetProperties(
        detents = listOf(SheetDetent.Medium, SheetDetent.Large),
        initialDetent = SheetDetent.Medium
    ),
    actions = {
        Button(onClick = { visible = false }, text = "完成")
    }
) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(100) { index -> Text("曲目 ${index + 1}") }
    }
}
```

布局按标题、正文、action 的顺序排列。正文获得有限视口，Sheet 不增加正文上下或左右边距。action 区域四周各留 16 dp；没有 action 时保留 8 dp 底部留白。滚动内容可显式设置 `contentLayout = SheetContentLayout.EdgeToEdge`，并使用 `SheetScope.contentPadding` 应用这段留白与底部安全区：视口延伸到屏幕底边，padding 留在可滚动的内容尾部，最后一项仍能滚到导航栏上方。默认的 `Inset` 配置自动使用安全视口；读取边距不会改变布局。Content 按实际内容高度显示，不为短正文额外补足 48 dp 空白。RoundedColumn、ItemOuterTip 等内容组件自带的边距仍由这些组件负责。不要在 Lazy 列表外再套一层滚动容器。标题与操作区通常固定；极小窗口会让标题和操作区可以滚动。

普通滚动内容将 padding 放在 `verticalScroll` 之后，使它成为滚动内容的一部分：

```kotlin
Sheet(
    visible = visible,
    onDismissRequest = { visible = false },
    properties = SheetProperties(contentLayout = SheetContentLayout.EdgeToEdge)
) {
    Column(
        modifier = Modifier
            .verticalScroll(state = rememberScrollState())
            .padding(paddingValues = contentPadding)
    ) {
        // Business content.
    }
}
```

LazyColumn 直接使用自己的 `contentPadding` 参数，不在滚动容器外添加 navigationBarsPadding：

```kotlin
Sheet(
    visible = visible,
    onDismissRequest = { visible = false },
    properties = SheetProperties(contentLayout = SheetContentLayout.EdgeToEdge)
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(count = 100) { index ->
            Text(text = "项目 $index")
        }
    }
}
```

`contentPadding` 是无副作用的只读布局信息。只有显式选择 `EdgeToEdge` 且无 action 时它提供滚动尾部边距；默认 `Inset` 和存在 action 时均为零，安全区仍由 Sheet 保护。无 action 时仅导航栏安全区与 8 dp 留白进入滚动尾部，IME 始终在视口外避让；浮动呈现仅提供 8 dp 留白，不重复添加已处理的系统边距。

呈现模式使用 `SheetDisplayMode`，正文滚动与面板展开的顺序使用 `SheetScrollBehavior`。默认配置如下：

```kotlin
SheetProperties(
    displayMode = SheetDisplayMode.Auto,
    scrollBehavior = SheetScrollBehavior.ExpandFirst
)
```

| 配置 | 用途 |
| --- | --- |
| `Content` | 测量内容的自然高度，也是默认配置 |
| `Medium` / `Large` | 可用高度的一半 / 最大可用高度；空间不足时 Medium 会退化 |
| `Height(320.dp)` | 固定高度，自动受可用空间约束 |
| `Fraction(0.75f)` | 可用安全区域高度的比例，范围 `(0, 1]` |
| `displayMode` | SheetDisplayMode.Auto 在手机横竖屏均采用 Bottom，平板与桌面采用 Floating；其他设备按窗口空间适配，也可显式指定 |
| `scrollBehavior` | SheetScrollBehavior.ExpandFirst 向上拖动时先扩展后滚动；ScrollFirst 优先滚动正文，仅在滚动到边界后调整面板高度。两种策略均先将正文滚回顶部再向下收起或请求关闭 |
| `contentLayout` | 默认 Inset 自动避让底部；EdgeToEdge 允许无 action 的正文延伸到底边，并由调用方应用 contentPadding |
| `dismissible = false` | 禁止用户关闭并隐藏关闭按钮，业务主动设置 false 仍有效 |
| `dragEnabled = false` | 禁止指针调整高度与拖拽关闭，并隐藏手柄；保留有效的无障碍高度动作 |

等高停靠点合并，只显示有效操作。Floating 将当前停靠点视为高度上限，不提供拖动柄或下滑关闭。初始停靠点是该实例的初始配置；同一实例重开会保留用户选择和正文中的 `rememberSaveable` 状态。

浮动高度上限动态取扣除安全边距后的绘制区域高度、面板宽度的 1.2 倍与 640 dp 三者的最小值。16 dp 是安全边距的最小值，不在系统边距之外再叠加。矮窗口中，长内容或填满视口的列表可利用全部可用高度；高窗口不让面板无限长，窄窗口同时按宽度缩小上限。短内容仍按自然高度显示。浮动的 Height、Fraction、Medium 与 Large 均在这个上限内解析。

手柄不提供单独的显示开关。Bottom 允许拖拽且有多个有效高度，或允许拖拽关闭时自动显示。因此默认的单一 Content 高度也会显示手柄；单一高度且禁止关闭、禁止拖拽或浮动呈现时隐藏。点击手柄切换高度仅在有多个有效高度时可用。

默认外观参考 [Apple Sheet 指南与示意图](https://developer.apple.com/design/human-interface-guidelines/sheets)：Bottom 仅顶部两角使用 34 dp 圆角，底部两角为直角；Floating 四角均为 34 dp。拖动柄为 44 × 3 dp，颜色透明度为 0.3，交互区域为 48 × 48 dp，保留高度描述与展开、收起动作。关闭按钮使用 `PillButton` 的默认圆形模式与 `SaltIcons.Close`，复用默认背景、细描边、点击反馈和禁用样式。按钮尺寸继承 LocalControlSize，并与同尺寸的 Button 共用高度、图标和间距。Regular 默认直径与图标大小在 Android 为 48 dp 与 20 dp、iOS 为 44 dp 与 20 dp、Desktop 为 32 dp 与 16 dp，Sheet 不额外覆盖尺寸。按钮距顶部和右边各 16 dp，标题栏同步适配按钮尺寸；显示拖动柄不会移动关闭按钮。这些尺寸是 SaltUI 的适配值。

遮罩始终覆盖完整窗口，只改变透明度；平台窗口动画已禁用。Bottom 的背景延伸到物理底边，最大宽度与浮动呈现一致，为 540 dp，并相对整个窗口水平居中；左右安全区取两侧 inset 的最大值，避免单侧切口或侧边导航栏推偏中心。普通内容和 action 的底部安全区与 IME 取并集并由内部 Spacer 预留；接管 contentPadding 的滚动正文仅在视口外避让 IME，导航栏 inset 进入滚动尾部。高度配置基于可用安全区域，底部 inset 另外添加，正文不需要重复处理；顶部只避让状态栏与切口，Large 没有额外上边距。Floating 的对称安全边距至少为 16 dp。

面板背景使用 `SaltTheme.colors.background`。浮动呈现将系统安全区对称处理：上下取顶部 inset、底部 inset 与 16 dp 的最大值，左右同样取左右 inset 与 16 dp 的最大值，保证面板位于窗口中央，不再重复叠加外部间距。比如状态栏 30 dp、底部导航栏 20 dp，则上下各避让 30 dp；二者都低于 16 dp 时，上下各避让 16 dp。IME 单独参与可用区域计算，避免将键盘高度复制到顶部。

action 保留标准 SaltUI Button 的胶囊形状，不覆盖按钮圆角。四周固定 16 dp，两侧按钮底部对齐。长文案增加高度时，胶囊圆角随按钮实际高度变化。自定义 BasicButton 的显式形状仍由调用方决定，Bottom 的系统底部 Spacer 另外保留。

浮动打开动画从窗口底部上移至居中位置，关闭时沿相同路径下移，参考 [Apple 默认 coverVertical 转场](https://developer.apple.com/documentation/uikit/uimodaltransitionstyle/coververtical)。面板使用内部高度引擎的可中断弹簧，保留当前位置与速度，正文尺寸在动画中保持稳定；遮罩仅进行独立透明度过渡。弹簧参数为 SaltUI 的适配值，未将其标注为 iOS 27 的私有系统曲线。

向下拖动可以越过最小停靠点直至面板完全移出屏幕。低于最小高度后保持内容尺寸并移动整个面板，避免正文继续缩小。释放时发起关闭请求；调用方保留 `visible = true` 时恢复原停靠点。

同一个所属窗口最多一个 Sheet 的 `visible` 为 true。可以在同一次业务更新中将旧 Sheet 设为 false、新 Sheet 设为 true，组件会等旧承载释放后呈现下一项。

## 草稿确认

在 `onDismissRequest` 中检查未保存内容，保持 visible 为 true，并在 Sheet 的正文组合内呈现普通确认对话框。继续编辑只关确认框；确认放弃后再将 visible 设为 false。正文内呈现确认框能让它成为当前 Sheet 的子模态。

## Sample 体验

桌面运行 `./gradlew.bat :desktopApp:run`，从首页进入 **Sheet**。Android 使用 `androidApp` 的 debug APK；iOS 使用现有 `iosApp` 工程。

页面提供四类内容：短内容动态增减、100 项播放队列、带放弃确认的草稿表单，以及水平 Pager、Slider、文本选择与普通按钮。内容、呈现方式和高度配置使用 ItemPopup 菜单选择；打开等列表操作使用 ItemButton，说明文字使用 ItemOuterTip；停靠点配置、action 按钮和普通滚动容器分别抽为私有函数，Lazy 播放队列保持独立。另有拖动、滚动优先、标题、操作区、长文案、关闭许可、请求保留和草稿保护开关。

默认手机横竖屏均使用 Bottom，平板与桌面使用 Floating；折叠设备及其他设备仍按窗口方向与宽度适配，键盘出现不会改变方向判断。体验拖动可选择 **Bottom** 和 **Medium / Large**。短内容和表单内提供显示期间的关闭开关、程序关闭及退场途中重开。关闭后重开队列可以检查位置保留。

自适应判断共用 `salt-adaptive-window` 的窗口信息，ui2 已通过 API 依赖提供该模块。`LocalSaltAdaptiveWindow.current` 自动读取当前原生窗口，应用入口和主题不需要手动添加 Provider；Sheet 通过内部宿主参数使用所属窗口的信息，不覆盖正文中的窗口 Local。旋转、分屏和 Desktop 多窗口分别跟随自己的窗口尺寸，在 Sheet 内创建的新原生窗口也自动使用自身信息；安全区与键盘仍由 Sheet 宿主负责。受控预览与兼容说明见[自适应窗口](adaptive-window.md)。

## 兼容与验证范围

旧 `BottomSheetScaffold`、`BottomSheetDialog` 保持原接口，不需要迁移。新增 Sheet 不引入公开 State，也不依赖 SwiftUI 运行时或 commonMain 的 Material 3 组件。

Android、Desktop 以及 iOS simulator 的 Kotlin 源码交叉编译已通过。Windows 真实窗口测试已覆盖关闭请求、输入隔离、边界、拖动、两种滚动策略、原生触摸取消、位置保留、快速重开、Tab、窗口缩小、多窗口和面板交接。Android API 36.1 模拟器已验证关闭策略、真实返回、IME、触摸列表及预测返回取消/提交；Sample 的短内容、列表和草稿确认流程已通过。

当前实现包含固定全窗口遮罩、Bottom 边缘与 inset、默认 PillButton 圆形关闭按钮、胶囊 action 按钮、按呈现方式处理底部圆角、完整下拉关闭、自动呈现与手柄、主题 background 色，以及精简后的 Sample。正文没有额外边距，action 四周各留 16 dp，无 action 时保留 8 dp，滚动正文通过 contentPadding 将导航栏 inset 与留白放到内容尾部；浮动高度按绘制区域、宽度和 640 dp 上限约束，打开与关闭使用垂直弹簧转场。退场开始即释放输入拦截；Android 同时恢复所属窗口的状态栏与导航栏图标样式。按用户要求，本轮只执行编译和静态检查，UI 测试全部交由用户。此前的 Windows / Android 运行结果属于修改前版本的历史记录，不能视为本轮运行验收。iOS 源码交叉编译不能代替 Xcode 链接与原生 UI 验收。

## 审查修复

显隐意图与停靠点目标分别保存，窗口、键盘和内容的几何更新不能把 Hidden 目标改为展开。Density 与字体缩放变化只更新换算和布局，不重建过渡状态；几何变化时保留动画位置与速度。多停靠点快速释放按当前位置和速度方向选择目标，支持一次拖动跨越多个高度。所属窗口的呈现占用随宿主离开组合释放，不依赖可取消的下一帧等待。

正文使用明确的 Inset / EdgeToEdge 配置，边距 getter 不再注册生命周期或修改视口。测量阶段先读取当前 inset、测量 action，再向正文提供不可变 PaddingValues；对称浮动 inset 同样延迟到布局阶段读取。退场内容从无障碍树中移除，Android 同时隐藏平台宿主的无障碍后代。浅色 Bottom 面板覆盖底部导航栏时使用深色图标；侧边导航栏位于遮罩区域时使用浅色图标。浮动面板沿用所属窗口的导航栏样式。

Desktop 默认同画布后端保留退场绘制并立即释放交互；启动时设置 compose.layers.type 为 COMPONENT 或 WINDOW 的原生后端在关闭时立即移除 Popup，避免原生绘制区域继续拦截背景输入。这两种后端不保留视觉退场。iOS 保留 Popup 退场绘制、释放交互并移除退场语义。平台行为仍由用户进行 UI 验收，源码编译不能代替运行验证。
