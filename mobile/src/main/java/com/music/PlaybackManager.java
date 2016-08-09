package com.music;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.music.utility.MusicHelper;

import java.io.IOException;

/**
 * Created by sahil-goel on 5/8/16.
 */
public class PlaybackManager implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private Context mContext;
    private boolean mPlayOnFocusGain;
    private ICallback mCallback;
    private MediaMetadata mCurrentMedia;
    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private int mCurrentState;

    public PlaybackManager(Context context, ICallback callback) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mCallback = callback;
    }

    public MediaMetadata getMediaMetaData() {
        return mCurrentMedia;
    }

    public void setMediaData(MediaMetadata mediaData) {
        mCurrentMedia = mediaData;
        mMediaPlayer.reset();
        mCurrentState = PlaybackState.STATE_NONE;
        updatePlaybackState(PlaybackState.STATE_NONE);
    }

    public String getCurrentMediaId() {
        return mCurrentMedia == null ? null : mCurrentMedia.getDescription().getMediaId();
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(this);
            mAudioManager = null;
        }
        mCurrentMedia = null;
        updatePlaybackState(PlaybackState.STATE_STOPPED);
    }

    public void none() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mCurrentMedia = null;
        updatePlaybackState(PlaybackState.STATE_NONE);
    }

    public void pause() {
        if (isPlaying() && mCurrentState == PlaybackState.STATE_PLAYING) {
            mMediaPlayer.pause();
            updatePlaybackState(PlaybackState.STATE_PAUSED);
        }
    }

    /**
     * Create and Reset Media Player if Needed
     */
    private void createMediaPlayerIfNeeded(boolean mediaChanged) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
            Log.d("Training", "New Media Player Created");
        } else {
            if (mediaChanged) {
                mMediaPlayer.setOnCompletionListener(null);
                mMediaPlayer.reset();
                Log.d("Training", "Media Player reset");
            }
        }
    }

    public void play(MediaMetadata metadata) {
        String mediaId = metadata.getDescription().getMediaId();
        boolean mediaChanged = (mCurrentMedia == null || !getCurrentMediaId().equals(mediaId));
        createMediaPlayerIfNeeded(mediaChanged);
        if (mediaChanged) {
            try {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(mContext, MusicHelper.getInstance().getSongURI(mediaId));
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mCurrentMedia = metadata;
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                mCurrentMedia = null;
                Toast.makeText(mContext, "File not found", Toast.LENGTH_LONG).show();
                updatePlaybackState(PlaybackState.STATE_NONE);
            }
        } else {
            if (tryToGetAudioFocus()) {
                startPlayer();
            } else {
                mPlayOnFocusGain = true;
            }
        }
    }

    /**
     * Try to get the system audio focus.
     */
    private boolean tryToGetAudioFocus() {
        int result = mAudioManager.requestAudioFocus(
                this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void updatePlaybackState(int state) {
        if (mCallback == null) {
            mCurrentState = -1;
            return;
        }
        mCurrentState = state;
        PlaybackState.Builder playbackState = new PlaybackState.Builder()
                .setActions(getAvailableActions())
                .setState(state, getCurrentStreamPosition(), 1.0f, SystemClock.elapsedRealtime());
        mCallback.onPlaybackStateChanged(playbackState.build());
    }

    private int getCurrentStreamPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

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
     * Used to Check whether Song is Playing or not
     *
     * @return True or False on the basis of Song is Playing or not
     */
    private boolean isPlaying() {
        return (mMediaPlayer != null && mMediaPlayer.isPlaying());
    }

    @Override
    public void onAudioFocusChange(int focus) {
        boolean gotFullFocus = false;
        boolean canDuck = false;
        if (focus == AudioManager.AUDIOFOCUS_GAIN) {
            gotFullFocus = true;
        } else if (focus == AudioManager.AUDIOFOCUS_LOSS || focus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            if (mCurrentState == PlaybackState.STATE_PLAYING && isPlaying()) {
                mMediaPlayer.pause();
                updatePlaybackState(PlaybackState.STATE_PAUSED);
            }
        } else if (focus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            canDuck = true;
        }

        if (gotFullFocus || canDuck) {
            if (mMediaPlayer != null) {
                float volume = canDuck ? 0.2f : 1.0f;
                mMediaPlayer.setVolume(volume, volume);
                if (mPlayOnFocusGain) {
                    startPlayer();
                }
            }
        }
    }

    private void startPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(this);
            updatePlaybackState(PlaybackState.STATE_PLAYING);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mCallback != null) {
            mCallback.onSongCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("Training", "OnPrepared");
        if (mediaPlayer != null) {
            if (tryToGetAudioFocus()) {
                startPlayer();
            } else {
                mPlayOnFocusGain = true;
            }
        }
    }

    public interface ICallback {
        void onPlaybackStateChanged(PlaybackState state);

        void onSongCompletion();
    }
}
