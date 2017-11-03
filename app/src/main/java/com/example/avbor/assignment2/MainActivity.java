package com.example.avbor.assignment2;

import android.content.pm.ActivityInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
   // initiate variables
    public double lat;
    public double longt;
    private static final String TAG = MainActivity.class.getSimpleName();
   // initiate widgets elements
    public TextView textLat;
    public TextView textLongt;
    public TextView textCity;
    public TextView textCityTemp;
    public TextView textCityCond;
    public TextView textNext;
    public TextView textNextTemp;
    public TextView textNextCond;
    // initiate elemets for maps and for weather
    public RequestQueue reqQ;
    FusedLocationProviderClient client;
    Task<Location> location;
    GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        textLat = (TextView) findViewById(R.id.textLat);
        textLongt = (TextView) findViewById(R.id.textLongt);

        textCity = (TextView) findViewById(R.id.textCity);
        textCityTemp = (TextView) findViewById(R.id.textCityTemp);
        textCityCond = (TextView) findViewById(R.id.textCityCond);

        textNext = (TextView) findViewById(R.id.textNext);
        textNextTemp = (TextView) findViewById(R.id.textNextTemp);
        textNextCond = (TextView) findViewById(R.id.textNextCond);

        client = LocationServices.getFusedLocationProviderClient(this);
        try
        {
            location = client.getLastLocation();
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        location.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                longt = task.getResult().getLongitude();
                lat = task.getResult().getLatitude();
                textLongt.setText(Double.toString(longt));
                textLat.setText(Double.toString(lat));
                getWeather();
                setPin();
            }
        });
        reqQ = Volley.newRequestQueue(this);
        MapFragment fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
        fragment.getMapAsync(this);
    }

    public void getWeather()
    {
        String url = getWehterLink(lat, longt);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject request)
            {
                try
                {
                    Log.d("query result: ", request.toString());

                    JSONObject result = request.getJSONObject("query").getJSONObject("results").getJSONObject("channel");
                    JSONObject cond = result.getJSONObject("item").getJSONObject("condition");
                    JSONArray fcast = result.getJSONObject("item").getJSONArray("forecast");

                    textCity.setText (result.getJSONObject("location").getString("city"));
                    textCityTemp.setText (cond.getString("temp"));
                    textCityCond.setText (cond.getString("text"));


                    textNext.setText (fcast.getJSONObject(1).getString("day"));
                    textNextTemp.setText (fcast.getJSONObject(1).getString("high"));
                    textNextCond.setText (fcast.getJSONObject(1).getString("text"));



                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e(TAG, "Error on responce");
            }
        });

        reqQ.add(request);
    }


    public String getWehterLink(double longt, double lat) {
        String YQL = String.format("select * from weather.forecast where woeid in (SELECT woeid FROM geo.places WHERE text=\"(%.7f,%.7f)\") and u='c'", longt, lat);
        String urlLink = String.format("https://query.yahooapis.com/v1/public/yql?q=%s&format=json", Uri.encode(YQL));
        Log.d(TAG, "get wether worked!!");
        //Log.d(TAG, urlLink);
        return urlLink;
    }

    @Override
    public void onMapReady(GoogleMap map)
    {
        this.map = map;
        Log.d(TAG, "Map started");
    }

    public void setPin()
    {
        LatLng currentLocation = new LatLng(lat, longt);
        map.addMarker(new MarkerOptions().position(currentLocation).title("You Are Here"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        Log.d(TAG, "Pin set");
    }
}
