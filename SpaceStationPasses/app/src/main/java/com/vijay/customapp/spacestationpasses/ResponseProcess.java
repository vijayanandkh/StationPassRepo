package com.vijay.customapp.spacestationpasses;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.vijay.customapp.spacestationpasses.ISSPassInfo.ISSPassDetail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static com.vijay.customapp.spacestationpasses.Constants.DBG;
import static com.vijay.customapp.spacestationpasses.Constants.TAG;

/**
 * Created by vijay on 12/23/2017.
 */

public class ResponseProcess implements Response.Listener<JSONObject>, Response.ErrorListener {

    private List<ISSPassDetail.ISSPassesItem> issPassesItemsList;

    public ResponseProcess() {
        issPassesItemsList = new ArrayList<ISSPassDetail.ISSPassesItem>(26);
    }
    @Override
    public void onResponse(JSONObject response) {
        // on success, parse objects
        // Process the JSON
        Log.d(TAG, "ResponseProcess:onResponse");
        try{
            issPassesItemsList.clear();
            // Loop through the array elements
            JSONArray arrayObject = response.getJSONArray("response");
            for(int i=0;i<arrayObject.length();i++){
                // Get current json object
                JSONObject passInfo = arrayObject.getJSONObject(i);
                Long risetime = passInfo.getLong("risetime");
                int duration = passInfo.getInt("duration");
                issPassesItemsList.add(i,new ISSPassDetail.ISSPassesItem(risetime, duration));
                if(DBG) Log.d(TAG, "Info itmes: " + i + " r: " + new SimpleDateFormat("MM/dd/YY HH:mm:ss").format(new Date(risetime)) + " d: "+ duration );
            }
        }catch (JSONException e){
            Log.e(TAG, "erroor processing response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // on error, check error and notify
        Log.d(TAG, "ResponseProcess:onErrorResponse" + error.getMessage());
        issPassesItemsList.clear();
    }

    public List<ISSPassDetail.ISSPassesItem> getIssPassesItemsList() {
        return issPassesItemsList;
    }

}
