package app.morphe.extension.boostforreddit.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

public class WelcomeDialogUtils {
    private static final String PREF_NAME = "patcheddit_welcome_prefs";
    private static final String KEY_SHOWN = "shown_welcome_v1.4.0";

    public static void showWelcomeDialog(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SHOWN, false)) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activity.isFinishing()) {
                    return;
                }
                new AlertDialog.Builder(activity)
                        .setTitle("Patched Successfully!")
                        .setMessage("Your patched Boost client is running correctly.\n\n" +
                                "Latest Feature Changes:\n" +
                                "• Redgifs Playback Fix (Solved 10-15s delay & audio disappearing on network switch)\n" +
                                "• Download All Gallery Media (Download all images/videos at once via option next to download button)")
                        .setPositiveButton("Awesome", null)
                        .show();

                prefs.edit().putBoolean(KEY_SHOWN, true).apply();
            }
        });
    }
}
