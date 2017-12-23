package com.vijay.customapp.spacestationpasses;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by vijay on 12/23/2017.
 */

/**
 * interface for response processing for network calls
 *
 */
public interface DataCallback {
    void onSuccess(JSONObject resultArray);
    void onFailure(VolleyError error);
}
