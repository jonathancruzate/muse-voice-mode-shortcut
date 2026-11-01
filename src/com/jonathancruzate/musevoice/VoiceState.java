package com.jonathancruzate.musevoice;

import android.content.Context;
import android.content.SharedPreferences;

/** State for the toggle.
 *
 *  The shortcut is a SWITCH (Jonathan's tweak, 2026-11-01): each
 *  trigger flips the session — OFF -> ON starts voice, ON -> OFF
 *  ends it, alternating. That state has to survive the shortcut
 *  activity finishing and the process being killed between taps,
 *  so sessionActive lives in SharedPreferences, not just memory.
 *
 *  awaitingVoiceTap / awaitingEndTap are transient hunt flags: set
 *  BEFORE Muse is opened, the accessibility service taps Muse's
 *  own voice / end-call button when the Muse window appears.
 *  (hatch://voice is TrustedCaller-gated to Meta-signed apps; the
 *  tap is the only way in — same trick as Muse for Android Auto.) */
public final class VoiceState {
    private static final String PREFS = "voice_state";
    private static final String KEY_ACTIVE = "session_active";

    public static volatile boolean awaitingVoiceTap = false;
    public static volatile boolean awaitingEndTap = false;
    /** In-memory mirror of the persisted flag; prefs are truth. */
    public static volatile boolean sessionActive = false;

    private VoiceState() {}

    public static boolean isActive(Context ctx) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            sessionActive = p.getBoolean(KEY_ACTIVE, sessionActive);
        } catch (Exception ignored) {
        }
        return sessionActive;
    }

    public static void setActive(Context ctx, boolean active) {
        sessionActive = active;
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            p.edit().putBoolean(KEY_ACTIVE, active).apply();
        } catch (Exception ignored) {
        }
    }
}
