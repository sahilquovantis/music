package com.music.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.music.services.MusicService;
import com.music.utility.Utils;

/**
 * @author sahil-goel
 * This Receiver handles the Action when the HeadPhones or HeadSets are removed from the Device.
 * And Pause the Song When HeadPhone is Removed.
 */
public class NoisyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d("Training", "On Noisy Receiver");
            Intent intent1 = new Intent(context, MusicService.class);
            intent1.setAction(Utils.INTENT_ACTION_PAUSE);
            context.startService(intent1);
        }
    }
}
