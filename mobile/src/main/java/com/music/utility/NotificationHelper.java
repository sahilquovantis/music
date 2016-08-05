package com.music.utility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import com.music.services.MusicService;
import com.music.ui.HomeActivity;
import com.music.R;

/**
 * @author sahil-goel
 * This class creates and cancels the Notification {@link MusicService}
 */
public class NotificationHelper {

    private Context mContext;
    private NotificationManager mNotificationManager;

    public NotificationHelper(Context context) {
        mContext = context;
    }

    /**
     * Create the Notification for Current Song
     * @param mCurrentMetaData Current Song MEdiaMetaData
     * @param token MediaSession Token
     * @param iconForPlayPause
     * @param playPause
     * @param action
     */
    public void createNotification(MediaMetadata mCurrentMetaData, MediaSession.Token token,
                                   int iconForPlayPause, String playPause, String action) {
        if (mCurrentMetaData != null && token != null) {
            String title = mCurrentMetaData.getDescription().getTitle().toString();
            String artist = mCurrentMetaData.getDescription().getSubtitle().toString();
            Bitmap image = mCurrentMetaData.getDescription().getIconBitmap();

            Notification.MediaStyle style = new Notification.MediaStyle();
            style.setMediaSession(token);

            Intent intent = new Intent(mContext, MusicService.class);
            intent.setAction(Utils.INTENT_ACTION_STOP);
            PendingIntent pendingIntent = PendingIntent.getService(mContext, 1, intent, 0);

            Intent showActivityIntent = new Intent(mContext, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(Utils.INTENT_ACTION_SHOW_ACTIVITY);
            PendingIntent showActivityPendingIntent = PendingIntent.getActivity(mContext, 1, showActivityIntent, 0);

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(mContext)
                    .setSmallIcon(iconForPlayPause)
                    .setContentTitle(title)
                    .setContentText(artist)
                    .setContentIntent(showActivityPendingIntent)
                    .setLargeIcon(image)
                    .setDeleteIntent(pendingIntent)
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(MediaSessionCompat.Token.fromToken(token)))
                    .setColor(0xff5722);
            builder.addAction(createAction(R.drawable.ic_action_previous, "Previous", Utils.INTENT_ACTION_PREVIOUS));
            builder.addAction(createAction(iconForPlayPause, playPause, action));
            builder.addAction(createAction(R.drawable.ic_action_next, "Next", Utils.INTENT_ACTION_NEXT));
            style.setShowActionsInCompactView(0, 1, 2);
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, builder.build());
        }
    }

    /**
     * It Cancel the Notification.
     */
    public void cancelNotification() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
    }

    /**
     * Create Pending Actions For Notification and Used When Icons Clicked from Notification
     * @param icon Icon For Action
     * @param title Title For Action
     * @param intentAction IntentAction used to Differentiate Intents
     * @return Returns Notification Action
     */
    private NotificationCompat.Action createAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(mContext, MusicService.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }
}
