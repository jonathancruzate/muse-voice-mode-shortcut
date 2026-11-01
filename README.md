# Muse Voice Mode Shortcut (v1.4.0, built 2026-11-01)

A one-icon **switch** for Muse voice mode on Jonathan's phone,
built at his direct ask ("Create a Muse Voice Mode Shortcut apk
using the same accessibility trick as Muse for Android Auto"),
then tweaked the same night at his order: **each triggering is a
switch — on, off, on, off.**

- Package `com.jonathancruzate.musevoice`, framework-only (no
  AndroidX), min SDK 29 / target 35.
- APK: `Muse-Voice-Mode-Shortcut-1.4.0.apk`, 124,300 B,
  SHA-256 `c10bef18bfeed10ac43d72c631a02caef9236a70b887e7dd19ff206bbc6d499b`.
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
  `~/workspace/muse-voice-mode-shortcut/`. Package and signing
  cert are unchanged across all versions, so v1.3.0 upgrades in
  place. The cert DN still reads `CN=Muse Voice` — it is the
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

Three entry points, all flipping the same switch:

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

1. Install `Muse-Voice-Mode-Shortcut-1.4.0.apk`.
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

- `src/com/jonathancruzate/musevoice/VoiceState.java` — persisted
  switch + transient hunt flags
- `src/com/jonathancruzate/musevoice/VoiceLauncher.java` — the
  toggle: `launchVoice` / `endVoice`, `ACTION_ASSIST` + Muse
  foregrounding, buzzes
- `src/com/jonathancruzate/musevoice/VoiceAccessibilityService.java`
  — both button hunts
- `src/com/jonathancruzate/musevoice/MainActivity.java` — launcher
  icon / setup screen / pin-shortcut
- `src/com/jonathancruzate/musevoice/ShortcutActivity.java` —
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
