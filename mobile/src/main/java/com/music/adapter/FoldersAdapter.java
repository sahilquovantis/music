package com.music.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.music.R;
import com.music.activities.SongsListActivity;
import com.music.interfaces.IFolderClickListener;
import com.music.models.SongPathModel;
import com.music.utility.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sahil-goel on 4/8/16.
 */
public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.ViewHolder> {

    private Context mContext;
    private IFolderClickListener iFolderClickListener;
    private MediaMetadataRetriever metadataRetriever;
    private WindowManager.LayoutParams mDialogLayoutParams;
    private View mDialogCustomView;
    private Dialog mBuilder;
    private ArrayList<SongPathModel> mSongPathModelArrayList = new ArrayList<>();
    public final static int FOLDER_PLAY = 1;
    public final static int FOLDER_ADD_TO_QUEUE = 2;

    public FoldersAdapter(Context context, ArrayList<SongPathModel> songPathModelList, IFolderClickListener folderClickListener) {
        mContext = context;
        iFolderClickListener = folderClickListener;
        mSongPathModelArrayList = songPathModelList;
        metadataRetriever = new MediaMetadataRetriever();
        mDialogCustomView = LayoutInflater.from(mContext).inflate(R.layout.custom_dialog, null);
        mDialogLayoutParams = new WindowManager.LayoutParams();
        mBuilder = new Dialog(context);
        mBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialogLayoutParams.copyFrom(mBuilder.getWindow().getAttributes());
        mDialogLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mDialogLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mDialogLayoutParams.gravity = Gravity.CENTER;
        mBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        loadBitmap(holder.mDirectoryThumbnail, songPathModel.getCompletePath());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SongsListActivity.class);
                intent.setAction(Utils.INTENT_ACTION_PATH_ID_SONG_ACTIVITY);
                intent.putExtra("id", id);
                intent.putExtra("title", directory);
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                LinearLayout playFolder = (LinearLayout) mDialogCustomView.findViewById(R.id.ll_play_folder);
                LinearLayout addToQueueFolder = (LinearLayout) mDialogCustomView.findViewById(R.id.ll_add_folder_to_queue);
                playFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        iFolderClickListener.onLongClick(FOLDER_PLAY, id);
                        mBuilder.cancel();
                    }
                });
                addToQueueFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        iFolderClickListener.onLongClick(FOLDER_ADD_TO_QUEUE, id);
                        mBuilder.cancel();
                    }
                });
                mBuilder.setContentView(mDialogCustomView);
                mBuilder.show();
                mBuilder.getWindow().setAttributes(mDialogLayoutParams);
                return true;
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
        @BindView(R.id.iv_song_thumbnail)
        ImageView mDirectoryThumbnail;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void loadBitmap(ImageView imageView, String data) {
        if (cancelPotentialWork(data, imageView)) {
            final DisplayImage task = new DisplayImage(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(data);
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<DisplayImage> bitmapWorkerTask;

        public AsyncDrawable(DisplayImage bitmapTask) {
            bitmapWorkerTask = new WeakReference<DisplayImage>(bitmapTask);
        }

        public DisplayImage getBitmapWorkerTask() {
            return bitmapWorkerTask.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final DisplayImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            if (bitmapData != data) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static DisplayImage getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }


    class DisplayImage extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<ImageView> imageViewWeakReference;
        public String data = null;

        DisplayImage(ImageView iv) {
            imageViewWeakReference = new WeakReference<ImageView>(iv);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            data = strings[0];
            Bitmap bitmap = null;
            try {
                metadataRetriever.setDataSource(data);
                byte[] imageData = metadataRetriever.getEmbeddedPicture();
                if (imageData == null) {
                    bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);
                } else {
                    bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                }
            } catch (IllegalArgumentException e) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (isCancelled()) {
                bitmap = null;
            }
            if (bitmap != null && imageViewWeakReference != null) {
                ImageView imageView = imageViewWeakReference.get();
                final DisplayImage bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
