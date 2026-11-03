# Muse Voice Mode Shortcut (v1.7.0, built 2026-11-03)

A one-icon **switch** for Muse voice mode on Jonathan's phone,
built at his direct ask ("Create a Muse Voice Mode Shortcut apk
using the same accessibility trick as Muse for Android Auto"),
then tweaked the same night at his order: **each triggering is a
switch — on, off, on, off.**

- Package `com.jonathancruzate.metamusevoicemodeshortcut`,
  framework-only (no AndroidX), min SDK 29 / target 35.
  (Renamed in v1.5.x from `com.jonathancruzate.musevoice` — see
  the v1.5.0 → v1.5.1 section below. The rename is a reinstall,
  not an in-place upgrade.)
- APK: `Muse-Voice-Mode-Shortcut-1.7.0.apk`, 124,300 B,
  SHA-256 `2974ab84865942c83b34a92bcadc3667eb457d56693036904991ce771e647de5`.
- Signing cert (own keystore in `keystore/`, password file
  alongside, 0600): SHA-256
  `b07b56806223d7461ad9149522ee9562ee91b689ae1d43750e08bd1f868a4b62`.
  v1.0.0 (SHA-256 `f19833a7…`, start-only) was built minutes
  earlier the same night and superseded by this toggle version
  before any install; v1.1.0 uses the same cert, so any future
  build upgrades in place.
- **Name: Muse Voice Mode Shortcut** (Jonathan's orders, same
  night: renamed from the v1.0.0–v1.1.0 display name "Muse
  Voice"; an intermediate v1.2.0 read "Muse Voice Shortcut" and
  he corrected it to the full **Muse Voice Mode Shortcut**,
  shipped as v1.3.0). App label, accessibility service label,
  setup screen, and both shortcut labels all read Muse Voice
  Mode Shortcut. Project directory is the name slug,
  `~/workspace/muse-voice-mode-shortcut/`. The signing cert is
  unchanged across all versions, and the package was unchanged
  through v1.4.0, so v1.0.0–v1.4.0 all upgraded in place;
  v1.5.x renamed the package (below), so it installs fresh.
  The cert DN still reads `CN=Muse Voice` — it is the
  pre-rename keystore's internal name, kept deliberately:
  changing it would break in-place upgrades.

## The switch

| Tap | State before | What happens |
|---|---|---|
| 1st | OFF | Opens Muse via `ACTION_ASSIST`, accessibility service taps Muse's own **voice button** → voice session starts. Short buzz confirms the tap. Switch flips ON. |
| 2nd | ON | Double buzz, brings Muse forward, accessibility service taps Muse's **end-call button** (fallback Back → Home) → session ends. Switch flips OFF. |
| 3rd | OFF | ON again. Alternates forever. |

The on/off state is persisted in SharedPreferences
(`voice_state/session_active`), so the alternation survives the
shortcut activity finishing and the process being killed between
taps. The switch flips at tap time (before Muse opens), so a
hunt that never finds its button still leaves the next tap in
the safe direction.

Five entry points, all flipping the same switch:

1. **Launcher icon "Muse Voice Mode Shortcut"** (`MainActivity`, translucent):
   with the accessibility service enabled it flips and finishes
   before any UI draws — no flash. With the service not yet
   enabled, the same tap shows the one-time setup screen instead
   of failing silently (setup status line also shows the current
   switch state).
2. **Long-press app shortcut "Muse Voice Mode Shortcut"**
   (`res/xml/shortcuts.xml` → `ShortcutActivity`, no UI ever).
3. **Pinned home-screen shortcut**: the setup screen's "Add
   home-screen shortcut" button uses
   `ShortcutManager.requestPinShortcut` → same `ShortcutActivity`.
4. **Galaxy S24+ side button, long press** — the mapping
   Jonathan actually uses day to day (his setup, recorded
   2026-11-01; see the next section).
5. **Galaxy Buds touch and hold, "Digital assistant"** (v1.7.0,
   Jonathan's ask 2026-11-03, from his screenshot of the Buds
   chooser): `ShortcutActivity` declares `ACTION_ASSIST` +
   `ACTION_VOICE_COMMAND`, so it appears in the Buds'
   "Complete action using" chooser alongside Bixby, Google,
   Home Assistant, and MacroDroid — see the Buds section below.

## How Jonathan uses it — side button long press (Galaxy S24+)

The shortcut is mapped to the **long press of the side button**
on Jonathan's Galaxy S24+, so voice mode is one hardware press
away, no screen interaction needed:

- **Long press once** → switch ON, Muse voice session starts.
- **Long press again** → switch OFF, session ends.
- Works the same from any app; combined with the buzzes (short =
  start tap landed, double = turning off) it is fully eyes-free.

Setup on the S24+ (One UI): **Settings → Advanced features →
Side button → Long press → Open app → Muse Voice Mode
Shortcut**. Notes:

- The accessibility service must be enabled first (one-time
  setup below), or the press lands on the setup screen instead
  of flipping the switch.
- Remapping long press gives up its default (Bixby / power-off
  menu): power off moves to the Quick settings power icon, or
  side button + Volume down together.
- This is an app launch mapping, not the digital-assistant
  slot — Muse keeps no assistant role, and Gemini's own
  triggers are untouched.

## How Jonathan uses it — Galaxy Buds touch and hold (v1.7.0)

Jonathan's ask (2026-11-03, with a screenshot of his Buds
settings): in **Galaxy Wearable → Touch and hold**, the **Left**
bud is set to **Digital assistant**, and tapping it opens the
system chooser **"Complete action using"** — which listed only
Bixby, Google, Home Assistant, and MacroDroid, because those are
the apps on his phone that handle the digital-assistant intents.
This app handled none of them, so it could not be picked.

**The fix (v1.7.0, manifest-only):** `ShortcutActivity` — the
existing no-UI toggle activity — now declares an intent filter
for **`android.intent.action.ASSIST`** and
**`android.intent.action.VOICE_COMMAND`** (category DEFAULT).
That is the documented pattern behind the other names in his
screenshot: Home Assistant ships a `VOICE_COMMAND`
activity-alias to its AssistActivity, and Samsung One UI
additionally requires an `ACTION_ASSIST` filter before an app is
enumerated as a digital assistant at all (online check
2026-11-03: Home Assistant manifest via the Dicio #226 thread;
the One UI ASSIST requirement via the kernel-ai-assistant #987
work; Pixel Buds projects use the same ASSIST + VOICE_COMMAND
pair for earbud long presses). The filters sit on the real
toggle activity, not a `finish()`-only trampoline. The activity
also gained `noHistory` + `excludeFromRecents` so assist
launches never pollute Recents. No code, package, or cert
changes — v1.7.0 upgrades v1.6.0 **in place**, and the
accessibility grant and side-button mapping carry over
untouched.

**Setup on the phone (Jonathan's hands, ~30 seconds):**

1. Install `Muse-Voice-Mode-Shortcut-1.7.0.apk` (in-place
   upgrade — do NOT uninstall the old one first).
2. Galaxy Wearable (or Settings → Buds) → **Touch and hold** →
   **Left** (or Right) → **Digital assistant**.
3. In **"Complete action using"**, tap **Muse Voice Mode
   Shortcut** → **Always**. (If Muse isn't in the list, the
   v1.7.0 upgrade hasn't landed yet — reinstall and reopen the
   Buds setting.)
4. Touch and hold that bud: **hold once = voice ON** (short
   buzz when the tap lands), **hold again = voice OFF** (double
   buzz) — the exact same switch as the side button, fully
   eyes-free, phone in pocket.

Notes:

- **"Always" binds the assistant intent system-wide,** not just
  for the Buds: anything else that fires a bare ASSIST /
  VOICE_COMMAND intent and previously opened his chosen
  assistant will now flip Muse voice instead. That is the
  point of the mapping, but it is a real trade — to undo it:
  Settings → Apps → Muse Voice Mode Shortcut → **Set as
  default → Clear defaults**, then re-pick in the Buds flow.
- The other bud keeps its own mapping (his Right bud stays on
  **Switch noise controls**), so ANC toggling is untouched.
- If a session is already running, a Buds hold ends it (the
  switch is shared with every entry point — the persisted state
  is one switch, not one per surface).
- On-device verification is Jonathan's first hold: the chooser
  listing is verifiable from the packaged manifest (done at
  build time — see the v1.7.0 section), but only a real Buds
  hold proves Samsung's Wearable app routes it as expected.

## The accessibility trick (from Muse for Android Auto)

Muse's voice deep link (`hatch://voice`) is TrustedCaller-gated
to Meta-signed apps — the REA verdict from the Muse Drive work —
so no outside app can fire it. Both halves of the switch reuse
Muse Drive's hunts verbatim (same REA-derived candidates):

- **Start** (Drive v2.1): `ACTION_ASSIST` into
  `com.facebook.aura` (explicit `AuraMainActivity`, implicit
  fallback), hunt armed *before* the launch. The service watches
  for the Muse window and taps the voice button: view-ids
  `hatch_voice_button`, `voice_button`, `voice_call_button`,
  `start_voice_button`, then exact labels "Voice call", "Start
  voice call", "Voice mode", "Voice", "Talk". ~9s of retries.
- **End** (Drive v2.0): view-ids `end_voice_button`,
  `end_call_button`, `voice_end_call`, then exact labels "End
  call", "End voice call", "Hang up", "Leave call", "End";
  after ~9s the fallback chain fires Back → Home. OFF first
  brings Muse forward via its package launch intent
  (`ACTION_ASSIST` fallback) so the voice screen is the active
  window.

The service only ever inspects the Muse window
(`com.facebook.aura`), and only while a hunt is armed by a tap.
No volume-key filtering in this app (that's Muse Drive's car
surface, not the switch).

Known desync edge: ending a session from inside Muse's own UI
(or Muse crashing) doesn't flip the stored switch, so the next
tap would try to end an already-ended session (harmless — the
hunt times out into Back → Home) and the tap after that starts
fresh. The switch self-heals every other tap.

## One-time setup (Jonathan's hands)

1. Install the current APK (`Muse-Voice-Mode-Shortcut-1.7.0.apk`).
   Coming from v1.6.0 (or v1.5.1) it upgrades **in place** — same
   package, same cert, accessibility grant and side-button
   mapping preserved; skip straight to the Buds setup in the
   Buds section above if you only want the new mapping. (The
   package changed in the v1.5.1/v1.6.0 rename, so coming from
   v1.5.0 or older it installs as a *separate* app next to the
   old one — both show the same label. Uninstall the old
   **Muse Voice Mode Shortcut** afterwards; the new one is the
   one whose accessibility service and side-button mapping you
   set up below.)
2. Tap **Muse Voice Mode Shortcut** → setup screen → **Enable accessibility
   service** → Settings → Installed apps → **Muse Voice Mode Shortcut** → ON.
3. Tap the icon again — voice session should start with a buzz.
   Tap again — it should end (double buzz, then the end tap).

Parked on-device verification (same status as Muse Drive's):
the voice/end button candidates are REA-derived and unproven
until seen live on the real Muse voice screen.

## Build

`./build.sh` — raw javac/d8/aapt2 flow via the meta-muse-widget
SDK (build-tools 35.0.0, android-35, read-only toolchain use) +
Temurin JDK 21, same as Muse for Android Auto. No Gradle, no
emulator touched. Creates `keystore/musevoice.keystore` on first
run if absent.

## Layout

- `src/com/jonathancruzate/metamusevoicemodeshortcut/VoiceState.java` — persisted
  switch + transient hunt flags
- `src/com/jonathancruzate/metamusevoicemodeshortcut/VoiceLauncher.java` — the
  toggle: `launchVoice` / `endVoice`, `ACTION_ASSIST` + Muse
  foregrounding, buzzes
- `src/com/jonathancruzate/metamusevoicemodeshortcut/VoiceAccessibilityService.java`
  — both button hunts
- `src/com/jonathancruzate/metamusevoicemodeshortcut/MainActivity.java` — launcher
  icon / setup screen / pin-shortcut
- `src/com/jonathancruzate/metamusevoicemodeshortcut/ShortcutActivity.java` —
  no-UI shortcut target
- `res/xml/shortcuts.xml`, `res/xml/voice_accessibility_config.xml`
- `github-export/` — scrubbed source snapshot for the GitHub
  upstream bundle (same pattern as Muse Drive's export)

## v1.4.0 — Muse Widget app icon (2026-11-01, Jonathan's order)

All Muse APKs now carry the same app icon as the Muse Widget
app ("set all muse apks app icons to the same app icon used by
muse widget app"). This app's purple vector mic icon is replaced
by the widget's launcher art — the blue M on transparency, from
his M art — as `res/mipmap-*/ic_launcher.png` +
`ic_launcher_round.png` (mdpi 48px through xxxhdpi 192px), with
manifest `icon`/`roundIcon` and the pinned-shortcut icon pointed
at the mipmaps. Verified: the icon inside the built APK is
byte-identical to the icon inside the Muse Widget's own shipped
APK (`Muse-Widget-4.10.0.apk`, xxhdpi md5 `9ecb2bfe…` — aapt2's
PNG crunch matches on both). Muse for Android Auto got the same
icon the same night (its v2.3.0). No behavior changes; version
code 5, same signing cert, upgrades in place.

## v1.5.0 → v1.5.1 — package renamed to the family scheme (2026-11-01)

Jonathan's orders the same night: **all APKs and package names
match their GitHub project names**, reinstalls accepted — then
refined minutes later: **all his app packages follow one family
scheme, `com.jonathancruzate.metamuse<product>`**, modeled on
Meta's own Muse app taxonomy (their app is
`com.meta.ai` / `com.facebook.aura`). v1.5.0
(`…musevoicemodeshortcut`) was superseded by v1.5.1 before it
was ever released or installed; v1.5.1 is the build that ships.

- Package: `com.jonathancruzate.musevoice` →
  `com.jonathancruzate.musevoicemodeshortcut` (v1.5.0,
  superseded) → **`com.jonathancruzate.metamusevoicemodeshortcut`**
  (v1.5.1, final — the family scheme:
  `com.jonathancruzate.metamuse` + `voicemodeshortcut`). The
  APK filename follows the repo name as before
  (`Muse-Voice-Mode-Shortcut-<version>.apk`) and is unchanged,
  as is the display label, **Muse Voice Mode Shortcut**.
- Renamed everywhere it appears: the manifest `package`
  attribute, all five Java sources (moved to
  `src/com/jonathancruzate/metamusevoicemodeshortcut/`, `package`
  declarations updated), and the launcher-shortcut intent in
  `res/xml/shortcuts.xml` (`targetPackage` / `targetClass`).
  Manifest component names are relative and followed
  automatically; the accessibility config and strings carry no
  package references. The keystore is untouched — same file
  (`keystore/musevoice.keystore`), same alias, same cert
  (SHA-256 `b07b5680…`) — a keystore's filename is not a package
  name, and the cert is what Android checks.
- versionCode 7 / versionName 1.5.1. APK
  `Muse-Voice-Mode-Shortcut-1.5.1.apk`, 124,300 B, SHA-256
  `2c0d186a075b5159a5feb126f6a51c9cd99a933cb047bf8cd610edc77923c9c8`.
  Verified: `apksigner` OK (cert unchanged); badging shows the
  new package, code 7 / 1.5.1, label "Muse Voice Mode
  Shortcut"; the packaged shortcuts.xml points at the new
  package.

**Reinstall required — a package name IS the app's identity
on Android, so this cannot upgrade in place.** Installing
v1.5.1 puts a second, identical-looking "Muse Voice Mode
Shortcut" next to the old one. After installing:

1. **Re-enable the accessibility service** for the NEW app:
   tap the new icon → setup screen → **Enable accessibility
   service** → Settings → Installed apps → **Muse Voice Mode
   Shortcut** → ON. (Accessibility grants are per-package; the
   old app's grant does not carry over. The switch state also
   starts fresh at OFF.)
2. **Re-point the side-button long press** at the new app:
   Settings → Advanced features → Side button → Long press →
   Open app → **Muse Voice Mode Shortcut** (the new one — do
   this after step 1 so the press flips the switch instead of
   opening the setup screen).
3. Uninstall the old app (same label — it's the one whose
   accessibility entry you just replaced; if unsure, the old
   package is `com.jonathancruzate.musevoice` in Settings →
   Apps). Any pinned home-screen shortcut to the old app dies
   with it; re-pin from the new app's setup screen if used.

## v1.6.0 — the family-scheme rename, release version (2026-11-01)

v1.6.0 is the release build of the rename documented in the
v1.5.0 → v1.5.1 section above: the package is
**`com.jonathancruzate.metamusevoicemodeshortcut`** — the family
segment `com.jonathancruzate.metamuse` + `voicemodeshortcut`,
aligned with the taxonomy of Jonathan's other Meta Muse
companion apps (Meta Muse Widget, Meta Muse Android Auto) and
modeled on Meta's own Muse app packages. No code changes from
v1.5.1; v1.6.0 supersedes it purely on versioning
(versionCode **8** — 7 was already consumed by the v1.5.1 build
of this same rename, so 1.6.0 could not reuse it).

- APK `Muse-Voice-Mode-Shortcut-1.6.0.apk`, 124,300 B,
  SHA-256 `332e78ece7d6966bf1b2e5924c45d5232ed992d758a003a6e5427e0ad6b08797`.
  Verified: `apksigner` OK, cert unchanged (SHA-256
  `b07b56806223d7461ad9149522ee9562ee91b689ae1d43750e08bd1f868a4b62`,
  keystore `keystore/musevoice.keystore` untouched); badging
  shows package `com.jonathancruzate.metamusevoicemodeshortcut`,
  code 8 / 1.6.0, label "Muse Voice Mode Shortcut"; the
  packaged `shortcuts.xml` targets the new package.
- **Install:** over v1.5.1 (same package, same cert) it
  upgrades in place. From v1.5.0 or older it is a **reinstall**
  — the package is the app's identity: install the new app,
  **re-enable its accessibility service**, **re-point the
  side-button long press** at the new app, then uninstall the
  old one (full steps in "One-time setup" above).

## v1.7.0 — Galaxy Buds digital-assistant mapping (2026-11-03)

Jonathan's ask, with his screenshot of the Buds "Touch and
hold" screen: the Left bud is set to **Digital assistant**, the
system's "Complete action using" chooser lists Bixby, Google,
Home Assistant, and MacroDroid — and Muse Voice Mode Shortcut
wasn't in it, because the app handled no assistant intent.

- **Change (manifest-only):** `ShortcutActivity` gains a second
  intent filter — `android.intent.action.ASSIST` +
  `android.intent.action.VOICE_COMMAND`, category DEFAULT — plus
  `noHistory` and `excludeFromRecents`. Pattern checked online
  the same day (his standing rule): Home Assistant's
  VOICE_COMMAND activity-alias to its AssistActivity (via the
  Dicio #226 thread), Samsung One UI's additional requirement of
  an ACTION_ASSIST filter for digital-assistant enumeration
  (kernel-ai-assistant #987), and earbud long-press projects
  using the same ASSIST + VOICE_COMMAND pair. No Java behavior
  changes (a doc comment on `ShortcutActivity` aside), no
  VoiceInteractionService: the Buds chooser is an activity
  chooser, and a VIS would additionally hijack the system
  default-assistant role, which this mapping does not need.
- versionCode **9** / versionName **1.7.0**. APK
  `Muse-Voice-Mode-Shortcut-1.7.0.apk`, 124,300 B, SHA-256
  `2974ab84865942c83b34a92bcadc3667eb457d56693036904991ce771e647de5`.
  Verified from the built APK: `apksigner` OK, cert unchanged
  (SHA-256 `b07b56806223d7461ad9149522ee9562ee91b689ae1d43750e08bd1f868a4b62`);
  badging shows the same package, code 9 / 1.7.0, label
  "Muse Voice Mode Shortcut"; `aapt2 dump xmltree` on the
  packaged manifest shows `.ShortcutActivity` carrying both
  `ASSIST` and `VOICE_COMMAND` actions with
  `excludeFromRecents=true` / `noHistory=true`.
- **Install:** over v1.6.0 / v1.5.1 it upgrades **in place**
  (same package + cert) — accessibility grant, switch state,
  and the side-button mapping all carry over. Then map it in
  the Buds flow (full steps in the Buds section above). The one
  remaining verification is on-device: Muse appearing in the
  "Complete action using" list and a real Buds hold flipping
  the switch are Jonathan's first-hold test.
