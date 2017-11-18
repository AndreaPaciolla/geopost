package com.pacho.geopost.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pacho.geopost.R;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateStateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateStateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateStateFragment extends Fragment implements LocationListener {

    private static final String TAG = "UpdateStateFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderDisabled(String s) {}

    @Override
    public void onProviderEnabled(String s) {}

    private boolean mLocationPermissionGranted;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    EditText mStateView;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public UpdateStateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpdateStateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpdateStateFragment newInstance(String param1, String param2) {
        UpdateStateFragment fragment = new UpdateStateFragment();
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
        mLocationPermissionGranted = false;
        // Assume thisActivity is the current activity
        getLocationPermission();


        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    private void getLocationPermission() {
        String[] permissions = { android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION };

        if(ContextCompat.checkSelfPermission( getContext(), FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission( getActivity().getApplicationContext(), COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
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
                    Toast.makeText(getContext(), "Got location permissin", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_state, container, false);
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
            Toast.makeText(context, "Update state fragment attached", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onLocationChanged(final Location location) {
        Log.d(TAG, "onLocationChanged: got new location " + location.toString());
        mStateView.setText(location.toString());
    }


}
