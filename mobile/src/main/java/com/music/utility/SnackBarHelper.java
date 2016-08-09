package com.music.utility;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by sahil-goel on 9/8/16.
 */
public class SnackBarHelper {
    public static void showSnackbar(View view , String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
