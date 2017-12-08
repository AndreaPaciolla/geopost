package com.pacho.geopost.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.pacho.geopost.R;
import com.pacho.geopost.UserModel;
import com.pacho.geopost.activities.LoginActivity;
import com.pacho.geopost.services.HttpVolleyQueue;
import com.pacho.geopost.utilities.Api;
import com.pacho.geopost.utilities.AppConstants;

import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "ProfileFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // View properties
    private Button mButtonLogout;
    private TextView mTxtLastState;
    private TextView mTxtUsername;
    private MapView mMapProfileView;

    private GoogleMap mMapRef;

    private HttpVolleyQueue volleyInstance;
    private String session_id;
    // Require an editor of shared preferences
    SharedPreferences.Editor editor;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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

        // Get volley instance
        volleyInstance = HttpVolleyQueue.getInstance();
        // Get the session_id
        session_id = this.getActivity()
                .getSharedPreferences(AppConstants.GEOPOST_PREFS, this.getActivity().MODE_PRIVATE)
                .getString(AppConstants.SESSION_ID, null);
        // Get an editable instance of editor
        editor = getActivity().getSharedPreferences(AppConstants.GEOPOST_PREFS, MODE_PRIVATE).edit();
    }

    private void initializeViewElements(View view, Bundle savedInstanceState) {
        mButtonLogout = (Button)view.findViewById(R.id.btnLogout);
        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogout();
            }
        });

        mTxtLastState = (TextView) view.findViewById(R.id.txtLastState);
        mTxtUsername = (TextView) view.findViewById(R.id.txtUsername);

        // Gets the MapView from the XML layout and creates it
        mMapProfileView = (MapView) view.findViewById(R.id.mapViewProfile);
        mMapProfileView.onCreate(savedInstanceState);
        mMapProfileView.getMapAsync(this);

        MapsInitializer.initialize( this.getActivity() );
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeViewElements(view, savedInstanceState);
    }

    private void doLogout() {
        String requestUri = Api.USERS + "?session_id=" + session_id;
        Log.d(TAG, "doLogout: firing request " + requestUri);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUri, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "doLogout onRespose" + response.toString());
                        Toast.makeText(getContext(), "Goodbye", Toast.LENGTH_LONG).show();
                        // Reset the sharedPreferences
                        Log.d(TAG, "doLogout onResponse: removing sharedPreferences...");
                        editor.putString(AppConstants.SESSION_ID, null);
                        editor.putString(AppConstants.USER_EMAIL, null);
                        editor.apply(); // better to call apply instead of commit() - commit is sync
                        // Change activity
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "doLogout onError".concat(error.toString()));
            }
        });

        volleyInstance.getRequestQueue().add(request);
    }

    private void getProfileInfo() {
        String requestUri = Api.PROFILE + "?session_id=" + session_id;
        Log.d(TAG, "getProfileInfo: firing request " + requestUri);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUri, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "getProfileInfo onRespose" + response.toString());

                        Gson gson = new Gson();
                        UserModel user = gson.fromJson(response.toString(), UserModel.class);

                        mTxtLastState.setText( user.getMsg() );
                        mTxtUsername.setText( user.getUsername() );

                        ArrayList<UserModel> users = new ArrayList<UserModel>();
                        users.add( user );
                        addMarkers(users);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "getProfileInfo onError".concat(error.toString()));
            }
        });

        volleyInstance.getRequestQueue().add(request);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is ready!");
        mMapRef = googleMap;
        mMapProfileView.onResume();

        getProfileInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);



        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup ) {
        viewGroup.removeAllViewsInLayout();
        View subview = inflater.inflate(R.layout.fragment_profile, viewGroup);

        initializeViewElements(subview, getArguments());
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            Toast.makeText(context, "Profile fragment attached", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

                    marker = mMapRef.addMarker( markerOptions );
                    builder.include( marker.getPosition() );
                    usersAdded++;
                }
            }

            if(usersAdded > 0) {
                LatLngBounds bounds = builder.build();
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                mMapRef.animateCamera(cu);
            }

        } else {
            // get the current position and move the camera where i am right now
        }


    }
}
