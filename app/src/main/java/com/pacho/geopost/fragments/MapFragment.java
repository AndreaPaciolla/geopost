package com.pacho.geopost.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.pacho.geopost.Manifest;
import com.pacho.geopost.R;
import com.pacho.geopost.UserModel;
import com.pacho.geopost.services.HttpVolleyQueue;
import com.pacho.geopost.utilities.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


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
    public static final String GEOPOST_PREFS = "geopost_prefs";
    public static final String SESSION_ID_KEY = "session_id";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final float DEFAULT_ZOOM = 15f;

    private boolean mLocationPermissionGranted = false;

    private Button mButtonChangeView;
    private ListView mFollowedUsersListView;

    private String mCurrentViewMode = "MAP"; // MAP | LIST

    private MapView mapView;
    private GoogleMap map;
    private String session_id;
    private HttpVolleyQueue volleyInstance;
    private ArrayList<UserModel> followedUsers = new ArrayList<UserModel>();
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        try {
            if(mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "found location");
                            Location currentLocation = (Location)task.getResult();
                            moveCamera( new LatLng( currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getContext(), "Could not get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch(SecurityException e) {
            Log.e(TAG, "getDeviceLocation: Security exception " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to " + latLng.toString());
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void getLocationPermission() {
        String[] permissions = { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION };

        if(ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), COURSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                //initMap();
            } else {
                ActivityCompat.requestPermissions( getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions( getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        // initialize the map
                        mLocationPermissionGranted = false;
                        return;
                    }
                    mLocationPermissionGranted = true;
                    //initMap();
                }
            }
        }
    }

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
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getActivity(), "You can't make map requests.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /*private void initMap() {
        Log.d(TAG, "initMap");
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "initMap: OnMapReady");
                map = googleMap;
                // Add markers
                queryFollowedUsers();
                if(mLocationPermissionGranted) {
                    getDeviceLocation();
                }
            }
        });
    }*/

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        volleyInstance = HttpVolleyQueue.getInstance();
        session_id = this.getActivity().getSharedPreferences(GEOPOST_PREFS, this.getActivity().MODE_PRIVATE).getString(SESSION_ID_KEY, null);

        if(isServicesOK()) {
            getLocationPermission();
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
        mButtonChangeView = (Button) v.findViewById(R.id.btnChangeView);
        mButtonChangeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleViewMode();
            }
        });

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            Toast.makeText(context, "Map fragment attached", Toast.LENGTH_SHORT).show();
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
            mButtonChangeView.setText("MAP");
            mapView.setVisibility(View.INVISIBLE);
            mFollowedUsersListView.setVisibility(View.VISIBLE);
        } else {
            mCurrentViewMode = "MAP";
            mButtonChangeView.setText("LIST");
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

                            // parse into Array<String> for list view
                            ArrayList<String> users = new ArrayList<String>();
                            for(int i = 0; i < followedUsers.size(); i++){
                                users.add(followedUsers.get(i).getUsername() + " - " + followedUsers.get(i).getMsg());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, users);
                            mFollowedUsersListView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                Log.d(TAG, "queryUsers onError".concat(error.toString()));
            }
        });

        volleyInstance.getRequestQueue().add(request);
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
                    Log.d(TAG, "AddMarkers for user ".concat( user.getUsername()));
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

        } else {
            // get the current position and move the camera where i am right now
        }


    }

}
