package com.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.R;
import com.music.models.SongDetailsModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sahil-goel on 9/8/16.
 */
public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> {

    private ArrayList<SongDetailsModel> mQueueList = new ArrayList<>();
    private Context mContext;

    public PopupAdapter(ArrayList<SongDetailsModel> mQueueList, Context context) {
        this.mQueueList = mQueueList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.queue_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mQueueList.get(position).getSongTitle();
        holder.mQueueSOngTitle.setText(title);
    }

    @Override
    public int getItemCount() {
        return mQueueList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_queue_song_title)
        TextView mQueueSOngTitle;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
