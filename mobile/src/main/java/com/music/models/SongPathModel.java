package com.music.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by sahil-goel on 3/8/16.
 */
public class SongPathModel extends RealmObject {

    @PrimaryKey
    private int mId;
    private String mSongDirectory;
    private String mCompletePath;
    private String mSongPath;

    public String getCompletePath() {
        return mCompletePath;
    }

    public void setCompletePath(String mCompletePath) {
        this.mCompletePath = mCompletePath;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getSongDirectory() {
        return mSongDirectory;
    }

    public void setSongDirectory(String mSongDirectory) {
        this.mSongDirectory = mSongDirectory;
    }

    public String getSongPath() {
        return mSongPath;
    }

    public void setSongPath(String mSongPath) {
        this.mSongPath = mSongPath;
    }
}
