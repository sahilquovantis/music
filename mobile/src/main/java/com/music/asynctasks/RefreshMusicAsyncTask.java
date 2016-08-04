package com.music.asynctasks;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.music.models.SongDetailsModel;
import com.music.interfaces.IMusicListListener;
import com.music.utility.Utils;

import io.realm.Realm;

/**
 * @author sahil-goel
 * Refreshes Songs from the Internal Storage and Saves them in the Local Database.
 */
public class RefreshMusicAsyncTask extends AsyncTask<Void, Void, Void> {
    private Context mContext;
    private Realm mRealm;
    private ProgressDialog mProgressDialog;
    private IMusicListListener iMusicListListener;

    public RefreshMusicAsyncTask(Context context, IMusicListListener iMusicListListener) {
        mContext = context;
        this.iMusicListListener = iMusicListListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("");
        mProgressDialog.setMessage("Fetching Songs ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("Training", "Thread running : Getting Music List");
        mRealm = Realm.getDefaultInstance();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mRealm.delete(SongDetailsModel.class);
            }
        });
        updateDatabase();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        iMusicListListener.onSuccess();
        mProgressDialog.cancel();
    }

    public void updateDatabase() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] columns = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d("Training", "Cursor Size : " + cursor.getCount());
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final String songName = cursor.getString(2);
                    final String songId = cursor.getString(0);
                    final String songArtist = cursor.getString(3);
                    final String songPath = cursor.getString(1);
                    metadataRetriever.setDataSource(songPath);
                    final byte[] data = metadataRetriever.getEmbeddedPicture();
                    final long duration = Long.parseLong(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    mRealm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            SongDetailsModel songDetailsModel = realm.createObject(SongDetailsModel.class);
                            songDetailsModel.setSongID(songId);
                            songDetailsModel.setSongArtist(songArtist);
                            songDetailsModel.setSongTitle(songName);
                            songDetailsModel.setSongDuration(duration);
                            songDetailsModel.setSongThumbnailData(data);
                            songDetailsModel.setSongPath(songPath);
                        }
                    });
                    cursor.moveToNext();
                }
                SharedPreferences.Editor editor = Utils.sSharedPreferences.edit();
                editor.putBoolean(Utils.SHARED_PREFERENCE_FIRST_TIME_OPEN, false);
                editor.commit();
                cursor.close();
                metadataRetriever.release();
            }
        }
    }
}
