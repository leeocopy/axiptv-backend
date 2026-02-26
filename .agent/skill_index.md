# skill_2.md — Internal Checklist & Index
> **This is the ONLY source of truth. If anything conflicts with this file, this file wins.**
> Source: `skill_2.md` | Platform: Native Android · Kotlin · Jetpack Compose · LANDSCAPE ONLY

---

## ✅ SECTION 1 — Design System Checklist

### 1.1 Color Palette (NEVER deviate)
| Token | Hex | Usage |
|---|---|---|
| `color-bg-primary` | `#1A1A2E` | Main screen background |
| `color-bg-surface` | `#22223A` | Cards, panels, sidebar |
| `color-bg-elevated` | `#2C2C48` | Dialogs, overlays |
| `color-accent-pink` | `#E91E8C` | Primary CTA, selected highlight, progress fill |
| `color-accent-purple` | `#9B27AF` | Secondary accent, gradient start |
| `color-accent-orange` | `#FF6B35` | Gradient end on hero buttons |
| `color-text-primary` | `#FFFFFF` | Headings, active labels |
| `color-text-secondary` | `#B0B0C8` | Subtitles, metadata |
| `color-text-muted` | `#606080` | Disabled, placeholder |
| `color-focus-glow` | `#E91E8C` | TV focus ring / glow color |
| `color-success` | `#4CAF50` | Activated state |
| `color-error` | `#F44336` | Invalid/expired state |
| `color-divider` | `#333355` | List separators |

### 1.2 Gradients
- **GradientPrimary** → `[#E91E8C → #FF6B35]` angle 0° — primary buttons
- **GradientCategory** → `[#9B27AF → #E91E8C]` angle 0° — category headers
- **GradientCardScrim** → `[Transparent → #000000CC]` angle 270° — home card overlay
- **GradientWave** → `[#9B27AF55 → #E91E8C99]` angle 0° — decorative

### 1.3 Typography
| Token | Size | Weight | Usage |
|---|---|---|---|
| `typo-display` | 32sp | Bold 700 | Page titles |
| `typo-headline` | 24sp | SemiBold 600 | Card titles |
| `typo-title` | 18sp | Medium 500 | Section headers |
| `typo-body` | 16sp | Regular 400 | List items |
| `typo-caption` | 14sp | Regular 400 | Metadata, EPG times |
| `typo-micro` | 12sp | Regular 400 | Channel numbers, badges |
| `typo-button` | 16sp | SemiBold 600 | Button labels |
| `typo-clock` | 14sp | Regular 400 | Time (monospace) |
- **Font:** Roboto (system) or Inter if bundled

### 1.4 Spacing Scale
```
xs=4dp  sm=8dp  md=16dp  lg=24dp  xl=32dp  2xl=48dp  3xl=64dp
```

### 1.5 Corner Radius
```
radius-sm=8dp   radius-md=12dp   radius-lg=16dp
radius-xl=24dp  radius-pill=50%
```

### 1.6 Shadows
- Card default: `offsetY=4, blur=16, color=#00000066`
- Focused card (TV): `blur=20, spread=4, color=#E91E8C99`
- Dialog: `offsetY=8, blur=32, color=#00000099`

### 1.7 Icons
- Style: Material Symbols Rounded (filled)
- Sizes: 20dp inline / 28dp nav+list / 40dp card badge
- Color: White default → `color-accent-pink` when active
- Background pill: `#FFFFFF22` circle

### 1.8 Buttons
- **PrimaryButton:** GradientPrimary bg, radius-pill, padding(14dp v, 32dp h), TV chevron hints, focus=scale(1.06)+glow
- **SecondaryButton:** `color-bg-surface` bg, 1.5dp `color-accent-pink` border, radius-pill
- **IconButton:** `color-bg-elevated` circle 44dp, focus → bg becomes `color-accent-pink`, scale(1.1)
- **TextButton:** No bg, `color-accent-pink` text, underline on focus

### 1.9 Inputs
- bg: `#FFFFFF` (white) or `color-bg-elevated` (dark variant)
- cornerRadius: `radius-pill`
- padding: 14dp vertical, 20dp horizontal
- border: none default → 2dp `color-accent-pink` on focus
- password: trailing eye toggle icon

---

## ✅ SECTION 2 — TV Focus System Checklist

### 2.1 Focus States (MANDATORY on every focusable element)
- **Card focus:** scale 1.08, border 2.5dp `color-focus-glow`, shadow `0 0 24dp 6dp #E91E8C88`, z-index elevated, 150ms ease-out
- **Button focus:** scale 1.06, glow shadow, bg brightened 10%
- **List item focus:** bg `color-accent-pink + 22 alpha`, left accent bar 3dp solid `color-accent-pink`
- **Input focus:** border 2dp `color-accent-pink`, subtle glow

### 2.2 D-Pad Keys
```
UP/DOWN     → move focus within panel/list
LEFT/RIGHT  → move between panels OR cycle categories
OK(Enter)   → select / confirm / toggle
BACK        → dismiss overlay → go back → exit confirmation
MENU        → context menu / overlay controls
FAST-FWD    → seek +30s
REWIND      → seek -10s
```

### 2.3 TV Safe Margins (MANDATORY on all full-screen layouts)
```kotlin
TV_SAFE_HORIZONTAL = 48.dp   // left + right
TV_SAFE_VERTICAL   = 27.dp   // top + bottom
```

### 2.4 Focus Memory
- Each page remembers last focused item via `rememberSaveable` or custom FocusManager
- On enter: restore last focused item or default to first

---

## ✅ SECTION 3 — Layout Rules Checklist
- [ ] Orientation locked: `android:screenOrientation="landscape"` in Manifest
- [ ] Baseline grid: 8dp
- [ ] Two-panel layout: sidebar ~280–320dp fixed, content fills rest
- [ ] Home grid: LazyVerticalGrid, 5 cols (TV) / 3 cols (phone landscape), gap=`spacing-md`
- [ ] Centered dialogs: max 560dp wide, centered
- [ ] All text: `textAlign=Start` (except titles and empty states)
- [ ] Header bar: 64dp height, time(left), logo+title(center), date(right), transparent bg

---

## ✅ SECTION 4 — Package Structure
```
com.yourapp.iptv/
├── data/local/db/          → AppDatabase, ProfileDao, entities/Profile
├── data/local/prefs/       → DataStoreManager, SecurePrefs
├── data/local/cache/       → EpgCache, VodCache
├── data/remote/            → XtreamApi, XtreamClient, dto/
├── data/repository/        → ProfileRepo, LiveRepo, VodRepo, SeriesRepo, EpgRepo, ActivationRepo
├── domain/model/           → Profile, Channel, EpgEntry, VodItem, Series, ActivationState
├── domain/usecase/         → profile/, live/, vod/, activation/
├── ui/theme/               → Color, Typography, Shape, Spacing, Theme
├── ui/components/          → FocusableCard, PrimaryButton, SecondaryButton, TvInput,
│                             CategoryPill, ChannelListItem, HomeGridCard, EpgRow,
│                             RatingBadge, AgeBadge, ToggleSwitch, SettingRow,
│                             TopBar, WaveformDecoration, LoadingOverlay
├── ui/navigation/          → AppNavGraph, Screen (sealed class)
├── ui/screens/             → splash/, activation/, profile/, home/, live/,
│                             player/, vod/, series/, search/, settings/
├── player/                 → PlayerManager (ExoPlayer wrapper), PlayerState
└── util/                   → Extensions, TimeFormatter, FocusUtils
```

---

## ✅ SECTION 5 — Navigation Graph

```
SplashScreen
 ├─[not activated]→ ActivationScreen → [success/trial] →┐
 └─[activated]─────────────────────────────────────────→ ProfilePickerScreen
                                                             │
                                                         HomeScreen (tabs)
                                                         ├── LiveTvScreen → PlayerScreen
                                                         ├── VodScreen → VodDetailScreen → PlayerScreen
                                                         ├── SeriesScreen → SeriesDetailScreen → EpisodeDetailScreen → PlayerScreen
                                                         ├── SearchScreen
                                                         └── SettingsScreen → ProfileManagerScreen → Add/EditProfileScreen
```
- **Nav implementation:** Jetpack Navigation Compose, `NavHost`
- **Auth flow:** `popUpTo + inclusive` so BACK from Home never returns to Activation

---

## ✅ SECTION 6 — Screens Checklist

### 6.1 Splash Screen
- [ ] Full dark bg (`color-bg-primary`)
- [ ] Center: Matrix hexagon "M" logo (pink→orange gradient, white "M")
- [ ] App name in `typo-display`
- [ ] Animated dot-pulse or linear progress bar (`color-accent-pink`)
- [ ] Version text bottom (`typo-micro color-text-muted`)
- [ ] Duration: 1.5–2.5s, auto-navigate, no user input

### 6.2 Activation Screen
- [ ] Full screen, centered card max 480dp
- [ ] Logo at top, login icon with decorative dots
- [ ] White pill input: "Enter your activation code"
- [ ] PrimaryButton with left/right chevrons: "Get Started"
- [ ] TextButton: "Start Free Trial"
- [ ] States: Idle / Loading / TrialActive / Activated / InvalidCode / NoInternet
- [ ] Trial: store `trialStartMs` in DataStore, allow 7 days
- [ ] DataStore keys: `IS_ACTIVATED`, `TRIAL_START_MS`, `LICENSE_EXPIRY_MS`

### 6.3 Profile Picker / Manager Screen
- [ ] Picker: grid 2–3 cols, avatar (initials circle gradient), name, host, last used
- [ ] "+ Add Profile" card: dashed border, plus icon
- [ ] Long-press: Edit/Delete overlay
- [ ] Manager: list view, chevron right per row, FAB "+"
- [ ] Form fields: Profile Name (opt), Host URL (req), Username (req), Password (req+eye)
- [ ] Buttons: Save (gradient), Cancel (secondary)
- [ ] Max 10 profiles — disable Add at 10, show tooltip
- [ ] Passwords in `EncryptedSharedPreferences` ONLY, key=`"pwd_${profileId}"`

### 6.4 Home Screen
- [ ] TopBar: time(left), MATRIX logo+icon(center), date(right)
- [ ] 2-row × 5-col LazyVerticalGrid of category cards
- [ ] D-pad hint legend at bottom
- [ ] HomeGridCard: full-bleed image, GradientCardScrim, icon badge + category name
- [ ] Category tints: Movies=Orange `#FF6B3566`, Live=Purple `#9B27AF66`, Fav=Blue `#1565C066`, Series=Gold `#F57F1766`, Music=Purple-pink `#AD1D7866`, Ent=Red-orange `#E5393566`, Radio=Amber `#F9A82566`, Devotional=Deep-purple `#4527A066`, Settings=Pink `#E91E8C66`
- [ ] Focus: scale 1.08, glow ring

### 6.5 Live TV Screen (two-panel)
- [ ] Left panel 280dp: CategoryPill(gradient, chevrons), LazyColumn ChannelListItems
- [ ] ChannelListItem: channel# `typo-micro muted`, name `typo-body`, selected=pink+left-bar
- [ ] Right panel: 16:9 preview, channel name `typo-headline pink`, star rating, age badge, logo
- [ ] EPG row: "Now: [name] | Next: [name]" or "No EPG data available"
- [ ] Sub-sections: All / Favourites / Recently Watched / Categories (L/R on pill)

### 6.6 Player Screen
- [ ] FULL SCREEN, ExoPlayer SurfaceView fills all
- [ ] Overlay auto-hides after 3s inactivity
- [ ] Overlay: bottom scrim, title(bottom-left), controls(bottom-center), Menu+Exit(bottom-right)
- [ ] Seek bar: pink fill, white thumb, time labels
- [ ] Remote: OK=toggle overlay, UP/DOWN=zap ch(live), L/R=seek±(VOD), BACK1=show overlay, BACK2=exit
- [ ] Resume: save `WatchHistory(contentId, type, positionMs, durationMs)`, show dialog if pos>30s

### 6.7 VOD / Movies Screen
- [ ] Same two-panel structure as Live
- [ ] Left: category + movie list (number+title)
- [ ] Right: poster(16:9 or 2:3), description max 4 lines, title `typo-headline pink`, rating, age badge
- [ ] Buttons: "Watch Trailer" (secondary) + "Watch Now" (primary gradient)

### 6.8 Series Screen
- [ ] Same two-panel structure
- [ ] Detail: poster+title+rating+description, season pill selector, episode LazyColumn
- [ ] Episode row: number, name, duration, progress bar (if partially watched)
- [ ] Per-episode contentId: `"series_{id}_s{season}_e{ep}"`

### 6.9 Search Screen
- [ ] Search input top, auto-focused
- [ ] Filter pills: All | Live | Movies | Series
- [ ] Results: LazyVerticalGrid (same card style)
- [ ] Empty state: centered icon + "No results for '[query]'"
- [ ] Debounce 300ms, search local cache

### 6.10 Settings Screen
- [ ] Left: "Setting Options" panel (white/gradient header, list rows)
- [ ] Right: Matrix hexagon logo decoration + floating orbs
- [ ] SettingRow: label(left) + value/control(right)
- [ ] Settings: Language, Subscription Info, Sync Data, Parental Control, Clear History, Sort Alpha (toggle), Auto Start (toggle), Player Settings, Manage Profiles, Clear Cache, Clear All Data, App Version
- [ ] Toggle: pill shape, `color-accent-purple` ON / `color-text-muted` OFF, spring animation
- [ ] Parental PIN: 4-digit, stored EncryptedSharedPrefs, required to access Settings if enabled

---

## ✅ SECTION 7 — Reusable Components Signatures

```kotlin
FocusableCard(onClick, modifier, content)
PrimaryButton(text, onClick, showArrows=false, enabled=true, loading=false)
CategoryPill(label, onPrevious, onNext)   // gradient bg + L/R chevrons
ChannelListItem(number, name, isSelected, isFocused, onClick)
HomeGridCard(title, icon, backgroundImage, tintColor, onClick, isFocused)
TopBar(title, icon, showClock=true)        // time(L), icon+title(C), date(R)
AgeBadge(rating: Int)                      // gradient circle badge
SettingRow(label, value?, control?, onClick?, isFocused)
```

---

## ✅ SECTION 8 — Local Storage Checklist

### Room DB Tables
```
Profile(id, name, host, username, createdAt, lastUsed)
WatchHistory(contentId, type, positionMs, durationMs, lastWatchedAt)
EpgCache(channelId, programTitle, startMs, endMs, cachedAt)
FavouriteChannel(channelId, type, addedAt)
```

### EncryptedSharedPreferences
```
"pwd_{profileId}"  → password (never plain text)
"parental_pin"     → 4-digit PIN
```

### DataStore
```
IS_ACTIVATED      : Boolean  (default false)
TRIAL_START_MS    : Long     (default 0)
LICENSE_EXPIRY_MS : Long     (default 0)
ACTIVE_PROFILE_ID : String
SORT_ALPHA        : Boolean
AUTO_START        : Boolean
LANGUAGE          : String   (default "en")
```

### Data Policy
- **All data stays on-device only** — no telemetry, no analytics calling home
- "Clear All Data" → clears Room + DataStore + EncryptedSharedPrefs → Activation screen

---

## ✅ SECTION 9 — Engineering & Dependencies

```kotlin
// TV Compose
"androidx.tv:tv-foundation:1.0.0-alpha10"
"androidx.tv:tv-material:1.0.0-alpha10"

// ExoPlayer
"androidx.media3:media3-exoplayer:1.3.0"
"androidx.media3:media3-exoplayer-hls:1.3.0"
"androidx.media3:media3-ui:1.3.0"
```

### Navigation Routes (sealed class Screen)
```
splash / activation / profile_picker / home / live_tv
player/{contentId}/{type} / vod_browse / vod_detail/{vodId}
series_browse / series_detail/{seriesId} / search / settings
profile_manager / add_profile / edit_profile/{profileId}
```

### Error Handling Quick-Ref
| Scenario | Response |
|---|---|
| Invalid credentials | Snackbar: "Could not connect. Check host/credentials." — profile NOT deleted |
| Network timeout | Retry button + show cached data |
| Stream fails | "Stream unavailable. Try another channel." overlay |
| Trial expired | Redirect to Activation: "Trial expired" |
| Max profiles (10) | Disable Add, tooltip: "Maximum 10 profiles reached" |
| EPG unavailable | "No EPG data available" — no crash |
| Empty category | "No channels in this category" empty state |

---

## ✅ SECTION 10 — Acceptance Criteria (all must pass)

- [ ] Max 10 profiles enforced at UI + repository level
- [ ] Profiles + watch history survive app close/reopen/reboot
- [ ] Passwords NEVER in plain text (only EncryptedSharedPreferences)
- [ ] App NEVER enters portrait orientation
- [ ] TV safe margins on ALL full-screen layouts (48dp H, 27dp V)
- [ ] Focus indicator ALWAYS visible on every focusable element
- [ ] BACK exits player overlay first, then player, never unexpected exit
- [ ] Trial start requires NO internet; activation code verifiable locally
- [ ] Profile switch clears in-memory cache + re-fetches from new host
- [ ] "Clear All Data" → factory state → Activation screen

---

## ✅ SECTION 11 — Animation Guidelines

| Element | Animation |
|---|---|
| Home card focus | `animateFloatAsState(1.08f)`, 150ms |
| Player overlay | `AnimatedVisibility(fadeIn + slideInVertically)` |
| Category change | Crossfade 200ms |
| Page transitions | `slideInHorizontally / slideOutHorizontally`, 250ms |
| Toggle switch | Spring animation on thumb |
| Splash logo | Scale `0.6f → 1.0f`, 500ms ease-out |
| Waveform (Radio) | Infinite looping translateX, 8s linear |

---

*End of index — generated from `skill_2.md`. This file is the enforced reference for every response.*
