package com.example.zubair.cg;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Zubair on 13-Dec-17.
 */
public class DirectionsJSONParser {


    LatLng firstLocation,secondLocation;


    public static List voicelist;
    DirectionsJSONParser (LatLng firstLocation, LatLng secondLocation){
        this.firstLocation = firstLocation;
        this.secondLocation = secondLocation;

        voicelist=new ArrayList();
    }

    List addStartLine(double Lat, double Lng, LatLng firstLocation){
        List li = new ArrayList();
        //Log.e("In func","");
        for(float t = 0 ; t<1; t+=0.1 ){
            double lat = firstLocation.latitude +(Lat-firstLocation.latitude)*t;
            double lng = firstLocation.longitude +(Lng-firstLocation.longitude)*t;
            //Log.e("Lat Lang",lat+" "+lng);
            LatLng p = new LatLng(lat,lng);
            li.add(p);
            if(t==0){
                voicelist.add("Journey started");
            }
            else{
                voicelist.add("no");
            }
        }
        //Log.e("List size",li.size()+"");
        return  li;
    }
    List addEndLine(double Lat, double Lng, LatLng secondLocation
    ){
        List li = new ArrayList();
        // Log.e("In func","");
        for(double t = 0 ; t<=1; t+=0.1 ){
            double lat = Lat  +(secondLocation.latitude-Lat)*t;
            double lng = Lng +(secondLocation.longitude-Lng)*t;
            //Log.e("Lat Lang",lat+" "+lng);
            LatLng p = new LatLng(lat,lng);
            li.add(p);
            voicelist.add("no");
        }
        double lat = Lat  +(secondLocation.latitude-Lat)*1;
        double lng = Lng +(secondLocation.longitude-Lng)*1;
        LatLng p = new LatLng(lat,lng);
        li.add(p);
        voicelist.add("You reach your destination");
        ///Log.e("List size",li.size()+"");
        return  li;
    }
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap>> parse(JSONObject jObject){

        List<List<HashMap>> routes = new ArrayList<List<HashMap>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;



        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                for (int j = 0; j < jLegs.length(); j++) {
                    MapsActivity.dur   = (String) ((JSONObject) ((JSONObject) jLegs.get(j)).get("duration")).get("text");
                    MapsActivity.dis = (String) ((JSONObject)    ((JSONObject) jLegs.get(j)).get("distance")).get("text");

                    // dis = (String) ((JSONObject) ((JSONObject) jLegs.get(j)).get("distance")).get("text");


                }
                List path = new ArrayList<HashMap<String, String>>();
                //Log.e("In Direcrion class", "in direction");
                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    Double start_lat,start_lng,end_lat,end_lng;
                    String voicetext=null;
                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        voicetext = (String) (((JSONObject) jSteps.get(k)).get("html_instructions"));
                        start_lat =(Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lat");
                        start_lng = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lng");
                        end_lat = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).get("lat");
                        end_lng = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("end_location")).get("lng");

                        //Log.e("in final loop", k + "");
                        if (k == 0) {
                            // Log.e("k", "k is zero");
                            Double lat = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lat");
                            Double lng = (Double) ((JSONObject) ((JSONObject) jSteps.get(k)).get("start_location")).get("lng");
                            // Log.e("lat lang is ", lat + lng + "");
                           // List li = getPoints(firstLocation,new LatLng(lat,lng),voicetext);
                            List li=addStartLine(lat,lng,firstLocation);
                            //Log.e("list size ", li.size() + "");
                            for (int z = 0; z < li.size(); z++) {
                                HashMap<String, String> hm = new HashMap<String, String>();
                                hm.put("lat", Double.toString(((LatLng) li.get(z)).latitude));
                                hm.put("lng", Double.toString(((LatLng) li.get(z)).longitude));
                                path.add(hm);
                                voicelist.add("no");
                            }
                        }
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                       // List list=getPoints(new LatLng(start_lat,start_lng),new LatLng(end_lat,end_lng),voicetext);
                         List list = decodePoly(polyline);

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                            hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    {
                        // Log.e("k", "k is zero");
                        Double lat = (Double) ((JSONObject) ((JSONObject) jSteps.get(jSteps.length() - 1)).get("end_location")).get("lat");
                        Double lng = (Double) ((JSONObject) ((JSONObject) jSteps.get(jSteps.length() - 1)).get("end_location")).get("lng");
                        //Log.e("lat lang is ", lat + lng + "");
                     Log.e("near end line","end line near");
                       List li=addEndLine(lat,lng,secondLocation);
                        // List li = getPoints(new LatLng(lat,lng), secondLocation,voicetext);
                        //Log.e("list size ", li.size() + "");
                        for (int z = 0; z < li.size(); z++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng) li.get(z)).latitude));
                            hm.put("lng", Double.toString(((LatLng) li.get(z)).longitude));
                            path.add(hm);
                        }


                        routes.add(path);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }


        return routes;
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    public List getPoints(LatLng start,LatLng end,String voicetext){
        double v=0.0001789875;  //250m step
        List points =new ArrayList();
        int lat_steps=(int)Math.abs((end.latitude-start.latitude)/v);
        int  lng_steps=(int)Math.abs((end.longitude-start.longitude)/v);
        int  count=getmax(lat_steps,lng_steps);
        double lat_inc =(end.latitude-start.latitude)/count;
        double lng_inc =(end.longitude-start.longitude)/count;
        for(int i=0;i<count; i++){
            points.add(new LatLng(start.latitude+lat_inc*i,start.longitude+lng_inc*i) );
            voicelist.add("no");
        }
        points.add(new LatLng(start.latitude+lat_inc*(count),start.longitude+lng_inc*(count)));
        if(checkRight(voicetext))
            voicelist.add("Turn Right");
        else if(checkLeft(voicetext))
        voicelist.add("Turn Left");
        else
        voicelist.add("no");


        return points;



    }
    public int  getmax(int lat,int lng){
        if(lat>lng){
            return lat;
        }
        else
            return lng;
    }
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    boolean checkRight(String text){
        for (int i = 0; i < text.length()-3; i++)
        {
            if (text.charAt(i) == 'l') {

                if (text.charAt(i+1) == 'e') {

                    if (text.charAt(i+2) == 'f') {

                        if (text.charAt(i+3) == 't')
                        {
                           return true;
                        }
                    }

                }

            }
        }
return  false;
    }
    boolean checkLeft(String text){
        for (int i = 0; i < text.length()-4; i++)
        {
            if (text.charAt(i) == 'r') {

                if (text.charAt(i+1) == 'i') {

                    if (text.charAt(i+2) == 'g') {

                        if (text.charAt(i+3) == 'h')
                        {
                            if(text.charAt(i+4)=='t')
                            return true;
                        }
                    }

                }

            }
        }
return false;
    }

}
