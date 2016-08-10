package com.music.utility;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.music.R;
import com.music.adapter.PopupAdapter;
import com.music.models.SongDetailsModel;

import java.util.ArrayList;

/**
 * Created by sahil-goel on 9/8/16.
 */
public class PopUpHelper {

    private View mView;
    private PopupWindow mPopupWindow;
    private static PopUpHelper sInstance;
    private RecyclerView mQueueListRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<SongDetailsModel> mQueueList = new ArrayList<>();

    private PopUpHelper(Context context) {
        mView = LayoutInflater.from(context).inflate(R.layout.custom_popup, null);
        mPopupWindow = new PopupWindow(context);
        mPopupWindow.setContentView(mView);
        mPopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mPopupWindow.setOutsideTouchable(true);
        mLayoutManager = new LinearLayoutManager(context);
        mQueueListRV = (RecyclerView) mView.findViewById(R.id.queue_recycler_view);
        mQueueListRV.setLayoutManager(mLayoutManager);
        mAdapter = new PopupAdapter(MusicHelper.getInstance().getCurrentPlaylist(), context);
        mQueueListRV.setAdapter(mAdapter);
    }

    public static synchronized PopUpHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PopUpHelper(context);
        }
        return sInstance;
    }

    public void createPopUp(View anchor, int height) {
        mPopupWindow.setHeight(height);
        mPopupWindow.showAsDropDown(anchor);
      /*  mQueueList.clear();
        mQueueList.addAll(MusicHelper.getInstance().getCurrentPlaylist());*/
        mAdapter.notifyDataSetChanged();
    }
}
