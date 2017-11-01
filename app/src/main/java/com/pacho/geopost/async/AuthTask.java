package com.pacho.geopost.async;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pacho.geopost.R;
import com.pacho.geopost.utilities.api;
import com.tapadoo.alerter.Alerter;

import org.json.JSONObject;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */

public class AuthTask extends AsyncTask<Void, Void, Boolean> {


    private final String mEmail;
    private final String mPassword;
    private final AppCompatActivity mActivityRef;

    public AuthTask(String email, String password, AppCompatActivity activityRef) {
        mEmail = email;
        mPassword = password;
        mActivityRef = activityRef;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // TODO: attempt authentication against a network service.

        try {
            // Simulate network access.
            Thread.sleep(2000);
            Log.d("AuthTask", "doInBackground....");
            StringRequest request = new StringRequest(Request.Method.GET, api.LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("AuthTask", "onResponse");
                        Alerter.create(mActivityRef)
                                .setTitle("Alert Title")
                                .setText(response.toString())
                                .show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("AuthTask", "onErrorResponse".concat(error.toString()));
                    }
            });

            RequestQueue queue = Volley.newRequestQueue(mActivityRef.getApplicationContext());
            queue.add(request);

        } catch (InterruptedException e) {
            Log.d("AuthTask", "Catch block... ".concat(e.toString()));
            return false;
        }

        // TODO: register the new account here.
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {

        // @TODO go to next activity by using intent...
        /*mAuthTask = null;
        showProgress(false);

        if (success) {
            finish();
        } else {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }*/
    }

    @Override
    protected void onCancelled() {
        //mAuthTask = null;
        //showProgress(false);
    }


}
