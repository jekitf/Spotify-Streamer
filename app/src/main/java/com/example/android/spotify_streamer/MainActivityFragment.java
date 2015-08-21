package com.example.android.spotify_streamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private String mArtistId;

    class ArtistAdapterItem {
        final private Artist _artist;
        public ArtistAdapterItem(Artist artist) {
            _artist=artist;
        }
        public String getImage(int pixels) {
            String imageUrl="";
            for (int i=0;i<_artist.images.size();i++){
                Image image=_artist.images.get(i);
                if (imageUrl.equals("") || (image.height>=pixels && image.width>pixels)) {
                    imageUrl=image.url;
                }
            }
            return imageUrl;
        }
        public String getArtistName() {
            return _artist.name;
        }

        public String getArtistId() {
            return _artist.id;
        }
    }

    public class ArtistAdapter extends ArrayAdapter<ArtistAdapterItem>
    {
        class ViewHolder {
            TextView tvArtistName;
            ImageView tvArtistImage;
        }

        public ArtistAdapter(Context context,ArtistAdapter artistAdapter) {
            super(context, 0, new ArrayList<ArtistAdapterItem>());

            // populate data from last artistAdapter
            if (artistAdapter!=null) {
                for (int i = 0; i < artistAdapter.getCount(); i++)
                    add(artistAdapter.getItem(i));
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ArtistAdapterItem adapterArtistItem = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.individual_artist, parent, false);

                ViewHolder holder=new ViewHolder();
                holder.tvArtistName=(TextView) convertView.findViewById(R.id.artistName);
                holder.tvArtistImage=(ImageView) convertView.findViewById(R.id.artistImage);
                convertView.setTag(holder);
            }

            ViewHolder holder=(ViewHolder) convertView.getTag();

            holder.tvArtistName.setText(adapterArtistItem.getArtistName());

            //get ImageView in pixels. No need to download bigger images
            int valueInPixels = (int) getResources().getDimension(R.dimen.spotify_small_image);

            String imageUrl=adapterArtistItem.getImage(valueInPixels);
            if (!imageUrl.equals("")) {
                Picasso.with(getContext()).load(imageUrl).into(holder.tvArtistImage);
            }
            return convertView;
        }
    }


    //to store/retrieve view data when going back
    private static Parcelable state;

    //used static so when hitting back, old data will be cached
    private static ArtistAdapter artistAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        state=listView.onSaveInstanceState();
    }

    private ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artistAdapter = new ArtistAdapter(getActivity(),artistAdapter);

        View rootView= inflater.inflate(R.layout.fragment_main, container, false);
        listView=(ListView) rootView.findViewById(R.id.artist);
        listView.setAdapter(artistAdapter);
        if (state!=null) {
            listView.onRestoreInstanceState(state);
        }


      //Just for developing
        /*Intent showTopTracks = new Intent(getActivity(), TopTracksActivity.class);
        showTopTracks.putExtra(Intent.EXTRA_REFERRER, "4gzpq5DPGxSnKTe4SA8HAU");
        showTopTracks.putExtra(Intent.EXTRA_TEXT, "Coldplay");
        startActivity(showTopTracks);*/


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MainActivity.UseTwoPane()) {
                    Bundle args = new Bundle();
                    args.putString(TopTracksActivityFragment.ARTIST_ID_URI, artistAdapter.getItem(position).getArtistId());
                    args.putString(TopTracksActivityFragment.ARTIST_NAME_URI, artistAdapter.getItem(position).getArtistName());

                    TopTracksActivityFragment topTracksActivityFragment = new TopTracksActivityFragment();
                    topTracksActivityFragment.setArguments(args);

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.topTracks_container, topTracksActivityFragment)
                            .commit();
                } else {
                    TopTracksActivityFragment.ClearAllTracks();
                    Intent showTopTracks = new Intent(getActivity(), TopTracksActivity.class);
                    showTopTracks.putExtra(Intent.EXTRA_REFERRER, artistAdapter.getItem(position).getArtistId());
                    showTopTracks.putExtra(Intent.EXTRA_TEXT, artistAdapter.getItem(position).getArtistName());
                    startActivity(showTopTracks);
                }
            }
        });

        EditText editArtist=((EditText) rootView.findViewById(R.id.editArtist));
        editArtist.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN) {
                            // Hide keyboard
                            v.clearFocus();

                            // Remove previous results
                            artistAdapter.clear();

                            //also clear top tracks
                            FragmentManager fragmentManager=getFragmentManager();
                            TopTracksActivityFragment topTracksActivityFragment=(TopTracksActivityFragment) fragmentManager.findFragmentById(R.id.topTracks_container);
                            if (topTracksActivityFragment!=null) {
                                topTracksActivityFragment.ClearAllTracks();
                            }

                            // Search for artist async
                            String searchArtistName = v.getText().toString();
                            new SearchForArtist().execute(searchArtistName);
                        }
                        return false;
                    }
                });


        if (artistAdapter.getCount()>0) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        return rootView;
    }
        private static Toast toast;
        private class SearchForArtist extends AsyncTask<String, Integer, ArrayList<ArtistAdapterItem>> {


        @Override
        protected ArrayList<ArtistAdapterItem> doInBackground(String... params) {

            final String artistName = params[0];

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager artistsPager = spotify.searchArtists(artistName);

            ArrayList<ArtistAdapterItem> adapterArtistItems = new ArrayList<>();
            for (Artist artist : artistsPager.artists.items) {
                adapterArtistItems.add(new ArtistAdapterItem(artist));
                //Log.v("Artist found: ", artist.name);
            }
            return adapterArtistItems;
        }

        @Override
        protected void onPostExecute(ArrayList<ArtistAdapterItem> artistAdapterItem) {
            artistAdapter.clear();
            artistAdapter.addAll(artistAdapterItem);

            if (artistAdapterItem.size()==0){
                if (toast!=null)
                    toast.cancel();
                toast=Toast.makeText(getActivity(), getResources().getString(R.string.no_results),Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }

}
