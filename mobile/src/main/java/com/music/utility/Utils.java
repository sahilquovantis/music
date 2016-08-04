package com.music.utility;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sahil-goel on 22/7/16.
 */
public class Utils {
    public static final String INTENT_ACTION_SHOW_ACTIVITY = "com.music.action.show.activity";
    public static final String INTENT_ACTION_PAUSE = "com.music.action.pause";
    public static final String INTENT_ACTION_PLAY = "com.music.action.play";
    public static final String INTENT_ACTION_STOP = "com.music.action.stop";
    public static final String INTENT_ACTION_NEXT = "com.music.action.next";
    public static final String INTENT_ACTION_PREVIOUS = "com.music.action.previous";
    public static final String SHARED_PREFERENCE_FILE_NAME = "com.music.shared.preferences.file.name";
    public static final String SHARED_PREFERENCE_FIRST_TIME_OPEN = "FIRST_TIME_OPEN";
    public static SharedPreferences sSharedPreferences;
}
