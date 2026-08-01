# 2.0.0-dev01

SaltUI 2.0 is now available!

- `TextDp.kt`, `Shadow.kt` is removed.
- `DynamicSaltTheme` in `SaltTheme.kt` is removed.
- `Modifier.fadeClickable` in `Clickable.kt` is removed.
- Redesigned `Button` around `ButtonAppearance`, `ButtonIntent`, and `ControlSize`, with
  platform-specific metrics for Android, iOS, and Desktop.
- Deprecated `ButtonType` and the `Button(..., type = ...)` overload. Migrate `Highlight` to
  `ButtonAppearance.Filled` and choose `Outlined` or `Plain` for former `Sub` actions.
- String-label buttons now default to one line. Use `maxLines` for compatibility or the content-slot
  overload for custom layouts.
- Button content now inherits `SaltTheme.textStyles.main`; platform button metrics only define
  control geometry such as height, padding, and icon size.
- Dialog cancel actions now use `ButtonAppearance.Plain`, while confirmation actions remain filled.

___
