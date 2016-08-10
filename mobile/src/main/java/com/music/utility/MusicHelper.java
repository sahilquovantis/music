package com.music.utility;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.net.Uri;
import android.util.Log;

import com.music.R;
import com.music.models.SongDetailsModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import io.realm.RealmResults;

/**
 * Created by sahil-goel on 21/7/16.
 */
public class MusicHelper {
    private int mCurrentPosition = -1;
    private static MusicHelper sInstance;
    private ArrayList<SongDetailsModel> mCurrentPlaylist;
    private LinkedHashSet<SongDetailsModel> mSongsSet;

    private MusicHelper() {
        mSongsSet = new LinkedHashSet<>();
        mCurrentPlaylist = new ArrayList<>(mSongsSet);
    }

    public void addSongToPlaylist(SongDetailsModel songDetailsModel) {
        try {
            if (mCurrentPlaylist != null &&
                    !mCurrentPlaylist.isEmpty() &&
                    mCurrentPlaylist.contains(songDetailsModel)) {
                mCurrentPosition = mCurrentPlaylist.indexOf(songDetailsModel);
            } else {
                mCurrentPosition += 1;
                mCurrentPlaylist.add(mCurrentPosition, songDetailsModel);
            }
        } catch (NullPointerException e) {
        }
    }

    public boolean addSongToPlaylist(RealmResults<SongDetailsModel> list) {
        boolean isAdded;
        mSongsSet.addAll(list);
        mCurrentPlaylist.clear();
        isAdded = mCurrentPlaylist.addAll(mSongsSet);
        return isAdded;
    }

    public boolean addSongToPlaylist(ArrayList<SongDetailsModel> list, int pos) {
        mCurrentPosition = pos;
        boolean isAdded;
        mSongsSet.clear();
        mSongsSet.addAll(list);
        mCurrentPlaylist.clear();
        isAdded = mCurrentPlaylist.addAll(mSongsSet);
        return isAdded;
    }

    public ArrayList<SongDetailsModel> getCurrentPlaylist() {
        return mCurrentPlaylist;
    }

    public static synchronized MusicHelper getInstance() {
        if (sInstance == null) {
            sInstance = new MusicHelper();
        }
        return sInstance;
    }

    /**
     * Used To create MediaMetaData for Selected Song or Current Song.
     *
     * @param context
     * @param mediaId ID of the Current Song whose MetaData is Needed.
     * @return Returns MediaMetaData.
     */
    public MediaMetadata getMetadata(Context context, String mediaId) {
        try {
            if (!mCurrentPlaylist.isEmpty()) {
                Log.d("Training", "Current Position : " + mCurrentPosition);
                SongDetailsModel songDetailsModel = mCurrentPlaylist.get(mCurrentPosition);
                MediaMetadata.Builder builder = new MediaMetadata.Builder();
                builder.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, songDetailsModel.getSongID());
                builder.putString(MediaMetadata.METADATA_KEY_ARTIST, songDetailsModel.getSongArtist());
                builder.putString(MediaMetadata.METADATA_KEY_TITLE, songDetailsModel.getSongTitle());
                builder.putLong(MediaMetadata.METADATA_KEY_DURATION, songDetailsModel.getSongDuration());
                builder.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, getAlbumBitmap(context,
                        songDetailsModel.getSongThumbnailData()));
                return builder.build();
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
        }
        return null;
    }

    /**
     * This Method is Used to Get The Song Uri.
     *
     * @param id Current Song Id whose URI is needed for Playing.
     * @return Returns SongUri
     */
    public Uri getSongURI(String id) {
        return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
    }

    /**
     * Used To Get the Previous Song on the Basis of Current Media Id.
     *
     * @param currentMediaId ID of the current Song.
     * @return Returns the ID of the Previous Song.
     */
    public String getPreviousSong(String currentMediaId) {
        String prevMediaId = null;
        try {
            if (mCurrentPlaylist != null && !mCurrentPlaylist.isEmpty()) {
                if (mCurrentPosition == 0) {
                    mCurrentPosition = mCurrentPlaylist.size() - 1;
                } else {
                    mCurrentPosition -= 1;
                }
                prevMediaId = mCurrentPlaylist.get(mCurrentPosition).getSongID();
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            mCurrentPosition = -1;
        }
        return prevMediaId;
    }

    /**
     * Used To Get the Next Song on the Basis of Current Media Id.
     *
     * @param currentMediaId ID of the current Song.
     * @return Returns the ID of the Next Song.
     */
    public String getNextSong(String currentMediaId) {
        String nextMediaId = null;
        try {
            if (mCurrentPlaylist != null && !mCurrentPlaylist.isEmpty()) {
                if (mCurrentPosition == mCurrentPlaylist.size() - 1) {
                    mCurrentPosition = 0;
                } else {
                    mCurrentPosition += 1;
                }
                nextMediaId = mCurrentPlaylist.get(mCurrentPosition).getSongID();
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {
            mCurrentPosition = -1;
        }
        return nextMediaId;
    }

    /**
     * This Method used to Get the Thumbnail of Current Song.
     *
     * @param context
     * @param data    Byte Data Contains the Picture's Blob.
     * @return Returns Bitmap of Current Song.
     */
    private Bitmap getAlbumBitmap(Context context, byte[] data) {
        Bitmap bitmap;
        if (data == null) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);
        } else {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return bitmap;
    }
}
