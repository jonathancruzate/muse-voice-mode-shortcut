package com.jonathancruzate.metamusevoicemodeshortcut;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/** The accessibility trick, lifted from Muse for Android Auto
 *  (DriveAccessibilityService v2.1/v2.0) — both halves of the toggle:
 *
 *  START (v2.1 trick): hatch://voice is TrustedCaller-gated to
 *  Meta-signed apps, so no outside app can deep-link into Muse
 *  voice. Instead the shortcut opens Muse via ACTION_ASSIST and
 *  this service taps Muse's own voice button when the Muse window
 *  appears (VoiceState.awaitingVoiceTap).
 *
 *  END (v2.0 trick): same hunt against Muse's end-call control
 *  (VoiceState.awaitingEndTap), fallback Back -> Home.
 *
 *  Hunt candidates (REA-derived, identical to Muse Drive):
 *    voice: view-ids hatch_voice_button / voice_button /
 *           voice_call_button / start_voice_button, then exact
 *           labels "Voice call" / "Start voice call" / "Voice mode"
 *           / "Voice" / "Talk"
 *    end:   view-ids end_voice_button / end_call_button /
 *           voice_end_call, then exact labels "End call" /
 *           "End voice call" / "Hang up" / "Leave call" / "End"
 *  Each hunt retries for ~9s while the UI renders; a short buzz
 *  confirms the tap landed. The service does nothing at all unless
 *  a shortcut tap armed a hunt, and only ever inspects the Muse
 *  window. */
public class VoiceAccessibilityService extends AccessibilityService {

    public static volatile VoiceAccessibilityService instance;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        instance = this;
    }

    @Override
    public void onDestroy() {
        if (instance == this) instance = null;
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null
                || !"com.facebook.aura".contentEquals(event.getPackageName())) {
            return;
        }
        if (VoiceState.awaitingVoiceTap) scheduleVoiceHunt();
        if (VoiceState.awaitingEndTap) scheduleEndHunt();
    }

    @Override
    public void onInterrupt() {}

    // ---- START hunt ----

    private int voiceHuntAttempts = 0;
    private boolean voiceHuntRunning = false;
    private final Runnable voiceHuntStep = new Runnable() {
        @Override
        public void run() {
            if (!VoiceState.awaitingVoiceTap) {
                voiceHuntRunning = false;
                return;
            }
            if (tryTapVoiceButton()) {
                VoiceState.awaitingVoiceTap = false;
                voiceHuntRunning = false;
                VoiceLauncher.buzz(VoiceAccessibilityService.this, 80);
                return;
            }
            voiceHuntAttempts++;
            if (voiceHuntAttempts >= 10) { // ~9s of retries, then stop
                VoiceState.awaitingVoiceTap = false;
                voiceHuntRunning = false;
                // Switch stays ON: the tap armed a session start and
                // Muse is open; if the button never rendered the
                // next tap still reads as OFF, which is the safe
                // direction (it can only try to end, never stack
                // a second start hunt).
                return;
            }
            handler.postDelayed(this, 900);
        }
    };

    private void scheduleVoiceHunt() {
        if (voiceHuntRunning) return;
        voiceHuntRunning = true;
        voiceHuntAttempts = 0;
        handler.postDelayed(voiceHuntStep, 400);
    }

    private boolean tryTapVoiceButton() {
        AccessibilityNodeInfo root = museRoot();
        if (root == null) return false;
        String[] ids = {
                "com.facebook.aura:id/hatch_voice_button",
                "com.facebook.aura:id/voice_button",
                "com.facebook.aura:id/voice_call_button",
                "com.facebook.aura:id/start_voice_button",
        };
        for (String id : ids) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes != null && !nodes.isEmpty() && clickNode(nodes.get(0))) return true;
        }
        String[] labels = {"Voice call", "Start voice call", "Voice mode", "Voice", "Talk"};
        return tapByLabel(root, labels);
    }

    // ---- END hunt ----

    private int endHuntAttempts = 0;
    private boolean endHuntRunning = false;
    private final Runnable endHuntStep = new Runnable() {
        @Override
        public void run() {
            if (!VoiceState.awaitingEndTap) {
                endHuntRunning = false;
                return;
            }
            if (tryTapEndButton()) {
                VoiceState.awaitingEndTap = false;
                endHuntRunning = false;
                return;
            }
            endHuntAttempts++;
            if (endHuntAttempts >= 10) { // ~9s, then the fallback chain
                VoiceState.awaitingEndTap = false;
                endHuntRunning = false;
                performGlobalAction(GLOBAL_ACTION_BACK);
                handler.postDelayed(new Runnable() {
                    @Override public void run() { performGlobalAction(GLOBAL_ACTION_HOME); }
                }, 600);
                return;
            }
            handler.postDelayed(this, 900);
        }
    };

    private void scheduleEndHunt() {
        if (endHuntRunning) return;
        endHuntRunning = true;
        endHuntAttempts = 0;
        handler.postDelayed(endHuntStep, 400);
    }

    private boolean tryTapEndButton() {
        AccessibilityNodeInfo root = museRoot();
        if (root == null) return false;
        String[] ids = {
                "com.facebook.aura:id/end_voice_button",
                "com.facebook.aura:id/end_call_button",
                "com.facebook.aura:id/voice_end_call",
        };
        for (String id : ids) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes != null && !nodes.isEmpty() && clickNode(nodes.get(0))) return true;
        }
        String[] labels = {"End call", "End voice call", "Hang up", "Leave call", "End"};
        return tapByLabel(root, labels);
    }

    // ---- shared helpers ----

    private AccessibilityNodeInfo museRoot() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || root.getPackageName() == null
                || !"com.facebook.aura".contentEquals(root.getPackageName())) {
            return null;
        }
        return root;
    }

    private boolean tapByLabel(AccessibilityNodeInfo root, String[] labels) {
        for (String label : labels) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(label);
            if (nodes != null) {
                for (AccessibilityNodeInfo n : nodes) {
                    CharSequence t = n.getText();
                    CharSequence d = n.getContentDescription();
                    if ((t != null && label.equalsIgnoreCase(t.toString()))
                            || (d != null && label.equalsIgnoreCase(d.toString()))) {
                        if (clickNode(n)) return true;
                    }
                }
            }
            if (searchByDescription(root, label)) return true;
        }
        return false;
    }

    private boolean searchByDescription(AccessibilityNodeInfo node, String label) {
        if (node == null) return false;
        CharSequence desc = node.getContentDescription();
        if (desc != null && label.equalsIgnoreCase(desc.toString()) && clickNode(node)) {
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            if (searchByDescription(node.getChild(i), label)) return true;
        }
        return false;
    }

    private boolean clickNode(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo n = node;
        while (n != null) {
            if (n.isClickable()) {
                return n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            n = n.getParent();
        }
        return false;
    }
}
