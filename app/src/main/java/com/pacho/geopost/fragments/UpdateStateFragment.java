package com.pacho.geopost.fragments;

import android.Manifest;
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
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.pacho.geopost.R;
import com.pacho.geopost.services.HttpVolleyQueue;
import com.pacho.geopost.utilities.Api;
import com.pacho.geopost.utilities.AppConstants;
import com.pacho.geopost.utilities.AppUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpdateStateFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateStateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateStateFragment extends Fragment {

    private static final String TAG = "UpdateStateFragment";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_LOCATION = 1;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location currentLocation;
    private HttpVolleyQueue volleyInstance;
    private String session_id;

    EditText mStateView;
    Button mUpdateStateButton;

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
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get volley instance
        volleyInstance = HttpVolleyQueue.getInstance();

        // get saved session_id
        session_id = this.getActivity()
                         .getSharedPreferences(AppConstants.GEOPOST_PREFS, this.getActivity().MODE_PRIVATE)
                         .getString(AppConstants.SESSION_ID, null);

        getLocationPermission();
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
                        Toast.makeText(getContext(), "Location found", Toast.LENGTH_LONG).show();
                    }
                } catch(Exception e) {
                    Log.d(TAG, "onLocationChanged method: Caught exception on location got..." + e.getMessage());
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d(TAG, "onStatusChanged callback:" + s.toString());
            }

            @Override
            public void onProviderEnabled(String s) {

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

    private void updateState(Editable text) {

        // Avoid crash if we don't have currentLocation
        if( currentLocation == null ) {
            Toast.makeText(getContext(), "Cannot find your position...", Toast.LENGTH_LONG).show();
            return;
        }

        String requestUri = Api.STATUS_UPDATE + "?session_id=" + session_id + "&message=" + text.toString() + "&lat="+currentLocation.getLatitude()+"&lon="+currentLocation.getLongitude();
        Log.d(TAG, "updateState: firing request " + requestUri);
        StringRequest request = new StringRequest(Request.Method.POST, requestUri,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "updateState onRespose" + response);
                        Toast.makeText(getContext(), "State has been updated", Toast.LENGTH_LONG).show();
                        mStateView.setText("");
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "updateState onError: " + error.toString());
            }
        });

        volleyInstance.getRequestQueue().add(request);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // Configure the views
        mStateView = (EditText) view.findViewById(R.id.txtState);
        mUpdateStateButton = (Button) view.findViewById(R.id.btnUpdateState);
        mUpdateStateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateState(mStateView.getText());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_state, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            if(AppUtility.isNetworkAvailable(context)) {
                Toast.makeText(context, "Say something new", Toast.LENGTH_SHORT).show();
            }
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

}
