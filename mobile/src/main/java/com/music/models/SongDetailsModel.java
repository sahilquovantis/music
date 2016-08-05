package com.music.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author sahil-goel
 * Model of the Song or Music and Contains Information of the Songs.
 */
public class SongDetailsModel extends RealmObject {
    @PrimaryKey
    private String mSongID;
    private String mSongTitle;
    private String mSongArtist;
    private int mSongPathID;
    private byte[] mSongThumbnailData;
    private long mSongDuration;

    public int getSongPathID() {
        return mSongPathID;
    }

    public void setSongPathID(int mSongPathID) {
        this.mSongPathID = mSongPathID;
    }

    public String getSongID() {
        return mSongID;
    }

    public void setSongID(String mSongID) {
        this.mSongID = mSongID;
    }

    public String getSongTitle() {
        return mSongTitle;
    }

    public void setSongTitle(String mSongTitle) {
        this.mSongTitle = mSongTitle;
    }

    public String getSongArtist() {
        return mSongArtist;
    }

    public void setSongArtist(String mSongArtist) {
        this.mSongArtist = mSongArtist;
    }

    public byte[] getSongThumbnailData() {
        return mSongThumbnailData;
    }

    public void setSongThumbnailData(byte[] mSongThumbnailData) {
        this.mSongThumbnailData = mSongThumbnailData;
    }

    public long getSongDuration() {
        return mSongDuration;
    }

    public void setSongDuration(long mSongDuration) {
        this.mSongDuration = mSongDuration;
    }
}
