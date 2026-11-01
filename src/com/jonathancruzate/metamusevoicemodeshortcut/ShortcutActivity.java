package com.jonathancruzate.metamusevoicemodeshortcut;

import android.app.Activity;
import android.os.Bundle;

/** The shortcut target: no UI, ever. Flips the toggle — OFF -> ON
 *  starts a Muse voice session, ON -> OFF ends it — and finishes
 *  immediately. This is what the long-press app shortcut and any
 *  pinned home-screen shortcut point at. */
public class ShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VoiceLauncher.toggle(this);
        finish();
    }
}
