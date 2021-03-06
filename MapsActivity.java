package com.example.zubair.cg;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.Manifest;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Marker firstmarker, secondmarker, m;
    Button firstlocation_btn, secondlocation_btn, clearlocation_btn,ReCenter,Explore;
    LatLng firstLocation, secondLocation, general;
    Polyline mPolyline;
    List allPoints;
    TextView duration, distance;
    List points;
    Marker marker;
    LatLng toPosition;
    int ii;
    TextToSpeech tt;
    Runnable runnable;
    public static String dis;
    public static String dur;
    List<Polyline> polyLines;
    List<Polyline> polyLines_green;
    String voicenottospeak="no";
    String voicetospeak;
    List voice;
    boolean reCenter = true;

String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        distance = (TextView) findViewById(R.id.distance);
        duration = (TextView) findViewById(R.id.duration);
        points = new ArrayList();
        firstlocation_btn = (Button) findViewById(R.id.first_loc_btn);
        secondlocation_btn = (Button) findViewById(R.id.second_loc_btn);
        clearlocation_btn = (Button) findViewById(R.id.clear_btn);
        ReCenter = (Button) findViewById(R.id.Recenter_btn);
        Explore = (Button) findViewById(R.id.Explore_btn);
        polyLines = new ArrayList<Polyline>();
        polyLines_green=new ArrayList<Polyline>();

        Explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reCenter = false;
            }
        });

        ReCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reCenter = true;
            }
        });

        tt=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tt.setLanguage(Locale.ENGLISH);

            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        firstlocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstLocation = general;
                firstmarker = mMap.addMarker(new MarkerOptions()
                        .position(firstLocation)
                        .title("Start Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                );

                firstlocation_btn.setEnabled(false);
                secondlocation_btn.setEnabled(true);
            }
        });
        secondlocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secondLocation = general;
                secondmarker = mMap.addMarker(new MarkerOptions()
                        .position(secondLocation)
                        .title("Destination")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );
                secondlocation_btn.setEnabled(false);
                clearlocation_btn.setEnabled(false);
              //  tt.speak("Journey Started",TextToSpeech.QUEUE_FLUSH,null);
                url = getDirectionsUrl(firstLocation, secondLocation);

                DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                downloadTask.execute(url);

            }
        });
        clearlocation_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstlocation_btn.setEnabled(true);
                secondlocation_btn.setEnabled(false);
                firstmarker.remove();
                secondmarker.remove();
                m.remove();
                m = mMap.addMarker(new MarkerOptions()
                        .position(secondLocation)
                        .title("Set Location"));
                distance.setText("Distance : ");
                duration.setText("Duration : ");
                ii=0;
                if(polyLines!=null) {
                    for (int i = 0; i < polyLines.size(); i++) {
                        polyLines.get(i).remove();
                    }
                }
                if(polyLines_green!=null) {
                    for (int i = 0; i < polyLines_green.size(); i++) {
                        polyLines_green.get(i).remove();
                    }
                }
            }
        });
    }

    @Override

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final LatLng myLocation = new LatLng(24.945990468981336, 67.11514055728912);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14.0f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f), 3000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                m = mMap.addMarker(new MarkerOptions()
                        .position(myLocation)
                        .title("Set Location"));

            }

            @Override
            public void onCancel() {

            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {

                // Get the center of the Map.
                LatLng centerOfMap = mMap.getCameraPosition().target;
                general = centerOfMap;
                // Update your Marker's position to the center of the Map.
                if (firstlocation_btn.isEnabled() || secondlocation_btn.isEnabled()) {
                    m.setPosition(centerOfMap);
                }
            }
        });

    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

//https://maps.googleapis.com/maps/api/directions/json?origin=24.94846,67.107078&destination=24.942868,67.096983&sensor=false&mode=driving
        return url;
    }

    public class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.connect();

                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();

                br.close();

            } catch (Exception e) {
                //  Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser(firstLocation, secondLocation);

                routes = parser.parse(jObject);

                Toast.makeText(getApplicationContext(), dis + dur, Toast.LENGTH_LONG).show();


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }


        @Override
        protected void onPostExecute(List<List<HashMap>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            if(result!= null) {
                for (int i = 0; i < result.size(); i++) {
                     points = new ArrayList();
                     lineOptions = new PolylineOptions();

                     List<HashMap> path = result.get(i);

                     for (int j = 0; j < path.size(); j++) {
                          HashMap point = path.get(j);
                          double lat = Double.parseDouble(point.get("lat").toString());
                          double lng = Double.parseDouble(point.get("lng").toString());
                          LatLng position = new LatLng(lat, lng);

                          points.add(position);
                      }

                 }
            }
            distance.setText(distance.getText() + dis);
            duration.setText(duration.getText() + dur);
            allPoints = points;
            voice=DirectionsJSONParser.voicelist;
            Log.e("Voice List size",voice.size()+"");
            Log.e("Points List size",allPoints.size()+"");
            if (allPoints != null && allPoints.size() > 0) {
                animateMarker(false);
                runnable = new Runnable() {
                    //Marker m = mMap.addMarker(new MarkerOptions().position((LatLng) allPoints.get(ii)));
                    @Override
                    public void run() {
                        marker.remove();
                        animateMarker(false);
                    }
                };

                if(points != null && points.size() >0) {
                     drawPolylines(points, lineOptions,1);
                }

            } else {
                Toast.makeText(getApplicationContext(), "Points Not fetched, Retrying.. Please Check Your Network Connection", Toast.LENGTH_SHORT).show();
                DownloadTask downloadTask = new DownloadTask(); //RESTART DOWNLOADING
                downloadTask.execute(url);
            }
        }


public void drawPolylines(List points,PolylineOptions lineOptions,int x){
    int count = points.size() / 20;  // counting how much polylines should be drawn
//each polyline should have maximum of 20 points // else it make application hanged
    int counter = 0;
    if(count ==0){
        lineOptions.addAll(points);
        lineOptions.width(12);
        if (x == 1) {
            lineOptions.color(Color.RED);
            polyLines.add(mMap.addPolyline(lineOptions));
        }
        else {
            lineOptions.width(14);
            lineOptions.color(Color.GREEN);
            polyLines_green.add(mMap.addPolyline(lineOptions));
        }

        lineOptions.geodesic(true);
    }
    else {
        for (int i = 0; i < count; i++) {
            List chunk = new ArrayList();
            for (int j = 0; j < 20; j++) {
                chunk.add(points.get((counter)));
                counter++;
            }
            lineOptions.addAll(chunk);
            lineOptions.width(12);

            if (x == 1) {
                lineOptions.color(Color.RED);
                polyLines.add(mMap.addPolyline(lineOptions));
            }
            else {
                lineOptions.width(14);
                lineOptions.color(Color.GREEN);
                polyLines_green.add(mMap.addPolyline(lineOptions));
            }
            lineOptions.geodesic(true);

        }
        if(points.size() % 20 != 0){  //adding remaining points if left e.g 45 points..5 points left after executing above application
            List chunk = new ArrayList();    //so adding remaining points here in this for loop (used % for that purpose )
            for(int p = 0; p< points.size() % 20; p++){

                    chunk.add(points.get((counter)));
                    counter++;
            }
            lineOptions.addAll(chunk);
            lineOptions.width(12);
            lineOptions.geodesic(true);
            if (x == 1) {
                lineOptions.color(Color.RED);
                polyLines.add(mMap.addPolyline(lineOptions));
            }
            else {
                lineOptions.width(14);
                lineOptions.color(Color.GREEN);
                polyLines_green.add(mMap.addPolyline(lineOptions));
            }


        }
    }
}

        public void animateMarker(final boolean hideMarker) {
            final Handler handler = new Handler();
            final long duration = 50;
            final Interpolator interpolator = new LinearInterpolator();

             //final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.west);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    marker = mMap.addMarker(new MarkerOptions()
                            .position((LatLng) allPoints.get(ii))
                            //.icon(icon)
                    );
                    toPosition = (LatLng) allPoints.get(ii + 1);
                    long start = SystemClock.uptimeMillis();
                    Projection proj = mMap.getProjection();
                    Point startPoint = proj.toScreenLocation(marker.getPosition());
                    final LatLng startLatLng = proj.fromScreenLocation(startPoint);
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed
                            / duration);
                    double lng = t * toPosition.longitude + (1 - t)
                            * startLatLng.longitude;
                    double lat = t * toPosition.latitude + (1 - t)
                            * startLatLng.latitude;
                    marker.setPosition(new LatLng(lat, lng));
                    if (reCenter){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));

                    }
                    if (t < 1.0) {
                        // Post again 16ms later.
                         //handler.postDelayed(this, 16);
                    } else {
                        if (hideMarker) {
                            marker.setVisible(false);
                        } else {

                            marker.setVisible(true);
                        }
                    }
                    if (ii < allPoints.size() - 2) {
                       // drawGreenLine();
                        if(voicenottospeak.equals((String)voice.get(ii))){

                        }
                        else{
                            voicetospeak=(String) voice.get(ii);
                            if(ii==0) {
                                Toast.makeText(getApplicationContext(), "Journey started", Toast.LENGTH_SHORT).show();
                            }
                            tt.speak(voicetospeak,TextToSpeech.QUEUE_FLUSH,null);
                        }
                        ii++;
                        handler.postDelayed(runnable, 50);
                    }
                    else{
                        tt.speak("You reached your destination",TextToSpeech.QUEUE_FLUSH,null);
                        Toast.makeText(getApplicationContext(), "You have reached your destination", Toast.LENGTH_SHORT).show();
                        marker.remove();
                        m.setTitle("Destination");
                        clearlocation_btn.setEnabled(true);
                    }
                }

            });
        }
        public void drawGreenLine(){
            if(polyLines_green!=null) {
                for (int i = 0; i < polyLines_green.size(); i++) {
                    polyLines_green.get(i).remove();
                }
            }
            PolylineOptions polylineOptions=new PolylineOptions();//help to draw line on map
            List chunk=new ArrayList();
            if(ii!=0) {
                for (int i = 0; i < ii; i++) {
                    if(allPoints.size()>0)
                    chunk.add(allPoints.get(i));


                }
            }
            drawPolylines(chunk,polylineOptions,2);

        }
    }
}


