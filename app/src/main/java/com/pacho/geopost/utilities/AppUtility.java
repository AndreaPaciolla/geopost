package com.pacho.geopost.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.pacho.geopost.R;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by do_ma on 20/01/2018.
 */

public class AppUtility {

    public static final String TAG = "AppUtility";

    /**
     * @description Check wether the connection is available or not
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void checkNetwork(Context context) {
        if(!AppUtility.isNetworkAvailable(context)) {
            Log.d(TAG, "checkNetwork method: detect no connection...");
            Toast.makeText(context, "No internet connection. Cannot load data...", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @description Show a dialog to let the user to enable the GPS functionality
     */
    public static void showGPSDiabledDialog(final Context context) {
        Log.d(TAG, "showGPSDiabledDialog method ");
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Enable GPS")
                .setMessage("Geopost requires GPS to work.")
                .setPositiveButton(R.string.enable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                })
                //.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                //    public void onClick(DialogInterface dialog, int which) {
                //        // do nothing
                //    }
                //})
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
