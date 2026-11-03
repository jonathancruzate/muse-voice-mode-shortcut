package com.jonathancruzate.metamusevoicemodeshortcut;

import android.app.Activity;
import android.os.Bundle;

/** The shortcut target: no UI, ever. Flips the toggle — OFF -> ON
 *  starts a Muse voice session, ON -> OFF ends it — and finishes
 *  immediately. This is what the long-press app shortcut, any
 *  pinned home-screen shortcut, and (v1.7.0) the digital-assistant
 *  intents point at: the manifest declares ACTION_ASSIST +
 *  ACTION_VOICE_COMMAND on this activity, so the Galaxy Buds
 *  touch-and-hold "Digital assistant" chooser ("Complete action
 *  using") can select it — every Buds long press then lands here
 *  and flips the switch, same as the side button. */
public class ShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VoiceLauncher.toggle(this);
        finish();
    }
}
