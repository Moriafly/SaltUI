[![stable](https://img.shields.io/github/v/release/Moriafly/SaltUI?sort=semver&display_name=release&label=stable&color=brightgreen)](https://github.com/Moriafly/SaltUI/releases/latest) [![Maven Central](https://img.shields.io/maven-central/v/io.github.moriafly/salt-ui)](https://search.maven.org/search?q=g:io.github.moriafly) [![CodeFactor](https://www.codefactor.io/repository/github/moriafly/saltui/badge/main)](https://www.codefactor.io/repository/github/moriafly/saltui/overview/main)

# SaltUI

SaltUI is UI components based on [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform). The 1.0 version is derived from some UI components of [Salt Player](https://github.com/Moriafly/SaltPlayerSource). Currently, SaltUI is used in Salt Player, Emo Scroll, Qinalt and other App to serve hundreds of thousands of users.

## Get started

Add dependency:

```kotlin
commonMain.dependencies {
    implementation("io.github.moriafly:salt-ui:3.0.0-beta03")

    // Optional Navigation 3 integration
    implementation("io.github.moriafly:salt-ui-navigation:3.0.0-beta03")
}
```

SaltUI requires Android minSdk 23; the optional Navigation 3 integration requires minSdk 24.

Simple start:

```kotlin
@Composable
fun App() {
    SaltTheme(
        configs = saltConfigs()
    ) {
        // ...
    }
}
```

See demo: [shared](https://github.com/Moriafly/SaltUI/tree/main/shared).

## Static material backgrounds

`MaterialSource` defaults to `MaterialSourceMode.Dynamic`. For a wallpaper or another visually static
source, opt into caching with a content version:

```kotlin
@OptIn(UnstableSaltUiApi::class)
@Composable
fun WallpaperBackground(wallpaperVersion: Any) {
    MaterialSource(
        mode = MaterialSourceMode.Static(key = wallpaperVersion)
    ) {
        // Draw the wallpaper here.
    }
}
```

Use an immutable key with stable equality. Keep it equal while the source content is unchanged;
change it when the wallpaper changes or finishes loading. Use `Unit` for content without external
changes. Sizes, density, layout direction, dark/light mode, and material styles are tracked internally.
Pixels are shared by consumers using the same style; source content and foreground UI compose normally.

Prefer wallpaper images or fixed drawing colors for a static source. Do not use changing theme
colors such as `SaltTheme.colors` in static source content without including their resolved values
in the content key. Fixed Compose `Color(...)` values are safe. The cache does not observe theme
palette, custom `CompositionLocal`, text style, or shape reads inside source content. For example:

```kotlin
val sourceColor = SaltTheme.colors.highlight
MaterialSource(mode = MaterialSourceMode.Static(key = sourceColor)) {
    Box(Modifier.fillMaxSize().background(sourceColor))
}
```

When a source depends on several inputs, combine their resolved values in an immutable content key.
Use `Dynamic` for continuously changing content.

The cache stores ARGB pixels at half the source width and height, preserving alpha and color depth.
A 1080 × 2280 source uses approximately 2.35 MiB per style, or 4.70 MiB for two styles. This is bitmap
storage, excluding temporary rendering resources and driver copies. A source reserves at most
16 MiB of bitmap storage; additional styles or allocation failures use live rendering. After the last
consumer leaves, at most one unused image remains for reentry. Changing the source key, environment,
or size invalidates it; admitting a new style can evict the unused image.

Initial frames use live rendering. Baking starts after the size stays unchanged for two frames plus
80 ms, so resizing does not repeatedly capture intermediate sizes. Baking covers the whole source,
even for a small consumer. Small glass or acrylic panels may save little GPU work; measure the actual
screen before opting in. Mica and large or shared surfaces can have substantially different costs.

Caching is an optimization and may fall back to dynamic rendering. The current cache path supports
a single source per theme and consumers contained within it with the same scale and orientation.
Multiple sources, other transforms, inherited custom Haze styles, and unsupported readback paths use
the live pipeline.

To measure the tradeoff on a connected Android 12+ device, run:

```powershell
./scripts/benchmark-material-cache.ps1 -Device <adb-device-serial>
```

The script builds a non-debuggable benchmark variant and measures moving small, large, and shared
material surfaces in fresh processes. It exports raw steady-state and baking frame metrics, process
PSS, bitmap allocations, cache readiness and readback latency, APK hashes, source revision, and
repeated Dynamic/Static comparisons under
`androidApp/build/reports/material-cache-performance`. Emulator graphics memory accounting can be
incomplete; bitmap allocation and process PSS are reported separately.

Use `-TargetApk <saved-apk>` to measure a previous build with the same instrumentation. Then use
`-BaselineReport <previous-report-directory>` when measuring the current build to export a comparison
report. Comparisons require matching instrumentation APK hashes.

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

| SaltUI         | Compose Multiplatform | Jetpack Compose |
|----------------|-----------------------|-----------------|
| 3.0.0-beta03+  | 1.12.2                | 1.12.2          |
| 3.0.0-beta02+  | 1.12.1                | 1.12.1          |
| 3.0.0-beta01+  | 1.12.0                | 1.12.0          |
| 3.0.0-alpha02+ | 1.12.0-rc01           | 1.12.0-rc01     |
| 3.0.0-alpha01+ | 1.12.0-alpha01        | 1.12.0-alpha02  |
| 2.9.0-beta02+  | 1.11.0-beta02         | 1.11.0-beta02   |
| 2.9.0-beta01+  | 1.11.0-beta01         | 1.11.0-beta01   |
| 2.9.0-alpha08+ | 1.11.0-alpha04        | 1.11.0-alpha06  |
| 2.9.0-alpha07+ | 1.11.0-alpha03        | 1.11.0-alpha05  |
| 2.9.0-alpha05+ | 1.11.0-alpha02        | 1.11.0-alpha03  |
| 2.9.0-alpha01+ | 1.11.0-alpha01        | 1.11.0-alpha01  |
| 2.8.1+         | 1.10.0-rc02           | 1.10.0          |
| 2.8.0+         | 1.10.0-rc01           | 1.10.0-rc01     |
| 2.8.0-rc02+    | 1.10.0-beta02         | 1.10.0-beta02   |
| 2.8.0-beta01+  | 1.10.0-beta01         | 1.10.0-beta01   |
| 2.8.0-alpha09+ | 1.10.0-alpha03        | 1.10.0-alpha05  |
| 2.8.0-alpha01+ | 1.10.0-alpha02        | 1.10.0-alpha04  |
| 2.7.0-alpha01+ | 1.10.0-alpha01        | 1.10.0-alpha02  |
| 2.6.0-beta02+  | 1.9.0-rc01            | 1.9.0           |
| 2.6.0-beta01+  | 1.9.0-beta03          | 1.9.0-rc01      |
| 2.5.0-alpha05+ | 1.8.2                 | 1.8.2           |
| 2.4.0+         | 1.8.0                 | 1.8.0           |
| 2.3.1+         | 1.7.3                 | 1.7.6           |
| 2.3.0-alpha02+ | 1.7.1                 | 1.7.5           |
| 2.2.0+         | 1.7.0                 | 1.7.1           |
| 2.2.0-beta01+  | 1.7.0-rc01            | 1.7.0           |
| 2.2.0-alpha01+ | 1.7.0-beta02          |                 |
| 2.0.7+         | 1.7.0-alpha03         |                 |
| 2.0.4+         | 1.7.0-alpha02         |                 |
| < 2.0.4        | 1.6.11                |                 |

## License

See [LICENSE](LICENSE).

## Contribute

See [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md).

## Thanks

- [AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass)
- [compose-native-look](https://github.com/ComposeNativeLook/compose-native-look)
- [compose-fluent-ui](https://github.com/compose-fluent/compose-fluent-ui)
- [haze](https://github.com/chrisbanes/haze)
- [Switcher](https://github.com/bitvale/Switcher)
