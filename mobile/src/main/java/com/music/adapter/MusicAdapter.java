package com.music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.R;
import com.music.models.SongDetailsModel;
import com.music.interfaces.IMusicListClickListener;
import com.music.utility.MusicHelper;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by sahil-goel on 21/7/16.
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private Context mContext;
    private IMusicListClickListener iMusicListClickListener;
    private RealmResults<SongDetailsModel> mSongList;
    public final static String INTENT_FILTER = "com.music.on.click";

    public MusicAdapter(Context context, RealmResults<SongDetailsModel> mSongList, IMusicListClickListener clickListener) {
        mContext = context;
        iMusicListClickListener = clickListener;
        MusicHelper.getInstance().setSongsPlayList(mSongList);
        this.mSongList = mSongList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.custom_music_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int pos = position;
        holder.mSongTV.setText(mSongList.get(pos).getSongTitle());
        holder.mSongArtistTV.setText(mSongList.get(pos).getSongArtist());
        loadBitmap(holder.mSongThumbnailIV, mSongList.get(pos).getSongThumbnailData());
        Log.d("Training","Path : " + mSongList.get(pos).getSongPath());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iMusicListClickListener.onClick(mSongList.get(pos).getSongID());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_song_thumbnail)
        ImageView mSongThumbnailIV;
        @BindView(R.id.tv_song_name)
        TextView mSongTV;
        @BindView(R.id.tv_song_artist)
        TextView mSongArtistTV;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void loadBitmap(ImageView imageView, byte[] data) {
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

    public static boolean cancelPotentialWork(byte[] data, ImageView imageView) {
        final DisplayImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final byte[] bitmapData = bitmapWorkerTask.data;
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


    class DisplayImage extends AsyncTask<byte[], Void, Bitmap> {

        private WeakReference<ImageView> imageViewWeakReference;
        public byte[] data = null;

        DisplayImage(ImageView iv) {
            imageViewWeakReference = new WeakReference<ImageView>(iv);
        }

        @Override
        protected Bitmap doInBackground(byte[]... strings) {
            data = strings[0];
            Bitmap bitmap = null;
            if (data == null) {
                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.music);
            } else {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
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
