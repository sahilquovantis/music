package com.music.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.music.PlaybackManager;
import com.music.R;
import com.music.utility.MusicHelper;
import com.music.utility.NotificationHelper;
import com.music.utility.Utils;

/**
 * Created by sahil-goel on 5/8/16.
 */
public class MusicService extends Service implements PlaybackManager.ICallback {

    private Binder mBinder = new ServiceBinder();
    private MediaSession mMediaSession;
    private MediaController mMediaController;
    private NotificationHelper mNotificationHelper;
    private PlaybackManager mPlaybackManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaSession = new MediaSession(this, "Training");
        mMediaSession.setCallback(mMediaCallback);
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setPlaybackState(new PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build());
        mPlaybackManager = new PlaybackManager(getApplicationContext(), this);
        mMediaController = new MediaController(this, getMediaSessionToken());
        mNotificationHelper = new NotificationHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Training", "OnStart Commend Service");
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (action.equals(Utils.INTENT_ACTION_PAUSE)) {
                mMediaController.getTransportControls().pause();
            } else if (action.equals(Utils.INTENT_ACTION_STOP)) {
                mMediaController.getTransportControls().stop();
            } else if (action.equals(Utils.INTENT_ACTION_NEXT)) {
                mMediaController.getTransportControls().skipToNext();
            } else if (action.equals(Utils.INTENT_ACTION_PREVIOUS)) {
                mMediaController.getTransportControls().skipToPrevious();
            } else if (action.equals(Utils.INTENT_ACTION_PLAY)) {
                mMediaController.getTransportControls().play();
            }
        }
        return START_NOT_STICKY;
    }

    private MediaSession.Callback mMediaCallback = new MediaSession.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            onPlayFromMediaId(mPlaybackManager.getMediaMetaData().getDescription().getMediaId(),
                    null);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            if (mediaId != null) {
                MediaMetadata mediaMetadata = MusicHelper.getInstance().getMetadata(getApplicationContext(),
                        mediaId);
                if (mediaMetadata == null) {
                    mPlaybackManager.setMediaData(null);
                } else {
                    mMediaSession.setActive(true);
                    mMediaSession.setMetadata(mediaMetadata);
                    mPlaybackManager.play(mediaMetadata);
                }
            } else {
                /**
                 * Cancels Notification and Update the State to NONE so that Music Player
                 * also gets hide.
                 */
                mPlaybackManager.setMediaData(null);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            mPlaybackManager.pause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            String mediaID = MusicHelper.getInstance().getNextSong(mPlaybackManager.getCurrentMediaId());
            onPlayFromMediaId(mediaID, null);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            String mediaID = MusicHelper.getInstance().getPreviousSong(mPlaybackManager.getCurrentMediaId());
            onPlayFromMediaId(mediaID, null);
        }

        @Override
        public void onStop() {
            super.onStop();
            mPlaybackManager.stop();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            Log.d("Training", "SEEK : " + pos);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            if (action.equals("NONE")) {
                mPlaybackManager.none();
            }
        }
    };

    /**
     * @return Returns the Media Session Token
     */
    public MediaSession.Token getMediaSessionToken() {
        return mMediaSession == null ? null : mMediaSession.getSessionToken();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState state) {
        mMediaSession.setPlaybackState(state);
        if (state.getState() == PlaybackState.STATE_PLAYING) {
            mNotificationHelper.createNotification(
                    mPlaybackManager.getMediaMetaData(),
                    getMediaSessionToken(),
                    R.drawable.ic_action_pause, "Pause",
                    Utils.INTENT_ACTION_PAUSE);
        } else if (state.getState() == PlaybackState.STATE_PAUSED) {
            mNotificationHelper.createNotification(
                    mPlaybackManager.getMediaMetaData(),
                    getMediaSessionToken(),
                    R.drawable.ic_action_play, "Play",
                    Utils.INTENT_ACTION_PLAY);
        } else {
            if (state.getState() == PlaybackState.STATE_STOPPED) {
                Intent intent = new Intent(Utils.INTENT_ACTION_STOP);
                sendBroadcast(intent);
                stopSelf();
            }
            mNotificationHelper.cancelNotification();
        }
    }

    @Override
    public void onSongCompletion() {
        String mediaId = MusicHelper.getInstance().getNextSong(mPlaybackManager.getCurrentMediaId());
        if (mediaId == null) {
            mPlaybackManager.setMediaData(null);
        } else {
            mMediaController.getTransportControls().playFromMediaId(mediaId, null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Training", "Service Stopped");
        if (mMediaSession != null) {
            mMediaSession.release();
            mMediaSession = null;
        }
        mMediaController = null;
    }

    /**
     * Bins the Service {@link com.music.activities.BaseActivity}
     */
    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Training", "OnBind Called");
        return mBinder;
    }
}
