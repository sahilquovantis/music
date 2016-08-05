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
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by sahil-goel on 21/7/16.
 */
public class MusicHelper {
    private int mCurrentPosition = 0;
    private Realm mRealm;
    private static MusicHelper sInstance;
    private List<SongDetailsModel> mSongsPlayList;

    private MusicHelper() {
        mSongsPlayList = new ArrayList<>();
        mRealm = Realm.getDefaultInstance();
    }

    public void setSongsPlayList(List<SongDetailsModel> mSongsPlayList) {
        this.mSongsPlayList = mSongsPlayList;
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


            RealmResults<SongDetailsModel> list = mRealm.where(SongDetailsModel.class)
                    .equalTo("mSongID", mediaId)
                    .findAll();
            if (list.size() == 1) {
                mCurrentPosition = mSongsPlayList.indexOf(list.get(0));
                Log.d("Training", "Current Position : " + mCurrentPosition);
                SongDetailsModel songDetailsModel = list.get(0);
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
            if (mSongsPlayList != null && !mSongsPlayList.isEmpty()) {
                if (currentMediaId == null || mCurrentPosition == 0) {
                    return mSongsPlayList.get(0).getSongID();
                }
                prevMediaId = mSongsPlayList.get(mCurrentPosition - 1).getSongID();
                if (prevMediaId == null) {
                    prevMediaId = mSongsPlayList.get(0).getSongID();
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {

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
            if (mSongsPlayList != null && !mSongsPlayList.isEmpty()) {
                if (currentMediaId == null || mCurrentPosition == mSongsPlayList.size() - 1) {
                    return mSongsPlayList.get(0).getSongID();
                }
                nextMediaId = mSongsPlayList.get(mCurrentPosition + 1).getSongID();
                if (nextMediaId == null) {
                    nextMediaId = mSongsPlayList.get(0).getSongID();
                }
            }
        } catch (IllegalArgumentException | IndexOutOfBoundsException | NullPointerException e) {

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
