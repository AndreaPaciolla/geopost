package com.pacho.geopost.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.pacho.geopost.R;
import com.pacho.geopost.models.UserModel;
import com.pacho.geopost.adapters.ListItemAdapter;
import com.pacho.geopost.services.HttpVolleyQueue;
import com.pacho.geopost.utilities.Api;
import com.pacho.geopost.utilities.AppConstants;
import com.pacho.geopost.utilities.AppUtility;
import com.pacho.geopost.utilities.LocationComparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.round;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_LOCATION = 1;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location currentLocation;

    private static final float DEFAULT_ZOOM = 15f;

    private boolean mLocationPermissionGranted = false;

    private ImageView mButtonChangeView;
    private ListView mFollowedUsersListView;

    private String mCurrentViewMode = "MAP"; // MAP | LIST

    private MapView mapView;
    private GoogleMap map;
    private String session_id;
    private HttpVolleyQueue volleyInstance;
    private ArrayList<UserModel> followedUsers = new ArrayList<UserModel>();

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    // Checks Google Play Services
    public boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if(available == ConnectionResult.SUCCESS) {
            // EVERYTHING IS FINE and user can make map requests
            Log.d(TAG, "isServicesOK: google play services is working");
            return true;
        } else if( GoogleApiAvailability.getInstance().isUserResolvableError(available) ) {
            // an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, 10);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        volleyInstance = HttpVolleyQueue.getInstance();
        session_id = this.getActivity().getSharedPreferences(AppConstants.GEOPOST_PREFS, this.getActivity().MODE_PRIVATE).getString(AppConstants.SESSION_ID, null);

        if(isServicesOK()) {
            getLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_LOCATION:
                getLocationPermission();
                break;
        }
    }

    private void getLocationPermission() {
        mLocationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    if(location == null) {
                        Toast.makeText(getContext(), "Trying to find your location...", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "onLocationChanged method: Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude() );
                        currentLocation = location;
                        orderFriendsListByDistance();
                        Toast.makeText(getContext(), "Location found", Toast.LENGTH_LONG).show();

                        // Draw my position
                        if( currentLocation != null ) {
                            LatLng userCoordinates = new LatLng( currentLocation.getLatitude(), currentLocation.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position( userCoordinates )
                                    .title( "My position" )
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                            Marker marker = map.addMarker( markerOptions );
                        }

                    }
                } catch( Exception e ) {
                    Log.d(TAG, "getLocationPermission method :: onLocationChanged cb: " + e.getMessage());
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d(TAG, "Location provider is already enabled. OK");
            }

            @Override
            public void onProviderDisabled(String s) {
                AppUtility.showGPSDiabledDialog(getContext());
            }
        };

        int accessFineLocation = ActivityCompat.checkSelfPermission(getContext(), FINE_LOCATION);
        int accessCoarseLocation = ActivityCompat.checkSelfPermission(getContext(), COARSE_LOCATION);

        if (accessFineLocation != PackageManager.PERMISSION_GRANTED && accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocationPermission method :: location access NOK.");
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET
            }, REQUEST_LOCATION);
            return;
        } else {
            Log.d(TAG, "getLocationPermission method :: location access OK");
            mLocationManager.requestLocationUpdates("gps", 10000, 0, mLocationListener);
        }
    }

    private void orderFriendsListByDistance() {
        Log.d(TAG, "orderFriendsListByDistance : Before " + followedUsers.toString());

        if( currentLocation == null ) {
            Log.d(TAG, "orderFriendsListByDistance: Skipping order. My current position has not been found yet.");
        } else {
            Log.d(TAG, "orderFriendsListByDistance: Ordering list.");

            for(int i = 0; i < followedUsers.size(); i++) {
                UserModel usr = followedUsers.get(i);
                Location l = new Location("distance");
                l.setLatitude( usr.getLat() != null ? usr.getLat() : -82.862752 );
                l.setLongitude( usr.getLon() != null ? usr.getLon() : 135.00000 );
                usr.setDistanceFromMe( l.distanceTo(currentLocation) );
            }

            Collections.sort(followedUsers, new LocationComparator());
            setListViewAdapter(followedUsers, true);
            Log.d(TAG, "orderFriendsListByDistance : After " + followedUsers.toString());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready!");
        map = googleMap;
        mapView.onResume();

        queryFollowedUsers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        MapsInitializer.initialize(this.getActivity());

        // Change view
        mFollowedUsersListView = (ListView) v.findViewById(R.id.followedUsersListView);
        mButtonChangeView = (ImageView) v.findViewById(R.id.btnChangeView);
        mButtonChangeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleViewMode();
            }
        });
        mButtonChangeView.setImageResource(R.drawable.ic_list_black_24dp);

        return v;
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume: method called..");
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            if(AppUtility.isNetworkAvailable(context)) {
                Toast.makeText(context, "Find your friends", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void toggleViewMode() {
        if(mCurrentViewMode.equals("MAP")) {
            mCurrentViewMode = "LIST";
            mButtonChangeView.setImageResource(R.drawable.ic_map_black_24dp);
            mapView.setVisibility(View.INVISIBLE);
            mFollowedUsersListView.setVisibility(View.VISIBLE);
        } else {
            mCurrentViewMode = "MAP";
            mButtonChangeView.setImageResource(R.drawable.ic_list_black_24dp);
            mapView.setVisibility(View.VISIBLE);
            mFollowedUsersListView.setVisibility(View.INVISIBLE);
        }
    }

    public void queryFollowedUsers() {

        String requestUri = Api.FOLLOWED.concat("?session_id=").concat(session_id);
        Log.d(TAG, requestUri);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUri, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "queryFollowedUsers onRespose".concat(response.toString()));

                        // instantiate  mapper
                        Gson gson = new Gson();
                        try {
                            JSONArray followed = response.getJSONArray("followed");

                            for(int i = 0; i < followed.length(); i++){
                                try {
                                    UserModel user = gson.fromJson(followed.getJSONObject(i).toString(), UserModel.class);
                                    followedUsers.add( user );
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            addMarkers(followedUsers);

                            setListViewAdapter(followedUsers, false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "queryUsers onError".concat(error.toString()));
            }
        });

        volleyInstance.getRequestQueue().add(request);
    }

    private void setListViewAdapter(ArrayList<UserModel> followedUsers, boolean showDistance) {
        // parse into Array<String> for list view
        ArrayList<String> users = new ArrayList<String>();
        for(int i = 0; i < followedUsers.size(); i++){
            if(showDistance) {
                users.add( followedUsers.get(i).getUsername() + " - " + followedUsers.get(i).getMsg() + " | " + followedUsers.get(i).getDistanceFromMe().toString() + "m" );
            } else {
                users.add( followedUsers.get(i).getUsername() + " - " + followedUsers.get(i).getMsg() );
            }
        }

        ListItemAdapter adapter = new ListItemAdapter( getActivity(), followedUsers);
        mFollowedUsersListView.setAdapter(adapter);
    }

    private void addMarkers(ArrayList<UserModel> users) {
        Log.d(TAG, "AddMarkers");

        if(users != null && users.size() > 0) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            int usersAdded = 0;

            for(int i = 0; i < users.size(); i++) {
                Marker marker;
                MarkerOptions markerOptions;
                UserModel user = users.get(i);

                if(user.getLat() != null && user.getLon() != null) {
                    Log.d(TAG, "AddMarkers for user " + user.getUsername());
                    LatLng userCoordinates = new LatLng( user.getLat(), user.getLon());

                    markerOptions = new MarkerOptions().position( userCoordinates )
                            .title( user.getUsername() );

                    if(user.getMsg() != null) {
                        markerOptions.snippet( user.getMsg() );
                    }

                    marker = map.addMarker( markerOptions );
                    builder.include( marker.getPosition() );
                    usersAdded++;
                }
            }

            if(usersAdded > 0) {
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                map.animateCamera(cu);
            }

        }

    }

}
