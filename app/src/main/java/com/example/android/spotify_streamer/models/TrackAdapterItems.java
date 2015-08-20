package com.example.android.spotify_streamer.models;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.android.spotify_streamer.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by PC on 03.08.2015.
 */
public class TrackAdapterItems implements Parcelable {
    private ArrayList<TrackAdapterItem> _trackAdapterItem;
    private int _position;
    private Activity _activity;
    public TrackAdapterItems(int index,ArrayAdapter<TrackAdapterItem> adapter) {
        _position =index;
        _trackAdapterItem=new ArrayList<TrackAdapterItem>();
        for(int i=0;i<adapter.getCount();i++){
            _trackAdapterItem.add(adapter.getItem(i));
        }
    }

    protected TrackAdapterItems(Parcel in) {
        _trackAdapterItem = in.createTypedArrayList(TrackAdapterItem.CREATOR);
        _position =in.readInt();
    }

    public static final Creator<TrackAdapterItems> CREATOR = new Creator<TrackAdapterItems>() {
        @Override
        public TrackAdapterItems createFromParcel(Parcel in) {
            return new TrackAdapterItems(in);
        }

        @Override
        public TrackAdapterItems[] newArray(int size) {
            return new TrackAdapterItems[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(_trackAdapterItem);
        dest.writeInt(_position);
    }

    public TrackAdapterItem getTrack() {
        return _trackAdapterItem.get(_position);
    }

    public boolean NextTrack() {
        if (_trackAdapterItem.size()==(_position+1))
            return false;
        _position++;
        return true;
    }

    public boolean PreviousTrack() {
        if (_trackAdapterItem.size()==0 || _position==0)
            return false;
        _position--;
        return true;
    }
}
