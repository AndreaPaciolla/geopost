package com.pacho.geopost.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.pacho.geopost.R;
import com.pacho.geopost.services.HttpVolleyQueue;
import com.pacho.geopost.utilities.Api;
import com.pacho.geopost.utilities.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddFriendFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddFriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFriendFragment extends Fragment {

    private static final String TAG = "AddFriendFragment";

    private String session_id;

    // Will be filled inside onCreateView callback
    private AutoCompleteTextView txtAutocomplete;
    private Button btnAddFriend;

    // Require an editor of shared preferences
    SharedPreferences.Editor editor;

    private HttpVolleyQueue volleyInstance = HttpVolleyQueue.getInstance();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public AddFriendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddFriendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddFriendFragment newInstance(String param1, String param2) {
        AddFriendFragment fragment = new AddFriendFragment();
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
        session_id = this.getActivity()
                         .getSharedPreferences(AppConstants.GEOPOST_PREFS, this.getActivity().MODE_PRIVATE)
                         .getString(AppConstants.SESSION_ID, null);
        Log.d(TAG, "Retrieved session_id=".concat(session_id.toString()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        txtAutocomplete = (AutoCompleteTextView)view.findViewById(R.id.txtFriends);

        txtAutocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "textWatcher onTextChanged -> ".concat(charSequence.toString()));
                queryUsers( charSequence.toString() );
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnAddFriend = (Button)view.findViewById(R.id.btnAddFriend);
        btnAddFriend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                addFriend(view);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            Toast.makeText(context, "Add friend fragment attached", Toast.LENGTH_SHORT).show();
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

    public void queryUsers(String findText) {

        String requestUri = Api.USERS.concat("?").concat(session_id).concat("&usernamestart=").concat(findText).concat("&limit=5");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, requestUri, null,
            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "queryUsers onRespose".concat(response.toString()));

                    try {
                        JSONArray usernames = response.getJSONArray("usernames");

                        List<String> usernamesList = new ArrayList<String>();
                        for(int i = 0; i < usernames.length(); i++){
                            usernamesList.add( usernames.getString(i) );
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, usernamesList);

                        txtAutocomplete.setAdapter(adapter);
                    } catch(JSONException e) {
                        Log.d(TAG, "queryUsers".concat( e.toString() ));
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

    public void addFriend(View view) {
        final String username = txtAutocomplete.getEditableText().toString();

        String requestUri = Api.FOLLOW.concat("?session_id=").concat(session_id).concat("&username=").concat(username);

        StringRequest request = new StringRequest(Request.Method.GET, requestUri,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "addFriend onRespose".concat( response.toString() ));

                        // Notify the user
                        String text = "You are now following ".concat( username );
                        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();

                        // Empty the model
                        txtAutocomplete.setText("");

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

}
