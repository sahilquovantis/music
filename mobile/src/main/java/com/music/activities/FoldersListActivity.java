package com.music.activities;

import android.content.Intent;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.music.R;
import com.music.adapter.FoldersAdapter;
import com.music.asynctasks.RefreshMusicAsyncTask;
import com.music.interfaces.IFolderClickListener;
import com.music.interfaces.IMusicListListener;
import com.music.models.SongDetailsModel;
import com.music.models.SongPathModel;
import com.music.services.MusicService;
import com.music.utility.MusicHelper;
import com.music.utility.SnackBarHelper;
import com.music.utility.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class FoldersListActivity extends BaseActivity implements IMusicListListener, IFolderClickListener {

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
        getSupportActionBar().setTitle("");
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActionBarToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mActionBarToggle);
        mActionBarToggle.syncState();

        mLayoutManager = new GridLayoutManager(this, 2);
        mContainsSongsRV.setLayoutManager(mLayoutManager);
        mAdapter = new FoldersAdapter(FoldersListActivity.this, mFoldersList, this);
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
        if (mediaController != null) {
            mediaController.getTransportControls().sendCustomAction("NONE", null);
        }
        new RefreshMusicAsyncTask(this, this).execute();
    }

    /**
     * Display Songs List
     */
    private void updateSongListUI() {
        Log.d("Training", "Showing List");
        RealmResults<SongPathModel> list = mRealm.where(SongPathModel.class).findAll().sort("mSongDirectory", Sort.ASCENDING);
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
                stopService(new Intent(FoldersListActivity.this, MusicService.class));
                Process.killProcess(Process.myPid());
            }
        }
    }

    @Override
    public void onLongClick(int i, long folderID) {
        Log.d("Training", "OnLong Click : Folders");
        RealmResults<SongDetailsModel> list = mRealm.where(SongDetailsModel.class).equalTo("mSongPathID", folderID).findAll();
        if (i == FoldersAdapter.FOLDER_PLAY) {

        } else if (i == FoldersAdapter.FOLDER_ADD_TO_QUEUE) {
            boolean isAdded = MusicHelper.getInstance().addSongToPlaylist(list);
            if (isAdded) {
                SnackBarHelper.showSnackbar(mDrawerLayout, "Songs Queued");
            } else {
                SnackBarHelper.showSnackbar(mDrawerLayout, "Something went wrong");
            }
        }
    }
}
