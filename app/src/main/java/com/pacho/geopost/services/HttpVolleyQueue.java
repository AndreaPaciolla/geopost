package com.pacho.geopost.services;

import com.pacho.geopost.application.GeopostApplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/* @description
 * A common RequestQueue object for your Android app using Google Volley in the form of a Singleton.
 * The singleton provides access to static RequestQueue and ImageLoader objects via its two methods:
 * @author: Andrea Paciolla
 */
public class HttpVolleyQueue {
    private static HttpVolleyQueue mInstance = null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private HttpVolleyQueue() {
        mRequestQueue = Volley.newRequestQueue( GeopostApplication.getAppContext() );
        mImageLoader = new ImageLoader(this.mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });
    }

    public static HttpVolleyQueue getInstance() {
        if(mInstance == null){
            mInstance = new HttpVolleyQueue();
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return this.mImageLoader;
    }

}