package com.music.activities;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.music.R;
import com.music.adapter.MusicAdapter;
import com.music.adapter.PopupAdapter;
import com.music.interfaces.IMusicListClickListener;
import com.music.models.SongDetailsModel;
import com.music.utility.MusicHelper;
import com.music.utility.QueueItemTouchHelper;
import com.music.utility.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

public class SongsListActivity extends BaseActivity implements IMusicListClickListener {

    @BindView(R.id.rv_display_songs_list)
    RecyclerView mDisplayListRV;
    @BindView(R.id.queue_recycler_view)
    RecyclerView mQueueListRV;
    @BindView(R.id.ll_queue_list_layout)
    LinearLayout mQueueListLL;
    private RecyclerView.LayoutManager mQueueLayoutManager;
    private PopupAdapter mQueueListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<SongDetailsModel> mSongList = new ArrayList<>();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Songs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mLayoutManager = new LinearLayoutManager(this);
        mDisplayListRV.setLayoutManager(mLayoutManager);
        mAdapter = new MusicAdapter(this, mSongList, this);
        mDisplayListRV.setAdapter(mAdapter);

        mQueueLayoutManager = new LinearLayoutManager(this);
        mQueueListRV.setLayoutManager(mQueueLayoutManager);
        mQueueListAdapter = new PopupAdapter(MusicHelper.getInstance().getCurrentPlaylist(), this);
        mQueueListRV.setAdapter(mQueueListAdapter);
        ItemTouchHelper.Callback callback = new QueueItemTouchHelper(mQueueListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mQueueListRV);

        Intent intent = getIntent();
        if (intent != null && intent.getAction().equals(Utils.INTENT_ACTION_PATH_ID_SONG_ACTIVITY)) {
            long id = intent.getLongExtra("id", -1);
            String title = intent.getStringExtra("title");
            getSupportActionBar().setTitle(title);
            showList(id);
        }
    }

    private void showList(long id) {
        Log.d("Training", "Id : " + id);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<SongDetailsModel> list = realm.where(SongDetailsModel.class).equalTo("mSongPathID", id).findAll();
        mSongList.clear();
        mSongList.addAll(list);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interface : Called when the Song is Clicked From the List {@link MusicAdapter}
     *
     * @param id Id of the Song Clicked
     */
    @Override
    public void onClick(String id) {
        Log.d("Training", "Item Clicked : " + id);
        if (mediaController != null) {
            mediaController.getTransportControls().playFromMediaId(id, null);
        }
    }

    @OnClick(R.id.iv_show_queue)
    public void showQueueList() {
        if (mQueueListLL.getVisibility() == View.VISIBLE) {
            mQueueListLL.setVisibility(View.GONE);
        } else {
            mQueueListLL.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public void onBackPressed() {
        if (mQueueListLL.getVisibility() == View.VISIBLE) {
            mQueueListLL.setVisibility(View.GONE);
            return;
        }
        super.onBackPressed();
    }
}
