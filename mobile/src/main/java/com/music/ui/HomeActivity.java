package com.music.ui;

import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.music.R;
import com.music.activities.BaseActivity;
import com.music.adapter.FoldersAdapter;
import com.music.asynctasks.RefreshMusicAsyncTask;
import com.music.interfaces.IMusicListListener;
import com.music.models.SongPathModel;
import com.music.services.MusicService;
import com.music.utility.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class HomeActivity extends BaseActivity implements IMusicListListener {

    @BindView(R.id.rl_home_layout)
    RelativeLayout mHomeLayoutRL;
    @BindView(R.id.rv_contains_songs)
    RecyclerView mContainsSongsRV;
    @BindView(R.id.iv_refresh_playlist)
    ImageView mRefreshListIV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private Realm mRealm;
    private ArrayList<SongPathModel> mFoldersList = new ArrayList<>();

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

        mLayoutManager = new LinearLayoutManager(this);
        mContainsSongsRV.setLayoutManager(mLayoutManager);
        mAdapter = new FoldersAdapter(HomeActivity.this, mFoldersList);
        mContainsSongsRV.setAdapter(mAdapter);
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

    /**
     * Display Songs List
     */
    private void updateSongListUI() {
        Log.d("Training", "Showing List");
        RealmResults<SongPathModel> list = mRealm.where(SongPathModel.class).findAll();
        mFoldersList.clear();
        mFoldersList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Interface : Called When The DataBase Is Refreshed {@link RefreshMusicAsyncTask}.
     */
    @Override
    public void onSuccess() {
        updateSongListUI();
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
                Process.killProcess(Process.myPid());
            }
        }
    }

}
