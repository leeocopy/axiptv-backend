# Project Instructions — IPTV Xtream Android TV App

## ⚠️ ABSOLUTE RULE — Single Source of Truth

**`skill_2.md` is the ONLY source of truth for UI, flow, style, navigation, and architecture.**
**`skill_index.md` is the enforced internal checklist derived from it.**

> If anything in a response conflicts with `skill_2.md`, **`skill_2.md` wins — always.**
> Do not ask the user to paste the skill file again. Read it from the workspace.

---

## 🔗 Key References (read before any implementation)

| File | Purpose |
|---|---|
| [`skill_2.md`](../skill_2.md) | Full specification — 859 lines, all sections |
| [`.agent/skill_index.md`](skill_index.md) | Condensed checklist / quick-access index |

---

## Platform

- **Native Android · Kotlin · Jetpack Compose · LANDSCAPE ONLY**
- Architecture: **MVVM · Room · DataStore · EncryptedSharedPreferences · ExoPlayer · Jetpack Navigation**

---

## Non-Negotiable Rules (enforced every response)

1. **Colors:** Use ONLY tokens from `skill_2.md` § 1.1. No plain red/blue/green.
2. **Gradients:** Use ONLY gradients from § 1.2 (GradientPrimary, GradientCategory, etc.).
3. **Typography:** Use ONLY `typo-*` tokens from § 1.3.
4. **Spacing:** Use ONLY `spacing-*` scale from § 1.4.
5. **Corner radius:** Use ONLY `radius-*` tokens from § 1.5.
6. **TV focus:** EVERY focusable element MUST have scale+glow+border focus state (§ 2.1).
7. **TV safe margins:** ALWAYS apply 48dp horizontal, 27dp vertical on full-screen layouts (§ 2.3).
8. **Orientation:** ALWAYS landscape — NEVER portrait on any screen.
9. **Passwords:** ALWAYS in `EncryptedSharedPreferences` ONLY — NEVER plain text.
10. **Package structure:** ALWAYS follow the exact structure in § 4.
11. **Navigation:** ALWAYS use sealed `Screen` class routes defined in § 9.
12. **Acceptance criteria:** ALL 10 criteria in § 10 must pass before any screen is considered done.

---

## Quick-Access Index

For per-screen checklists, component signatures, storage keys, animations, and error handling:
→ **Read `.agent/skill_index.md`**
