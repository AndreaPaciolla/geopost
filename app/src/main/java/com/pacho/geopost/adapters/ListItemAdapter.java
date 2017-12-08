package com.pacho.geopost.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacho.geopost.R;
import com.pacho.geopost.models.UserModel;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {


    // Views refs
    private TextView mTxtUsername;
    private TextView mTxtState;
    private TextView mTxtDistance;
    private ImageView mImgUser;

    private ArrayList<UserModel> items;
    private Activity context;

    public ListItemAdapter(Activity context, ArrayList<UserModel> users) {
        this.context = context;
        this.items = users;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_item, null);

        mImgUser = (ImageView) view.findViewById(R.id.imgUser);
        mTxtUsername = (TextView) view.findViewById(R.id.txtUsername);
        mTxtState = (TextView) view.findViewById(R.id.txtState);
        mTxtDistance = (TextView) view.findViewById(R.id.txtDistance);

        mImgUser.setImageResource(R.drawable.ic_face_black_24dp);
        mTxtState.setText( items.get(i).getMsg() );
        mTxtUsername.setText( items.get(i).getUsername() );
        if( items.get(i).getDistanceFromMe() != null ) {
            float distance = items.get(i).getDistanceFromMe();
            if( distance > 1000 ) {
                distance = distance / 1000;
                if( distance > 10000) { // it means data come from NULL values...
                    mTxtDistance.setText( "N.A." );
                } else {
                    mTxtDistance.setText( distance + " km" );
                }
            } else {
                mTxtDistance.setText( distance + " m" );
            }

        }

        return view;
    }
}