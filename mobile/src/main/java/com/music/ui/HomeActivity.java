package com.music.ui;

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
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.music.R;
import com.music.adapter.MusicAdapter;
import com.music.models.SongDetailsModel;
import com.music.interfaces.IMusicListClickListener;
import com.music.interfaces.IMusicListListener;
import com.music.services.MusicService;
import com.music.asynctasks.RefreshMusicAsyncTask;
import com.music.utility.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity implements ServiceConnection, IMusicListListener,
        IMusicListClickListener {

    @BindView(R.id.rl_home_layout)
    RelativeLayout mHomeLayoutRL;
    @BindView(R.id.rv_contains_songs)
    RecyclerView mContainsSongsRV;
    @BindView(R.id.iv_selected_song_thumbnail)
    ImageView mSelectedSongThumbnailIV;
    @BindView(R.id.iv_refresh_playlist)
    ImageView mRefreshListIV;
    @BindView(R.id.tv_selected_song)
    TextView mSelectedSongTV;
    @BindView(R.id.tv_selected_song_artist)
    TextView mSelectedSongArtistTV;
    @BindView(R.id.rl_music_layout)
    RelativeLayout mMusicLayoutRL;
    @BindView(R.id.iv_play_pause_button)
    ImageView mPlayPaueIV;
    @BindView(R.id.iv_previous_song)
    ImageView mPreviousSongIV;
    @BindView(R.id.iv_next_song)
    ImageView mNextSongIV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private MediaMetadata mCurrentMetadata;
    private PlaybackState mCurrentState;
    private MediaController mediaController;
    private Realm mRealm;
    private RealmResults<SongDetailsModel> mSongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d("Training", "OnCreate Activity");
        ButterKnife.bind(this);
        mRealm = Realm.getDefaultInstance();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Music");
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        Utils.sSharedPreferences = getSharedPreferences(Utils.SHARED_PREFERENCE_FILE_NAME, MODE_PRIVATE);
        boolean isFirstTime = Utils.sSharedPreferences.getBoolean(Utils.SHARED_PREFERENCE_FIRST_TIME_OPEN, true);
        if (isFirstTime) {
            refreshPlayList();
        } else {
            updateSongListUI();
        }
    }

    /**
     * Refresh Music List and Get Songs From Internal Storage and Store them in Database
     * {@link RefreshMusicAsyncTask}.
     */
    @OnClick(R.id.iv_refresh_playlist)
    public void refreshPlayList() {
        Log.d("Training", "Refreshing Playlist");
        new RefreshMusicAsyncTask(this, this).execute();
    }

    private void updateSongListUI() {
        Log.d("Training", "Showing List");
        mSongList = mRealm.where(SongDetailsModel.class).findAll();
        mLayoutManager = new GridLayoutManager(HomeActivity.this, 2);
        mContainsSongsRV.setLayoutManager(mLayoutManager);
        mAdapter = new MusicAdapter(HomeActivity.this, mSongList, this);
        mContainsSongsRV.setAdapter(mAdapter);
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
                mediaController.getTransportControls().playFromMediaId(mCurrentMetadata.getDescription().getMediaId(), null);
            }
        }
    }

    /**
     * Handle Song Change By CLicking Previous and Next Song.
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
     * Broadcast Receiver To Close Application when Notification is Removed or Canceled.
     */
    private BroadcastReceiver mCloseApplicationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                onBackPressed();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Training", "On Resume Activity");
        registerReceiver(mCloseApplicationReceiver, new IntentFilter(Utils.INTENT_ACTION_STOP));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Training", "On Pause Activity");
        unregisterReceiver(mCloseApplicationReceiver);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder instanceof MusicService.ServiceBinder) {
            Log.d("Training", "Service Connected");
            mediaController = new MediaController(HomeActivity.this,
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

    /**
     * MediaController Callbacks Called When Song MetaData Changed or
     * PlayBack State Changed
     */
    private MediaController.Callback mMediaCallback = new MediaController.Callback() {
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
     * Update Music Player UI With the Details of Current Song like Title, Thumbnail, Artist at Bottom
     * @param mediaMetadata Contains Information of Current Song Selected like Title, Thumbanil, Artist
     */
    private void updateUI(MediaMetadata mediaMetadata) {
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
     * @param state Contains Information whether the music player is Playing or Paused or Stopped
     */
    private void updateState(PlaybackState state) {
        mCurrentState = state;
        Log.d("Training", "Activity Current Playback State : " + state);
        mMusicLayoutRL.setVisibility(View.VISIBLE);
        if (state == null || state.getState() == PlaybackState.STATE_NONE || mCurrentMetadata == null) {
            mMusicLayoutRL.setVisibility(View.GONE);
        } else if (state.getState() == PlaybackState.STATE_PAUSED || state.getState() == PlaybackState.STATE_STOPPED) {
            mPlayPaueIV.setImageResource(R.drawable.ic_action_play);
        } else if (state.getState() == PlaybackState.STATE_PLAYING) {
            mPlayPaueIV.setImageResource(R.drawable.ic_action_pause);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Training", "On Start Activity");
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
        Log.d("Training", "Activity Home destroying");
        if (mediaController != null) {
            if (mediaController.getPlaybackState() == null ||
                    mediaController.getPlaybackState().getState() == PlaybackState.STATE_NONE) {
                Log.d("Training", "Stopping Service from main activity");
                stopService(new Intent(HomeActivity.this, MusicService.class));
            }
        }
    }

    /**
     * Interface : Called When The DataBase Is Refreshed {@link RefreshMusicAsyncTask}.
     */
    @Override
    public void onSuccess() {
        updateSongListUI();
    }

    /**
     * Interface : Called when the Song is Clicked From the List {@link MusicAdapter}
     * @param id Id of the Song Clicked
     */
    @Override
    public void onClick(String id) {
        Log.d("Training", "Item Clicked : " + id);
        if (mediaController != null) {
            mediaController.getTransportControls().playFromMediaId(id, null);
        }
    }
}
