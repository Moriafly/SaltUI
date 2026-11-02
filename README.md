<div align="center">

<img src="img/saltui-logo.svg" width="72" height="81" alt="SaltUI logo" />

# SaltUI

**UI components for Compose Multiplatform: Android, Desktop, iOS**

[![stable](https://img.shields.io/github/v/release/Moriafly/SaltUI?sort=semver&display_name=release&label=stable&color=brightgreen)](https://github.com/Moriafly/SaltUI/releases/latest)
[![docs](https://img.shields.io/badge/docs-moriafly.com%2Fprogram%2Fsalt--ui-blue)](https://moriafly.com/program/salt-ui)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.moriafly/salt-ui)](https://search.maven.org/search?q=g:io.github.moriafly)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE)
[![CodeFactor](https://www.codefactor.io/repository/github/moriafly/saltui/badge/main)](https://www.codefactor.io/repository/github/moriafly/saltui/overview/main)

</div>

SaltUI is a UI component library built on [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform). The 1.0 version was derived from parts of [Salt Player](https://github.com/Moriafly/SaltPlayerSource); today SaltUI powers Salt Player, Emo Scroll, Qinalt and other apps used by millions of people.

It ships the pieces Compose Multiplatform leaves to you: a themed component set, adaptive window plumbing that follows Material 3 breakpoints, Navigation 3 integration with a forward stack, and window decoration for desktop.

## Documentation

**The full documentation is at [moriafly.com/program/salt-ui](https://moriafly.com/program/salt-ui).** It covers installation, theme, components, material, Sheet, navigation, adaptive window and platform setup, and tracks the current source rather than the latest release, so it also documents APIs marked unstable and some APIs not yet shipped. For the version you actually depend on, check the [compatibility table](#compatibility) and the [releases](https://github.com/Moriafly/SaltUI/releases).

This README gets you to a running screen; the site is the reference for everything else.

## What's new in 3.0.0

- **`salt-adaptive-window`** — a new module. `LocalSaltAdaptiveWindow` reports the device form factor (Phone / Tablet / Foldable / Desktop / Car) and the Material 3 width size class, stays reactive on resize, rotation, and moving between displays, and needs no provider at the window root.
- **New `Sheet`** — a detent-capable bottom sheet that turns into a floating panel when the owning window is at least 600 dp wide and 480 dp tall (`SheetDisplayMode.Auto`), adapting as the window is resized or rotated. It also exposes independent dismissal entry points, a presentation lifecycle state, and explicit state retention for hidden sheets.
- **Navigation 3 integration** — `salt-ui-navigation` adds `SaltNavigator` with a back stack *and* a forward stack, so browser-style forward navigation works out of the box.
- **Desktop input and window decoration** — multi-finger caption bar dragging on Windows touch screens, themed resize cursors and window shadows on Linux, and correct undecorated window resizing.
- **Sample showcase** — the demo app now covers every component and every adaptive layout, on all targets.

### Migrating from 2.x

- `saltConfigs()` is deprecated. Use `SaltConfigs.default(isDarkTheme = …)` instead.
- If you were reading window metrics by hand to pick a layout, switch to `LocalSaltAdaptiveWindow.current.sizeClass` and drop the hardcoded breakpoints.

See the [releases](https://github.com/Moriafly/SaltUI/releases) for the complete list.

## Artifacts

All artifacts are published to Maven Central under `io.github.moriafly`. **Keep every SaltUI artifact on the same version** — they are released together and are not tested against each other across versions.

| Artifact | Purpose | Android minSdk |
| --- | --- | --- |
| `salt-ui` | The component and theme library | 23 |
| `salt-ui-navigation` | Navigation 3 integration (`SaltNavigator`) | 24 |
| `salt-adaptive-window` | Adaptive window state, size classes, form factors | 23 |
| `salt-core` | Shared primitives used by the modules above | 23 |

`salt-ui` depends on `salt-core` and `salt-adaptive-window` through `api`, so a normal app only adds `salt-ui`.

## Requirements

| Requirement | Version |
| --- | --- |
| Kotlin | 2.4.20 |
| Compose Multiplatform | 1.12.2 |
| Android Gradle Plugin | 9.1.1 |
| JDK | 21 |

Targets: Android (`minSdk` 23), JVM desktop (Windows / macOS / Linux), iOS (`iosArm64`, `iosSimulatorArm64`).

## Get started

```kotlin
commonMain.dependencies {
    implementation("io.github.moriafly:salt-ui:3.0.0")

    // Optional Navigation 3 integration
    implementation("io.github.moriafly:salt-ui-navigation:3.0.0")
}
```

`salt-ui` already exposes `salt-core` and `salt-adaptive-window` through `api`, so do not add them separately unless you want one of them without the component library. Version list: [Maven Central](https://central.sonatype.com/artifact/io.github.moriafly/salt-ui), [releases](https://github.com/Moriafly/SaltUI/releases).

Wrap your content in `SaltTheme`:

```kotlin
@Composable
fun App() {
    // Pass your own state; SaltUI does not observe the system theme for you.
    SaltTheme(
        configs = SaltConfigs.default(
            isDarkTheme = isSystemInDarkTheme()
        )
    ) {
        // ...
    }
}
```

Prefer the defaults? `SaltTheme { }` already works — `configs` defaults to `SaltConfigs.default()`.

## Adaptive window

Window state comes from the current native window, so there is no provider to install. Reading it needs `@OptIn(UnstableSaltAdaptiveWindowApi::class)`:

```kotlin
@OptIn(UnstableSaltAdaptiveWindowApi::class)
@Composable
fun Content() {
    val window = LocalSaltAdaptiveWindow.current

    if (window.sizeClass == WindowSizeClass.Compact) {
        CompactLayout()
    } else {
        // window.formFactor tells Phone / Tablet / Foldable / Desktop / Car
        ExpandedLayout()
    }
}
```

Use `sizeClass` for layout decisions and `formFactor` only for choices that genuinely depend on the hardware, such as touch versus pointer interaction. A tablet in split-screen is still `Compact`. Use `ProvideSaltAdaptiveWindow` only when you need a controlled value, such as a preview or a test fixture.

## Navigation 3

Register your `NavKey` subclasses once, then create the navigator:

```kotlin
private val navigatorConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenRoute.Main::class, ScreenRoute.Main.serializer())
            subclass(ScreenRoute.Detail::class, ScreenRoute.Detail.serializer())
        }
    }
}

@Composable
fun App() {
    val navigator = rememberSaltNavigator(
        configuration = navigatorConfig,
        initRoute = ScreenRoute.Main,
        topLevelRoutes = setOf(ScreenRoute.Main)
    )

    NavDisplay(
        backStack = navigator.navBackStack,
        onBack = navigator::back,
        entryProvider = entryProvider {
            entry<ScreenRoute.Main> { MainScreen() }
            entry<ScreenRoute.Detail> { DetailScreen() }
        }
    )
}
```

Navigating to a route in `topLevelRoutes` collapses the back stack to a single element, matching bottom-bar or sidebar behaviour. `navForwardStack` is saved across configuration changes, so `navigator.forward()` — and a forward button that reflects `navForwardStack.isNotEmpty()` — works like a browser.

## Sample

The demo app lives in [`shared`](https://github.com/Moriafly/SaltUI/tree/main/shared) and runs on every target:

```bash
./gradlew :desktopApp:run            # desktop
./gradlew :androidApp:installDebug   # Android device or emulator
```

## Text Description and Translation Standards

See: [SaltUI Text Description and Translation Standards](https://moriafly.com/standards/tdts).

## Google Play

SaltUI performs operations related to internal ART APIs. Please refer to the following solution for publishing on the Google Play platform.

> Google Play doesn't allow apps to use hidden APIs, reporting library usage will cause your app to fail app review, you need to disable dependencies info reporting in build.gradle. Remember to update this library to latest version to be compatible with new Android version.
>
> Quoted from **AndroidHiddenApiBypass**
>
> ```kotlin
>  android {
>      dependenciesInfo {
>          includeInApk = false
>          includeInBundle = false
>      }
>  }
>  ```

## Compatibility

| SaltUI | Compose Multiplatform | Jetpack Compose |
| --- | --- | --- |
| 3.0.0+ | 1.12.2 | 1.12.2 |
| 3.0.0-beta03+ | 1.12.2 | 1.12.2 |
| 3.0.0-beta02+ | 1.12.1 | 1.12.1 |
| 3.0.0-beta01+ | 1.12.0 | 1.12.0 |
| 3.0.0-alpha02+ | 1.12.0-rc01 | 1.12.0-rc01 |
| 3.0.0-alpha01+ | 1.12.0-alpha01 | 1.12.0-alpha02 |
| 2.9.0-beta02+ | 1.11.0-beta02 | 1.11.0-beta02 |

<details>
<summary>Older versions (2.9.0-beta01 and below)</summary>

| SaltUI | Compose Multiplatform | Jetpack Compose |
| --- | --- | --- |
| 2.9.0-beta01+ | 1.11.0-beta01 | 1.11.0-beta01 |
| 2.9.0-alpha08+ | 1.11.0-alpha04 | 1.11.0-alpha06 |
| 2.9.0-alpha07+ | 1.11.0-alpha03 | 1.11.0-alpha05 |
| 2.9.0-alpha05+ | 1.11.0-alpha02 | 1.11.0-alpha03 |
| 2.9.0-alpha01+ | 1.11.0-alpha01 | 1.11.0-alpha01 |
| 2.8.1+ | 1.10.0-rc02 | 1.10.0 |
| 2.8.0+ | 1.10.0-rc01 | 1.10.0-rc01 |
| 2.8.0-rc02+ | 1.10.0-beta02 | 1.10.0-beta02 |
| 2.8.0-beta01+ | 1.10.0-beta01 | 1.10.0-beta01 |
| 2.8.0-alpha09+ | 1.10.0-alpha03 | 1.10.0-alpha05 |
| 2.8.0-alpha01+ | 1.10.0-alpha02 | 1.10.0-alpha04 |
| 2.7.0-alpha01+ | 1.10.0-alpha01 | 1.10.0-alpha02 |
| 2.6.0-beta02+ | 1.9.0-rc01 | 1.9.0 |
| 2.6.0-beta01+ | 1.9.0-beta03 | 1.9.0-rc01 |
| 2.5.0-alpha05+ | 1.8.2 | 1.8.2 |
| 2.4.0+ | 1.8.0 | 1.8.0 |
| 2.3.1+ | 1.7.3 | 1.7.6 |
| 2.3.0-alpha02+ | 1.7.1 | 1.7.5 |
| 2.2.0+ | 1.7.0 | 1.7.1 |
| 2.2.0-beta01+ | 1.7.0-rc01 | 1.7.0 |
| 2.2.0-alpha01+ | 1.7.0-beta02 | |
| 2.0.7+ | 1.7.0-alpha03 | |
| 2.0.4+ | 1.7.0-alpha02 | |
| < 2.0.4 | 1.6.11 | |

</details>

## License

Apache License 2.0 — see [LICENSE](LICENSE).

## Contribute

See [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).

## Thanks

- [AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass)
- [compose-native-look](https://github.com/ComposeNativeLook/compose-native-look)
- [compose-fluent-ui](https://github.com/compose-fluent/compose-fluent-ui)
- [haze](https://github.com/chrisbanes/haze)
- [Switcher](https://github.com/bitvale/Switcher)
