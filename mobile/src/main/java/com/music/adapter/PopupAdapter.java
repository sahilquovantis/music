package com.music.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.R;
import com.music.interfaces.IItemTouchHelperAdapter;
import com.music.models.SongDetailsModel;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sahil-goel on 9/8/16.
 */
public class PopupAdapter extends RecyclerView.Adapter<PopupAdapter.ViewHolder> implements IItemTouchHelperAdapter{

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
        SongDetailsModel model = mQueueList.get(position);
        String title = model.getSongTitle();
        String artist = model.getSongArtist();
        holder.mQueueSOngTitle.setText(title);
        holder.mQueueSongArtist.setText(artist);
        holder.mDragIconLL.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mQueueList.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mQueueList,fromPosition, toPosition);
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        mQueueList.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_queue_song_title)
        TextView mQueueSOngTitle;
        @BindView(R.id.tv_queue_song_artist)
        TextView mQueueSongArtist;
        @BindView(R.id.ll_drag_icon)
        LinearLayout mDragIconLL;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
