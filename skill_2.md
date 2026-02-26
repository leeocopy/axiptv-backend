# IPTV Xtream — Android TV / Landscape UI Skill

> **Platform:** Native Android · Kotlin · Jetpack Compose · **LANDSCAPE ONLY**
> **Reference screenshots:** Matrix IPTV UI (dark, pink-purple gradient design system)

---

## 1. Design System

### 1.1 Color Palette

| Token | Hex | Usage |
|---|---|---|
| `color-bg-primary` | `#1A1A2E` | Main screen background |
| `color-bg-surface` | `#22223A` | Cards, panels, sidebar |
| `color-bg-elevated` | `#2C2C48` | Dialogs, overlays |
| `color-accent-pink` | `#E91E8C` | Primary CTA, selected item highlight, progress fill |
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

```
// Primary button gradient (left → right)
GradientPrimary = LinearGradient(
  colors = [#E91E8C, #FF6B35],
  angle = 0°
)

// Category header gradient
GradientCategory = LinearGradient(
  colors = [#9B27AF, #E91E8C],
  angle = 0°
)

// Home card overlay (bottom scrim)
GradientCardScrim = LinearGradient(
  colors = [Transparent, #000000CC],
  angle = 270°
)

// Audio waveform / decorative
GradientWave = LinearGradient(
  colors = [#9B27AF55, #E91E8C99],
  angle = 0°
)
```

### 1.3 Typography

| Style Token | Size (sp) | Weight | Usage |
|---|---|---|---|
| `typo-display` | 32 | Bold (700) | Page titles |
| `typo-headline` | 24 | SemiBold (600) | Card titles, selected channel |
| `typo-title` | 18 | Medium (500) | Section headers |
| `typo-body` | 16 | Regular (400) | List items, descriptions |
| `typo-caption` | 14 | Regular (400) | Metadata, EPG times |
| `typo-micro` | 12 | Regular (400) | Channel numbers, badges |
| `typo-button` | 16 | SemiBold (600) | Button labels |
| `typo-clock` | 14 | Regular (400) | Time display (monospace preferred) |

**Font:** System default (Roboto) or `Inter` if bundled.

### 1.4 Spacing Scale

```
spacing-xs  =  4 dp
spacing-sm  =  8 dp
spacing-md  = 16 dp
spacing-lg  = 24 dp
spacing-xl  = 32 dp
spacing-2xl = 48 dp
spacing-3xl = 64 dp
```

### 1.5 Corner Radius

| Token | Value | Usage |
|---|---|---|
| `radius-sm` | 8 dp | Inputs, small chips |
| `radius-md` | 12 dp | List items, setting rows |
| `radius-lg` | 16 dp | Cards, panels |
| `radius-xl` | 24 dp | Category header pill, home grid cards |
| `radius-pill` | 50% | Buttons (fully rounded), toggle, badges |

### 1.6 Shadows / Elevation

```kotlin
// Card default
BoxShadow(offsetY = 4, blur = 16, color = #00000066)

// Focused card (TV)
BoxShadow(offsetY = 0, blur = 20, spread = 4, color = color-focus-glow + 99)

// Dialog
BoxShadow(offsetY = 8, blur = 32, color = #00000099)
```

### 1.7 Icon Style

- **Style:** Filled rounded icons (Material Symbols Rounded or custom SVG)
- **Sizes:** 20 dp (inline), 28 dp (nav/list), 40 dp (card badge)
- **Color:** White by default; `color-accent-pink` when active/selected
- **Background pill:** Translucent white `#FFFFFF22` circle behind card icons

### 1.8 Button Styles

```
PrimaryButton:
  background    = GradientPrimary
  cornerRadius  = radius-pill
  padding       = (14 dp vertical, 32 dp horizontal)
  textStyle     = typo-button, color-text-primary
  focusState    = scale(1.06) + glow shadow color-focus-glow
  arrows        = left/right chevron icons inside button (TV hint)

SecondaryButton:
  background    = color-bg-surface
  border        = 1.5 dp color-accent-pink
  cornerRadius  = radius-pill
  textStyle     = typo-button, color-accent-pink

IconButton (TV remote hint):
  background    = color-bg-elevated (circle)
  size          = 44 dp
  icon          = 24 dp white
  focusState    = background → color-accent-pink, scale(1.1)

TextButton:
  no background
  textStyle     = typo-body, color-accent-pink
  underline on focus
```

### 1.9 Input / Text Field Style

```
Input:
  background    = #FFFFFF (white) OR color-bg-elevated (dark variant)
  cornerRadius  = radius-pill
  padding       = (14 dp vertical, 20 dp horizontal)
  textStyle     = typo-body, color-bg-primary (on white) / color-text-primary (on dark)
  placeholder   = color-text-muted
  border        = none default; 2 dp color-accent-pink on focus
  passwordField = trailing eye toggle icon
```

---

## 2. TV Focus System (D-Pad Navigation)

### 2.1 Focus State Rules

Every focusable element MUST have a distinct focus state:

```
Card Focus:
  - scale: 1.08
  - border: 2.5 dp solid color-focus-glow
  - shadow: 0 0 24dp 6dp #E91E8C88
  - z-index: elevated above siblings
  - transition: 150ms ease-out

Button Focus:
  - scale: 1.06
  - glow shadow (same as card)
  - background brightened by 10%

List Item Focus:
  - background: color-accent-pink + 22 alpha
  - left accent bar: 3 dp solid color-accent-pink
  - text: color-text-primary (brighten if needed)

Input Focus:
  - border: 2 dp color-accent-pink
  - subtle glow
```

### 2.2 D-Pad Navigation Rules

```
UP / DOWN    → move focus within current panel/list
LEFT / RIGHT → move focus between panels (sidebar ↔ content) or cycle categories
OK (Enter)   → select / confirm / toggle
BACK         → dismiss overlay → go back in nav stack → exit confirmation
MENU         → show context menu or overlay controls
FAST-FWD/REW → seek +30s / -10s in player
```

### 2.3 TV Safe Margins

```kotlin
// Apply to ALL full-screen layouts
val TV_SAFE_HORIZONTAL = 48.dp   // left + right
val TV_SAFE_VERTICAL   = 27.dp   // top + bottom
// This accounts for ~5% overscan on all sides
```

### 2.4 Focus Memory

- Each page remembers last focused item when returning (via `rememberSaveable` or custom FocusManager wrapper).
- On page enter, restore focus to last item or default to first item.

---

## 3. Layout Rules

- **Orientation:** `android:screenOrientation="landscape"` in Manifest + `ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE` enforced in `onCreate`.
- **Baseline grid:** 8 dp.
- **Two-panel layout** (sidebar + content): sidebar ~280–320 dp fixed width; content fills remainder.
- **Home grid:** `LazyVerticalGrid`, 5 columns (TV) / 3 columns (phone landscape), gap = `spacing-md`.
- **Centered dialogs/overlays:** max width 560 dp, horizontally centered.
- **All text:** `textAlign = Start` unless explicitly centered (titles, empty states).
- **Header bar:** fixed top, 64 dp height, left = time, center = logo+title, right = date. Background transparent over dark bg.

---

## 4. Package Structure

```
com.yourapp.iptv/
├── data/
│   ├── local/
│   │   ├── db/                   # Room DB
│   │   │   ├── AppDatabase.kt
│   │   │   ├── ProfileDao.kt
│   │   │   └── entities/Profile.kt
│   │   ├── prefs/
│   │   │   ├── DataStoreManager.kt   # trial/activation flags
│   │   │   └── SecurePrefs.kt        # EncryptedSharedPreferences (passwords)
│   │   └── cache/
│   │       ├── EpgCache.kt
│   │       └── VodCache.kt
│   ├── remote/
│   │   ├── XtreamApi.kt          # Retrofit interface
│   │   ├── XtreamClient.kt       # OkHttp + per-profile base URL
│   │   └── dto/                  # API response DTOs
│   └── repository/
│       ├── ProfileRepository.kt
│       ├── LiveRepository.kt
│       ├── VodRepository.kt
│       ├── SeriesRepository.kt
│       ├── EpgRepository.kt
│       └── ActivationRepository.kt
├── domain/
│   ├── model/                    # Pure Kotlin models
│   │   ├── Profile.kt
│   │   ├── Channel.kt
│   │   ├── EpgEntry.kt
│   │   ├── VodItem.kt
│   │   ├── Series.kt
│   │   └── ActivationState.kt
│   └── usecase/
│       ├── profile/
│       ├── live/
│       ├── vod/
│       └── activation/
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Typography.kt
│   │   ├── Shape.kt
│   │   ├── Spacing.kt
│   │   └── Theme.kt
│   ├── components/               # Reusable Compose components
│   │   ├── FocusableCard.kt
│   │   ├── PrimaryButton.kt
│   │   ├── SecondaryButton.kt
│   │   ├── TvInput.kt
│   │   ├── CategoryPill.kt
│   │   ├── ChannelListItem.kt
│   │   ├── HomeGridCard.kt
│   │   ├── EpgRow.kt
│   │   ├── RatingBadge.kt
│   │   ├── AgeBadge.kt
│   │   ├── ToggleSwitch.kt
│   │   ├── SettingRow.kt
│   │   ├── TopBar.kt
│   │   ├── WaveformDecoration.kt
│   │   └── LoadingOverlay.kt
│   ├── navigation/
│   │   ├── AppNavGraph.kt
│   │   └── Screen.kt             # sealed class / routes
│   └── screens/
│       ├── splash/
│       ├── activation/
│       ├── profile/
│       ├── home/
│       ├── live/
│       ├── player/
│       ├── vod/
│       ├── series/
│       ├── search/
│       └── settings/
├── player/
│   ├── PlayerManager.kt          # ExoPlayer wrapper
│   └── PlayerState.kt
└── util/
    ├── Extensions.kt
    ├── TimeFormatter.kt
    └── FocusUtils.kt
```

---

## 5. Navigation Graph

```
SplashScreen
    │
    ├──[not activated]──► ActivationScreen
    │                          │
    │                    [success / trial start]
    │                          │
    └──[activated]─────────────▼
                        ProfilePickerScreen
                               │
                         [profile selected]
                               │
                               ▼
                         HomeScreen (bottom/side tabs)
                         ├── LiveTvScreen
                         │       └── PlayerScreen (full-screen overlay)
                         ├── VodScreen
                         │       ├── VodDetailScreen
                         │       └── PlayerScreen
                         ├── SeriesScreen
                         │       ├── SeriesDetailScreen
                         │       │       └── EpisodeDetailScreen
                         │       └── PlayerScreen
                         ├── SearchScreen
                         └── SettingsScreen
                                 └── ProfileManagerScreen
                                         ├── AddProfileScreen
                                         └── EditProfileScreen
```

**Navigation implementation:** Jetpack Navigation Compose with `NavHost`. Use `popUpTo` + `inclusive` for auth flow so BACK from Home doesn't return to Activation.

---

## 6. Pages — Detailed Specifications

---

### 6.1 Splash / Loading Screen

**Layout:**
- Full dark background (`color-bg-primary`)
- Center: Matrix-style hexagon "M" logo (pink-to-orange gradient fill, white "M" letter)
- Below logo: app name in `typo-display`
- Below name: animated dot-pulse or linear progress bar (`color-accent-pink`)
- Bottom: version text `typo-micro color-text-muted`

**Behavior:**
- Duration: 1.5–2.5 s (check DataStore for activation state + load Room profiles)
- Navigate automatically; no user input.

**States:** `Loading` → checks activation → routes to `Activation` or `ProfilePicker`.

---

### 6.2 Activation Screen (Trial & License)

**Layout:**
- Full screen, centered card (max 480 dp wide)
- Logo at top
- Icon: login/arrow icon with decorative dots (`color-accent-pink`)
- Title: "Login Screen" or "Activate"
- Input field: white pill input, placeholder "Enter your activation code"
- Primary button: gradient "Get Started" with left/right chevron arrows (TV hint)
- Below button: "Start Free Trial" text button

**States:**

| State | UI |
|---|---|
| `Idle` | Input empty, button enabled |
| `Loading` | Spinner on button, input disabled |
| `TrialActive` | Green checkmark, "Trial active – X days remaining" |
| `Activated` | Green checkmark, "Activated successfully", auto-navigate |
| `InvalidCode` | Red border on input, error text below: "Invalid or expired code" |
| `NoInternet` | Info banner: "Activation requires internet. Start trial works offline." |

**Local Logic (offline verification):**
- Trial: store `trialStartMs` in DataStore; allow 7 days; check on every launch.
- Activation code: verify against a hardcoded hash list OR a pre-bundled encrypted keys file (no network call required for offline mode).
- If network available, optionally hit a validation endpoint.

**Storage:**
```kotlin
// DataStore keys
IS_ACTIVATED       : Boolean
TRIAL_START_MS     : Long
LICENSE_EXPIRY_MS  : Long   // 0 = no expiry
```

---

### 6.3 Profile Picker / Manager Screen

**Layout (Picker mode):**
- Top bar with logo
- Title: "Select Profile"
- Profile grid: 2–3 columns, cards with avatar (initials circle, gradient bg), profile name, host URL (truncated), "Last used" timestamp
- Bottom row: "+ Add Profile" card (dashed border, plus icon)
- Long-press or dedicated button: reveals Edit / Delete overlay per card

**Layout (Manager mode — from Settings):**
- List view instead of grid
- Each row: avatar, name, host, chevron right
- Floating "+" FAB or top-right button

**Add / Edit Form:**
- Full-screen overlay or bottom sheet (TV: full dialog centered)
- Fields:
  - Profile Name (optional, placeholder = host domain)
  - Host URL (required) — `http(s)://host:port`
  - Username (required)
  - Password (required, masked, eye toggle)
- Buttons: "Save" (primary gradient), "Cancel" (secondary)
- Validation: host must be valid URL; username/password not empty

**Delete Confirmation:**
- Dialog: "Delete [profile name]? This cannot be undone."
- Buttons: "Delete" (error red), "Cancel"

**Constraints:**
- Max 10 profiles enforced (disable "Add" when count = 10, show tooltip).
- Passwords stored ONLY in `EncryptedSharedPreferences`, key = `"pwd_${profileId}"`.
- Profile entity:
```kotlin
@Entity data class Profile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val host: String,
    val username: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = 0L
)
```

---

### 6.4 Home Screen

**Layout:**
- Top bar: time (left), MATRIX logo + icon (center), date (right)
- 2-row × 5-column `LazyVerticalGrid` of category cards
- Bottom: D-pad hint legend (Up/Down/Left/Right/OK/Back icons + labels)

**Home Grid Cards (`HomeGridCard`):**
- Size: fills column, ~16:9 or 1:1 ratio, `radius-xl`
- Background: full-bleed image with `GradientCardScrim` overlay
- Bottom-left: icon badge (circle `#FFFFFF22` bg) + category name `typo-body bold white`
- Color tint overlay unique per category:

| Category | Tint | Image |
|---|---|---|
| Movies | Orange `#FF6B3566` | Movie poster collage |
| Live | Purple `#9B27AF66` | Sports/event photo |
| Favourite | Blue `#1565C066` | Sports action |
| Series | Gold `#F57F1766` | Drama still |
| Music | Purple-pink `#AD1D7866` | Artist photo |
| Entertainment | Red-orange `#E53935 66` | Show still |
| Radio | Amber `#F9A82566` | Radio/mic photo |
| Devotional | Deep purple `#4527A066` | Religious imagery |
| Settings | Pink `#E91E8C66` | Gears/abstract |

**Focus:** hovered card scales `1.08`, glow ring `color-focus-glow`.

---

### 6.5 Live TV Screen

**Layout (two-panel):**

**Left Panel (280 dp):**
- Category header pill: gradient bg, left/right chevron, category name `typo-title white bold`
- `LazyColumn` of `ChannelListItem`:
  - Channel number `typo-micro color-text-muted` (4 chars, e.g., "0001")
  - Channel name `typo-body`
  - Selected: name in `color-accent-pink`, left accent bar
- Scroll follows focus

**Right Panel (content area):**
- Top: live preview thumbnail / stream placeholder (16:9)
- Middle: channel name `typo-headline color-accent-pink`, star rating + score, age badge, channel logo (right)
- EPG row: "Now: [program name] | Next: [program name]" (if data available, else "No EPG data available")
- Bottom bar: Selector / Category / OK Select / Menu / Exit hints

**Sub-sections reachable via Category navigation:**
- All Channels (default)
- Favourites (heart icon)
- Recently Watched
- Individual Categories (navigate with LEFT/RIGHT on category pill)

**EPG Minimum:**
- Show current program name + end time
- Show next program name
- Source: Xtream API `/xmltv.php` parsed lazily, cached in Room EpgCache

---

### 6.6 Player Screen (Now Playing)

**Layout:**
- **Full screen**, no UI chrome except overlay
- ExoPlayer `SurfaceView` fills entire screen
- Overlay (auto-hides after 3 s of inactivity):
  - Bottom gradient scrim (`GradientCardScrim`)
  - Bottom-left: content title `typo-title white`
  - Bottom-center: playback controls (rewind 10s, play/pause, forward 30s)
  - Bottom-right: Menu button, Exit button
  - Seek bar: pink fill, white thumb, time labels both ends

**Remote Behavior in Player:**
```
OK           → toggle overlay visibility
UP           → next channel (Live only) – zap up
DOWN         → previous channel (Live only) – zap down
LEFT         → seek -10s (VOD/Series)
RIGHT        → seek +30s (VOD/Series)
FAST_FWD     → seek +30s
REWIND       → seek -10s
BACK (1st)   → show overlay if hidden
BACK (2nd)   → exit player → return to source screen
MENU         → show Menu overlay (quality, audio tracks, subtitles)
```

**Overlay auto-hide:** `LaunchedEffect` with `delay(3000)` reset on any key event.

**Resume Watching:**
- Save position to Room `WatchHistory(contentId, type, positionMs, durationMs, timestamp)`.
- On VOD/Series open: if `positionMs > 30s AND positionMs < durationMs - 60s`, show "Resume from X:XX?" dialog.

---

### 6.7 VOD (Movies) Screen

**Layout (two-panel same as Live):**

**Left Panel:**
- Category header (same style) — e.g., "Action"
- `LazyColumn` of movie list items (number + title)

**Right Panel:**
- Movie poster (left half of panel) — 16:9 or 2:3
- Right of poster: description text `typo-body color-text-secondary` (max 4 lines, ellipsize)
- Below poster area: movie title `typo-headline color-accent-pink`, rating, age badge
- Buttons: "Watch Trailer" (secondary), "Watch Now" (primary gradient)

**VOD Detail Screen** (when "Watch Now" pressed or OK on list):
- Navigates to PlayerScreen with stream URL.
- If resume position exists, show resume dialog first.

---

### 6.8 Series Screen

**Layout:** Same two-panel structure.

**Series Detail Screen:**
- Header: series poster + title, rating, description
- Season selector: horizontal scrollable pill row
- Episode list: `LazyColumn`, each row = episode number + name + duration + progress bar (if partially watched)
- "Play" button on focused episode

**Episode Player:** Same PlayerScreen, saves per-episode position with `contentId = "series_{seriesId}_s{season}_e{ep}"`.

---

### 6.9 Search Screen

**Layout:**
- Search input at top (full-width, auto-focused on entry)
- Filter pills below: "All" | "Live" | "Movies" | "Series"
- Results in `LazyVerticalGrid` (same card style as VOD/Live list items)
- Empty state: centered icon + "No results for '[query]'"
- Searching state: spinner

**Behavior:**
- Debounce 300 ms on text change
- Search against locally cached channel/VOD/series lists (after loading from Xtream API)
- TV: virtual keyboard OR physical keyboard if available

---

### 6.10 Settings Screen

**Layout:**
- Left: "Setting Options" panel card (white/gradient header, list of rows)
- Right: Matrix hexagon logo decoration + floating orbs
- Each `SettingRow`: label (left) + value/control (right)

**Setting Rows:**

| Setting | Control | Values |
|---|---|---|
| Language | Label → navigate to picker | "English", "Arabic", … |
| Subscription Information | Label | Activation status / trial expiry |
| Sync Data | Label → action | "Sync Now" / "None" |
| Parental Control | Label → PIN dialog | "All Pages" / "Off" |
| Clear History | Label → confirm dialog | "Select" → confirm |
| Sort Channels Alphabetically | Toggle switch | On/Off |
| Auto Start | Toggle switch | On/Off |
| Player Settings | Label → sub-screen | Buffer size, preferred quality |
| Manage Profiles | Label → ProfileManagerScreen | — |
| Clear Cache | Label → confirm dialog | — |
| Clear All Data | Label → danger confirm dialog | Resets everything including activation |
| App Version | Label (read-only) | "v1.0.0" |

**Toggle Switch style:** pill shape, `color-accent-purple` when ON, `color-text-muted` when OFF. Animate thumb horizontally on toggle.

**Parental PIN:**
- 4-digit PIN entry dialog
- Stored in `EncryptedSharedPreferences`
- Required to access Settings if enabled

---

## 7. Reusable Components Specification

### `FocusableCard`
```kotlin
@Composable
fun FocusableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
)
// Handles: isFocused state, scale animation, glow shadow, border
```

### `PrimaryButton`
```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    showArrows: Boolean = false,  // TV hint chevrons
    enabled: Boolean = true,
    loading: Boolean = false
)
```

### `CategoryPill`
```kotlin
@Composable
fun CategoryPill(
    label: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
)
// Gradient background, left/right chevron buttons inside
```

### `ChannelListItem`
```kotlin
@Composable
fun ChannelListItem(
    number: String,
    name: String,
    isSelected: Boolean,
    isFocused: Boolean,
    onClick: () -> Unit
)
```

### `HomeGridCard`
```kotlin
@Composable
fun HomeGridCard(
    title: String,
    icon: ImageVector,
    backgroundImage: Painter?,
    tintColor: Color,
    onClick: () -> Unit,
    isFocused: Boolean
)
```

### `TopBar`
```kotlin
@Composable
fun TopBar(
    title: String,
    icon: ImageVector,
    showClock: Boolean = true
)
// Shows time (left), icon+title (center), date (right)
```

### `AgeBadge`
```kotlin
@Composable
fun AgeBadge(rating: Int)
// Circle badge: gradient bg, white number, e.g., "12", "18"
```

### `SettingRow`
```kotlin
@Composable
fun SettingRow(
    label: String,
    value: String? = null,
    control: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isFocused: Boolean
)
```

---

## 8. Local Storage Rules

### Room Database (`AppDatabase`)

```kotlin
// Tables
Profile(id, name, host, username, createdAt, lastUsed)
WatchHistory(contentId, type, positionMs, durationMs, lastWatchedAt)
EpgCache(channelId, programTitle, startMs, endMs, cachedAt)
FavouriteChannel(channelId, type, addedAt)
```

### EncryptedSharedPreferences

```
Key pattern: "pwd_{profileId}"   → password (String)
Key: "parental_pin"              → 4-digit PIN (String)
```

### DataStore (Proto or Preferences)

```
IS_ACTIVATED      : Boolean  (default false)
TRIAL_START_MS    : Long     (default 0)
LICENSE_EXPIRY_MS : Long     (default 0)
ACTIVE_PROFILE_ID : String   (last used profile)
SORT_ALPHA        : Boolean
AUTO_START        : Boolean
LANGUAGE          : String   (default "en")
```

### Data Retention Policy

- **All data stays on-device only.**
- No analytics, no crash reporting calling home, no telemetry.
- "Clear All Data" → clears Room DB + DataStore + EncryptedSharedPrefs → relaunch → Activation screen.

---

## 9. Engineering Notes

### Compose vs XML Decision

**Use Jetpack Compose** because:
- Focus management via `FocusRequester` / `onFocusChanged` is cleaner.
- Animation (`animateFloatAsState` for scale, `AnimatedVisibility` for overlays) is first-class.
- TV Compose library (`androidx.tv:tv-compose`) provides `Lazy` grids optimized for D-pad.

**TV Compose dependency:**
```kotlin
implementation("androidx.tv:tv-foundation:1.0.0-alpha10")
implementation("androidx.tv:tv-material:1.0.0-alpha10")
```

### ExoPlayer Setup

```kotlin
implementation("androidx.media3:media3-exoplayer:1.3.0")
implementation("androidx.media3:media3-exoplayer-hls:1.3.0")
implementation("androidx.media3:media3-ui:1.3.0")
```

- Use `MediaItem.fromUri(streamUrl)` with HLS MIME type hint.
- Per-profile `OkHttpDataSource.Factory` with auth headers.
- Keep `ExoPlayer` instance in a `ViewModel` scoped to Player nav back-stack entry.

### Navigation

```kotlin
// Screen sealed class
sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Activation   : Screen("activation")
    object ProfilePicker: Screen("profile_picker")
    object Home         : Screen("home")
    object LiveTv       : Screen("live_tv")
    object Player       : Screen("player/{contentId}/{type}") {
        fun createRoute(contentId: String, type: String) = "player/$contentId/$type"
    }
    object VodBrowse    : Screen("vod_browse")
    object VodDetail    : Screen("vod_detail/{vodId}")
    object Series       : Screen("series_browse")
    object SeriesDetail : Screen("series_detail/{seriesId}")
    object Search       : Screen("search")
    object Settings     : Screen("settings")
    object ProfileMgr   : Screen("profile_manager")
    object AddProfile   : Screen("add_profile")
    object EditProfile  : Screen("edit_profile/{profileId}")
}
```

### Error Handling (Edge Cases)

| Scenario | Handling |
|---|---|
| Invalid credentials on profile select | Snackbar/dialog: "Could not connect. Check host/credentials." Profile NOT deleted. |
| Network timeout | Retry button shown; cached data displayed if available |
| Stream fails to load in player | Error overlay: "Stream unavailable. Try another channel." |
| Trial expired | On launch, redirect to Activation screen with "Trial expired" message |
| License expired | Same as trial expired |
| Max profiles reached (10) | "Add" button disabled; tooltip: "Maximum 10 profiles reached" |
| EPG unavailable | Show "No EPG data available" gracefully, no crash |
| Empty category | "No channels in this category" empty state |
| Player codec unsupported | Fallback to software decode; show warning if fails |

---

## 10. Acceptance Criteria

1. **Max 10 profiles** — enforced at UI and repository level; 11th add rejected.
2. **Persistence** — profiles and watch history survive app close/reopen and device reboot.
3. **Password security** — passwords never stored in plain text; only in `EncryptedSharedPreferences`.
4. **Landscape only** — app never enters portrait orientation on any screen/device.
5. **TV safe margins** — no UI content rendered inside 48 dp left/right and 27 dp top/bottom margins.
6. **Focus always visible** — no focusable element ever lacks a distinct focus indicator.
7. **BACK key** exits player overlay first, then exits player, never exits app unexpectedly without confirmation.
8. **Activation offline** — trial start requires no internet; activation code can be verified locally.
9. **Profile switch** — switching profile clears in-memory cache and re-fetches from new host.
10. **Clear All Data** — returns app to factory state (Activation screen) with all local data wiped.

---

## 11. Animation Guidelines

| Element | Animation |
|---|---|
| Home card focus | `animateFloatAsState(if focused 1.08f else 1.0f)`, 150ms |
| Player overlay show/hide | `AnimatedVisibility(fadeIn + slideInVertically)` |
| Category change | Crossfade on content area, 200ms |
| Page transitions | `slideInHorizontally` / `slideOutHorizontally`, 250ms |
| Toggle switch | Spring animation on thumb position |
| Splash logo | Scale in `0.6f → 1.0f`, 500ms ease-out |
| Waveform decoration (Radio) | Infinite looping translate X, 8s linear |

---

*End of skill.md — keep this file as the single source of truth for all IPTV Xtream UI/UX and architecture decisions.*
