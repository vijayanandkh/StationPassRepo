package com.vijay.customapp.spacestationpasses;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.net.Proxy.Type.HTTP;


/**
 * Created by vijay on 12/23/2017.
 */

public class NetworkController {

    private static final NetworkController mInstance = new NetworkController();
    private static final String TAG = NetworkController.class.getSimpleName();
    private RequestQueue mRequestQueue;
    private Context mContext;



    public static synchronized NetworkController getInstance() {
        return mInstance;
    }

    private NetworkController() {
    }
    public RequestQueue getRequestQueue(Context context) {
        mContext = context;
        if(mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Context context, Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue(context).add(request);
    }

    public <T> void addToRequestQueue(Context context, Request<T> request) {
        getRequestQueue(context).add(request);
    }

    public void cancelPendingRequests(Object object) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(object);
        }
    }



}
