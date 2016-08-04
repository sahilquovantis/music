package com.music.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.music.R;
import com.music.utility.MusicHelper;
import com.music.utility.NotificationHelper;
import com.music.utility.Utils;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    private MediaSession mMediaSession;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private PlaybackState mPlaybackState;
    private MediaController mMediaController;
    private Binder mBinder = new ServiceBinder();
    private boolean mIsPlayOnFocusGain;
    private MediaMetadata mCurrentMetaData;
    private NotificationHelper mNotificationHelper;

    public MusicService() {
    }

    /**
     * Create and Reset Media Player if Needed
     */
    private void createMediaPlayerIfNeeded() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            Log.d("Training", "New Media Player Created");
        } else {
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.reset();
            Log.d("Training", "Media Player reset");
        }
    }

    /**
     * Used to Get the ID of the Current Song.
     * @return Returns the ID of the Current Song
     */
    private String getCurrentMediaID() {
        return (mCurrentMetaData == null ? null : mCurrentMetaData.getDescription().getMediaId());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Training", "On Create Service");
        mPlaybackState = new PlaybackState.Builder()
                .setState(PlaybackState.STATE_NONE, 0, 1.0f)
                .build();
        mMediaSession = new MediaSession(this, "Training");
        mMediaSession.setCallback(mMediaCallback);
        mMediaSession.setActive(true);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setPlaybackState(mPlaybackState);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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

    /**
     * Media Callbacks : Called when the Song is Paused , Played, Stopped, Skipped etc.
     */
    private MediaSession.Callback mMediaCallback = new MediaSession.Callback() {

        @Override
        public void onPlay() {
            Log.d("Training", "OnPlay");
            if (getCurrentMediaID() != null) {
                onPlayFromMediaId(getCurrentMediaID(), null);
            }
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d("Training", "OnPlayFromMetaData");
            if (mediaId != null && mMediaSession != null) {
                MediaMetadata metadata = MusicHelper.getInstance().getMetadata(getApplicationContext(), mediaId);
                initializeMediaPlayerWithUri(metadata);
            }
        }

        @Override
        public void onPause() {
            Log.d("Training", "OnPause");
            if (isPlaying() && mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                mMediaPlayer.pause();
                updatePlaybackState(PlaybackState.STATE_PAUSED);
            }
        }

        @Override
        public void onSkipToNext() {
            Log.d("Training", "OnSkip");
            String mediaid = MusicHelper.getInstance().getNextSong(getCurrentMediaID());
            onPlayFromMediaId(mediaid, null);
        }

        @Override
        public void onSkipToPrevious() {
            Log.d("Training", "OnPrevious");
            String mediaid = MusicHelper.getInstance().getPreviousSong(getCurrentMediaID());
            onPlayFromMediaId(mediaid, null);
        }

        @Override
        public void onStop() {
            Log.d("Training", "OnStop");
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                updatePlaybackState(PlaybackState.STATE_STOPPED);
            }
            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(MusicService.this);
                mAudioManager = null;
            }
            Intent intent = new Intent(Utils.INTENT_ACTION_STOP);
            sendBroadcast(intent);
            stopSelf();
        }
    };

    /**
     * use to initialize a media player with {@link MusicHelper}
     * @param metadata metadata of a music file
     */
    private void initializeMediaPlayerWithUri(MediaMetadata metadata) {
        String mediaId = metadata.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMetaData == null || !getCurrentMediaID().equals(mediaId));
        if (mediaChanged) {
            createMediaPlayerIfNeeded();
            try {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(getApplicationContext(), MusicHelper.getInstance().getSongURI(mediaId));
                mCurrentMetaData = metadata;
                mMediaSession.setActive(true);
                mMediaSession.setMetadata(metadata);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                Log.d("Training", "IOException On setData Source : " + e.toString());
            }
        } else {
            if (mMediaController.getPlaybackState().getState() == PlaybackState.STATE_PAUSED) {
                if (tryToGetAudioFocus()) {
                    startPlayer();
                } else {
                    mIsPlayOnFocusGain = true;
                }

            }
        }
    }

    /**
     * @return Returns the Media Session Token
     */
    public MediaSession.Token getMediaSessionToken() {
        return mMediaSession == null ? null : mMediaSession.getSessionToken();
    }

    /**
     * Called When the Song is Completed.
     * @param mediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d("Training", "OnSong Completed");
        mMediaController.getTransportControls().skipToNext();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.d("Training", "Error Media Player : " + i);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("Training", "OnPrepared");
        if (mediaPlayer != null) {
            if (tryToGetAudioFocus()) {
                startPlayer();
            } else {
                mIsPlayOnFocusGain = true;
            }
        }
    }

    /**
     * Here the Song is Started Playing and We Update the Playback State
     */
    private void startPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(this);
            updatePlaybackState(PlaybackState.STATE_PLAYING);
        }
    }

    /**
     * Update The Playback States and Create the Notification
     * @param state State Defines the MusicPlayer state like Paused, Playing, Stopped.
     */
    private void updatePlaybackState(int state) {
        if (mMediaCallback == null) {
            return;
        }
        if (state == PlaybackState.STATE_PLAYING) {
            mNotificationHelper.createNotification(mCurrentMetaData, getMediaSessionToken(),
                    R.drawable.ic_action_pause, "Pause", Utils.INTENT_ACTION_PAUSE);
        } else if(state == PlaybackState.STATE_PAUSED){
            mNotificationHelper.createNotification(mCurrentMetaData, getMediaSessionToken(),
                    R.drawable.ic_action_play, "Play", Utils.INTENT_ACTION_PLAY);
        }
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(getAvailableActions());
        stateBuilder.setState(state, getCurrentStreamPosition(), 1.0f);
        mMediaSession.setPlaybackState(stateBuilder.build());
    }

    private int getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    /**
     * Used to Check whether Song is Playing or not
     * @return True or False on the basis of Song is Playing or not
     */
    private boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    /**
     * Used TO Get The Actions on the Media Player a User can Perform like
     * User can Stop, pause, skip etc.
     * @return Returns All the Available Actions
     */
    private long getAvailableActions() {
        long actions = PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_FROM_SEARCH |
                PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        if (isPlaying()) {
            actions |= PlaybackState.ACTION_PAUSE;
        }
        return actions;
    }

    /**
     * Used to Check if the App has Audio Focus Or Not.
     * @return
     */
    private boolean tryToGetAudioFocus() {
        int result = mAudioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * Called When The Audio Focus Changes like When we gain focus or we loss focus
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        boolean gotFullFocus = false;
        boolean canDuck = false;
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            gotFullFocus = true;

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
        }
        if (mMediaController != null) {
            int mState = mMediaController.getPlaybackState().getState();

            if (gotFullFocus || canDuck) {
                if (mMediaPlayer != null) {
                    if (mIsPlayOnFocusGain) {
                        startPlayer();
                    }
                    float volume = canDuck ? 0.2f : 1.0f;
                    mMediaPlayer.setVolume(volume, volume);
                }
            } else if (mState == PlaybackState.STATE_PLAYING) {
                mMediaPlayer.pause();
                mState = PlaybackState.STATE_PAUSED;
                updatePlaybackState(mState);
            }
        }
    }

    /**
     * Bins the Service {@link com.music.ui.HomeActivity}
     */
    public class ServiceBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Training", "Service Stopped");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mMediaSession != null) {
            mMediaSession.release();
            mMediaSession = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Training", "OnBind Called");
        return mBinder;
    }

    /**
     * Called when the App is Forcefully Removed From the History.
     * @param rootIntent
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("Training","On Task Removed");
        mNotificationHelper.cancelNotification();
        mMediaController.getTransportControls().stop();
    }
}
