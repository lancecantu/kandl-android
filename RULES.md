# Kandl Android Rules

Rules that apply to all code in this project. Add new rules under the
appropriate section; create a new section if none fits.

Paths below are relative to `app/src/main/java/com/lancedcan/kandl_android/`.

## Design System

The design system lives in `designsystem/`: `Colors.kt` (palette), `Type.kt`
(font scale), `Tokens.kt` (spacing, radii, sizes), `Theme.kt` (wires the
palette into `MaterialTheme`), and shared building blocks in
`designsystem/components/`.

1. Always build UI out of the design system. Colors, fonts, spacing and sizes
   come from its tokens, and anything that has a template component — buttons,
   list items, icons, and so on — uses that component instead of a one-off.
   `Tokens.kt` is the only file in the app allowed to contain a raw `dp`
   literal, and `Type.kt` the only one allowed a raw `sp` literal.
2. If the design system is missing something you need, add it there first and
   then use it. Don't define a local color, text style, spacing value, or a
   near-copy of an existing component next to the code that needs it.
3. **Colors** — read them from `MaterialTheme.colorScheme`. Every role the app
   uses is defined for both light and dark in `Theme.kt`, so a role is always
   preferable to naming a palette value directly. Reach into `Colors.kt` only
   for something the scheme genuinely can't express (for example, text on a
   fixed-colour badge). New colors get added to `Colors.kt` and mapped to a
   role in `Theme.kt`.
4. **Text** — there are exactly five sizes, defined as `FontSize.xs` (12),
   `.s` (14), `.m` (16), `.l` (22) and `.xl` (28). Every Material role is
   backed by one of them, so use `MaterialTheme.typography.*` and the text
   stays on the scale. Italicize or bold where it seems appropriate. Do not
   introduce a sixth size — snap to the nearest existing one.
5. **Spacing** — all padding, gaps and insets come from `Spacing`:
   `s1` 2, `s2` 4, `s3` 8, `s4` 12, `s5` 16, `s6` 24, `s7` 36, `s8` 48.
   Values outside the scale get snapped to the nearest step; genuinely larger
   insets are composed from steps (`Spacing.s8 + Spacing.s7`).
6. **Everything else dimensional** — corner radii come from `CornerRadius` or
   the ready-made `KandlShapes`, icon sizes from `IconSize`, hit targets from
   `TouchTarget`, and a component's own intrinsic size (a row height, a
   minimum text-area height) from `ComponentSize`.

## Code Standards

1. When a complex component is repeated on a screen, extract it into its own
   file under `components/`. Put it in `designsystem/components/` instead when
   it is generic enough to be reused across screens.
2. Rarely add comments. Only comment something novel or a genuinely complex
   chunk of code.

## Execution Guidelines

1. NEVER run `gradlew` or any compilation command to vet changes. Lance decides
   when to build and debugs himself.
