package com.example.android.spotify_streamer.models;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by PC on 03.08.2015.
 */
public class TrackAdapterItem implements Parcelable {
    private String _artistName="";
    private String _albumName;
    private String _trackName;
    private String _imageUrl="";
    private String _previewUrl="";
    public TrackAdapterItem(Track track) {

        _trackName=track.name;
        _albumName=track.album.name;
        if(track.artists.size()>0)
            _artistName= track.artists.get(0).name;
        _previewUrl=track.preview_url;

        int pixels=100;
        for (int i=0;i<track.album.images.size();i++){
            Image image=track.album.images.get(i);
            if (_imageUrl.equals("") || (image.height>=pixels && image.width>pixels)) {
                _imageUrl=image.url;
            }
        }
    }
    private TrackAdapterItem(Parcel in) {
        _trackName=in.readString();
        _albumName=in.readString();
        _artistName=in.readString();
        _imageUrl=in.readString();
        _previewUrl=in.readString();
    }

    public static final Creator<TrackAdapterItem> CREATOR = new Creator<TrackAdapterItem>() {
        @Override
        public TrackAdapterItem createFromParcel(Parcel in) {
            return new TrackAdapterItem(in);
        }

        @Override
        public TrackAdapterItem[] newArray(int size) {
            return new TrackAdapterItem[size];
        }
    };

    public String getImage(int pixels) {
        return _imageUrl;
    }
    public String getTrackName() {
        return _trackName;
    }
    public String getArtistName() { return _artistName;}
    public String getAlbumName() { return _albumName;}
    public String getPreviewUrl() { return _previewUrl;}

    public String getTrackNameAndAlbumName() {
        return _trackName+"\r\n"+_albumName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_trackName);
        dest.writeString(_albumName);
        dest.writeString(_artistName);
        dest.writeString(_imageUrl);
        dest.writeString(_previewUrl);
    }


}