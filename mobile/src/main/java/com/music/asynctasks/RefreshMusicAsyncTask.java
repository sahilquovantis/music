package com.music.asynctasks;

import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.music.R;
import com.music.models.SongDetailsModel;
import com.music.interfaces.IMusicListListener;
import com.music.models.SongPathModel;
import com.music.utility.Utils;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * @author sahil-goel
 *         Refreshes Songs from the Internal Storage and Saves them in the Local Database.
 */
public class RefreshMusicAsyncTask extends AsyncTask<Void, Integer, Void> {
    private Context mContext;
    private Realm mRealm;
    private AlertDialog mDialog;
    private IMusicListListener iMusicListListener;
    private TextView mDialogTitleTV;
    private int mCursorSize = 0;
    private ProgressBar mProgressBar;
    private TextView mSongsFetchedTV;

    public RefreshMusicAsyncTask(Context context, IMusicListListener iMusicListListener) {
        mContext = context;
        this.iMusicListListener = iMusicListListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        View view = LayoutInflater.from(mContext).inflate(R.layout.progress_dialog, null, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setView(view);
        mDialogTitleTV = (TextView) view.findViewById(R.id.tv_dialog_title);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_update_progress);
        mSongsFetchedTV = (TextView) view.findViewById(R.id.tv_songs_fetched);
        mDialog = builder.create();
        mDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mProgressBar.setMax(mCursorSize);
        mProgressBar.setProgress(values[0]);
        mSongsFetchedTV.setText("Songs Fetched : " + values[0] + "/" + mCursorSize);
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
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                mRealm.delete(SongPathModel.class);
            }
        });
        updateDatabase();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        iMusicListListener.onSuccess();
        mDialog.cancel();
    }

    public void updateDatabase() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] columns = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
        Cursor cursor = contentResolver.query(uri, columns, null, null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                mCursorSize = cursor.getCount();
                Log.d("Training", "Cursor Size : " + mCursorSize);
                publishProgress(0);
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final String songName = cursor.getString(2);
                    final String songId = cursor.getString(0);
                    final String songArtist = cursor.getString(3);
                    final String songPath = cursor.getString(1);

                    final String path = songPath.substring(0, songPath.lastIndexOf("/"));
                    Log.d("Training",path);
                    final int[] id = {getPathStoredID(path)};
                    if (id[0] == -1) {
                        final String directory = path.substring(path.lastIndexOf("/") + 1);
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                SongPathModel songPathModel = realm.createObject(SongPathModel.class);
                                songPathModel.setSongDirectory(directory);
                                songPathModel.setSongPath(path);
                                songPathModel.setCompletePath(songPath);
                                id[0] = getKey();
                                songPathModel.setId(id[0]);
                            }
                        });
                    }

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
                            songDetailsModel.setSongPathID(id[0]);
                        }
                    });
                    publishProgress(cursor.getPosition() + 1);
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

    private int getPathStoredID(String path) {
        int songPathID = -1;
        RealmResults<SongPathModel> list = mRealm.where(SongPathModel.class).contains("mSongPath", path).findAll();
        if (list != null && list.size() == 1) {
            songPathID = list.get(0).getId();
        }
        return songPathID;
    }

    private int getKey() {
        int key;
        try {
            key = mRealm.where(SongPathModel.class).max("mId").intValue() + 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            key = 1;
        }
        return key;
    }
}
