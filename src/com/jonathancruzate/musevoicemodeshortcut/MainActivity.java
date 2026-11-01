package com.jonathancruzate.musevoicemodeshortcut;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** The launcher icon IS the switch: with the accessibility service
 *  enabled, tapping "Muse Voice Mode Shortcut" toggles — OFF -> ON starts a Muse
 *  voice session, ON -> OFF ends it (this activity flips the switch
 *  and finishes before any UI draws). With the service not yet
 *  enabled, the same tap shows the one-time setup screen instead of
 *  failing silently. */
public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isA11yEnabled()) {
            // Configured: the tap is the switch. No UI flash.
            VoiceLauncher.toggle(this);
            finish();
            return;
        }

        // Not configured yet: one-time setup screen.
        LinearLayout root = new LinearLayout(this);
        root.setBackgroundColor(0xFFFFFFFF);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("Muse Voice Mode Shortcut");
        title.setTextSize(24);
        root.addView(title);

        TextView body = new TextView(this);
        body.setText("\nThis icon is a switch for Muse voice mode.\n"
                + "Tap once = ON, voice session starts. Tap again = OFF, session ends. "
                + "On, off, on, off — every tap flips it.\n\n"
                + "How it works: ON opens Muse and this app's accessibility service "
                + "taps Muse's own voice button for you; OFF brings Muse forward "
                + "and taps its end-call button. (Muse's voice deep link is locked "
                + "to Meta's own apps, so the tap is the only way in — same trick "
                + "as Muse for Android Auto.)\n\n"
                + "One-time setup: enable the Muse Voice Mode Shortcut accessibility service. "
                + "It only ever looks at the Muse window, and only right after "
                + "you tap this icon.\n");
        body.setTextSize(15);
        root.addView(body);

        root.addView(button("Enable accessibility service", new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }));

        status = new TextView(this);
        status.setTextSize(15);
        root.addView(status);

        root.addView(button("Add home-screen shortcut", new View.OnClickListener() {
            @Override public void onClick(View v) { pinShortcut(); }
        }));

        root.addView(button("Try the switch now", new View.OnClickListener() {
            @Override public void onClick(View v) {
                VoiceLauncher.toggle(MainActivity.this);
                refreshStatus();
            }
        }));

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        if (status == null) return;
        status.setText("\nStatus: accessibility " + (isA11yEnabled() ? "ON" : "OFF")
                + " — voice switch is " + (VoiceState.isActive(this) ? "ON (next tap turns it OFF)" : "OFF (next tap turns it ON)"));
    }

    private boolean isA11yEnabled() {
        if (VoiceAccessibilityService.instance != null) return true;
        try {
            String enabled = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (enabled == null) return false;
            String mine = new ComponentName(this, VoiceAccessibilityService.class).flattenToString();
            return enabled.contains(mine);
        } catch (Exception e) {
            return false;
        }
    }

    private void pinShortcut() {
        if (Build.VERSION.SDK_INT < 26) {
            Toast.makeText(this, "Pinned shortcuts need Android 8+", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            ShortcutManager sm = getSystemService(ShortcutManager.class);
            if (sm == null || !sm.isRequestPinShortcutSupported()) {
                Toast.makeText(this, "This launcher doesn't support pinned shortcuts — long-press the icon instead", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, ShortcutActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            ShortcutInfo info = new ShortcutInfo.Builder(this, "muse-voice-mode")
                    .setShortLabel("Muse Voice Mode Shortcut")
                    .setLongLabel("Muse Voice Mode Shortcut")
                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                    .setIntent(intent)
                    .build();
            sm.requestPinShortcut(info, null);
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't pin the shortcut on this launcher", Toast.LENGTH_LONG).show();
        }
    }

    private Button button(String label, View.OnClickListener onClick) {
        Button b = new Button(this);
        b.setText(label);
        b.setOnClickListener(onClick);
        return b;
    }
}
