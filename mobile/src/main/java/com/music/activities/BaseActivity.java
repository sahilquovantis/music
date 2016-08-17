package com.music.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.R;
import com.music.services.MusicService;
import com.music.utility.Utils;

import butterknife.BindView;
import butterknife.OnClick;

public class BaseActivity extends AppCompatActivity implements ServiceConnection, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.iv_selected_song_thumbnail)
    ImageView mSelectedSongThumbnailIV;
    @BindView(R.id.tv_selected_song)
    public TextView mSelectedSongTV;
    @BindView(R.id.tv_selected_song_artist)
    public TextView mSelectedSongArtistTV;
    @BindView(R.id.rl_music_layout)
    public RelativeLayout mMusicLayoutRL;
    @BindView(R.id.iv_play_pause_button)
    public ImageView mPlayPaueIV;
    @BindView(R.id.iv_previous_song)
    public ImageView mPreviousSongIV;
    @BindView(R.id.iv_next_song)
    public ImageView mNextSongIV;
    public MediaMetadata mCurrentMetadata;
    public PlaybackState mCurrentState;
    public MediaController mediaController;
    public DrawerLayout mDrawerLayout;
    public NavigationView mNavigationView;
    public ActionBarDrawerToggle mActionBarToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    /**
     * Update Music Player UI With the Details of Current Song like Title, Thumbnail, Artist at Bottom
     *
     * @param mediaMetadata Contains Information of Current Song Selected like Title, Thumbanil, Artist
     */
    public void updateUI(MediaMetadata mediaMetadata) {
        mCurrentMetadata = mediaMetadata;
        if (mediaMetadata != null) {
            String title = mediaMetadata.getDescription().getTitle().toString();
            String artist = mediaMetadata.getDescription().getSubtitle().toString();
            Bitmap bitmap = mediaMetadata.getDescription().getIconBitmap();
            mSelectedSongTV.setText(title);
            mSelectedSongArtistTV.setText(artist);
            mSelectedSongThumbnailIV.setImageBitmap(bitmap);
        }
    }

    /**
     * Update the State of Media Player like Whether it is Playing or Paused and We Update the ImageView
     * and set the icons of Play and Pause
     *
     * @param state Contains Information whether the music player is Playing or Paused or Stopped
     */
    public void updateState(PlaybackState state) {
        mCurrentState = state;
        Log.d("Training", "Activity Current Playback State : " + state);
        mMusicLayoutRL.setVisibility(View.GONE);
        if (state == null || state.getState() == PlaybackState.STATE_NONE || mCurrentMetadata == null) {
            mMusicLayoutRL.setVisibility(View.GONE);
            return;
        } else if (state.getState() == PlaybackState.STATE_PAUSED || state.getState() == PlaybackState.STATE_STOPPED) {
            mPlayPaueIV.setImageResource(R.drawable.ic_action_play);
            mMusicLayoutRL.setVisibility(View.VISIBLE);
        } else if (state.getState() == PlaybackState.STATE_PLAYING) {
            mPlayPaueIV.setImageResource(R.drawable.ic_action_pause);
            mMusicLayoutRL.setVisibility(View.VISIBLE);
        }
        long currentPosition = state.getPosition();
        if (state.getState() != PlaybackState.STATE_PAUSED) {
            // Calculate the elapsed time between the last position update and now and unless
            // paused, we can assume (delta * speed) + current position is approximately the
            // latest position. This ensure that we do not repeatedly call the getPlaybackState()
            // on MediaController.
            long timeDelta = SystemClock.elapsedRealtime() -
                    state.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * state.getPlaybackSpeed();
            Log.d("Training", "POS : " + currentPosition);
        }
    }

    /**
     * Handle Play Pause OnClick.
     */
    @OnClick(R.id.iv_play_pause_button)
    public void toggleButton() {
        Log.d("Training", "Toggle Button Clicked");
        if (mCurrentState != null && mCurrentState.getState() != PlaybackState.STATE_NONE
                && mCurrentMetadata != null) {
            if (mCurrentState.getState() == PlaybackState.STATE_PLAYING) {
                mediaController.getTransportControls().pause();
            } else if (mCurrentState.getState() == PlaybackState.STATE_PAUSED ||
                    mCurrentState.getState() == PlaybackState.STATE_STOPPED) {
                mediaController.getTransportControls().play();
            }
        }
    }

    /**
     * Handle Song Change By CLicking Previous and Next Song.
     *
     * @param imageView Used To get the ID of the ImageClick like Previous Song ImageView or Next ImageView
     */
    @OnClick({R.id.iv_next_song, R.id.iv_previous_song})
    public void songChange(ImageView imageView) {
        int id = imageView.getId();
        if (mediaController != null) {
            if (id == R.id.iv_next_song) {
                mediaController.getTransportControls().skipToNext();
            } else if (id == R.id.iv_previous_song) {
                mediaController.getTransportControls().skipToPrevious();
            }
        }
    }

    /**
     * MediaController Callbacks Called When Song MetaData Changed or
     * PlayBack State Changed
     */
    public MediaController.Callback mMediaCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            Log.d("Training", "Activity Playback State Changed : " + state.getState());
            updateState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            Log.d("Training", "Activity Metadata Changed");
            updateUI(metadata);
        }
    };

    /**
     * Broadcast Receiver To Close Application when Notification is Removed or Canceled.
     */
    public BroadcastReceiver mCloseApplicationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
              //  Process.killProcess(Process.myPid());
                System.exit(0);
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder instanceof MusicService.ServiceBinder) {
            Log.d("Training", "Service Connected");
            mediaController = new MediaController(this,
                    ((MusicService.ServiceBinder) iBinder).getService().getMediaSessionToken());
            mediaController.registerCallback(mMediaCallback);
            Log.d("Training", "Activity : Media Callback registered");
            mCurrentMetadata = mediaController.getMetadata();
            mCurrentState = mediaController.getPlaybackState();
            updateUI(mCurrentMetadata);
            updateState(mCurrentState);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mCloseApplicationReceiver, new IntentFilter(Utils.INTENT_ACTION_STOP));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mCloseApplicationReceiver);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
