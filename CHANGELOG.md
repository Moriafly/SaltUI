# 2.0.0-dev01

SaltUI 2.0 is now available!

- `TextDp.kt`, `Shadow.kt` is removed.
- `DynamicSaltTheme` in `SaltTheme.kt` is removed.
- `Modifier.fadeClickable` in `Clickable.kt` is removed.
- Redesigned `Button` around `ButtonAppearance`, `ButtonIntent`, and `ControlSize`, with
  platform-specific metrics for Android, iOS, and Desktop.
- Removed `ButtonType` and the `Button(..., type = ...)` overload. Migrate `Highlight` to
  `ButtonAppearance.Filled` and `Sub` to `ButtonAppearance.Subtle`.
- Replaced the provisional `ButtonAppearance.Outlined` and `ButtonDefaults.border()` APIs with the
  implementation-independent `ButtonAppearance.Subtle`. Custom outlines belong on `BasicButton`.
- String-label buttons now default to one line. Use `maxLines` for compatibility or the content-slot
  overload for custom layouts.
- Button content now inherits `SaltTheme.textStyles.main`; platform button metrics only define
  control geometry such as height, padding, and icon size.
- Dialog cancel actions reuse `ButtonAppearance.Subtle`; confirmation actions remain filled.
- Dialogs now use platform-resolved continuous shapes, spacing, unclipped dual-layer drop shadows,
  control density, scroll-safe content, and adaptive action layouts. Alert dialog titles and
  messages inherit `SaltTheme` typography, and default action labels are no longer forced to
  uppercase.
- `AdaptiveDialogSize.Min` resolves to a maximum width of 260 dp on Desktop and 320 dp on Android
  and iOS. Compact alerts use equal-width horizontal actions, stacking only when available width or
  accessibility font scale requires it.
- `YesDialog`, `YesNoDialog`, and `InputDialog` now use `BasicAdaptiveDialog`; `BasicDialog` uses
  the standard adaptive width internally.

___
