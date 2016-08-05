package com.music.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.music.R;
import com.music.activities.SongsListActivity;
import com.music.models.SongPathModel;
import com.music.utility.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sahil-goel on 4/8/16.
 */
public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<SongPathModel> mSongPathModelArrayList = new ArrayList<>();

    public FoldersAdapter(Context context, ArrayList<SongPathModel> songPathModelList) {
        mContext = context;
        mSongPathModelArrayList = songPathModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.folders_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SongPathModel songPathModel = mSongPathModelArrayList.get(position);
        final String directory = songPathModel.getSongDirectory();
        String path = songPathModel.getSongPath();
        path = path.substring(0, path.lastIndexOf("/"));
        final long id = songPathModel.getId();
        holder.mDirectoryNameTV.setText(directory);
        holder.mDirectoryPathTV.setText(path);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Training", "IDs : " + id);
                Intent intent = new Intent(mContext, SongsListActivity.class);
                intent.setAction(Utils.INTENT_ACTION_PATH_ID_SONG_ACTIVITY);
                intent.putExtra("id", id);
                intent.putExtra("title",directory);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongPathModelArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_folder_name)
        TextView mDirectoryNameTV;
        @BindView(R.id.tv_folder_path)
        TextView mDirectoryPathTV;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
