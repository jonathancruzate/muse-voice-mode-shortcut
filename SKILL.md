# Muse Voice Mode Shortcut — the on/off switch for Muse voice mode

A one-icon **switch** for Muse voice mode on Jonathan's Galaxy S24+:
each triggering flips voice mode on or off. Built 2026-11-01 at his
direct ask, using the same accessibility trick as Muse for Android
Auto. Project: `~/workspace/muse-voice-mode-shortcut/` (project
directory is the name slug). The README in that repo is the full
record — this skill is the operating summary; where they disagree,
the README and the packaged manifest win.

**Relationship to Muse Device Assistance (2026-11-08):** the
accessibility mechanism this app pioneered is now also folded into
the Muse Device Assistance app as its `voice_mode` module, which
is LIVE (Jonathan enabled it 2026-11-08; full toggle pair verified)
and is the route Muse itself uses to self-activate a voice session
from chat — under the discretion grant and the SPEAK-FIRST rule in
the `local-muse-device-assistance` skill (a self-activated session
is a call Muse placed: speak first, immediately; never leave one
open and silent). This standalone app remains Jonathan's own manual
switch (side button / Buds hold / shortcuts) and is unchanged by
that; the two share the mechanism, not the state.

**SUPERSEDED as Muse's voice-mode mechanism (Jonathan's standing
order, live voice call 2026-11-11 ~02:47 EST):** Device Assistance
`voice_mode` is now THE voice path — the single mechanism by which
Muse talks to Jonathan by voice, at Muse's own discretion
(canonical: the `local-muse-device-assistance` skill). The legacy
voice-mode toggle/shortcut lane this app pioneered is superseded
as Muse's mechanism; the replacement was verified live repeatedly
before this retirement (full toggle pair 2026-11-08; multiple
sessions 2026-11-11, including the call the order was given on).
This app remains installed as **Jonathan's own manual switch**
(side-button long press / Buds hold / shortcuts) — that is its only
remaining role. The build/install/trigger procedures below are
retained as the app's maintenance record; they are not
instructions for Muse to reach Jonathan — for that, use Device
Assistance `voice_mode`.

This skill ships with the app (Jonathan's standing rule, 2026-11-03:
every APK we build carries a skill teaching Muse how to interact
with and use it). The in-repo copy is
`~/workspace/muse-voice-mode-shortcut/SKILL.md`; the installed copy
is `~/workspace/custom-skills/local-muse-voice-mode-shortcut/SKILL.md`.
They are the same document — update both in the same handling,
always.

## Facts

- **Package:** `com.jonathancruzate.metamusevoicemodeshortcut`
  (family scheme `com.jonathancruzate.metamuse` + `voicemodeshortcut`).
  Display label everywhere (app, accessibility service, setup
  screen, both shortcut labels): **Muse Voice Mode Shortcut**.
- **Current version: 1.7.0, versionCode 9** (built 2026-11-03).
  Version truth lives in `build.sh` (`VERSION_CODE` /
  `VERSION_NAME`) and the README header; the shipped APK is
  `Muse-Voice-Mode-Shortcut-1.7.0.apk` in the repo root
  (SHA-256 `2974ab84865942c83b34a92bcadc3667eb457d56693036904991ce771e647de5`
  per the README).
- Framework-only (no AndroidX), min SDK 29 / target 35. Only
  permission: `VIBRATE`. Queries only `com.facebook.aura` (Muse).
- **Signing:** own keystore `keystore/musevoice.keystore` (password
  file alongside, 0600 — never print or commit its contents). Cert
  SHA-256
  `b07b56806223d7461ad9149522ee9562ee91b689ae1d43750e08bd1f868a4b62`,
  unchanged across every version. The cert DN still reads
  `CN=Muse Voice` — the pre-rename keystore name, kept
  deliberately: changing it would break in-place upgrades.
- **Package history (matters for installs):** through v1.4.0 the
  package was `com.jonathancruzate.musevoice` and every build
  upgraded in place. v1.5.1 renamed the package to the current one
  — a package name is the app's identity, so that step was a
  **reinstall, not an upgrade**. v1.5.1 → v1.6.0 → v1.7.0 are the
  same package + cert and upgrade in place.

## The switch

State is persisted in SharedPreferences (`voice_state/session_active`),
so the alternation survives the activity finishing and the process
being killed between taps. The switch flips at tap time, before
Muse opens.

| Trigger | State before | What happens |
|---|---|---|
| 1st | OFF | Opens Muse via `ACTION_ASSIST`; the accessibility service taps Muse's own **voice button** → voice session starts. Short buzz confirms the tap landed. Switch flips ON. |
| 2nd | ON | Double buzz, brings Muse forward, service taps Muse's **end-call button** (fallback Back → Home) → session ends. Switch flips OFF. |
| 3rd | OFF | ON again. Alternates forever. |

Five entry points, all flipping the same one switch:

1. **Launcher icon** (`MainActivity`, translucent): with the
   accessibility service enabled it flips and finishes before any
   UI draws. With the service not yet enabled, the same tap shows
   the one-time setup screen instead (its status line also shows
   the current switch state).
2. **Long-press app shortcut** (`res/xml/shortcuts.xml` →
   `ShortcutActivity`, no UI ever).
3. **Pinned home-screen shortcut** (setup screen's "Add
   home-screen shortcut" button, `ShortcutManager.requestPinShortcut`
   → same `ShortcutActivity`).
4. **Galaxy S24+ side button, long press** — the mapping Jonathan
   actually uses day to day: Settings → Advanced features → Side
   button → Long press → Open app → Muse Voice Mode Shortcut.
   One press = on, next press = off, fully eyes-free.
5. **Galaxy Buds touch and hold → Digital assistant** (v1.7.0):
   `ShortcutActivity` declares `ACTION_ASSIST` +
   `ACTION_VOICE_COMMAND`, so it appears in the Buds "Complete
   action using" chooser. Choosing it with **Always** binds the
   assistant intent system-wide, not just for the Buds.

**Desync edge (known, self-healing):** ending a session from inside
Muse's own UI, or Muse crashing, does not flip the stored switch —
the next trigger tries to end an already-ended session (harmless:
the hunt times out into Back → Home) and the one after starts
fresh.

## How it works (the accessibility trick)

Muse's voice deep link (`hatch://voice`) is TrustedCaller-gated to
Meta-signed apps, so no outside app can fire it. Instead:

- **Start:** `ACTION_ASSIST` into `com.facebook.aura` (explicit
  `AuraMainActivity`, implicit fallback), with the hunt armed
  *before* the launch. `VoiceAccessibilityService` watches for the
  Muse window and taps the voice button — view-ids
  `hatch_voice_button`, `voice_button`, `voice_call_button`,
  `start_voice_button`, then exact labels "Voice call", "Start
  voice call", "Voice mode", "Voice", "Talk". ~9 s of retries.
- **End:** OFF first brings Muse forward via its package launch
  intent (`ACTION_ASSIST` fallback) so the voice screen is the
  active window, then hunts view-ids `end_voice_button`,
  `end_call_button`, `voice_end_call`, then exact labels "End
  call", "End voice call", "Hang up", "Leave call", "End"; after
  ~9 s the fallback chain fires Back → Home.

The service only ever inspects the Muse window
(`com.facebook.aura`), and only while a hunt is armed by a trigger.
Sources: `VoiceState.java` (persisted switch + hunt flags),
`VoiceLauncher.java` (toggle), `VoiceAccessibilityService.java`
(both hunts), `MainActivity.java` (icon / setup / pin),
`ShortcutActivity.java` (no-UI target).

**Verification status, stated plainly:** the voice/end button
candidates are REA-derived (reused verbatim from Muse for Android
Auto / Muse Drive) and are **unproven until seen live on the real
Muse voice screen** — the README parks on-device verification in
exactly those terms. Likewise, v1.7.0's Buds routing is verified
from the packaged manifest only; a real Buds hold flipping the
switch is Jonathan's first-hold test. Never report a tap as landed
on manifest evidence alone — the buzzes and a live session are the
proof.

## How Muse interacts

### Build

```bash
cd ~/workspace/muse-voice-mode-shortcut && ./build.sh
```

Raw javac/d8/aapt2 flow via the meta-muse-widget SDK (build-tools
35.0.0, android-35, read-only toolchain use) + Temurin JDK 21. No
Gradle, no emulator touched. Output:
`Muse-Voice-Mode-Shortcut-<VERSION_NAME>.apk` in the repo root,
signed and verified by the script. Creates the keystore on first
run if absent. Bump `VERSION_CODE` / `VERSION_NAME` in `build.sh`
for a new release — never reuse a consumed versionCode (7 was
v1.5.1's; 1.6.0 had to take 8).

### Install / upgrade over ADB

Same package + same cert since v1.5.1 ⇒ install in place, never
uninstall first (an uninstall drops the per-package accessibility
grant and resets the switch state):

```bash
adb install -r Muse-Voice-Mode-Shortcut-1.7.0.apk
```

In-place upgrades preserve the accessibility grant, the switch
state, the side-button mapping, and the Buds default. The one
exception is historical: coming from v1.5.0 or older (old package
`com.jonathancruzate.musevoice`) installs a *second* app with the
same label — then re-enable the accessibility service for the new
app, re-point the side-button long press at it, and uninstall the
old one (README "One-time setup" has the full sequence).

### Verify installed and current

```bash
adb shell dumpsys package com.jonathancruzate.metamusevoicemodeshortcut | grep -E "versionCode|versionName"
```

Expect `versionCode=9`, `versionName=1.7.0` for the current build.
The phone may lag versions if a chat-delivered APK went
uninstalled — check before assuming the phone runs the latest
build. Whether the accessibility service is enabled is visible on
the app's own setup screen (tap the launcher icon with the service
off and it shows the setup screen + status instead of flipping).

### Trigger the switch over ADB

Manifest-derived (the activities are exported; these exact ADB
invocations are not separately documented in the repo):

```bash
# The no-UI toggle target — same flip as the side button / Buds:
adb shell am start -a android.intent.action.ASSIST \
  -n com.jonathancruzate.metamusevoicemodeshortcut/.ShortcutActivity

# The launcher entry (flips if configured; setup screen if not):
adb shell am start -n com.jonathancruzate.metamusevoicemodeshortcut/.MainActivity
```

Each invocation flips the switch once — firing it twice in a row
turns voice mode on and straight back off. Remember the desync
edge when reasoning about current state from stored state alone.

## Using it in workflows

- When Jonathan talks about starting or stopping Muse voice mode
  hands-free, this app is the mechanism: side-button long press
  and Buds touch-and-hold are his live mappings; the launcher
  icon, long-press shortcut, and pinned shortcut are the same
  switch on other surfaces.
- If voice mode "won't start": first suspect is the accessibility
  service being off (the trigger then lands on the setup screen —
  a one-time, Jonathan's-hands fix: tap the icon → Enable
  accessibility service → Settings → Installed apps → Muse Voice
  Mode Shortcut → ON). Second suspect is an old-package duplicate
  from the rename era soaking up the mapping — check which
  package the side button / accessibility grant points at.
- If a trigger "did nothing": the hunt may have timed out (~9 s)
  without finding its button — the candidates are unproven (see
  verification status above). Report that honestly; don't claim
  the session started without live evidence.
- The Buds **Always** binding is system-wide: if Jonathan
  complains that some other assistant trigger now opens Muse
  voice, that's this binding, and the undo is Settings → Apps →
  Muse Voice Mode Shortcut → Set as default → Clear defaults,
  then re-pick in the Buds flow. Don't change that default
  without his say.

## Guardrails

- **Never uninstall-then-reinstall a same-package upgrade** —
  in-place `adb install -r` is what preserves the accessibility
  grant, switch state, and hardware mappings.
- Never print, copy, or commit `keystore/keystore-password.txt`
  or the keystore's contents beyond its path; scrub them from any
  GitHub export (the `github-export/` snapshot is the scrubbed
  pattern to follow).
- Don't rename the package or the cert DN casually: either one
  breaks in-place upgrades and forces the full reinstall +
  re-grant + re-map sequence on Jonathan.
- Every build/change upstreams in the same handling: source push
  + GitHub Release (Latest, asset SHA verified against the
  build) + README update + this skill (both copies) if the
  procedure changed.

## Pointers

- Full record, per-version history, and setup steps:
  `~/workspace/muse-voice-mode-shortcut/README.md`
- Manifest truth: `~/workspace/muse-voice-mode-shortcut/AndroidManifest.xml`
- Sibling using the same accessibility trick:
  `~/workspace/muse-android-auto/` (Muse for Android Auto /
  Muse Drive)
