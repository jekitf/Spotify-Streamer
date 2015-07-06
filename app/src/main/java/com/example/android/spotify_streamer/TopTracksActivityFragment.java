package com.example.android.spotify_streamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;


/**
 * A placeholder fragment containing a simple view.
 */
public class TopTracksActivityFragment extends Fragment {

    class TrackAdapterItem {
        final private Track _track;
        public TrackAdapterItem(Track track) {
            _track=track;
        }
        public String getImage(int pixels) {
            String imageUrl="";
            for (int i=0;i<_track.album.images.size();i++){
                Image image=_track.album.images.get(i);
                if (imageUrl.equals("") || (image.height>=pixels && image.width>pixels)) {
                    imageUrl=image.url;
                }
            }
            return imageUrl;
        }
        public String getTrackName() {
            return _track.name;
        }

        public String getTrackNameAndAlbumName() {
            return _track.name+"\r\n"+_track.album.name;
        }
    }

    public class TrackAdapter extends ArrayAdapter<TrackAdapterItem>
    {
        class ViewHolder {
            TextView tvTrackName;
            ImageView tvAlbumImage;
        }
        public TrackAdapter(Context context) {
            super(context, 0, new ArrayList<TrackAdapterItem>());

            // populate data from last trackAdapter
            if (trackAdapter!=null) {
                for (int i = 0; i < trackAdapter.getCount(); i++)
                    add(trackAdapter.getItem(i));
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrackAdapterItem trackAdapterItem = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.individual_track, parent, false);
                ViewHolder holder=new ViewHolder();
                holder.tvTrackName=(TextView) convertView.findViewById(R.id.trackName);
                holder.tvAlbumImage=(ImageView) convertView.findViewById(R.id.albumImage);
                convertView.setTag(holder);
            }

            ViewHolder holder=(ViewHolder) convertView.getTag();
            holder.tvTrackName.setText(trackAdapterItem.getTrackNameAndAlbumName());

            //get ImageView in pixels. No need to download bigger images
            int valueInPixels = (int) getResources().getDimension(R.dimen.spotify_small_image);

            String imageUrl=trackAdapterItem.getImage(valueInPixels);
            if (!imageUrl.equals("")) {
                Picasso.with(getContext()).load(imageUrl).into(holder.tvAlbumImage);
            }
            return convertView;
        }
    }

    //to store/retrieve view data when going back
    private static Parcelable state;

    //used static so when hitting back, old data will be cached
    private static TrackAdapter trackAdapter;

    public TopTracksActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        state=listView.onSaveInstanceState();
    }

    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        trackAdapter=new TrackAdapter(getActivity());

        Intent intent=getActivity().getIntent();
        View rootView =  inflater.inflate(R.layout.fragment_top_tracks, container, false);
        if (intent!=null && intent.hasExtra(Intent.EXTRA_REFERRER)){
            String artistName=intent.getStringExtra(Intent.EXTRA_TEXT);
            String artistId=intent.getStringExtra(Intent.EXTRA_REFERRER);

            AppCompatActivity activity=(AppCompatActivity) getActivity();
            android.support.v7.app.ActionBar mActionBar = activity.getSupportActionBar();
            if (mActionBar!=null) {
                mActionBar.setTitle("Top 10 Tracks");
                mActionBar.setSubtitle(artistName);
            }
            new SearchTopTracks().execute(artistId);
        }

        listView=(ListView) rootView.findViewById(R.id.tracks);
        listView.setAdapter(trackAdapter);
        if (state!=null) {
            listView.onRestoreInstanceState(state);
        }
        return rootView;
    }

    private class SearchTopTracks extends AsyncTask<String,Integer,ArrayList<TrackAdapterItem>>{
        @Override
        protected ArrayList<TrackAdapterItem> doInBackground(String... params) {
            final String artistId = params[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            HashMap<String, Object> query=new HashMap<>();
            query.put("country","NO");

            Tracks tracks = spotify.getArtistTopTrack(artistId,query);

            ArrayList<TrackAdapterItem> trackAdapterItems = new ArrayList<>();
            for (Track track : tracks.tracks) {
                trackAdapterItems.add(new TrackAdapterItem(track));
                //Log.v("Track found: ", track.name);
            }
            return trackAdapterItems;
        }

        @Override
        protected void onPostExecute(ArrayList<TrackAdapterItem> trackAdapterItems) {
            trackAdapter.clear();
            trackAdapter.addAll(trackAdapterItems);

            if (trackAdapterItems.size()==0){
                Toast.makeText(getActivity(), "No results", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
