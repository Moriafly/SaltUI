# Sheet 使用说明

`Sheet` 是 SaltUI 的模态面板，通过 `visible: Boolean` 和 `onDismissRequest` 管理显隐。高度、动画、平台承载、遮罩、返回处理与退出释放由组件管理。需要等待退场完成时，可额外传入 `SheetState`。接口位于 `com.moriafly.salt.ui.sheet`，目前标记为 `@UnstableSaltUiApi`。

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

同一窗口内切换两个 Sheet 时，在同一次状态更新中隐藏旧面板并显示新面板；新面板测量完成后，两者完整运行各自的退场与展开动画，不等待旧面板完全关闭。遮罩和输入由所属窗口统一协调，避免切换中误触背景；没有后续 Sheet 时，普通关闭仍立即释放背景输入。

默认允许用户点击遮罩、关闭按钮，按 Back / Escape，或在 Bottom 模式向下拖动越过最小高度来产生关闭请求。回调保持 `visible = true` 就继续显示；拖动请求会回到合法高度。业务主动设置 `visible = false` 不触发关闭请求。

## 关闭方式

`SheetProperties` 提供四个独立开关，默认均为 true：

| 配置 | 设为 false 时的行为 |
| --- | --- |
| `dismissOnClickOutside` | 点击面板外的遮罩不产生关闭请求，点击仍被消费，不会穿透到背景内容 |
| `dismissOnBackPress` | Back / Escape 不产生 Sheet 关闭请求，面板仍保持模态输入；Android 返回键收起输入法的行为不变 |
| `dismissOnDrag` | Bottom 模式不能通过下拖关闭；`dragEnabled = true` 且存在多个有效停靠点时，仍可拖动调整高度 |
| `dismissOnCloseButtonClick` | 隐藏标题栏关闭按钮，并移除对应的点击区域与无障碍入口 |

表单只需要防止误触遮罩时，单独设置 `dismissOnClickOutside`，其余关闭方式保留默认行为：

```kotlin
Sheet(
    visible = visible,
    onDismissRequest = { visible = false },
    title = "填写内容",
    properties = SheetProperties(dismissOnClickOutside = false)
) {
    Text("表单内容")
}
```

`dismissible` 作为构造函数和 `copy` 的快捷重载，一次设置上述四个开关，不改变 `dragEnabled` 或其他布局配置：

```kotlin
val properties = SheetProperties(dismissible = false)
val enabledProperties = properties.copy(dismissible = true)
```

需要批量设置后再覆盖某个入口时，继续使用独立开关的 `copy`。例如，上传期间禁用所有用户关闭入口，平时只禁用遮罩点击关闭，`uploading` 由业务状态提供：

```kotlin
val properties = SheetProperties(dismissible = !uploading)
    .copy(dismissOnClickOutside = false)
```

同一次构造或 `copy` 调用选择批量重载或独立开关，不能混用两者的参数。普通 `copy()` 或只修改某个独立开关时，其余配置保持原值；显式 `copy(dismissible = ...)` 会重新统一四个开关。

这些配置只控制 Sheet 自身的用户关闭入口。业务主动设置 `visible = false`、移除导航页面，或自行处理取消、完成按钮时，不受这些开关限制。上传期间业务按钮是否可用由调用方决定。迁移时，将对 `properties.dismissible` 的读取改为所关心的独立开关；`dismissible` 仅用于批量配置。

## 等待退场完成

普通页面继续使用上面的调用即可。导航场景需要等待 Sheet 完全退出，再释放页面或导航 Entry 时，可以外提 `SheetState`：

```kotlin
val sheetState = rememberSheetState()
val scope = rememberCoroutineScope()

Sheet(
    visible = visible,
    onDismissRequest = {
        scope.launch {
            visible = false
            sheetState.awaitHidden()
            onRemove()
        }
    },
    state = sheetState,
    title = "选择内容"
) {
    Text("业务内容")
}
```

`SheetState` 只观察呈现生命周期，`visible` 仍是唯一显隐输入。`isHidden` 初始为 true；Sheet 进入、显示和退场期间均为 false，动画结束且承载移出组合后才恢复为 true。值在组合变更应用后更新，不会同步反映尚未应用的 `visible` 修改。

`awaitHidden()` 等待当前呈现完整退出，不会发起关闭；已经隐藏或尚未呈现时立即返回，不等待未来的打开。等待期间重新打开会中断退场，等待继续到后续完整退出。多个协程可同时等待，取消其中一个不会影响动画或其他等待者。如果 Sheet 调用提前离开组合，或者换用了另一个 state，未完成的等待会抛出 `CancellationException`，不会把强制移除当作正常退场完成。

等待完成表示动画结束且承载已移出组合，不表示独立的正文状态清理 Effect 或业务任务全部完成。等待者何时继续执行取决于其协程调度器，不保证额外延迟一帧或一次事件循环。

在 Navigation3 中，Scene 可以直接持有 `val sheetState = SheetState()`，页面将它传给 `Sheet`。`OverlayScene.onRemove()` 更新用于计算 `visible` 的移除状态，再调用 `sheetState.awaitHidden()`，使 Entry 在退场期间保留。若 Navigation3 取消移除，应恢复该移除状态并继续抛出取消异常；不需要从 Sheet 正文的 `DisposableEffect` 推断动画是否完成。

一个 state 同时只能绑定一个 Sheet 调用，完全解绑后可以复用。它属于 UI 生命周期，应在 UI 线程访问，不放入 `rememberSaveable` 或持久化数据。Sheet 自己管理正文状态的保留与清理，`SheetState` 不保存正文、停靠高度或导航栈。

## 隐藏与状态保留

默认情况下，Sheet 完整退出后清理正文中 `rememberSaveable` 保存的状态，所选停靠高度也重置为 `initialDetent`；下次打开从初始状态开始。在退场动画结束前重新打开会中断退场并保留当前状态。

进入子页面时，可以使用 `retainStateWhenHidden = true` 保留父 Sheet 的可保存状态与停靠高度。正文仍会在退场结束后离开组合：普通 `remember`、`LaunchedEffect` 和其他组合副作用不会继续存活。需要持续运行的业务任务和数据仍由调用方管理。

调用方管理页面关系和整个流程的显隐。父 Sheet 临时隐藏时，整个流程保持打开；结束流程时取消保留，即使父 Sheet 早已隐藏，也会清理保存状态。显示中的内容不会因为取消保留被立即重置，正在退场的内容则等动画完成后清理。

```kotlin
var flowVisible by rememberSaveable { mutableStateOf(false) }
var showingDetails by rememberSaveable { mutableStateOf(false) }

Sheet(
    visible = flowVisible && !showingDetails,
    onDismissRequest = { flowVisible = false },
    properties = SheetProperties(retainStateWhenHidden = flowVisible)
) {
    // State owned by this content survives a return from Details.
    var note by rememberSaveable { mutableStateOf("") }
    ItemEdit(text = note, onChange = { note = it }, hint = "Note")
    Button(onClick = { showingDetails = true }, text = "Details")
}
Sheet(
    visible = flowVisible && showingDetails,
    onDismissRequest = { showingDetails = false },
    actions = {
        Button(
            onClick = {
                showingDetails = false
                flowVisible = false
            },
            text = "Done"
        )
    }
) {
    Text(text = "Details")
}
```

重新进入流程时将 `showingDetails` 设为 false，再将 `flowVisible` 设为 true。组件不推断父子关系，也不提供导航栈。调用方外提的状态不属于 Sheet 的清理范围。

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
    dismissOnClickOutside = true,
    dismissOnBackPress = true,
    dismissOnDrag = true,
    dismissOnCloseButtonClick = true,
    scrollBehavior = SheetScrollBehavior.ExpandFirst
)
```

| 配置 | 用途 |
| --- | --- |
| `Content` | 测量内容的自然高度，也是默认配置 |
| `Medium` / `Large` | 可用高度的一半 / 最大可用高度；空间不足时 Medium 会退化 |
| `Height(320.dp)` | 固定高度，自动受可用空间约束 |
| `Fraction(0.75f)` | 可用安全区域高度的比例，范围 `(0, 1]` |
| `displayMode` | SheetDisplayMode.Auto 在所属窗口宽度至少为 600 dp 且高度至少为 480 dp 时采用 Floating，否则采用 Bottom；也可显式指定 |
| `scrollBehavior` | SheetScrollBehavior.ExpandFirst 向上拖动时先扩展后滚动；ScrollFirst 优先滚动正文，仅在滚动到边界后调整面板高度。两种策略均先将正文滚回顶部再向下收起或请求关闭 |
| `contentLayout` | 默认 Inset 自动避让底部；EdgeToEdge 允许无 action 的正文延伸到底边，并由调用方应用 contentPadding |
| 关闭方式 | 四个 `dismissOn…` 开关独立控制用户关闭入口，也可用 `dismissible` 重载统一设置，见[关闭方式](#关闭方式) |
| `dragEnabled = false` | 禁止指针调整高度与拖拽关闭，并隐藏手柄；保留有效的无障碍高度动作 |
| `retainStateWhenHidden` | 默认 false；为 true 时，完整隐藏后保留正文的可保存状态与所选停靠高度，供返回父页面使用 |

等高停靠点合并，只显示有效操作。Floating 将当前停靠点视为高度上限，不提供拖动柄或下滑关闭。默认完整关闭后重开使用 `initialDetent`；临时隐藏时显式保留状态，也会保留用户选择的高度。

浮动高度上限动态取扣除安全边距后的绘制区域高度、面板宽度的 1.2 倍与 640 dp 三者的最小值。16 dp 是安全边距的最小值，不在系统边距之外再叠加。矮窗口中，长内容或填满视口的列表可利用全部可用高度；高窗口不让面板无限长，窄窗口同时按宽度缩小上限。短内容仍按自然高度显示。浮动的 Height、Fraction、Medium 与 Large 均在这个上限内解析。

手柄不提供单独的显示开关。在 Bottom 模式且 `dragEnabled = true` 时，有多个有效高度或 `dismissOnDrag = true` 就会自动显示手柄。因此默认的单一 Content 高度也会显示手柄；单一高度且 `dismissOnDrag = false`、禁止拖拽或浮动呈现时隐藏。点击手柄切换高度仅在有多个有效高度时可用。

默认外观参考 [Apple Sheet 指南与示意图](https://developer.apple.com/design/human-interface-guidelines/sheets)。圆角使用与对话框相同的平滑曲线，半径为当前关闭按钮直径的一半加上 `SaltTheme.dimens.padding`，与关闭按钮的圆心对齐；Bottom 仅顶部两角使用曲线，底部两角为直角，Floating 四角均使用曲线。拖动柄为 44 × 3 dp，颜色透明度为 0.3，交互区域为 48 × 48 dp，保留高度描述与展开、收起动作。关闭按钮使用 `PillButton` 的默认圆形模式与 `SaltIcons.Close`，复用默认背景、细描边和点击反馈。按钮尺寸继承 LocalControlSize，并与同尺寸的 Button 共用高度、图标和间距。Regular 默认直径与图标大小在 Android 为 48 dp 与 20 dp、iOS 为 44 dp 与 20 dp、Desktop 为 32 dp 与 16 dp，Sheet 不额外覆盖尺寸。按钮距顶部和右边各 16 dp，标题栏同步适配按钮尺寸；显示拖动柄不会移动关闭按钮。这些尺寸是 SaltUI 的适配值。

遮罩始终覆盖完整窗口，只改变透明度；平台窗口动画已禁用。Bottom 的背景延伸到物理底边，最大宽度与浮动呈现一致，为 540 dp，并相对整个窗口水平居中；左右安全区取两侧 inset 的最大值，避免单侧切口或侧边导航栏推偏中心。普通内容和 action 的底部安全区与 IME 取并集并由内部 Spacer 预留；接管 contentPadding 的滚动正文仅在视口外避让 IME，导航栏 inset 进入滚动尾部。高度配置基于可用安全区域，底部 inset 另外添加，正文不需要重复处理；顶部只避让状态栏与切口，Large 没有额外上边距。Floating 的对称安全边距至少为 16 dp。

面板背景使用 `SaltTheme.colors.background`。浮动呈现将系统安全区对称处理：上下取顶部 inset、底部 inset 与 16 dp 的最大值，左右同样取左右 inset 与 16 dp 的最大值，保证面板位于窗口中央，不再重复叠加外部间距。比如状态栏 30 dp、底部导航栏 20 dp，则上下各避让 30 dp；二者都低于 16 dp 时，上下各避让 16 dp。IME 单独参与可用区域计算，避免将键盘高度复制到顶部。

action 保留标准 SaltUI Button 的胶囊形状，不覆盖按钮圆角。四周固定 16 dp，两侧按钮底部对齐。长文案增加高度时，胶囊圆角随按钮实际高度变化。自定义 BasicButton 的显式形状仍由调用方决定，Bottom 的系统底部 Spacer 另外保留。

浮动打开动画从窗口底部上移至居中位置，关闭时沿相同路径下移，参考 [Apple 默认 coverVertical 转场](https://developer.apple.com/documentation/uikit/uimodaltransitionstyle/coververtical)。面板使用内部高度引擎的可中断弹簧，保留当前位置与速度，正文尺寸在动画中保持稳定；遮罩仅进行独立透明度过渡。弹簧参数为 SaltUI 的适配值，未将其标注为 iOS 27 的私有系统曲线。

在 Bottom 模式且 `dragEnabled` 与 `dismissOnDrag` 均为 true 时，向下拖动可以越过最小停靠点直至面板完全移出屏幕。低于最小高度后保持内容尺寸并移动整个面板，避免正文继续缩小。释放时发起关闭请求；调用方保留 `visible = true` 时恢复原停靠点。

同一个所属窗口最多一个 Sheet 的 `visible` 为 true。可以在同一次业务更新中将旧 Sheet 设为 false、新 Sheet 设为 true，两个面板的退场和展开动画交叠运行。所属窗口的协调器只管理呈现、遮罩和输入，不表示导航栈；是否返回父页面以及何时结束流程由调用方决定。

## 草稿确认

在 `onDismissRequest` 中检查未保存内容，保持 visible 为 true，并在 Sheet 的正文组合内呈现普通确认对话框。继续编辑只关确认框；确认放弃后再将 visible 设为 false。正文内呈现确认框能让它成为当前 Sheet 的子模态。

## Sample 体验

桌面运行 `./gradlew.bat :desktopApp:run`，从首页进入 **Sheet**。Android 使用 `androidApp` 的 debug APK；iOS 使用现有 `iosApp` 工程。

页面提供四类内容：短内容动态增减、100 项播放队列、带放弃确认的草稿表单，以及水平 Pager、Slider、文本选择与普通按钮。内容、呈现方式和高度配置使用 ItemPopup 菜单选择；打开等列表操作使用 ItemButton，说明文字使用 ItemOuterTip；停靠点配置、action 按钮和普通滚动容器分别抽为私有函数，Lazy 播放队列保持独立。另有拖动、滚动优先、标题、操作区、长文案、关闭许可、忽略关闭请求和草稿保护开关。忽略关闭请求会保持面板显示，与隐藏后的状态保留无关。

默认根据所属窗口宽、高选择呈现方式：宽度至少为 600 dp 且高度至少为 480 dp 时使用 Floating，其余使用 Bottom，不按手机或平板分类。判断使用扣除系统安全区与键盘 inset 之前的窗口尺寸。体验拖动可选择 **Bottom** 和 **Medium / Large**。短内容和表单内提供显示期间的关闭开关、程序关闭及退场途中重开。完整关闭后重新打开队列应回到顶部，选中曲目也应重置。

**Return to a parent sheet** 示例演示临时隐藏：修改歌曲菜单的 Favorite 或 Note，选择停靠高度，并在较小高度下滚动，再进入歌曲信息。返回后恢复父菜单的可保存状态与高度；Done 结束整个流程，下次打开使用初始状态。两者切换仍完整运行各自的展开与关闭动画，没有额外的导航框架。

自适应判断共用 `salt-adaptive-window` 的窗口信息，ui2 已通过 API 依赖提供该模块。`LocalSaltAdaptiveWindow.current` 自动读取当前原生窗口，应用入口和主题不需要手动添加 Provider；Sheet 通过内部宿主参数使用所属窗口的信息，不覆盖正文中的窗口 Local。旋转、分屏和 Desktop 多窗口分别跟随自己的窗口尺寸，在 Sheet 内创建的新原生窗口也自动使用自身信息；安全区与键盘仍由 Sheet 宿主负责。受控预览与兼容说明见[自适应窗口](adaptive-window.md)。

## 兼容与验证范围

旧 `BottomSheetScaffold`、`BottomSheetDialog` 保持原接口，不需要迁移。Sheet 不依赖 SwiftUI 运行时或 commonMain 的 Material 3 组件，`SheetState` 仅用于观察呈现生命周期。

`SheetProperties(dismissible = ...)` 和 `copy(dismissible = ...)` 继续统一设置关闭权限，原有位置参数构造与 `copy(displayMode, dismissible, ...)` 均保持该行为。原有 JVM 构造函数、默认参数桥、属性 getter、`copy` 与 `copy` 默认参数入口保留，已使用旧版编译的客户端直接加载新版 Desktop JAR 验证链接与统一关闭配置的行为。旧二进制的统一关闭入口不能表达混合配置：即使只执行 `copy(displayMode = ...)`，只要传入配置有任一关闭入口被禁用，返回配置的四个关闭入口都会禁用。需要读取或保留各入口不同权限的客户端应使用新的独立开关并重新编译。

关闭配置已通过 Windows 原生窗口中的鼠标、键盘与触摸交互回归，覆盖独立入口禁用、动态配置、遮罩输入拦截，以及自适应与拖拽回归。Android Sample APK 和测试 APK 构建通过，Android 真机完整交互尚未完成；未验证 iOS 编译、Xcode 链接或原生 UI。构建与兼容检查不代替对应平台的运行验收。

## 审查修复

显隐意图与停靠点目标分别保存，窗口、键盘和内容的几何更新不能把 Hidden 目标改为展开。Density 与字体缩放变化只更新换算和布局，不重建过渡状态；几何变化时保留动画位置与速度。多停靠点快速释放按当前位置和速度方向选择目标，支持一次拖动跨越多个高度。所属窗口的呈现占用随宿主离开组合释放，不依赖可取消的下一帧等待。

正文使用明确的 Inset / EdgeToEdge 配置，边距 getter 不再注册生命周期或修改视口。测量阶段先读取当前 inset、测量 action，再向正文提供不可变 PaddingValues；对称浮动 inset 同样延迟到布局阶段读取。退场内容从无障碍树中移除，Android 同时隐藏平台宿主的无障碍后代。浅色 Bottom 面板覆盖底部导航栏时使用深色图标；侧边导航栏位于遮罩区域时使用浅色图标。浮动面板沿用所属窗口的导航栏样式。

Desktop 默认同画布后端保留退场绘制并立即释放交互；启动时设置 compose.layers.type 为 COMPONENT 或 WINDOW 的原生后端在关闭时立即移除 Popup，避免原生绘制区域继续拦截背景输入。这两种后端不保留视觉退场。iOS 保留 Popup 退场绘制、释放交互并移除退场语义。平台行为仍由用户进行 UI 验收，源码编译不能代替运行验证。
