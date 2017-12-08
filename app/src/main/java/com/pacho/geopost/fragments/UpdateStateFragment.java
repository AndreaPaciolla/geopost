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
    private HttpVolleyQueue volleyInstance;
    private Location currentLocation;
    private String session_id;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    EditText mStateView;
    Button mUpdateStateButton;

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

        // Get volley instance
        volleyInstance = HttpVolleyQueue.getInstance();

        // get saved session_id
        session_id = this.getActivity()
                         .getSharedPreferences(AppConstants.GEOPOST_PREFS, this.getActivity().MODE_PRIVATE)
                         .getString(AppConstants.SESSION_ID, null);

        mLocationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location == null) {
                    Toast.makeText(getContext(), "Null location got", Toast.LENGTH_LONG).show();
                } else {
                    currentLocation = location;
                    Toast.makeText(getContext(), "Location got. Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        if (ActivityCompat.checkSelfPermission(getContext(), FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET
            }, REQUEST_LOCATION);
            return;
        } else {
            configureButton();
        }


    }

    private void updateState(Editable text) {

        // Avoid crash if we don't have currentLocation
        if( currentLocation == null ) {
            currentLocation = new Location("dummyprovider");
            currentLocation.setLatitude(45.4942699);
            currentLocation.setLongitude(9.1122565);
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

    private void configureButton() {
        mLocationManager.requestLocationUpdates("gps", 5000, 0, mLocationListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case REQUEST_LOCATION:
                configureButton();
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

}
