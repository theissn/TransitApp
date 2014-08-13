package com.example.iftekprojekt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Log;


public class SearchResultsActivity extends Activity {
/*
    Her sendes brugeren hen, når han trykker på søg i MainActivity.
    Der vises en liste over alle søgeresultater, som er fundet ved hjælp af Google Maps API.
 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);
        handleIntent(getIntent());

	}

    //Brugerens søgning tilpasses, så den er URL-friendly
    //Hvis brugeren har lavet et komma, deles søgningen op.
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            query = query.replace(" ","%20");
            String location;
            if (query.contains(",")) {
                String[] parts = query.split(",");
                query = parts[0];
                location = parts[1];
            }
            else {
                location = query;
                query = "";
            }

            RequestTask getJSON = new RequestTask();
            RequestTask getLoc = new RequestTask();
            ListView searchList = (ListView) findViewById(R.id.searchresults);
            ArrayList<String> nameList = new ArrayList<String>();
            final ArrayList<Double> latList = new ArrayList<Double>();
            final ArrayList<Double> lngList = new ArrayList<Double>();


            String locJSON = "";
            String queryJSON = "";

            //Her bruges Google Geocode API til at finde det område, som destinationen findes i
            try {
                locJSON = getLoc.execute("https://maps.googleapis.com/maps/api/geocode/json?address="+location+"&sensor=false&key=AIzaSyDxi88U13KO2alOe_I8UxLIwovuArYPYPY").get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            Double latitude = null;
            Double longitude = null;


            try {//Det første resultat bruges, og ved hjælp af JSON, findes latitude og longitude for området.
                JSONObject locObject = new JSONObject(locJSON);
                String status = locObject.getString("status");
                if(status.equals("OK")) {
                    JSONArray results = locObject.getJSONArray("results");
                    JSONObject firstObj = results.getJSONObject(0);
                    latitude = firstObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    longitude = firstObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                if (latitude != null) { //Hvis der kunne findes et område, bruges koordinater nu sammen med brugerens søgning, til at finde resultater
                    queryJSON = getJSON.execute("https://maps.googleapis.com/maps/api/place/search/json?name="+ query +"&types=bus_station" + URLEncoder.encode("|","UTF-8") + "train_station&location="+latitude.toString()+","+longitude.toString()+"&radius=5000&sensor=false&key=AIzaSyDxi88U13KO2alOe_I8UxLIwovuArYPYPY").get();
                }
                else
                    nameList.add("Ingen søgeresultater!");//Hvis der ikke kunne findes et område
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try { //Her trækkes alle søgeresultater ud, hvis der er nogen, ved hjælp af JSON
                JSONObject searchObj = new JSONObject(queryJSON);
                String status = searchObj.getString("status");
                if(status.equals("OK")) { //Hvis der er resultater
                    JSONArray results = searchObj.getJSONArray("results");
                    for (int i=0; i < results.length(); i++) {
                        JSONObject listObj = results.getJSONObject(i);
                        String name = listObj.getString("name");
                        Double lat = listObj.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        Double lng = listObj.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        latList.add(lat);
                        lngList.add(lng);
                        nameList.add(name);
                    }

                }

                else //Hvis der ingen resultater findes i det valgte område.
                    nameList.add("Ingen søgeresultater!");
            } catch (JSONException e) {
                e.printStackTrace();
            }



            //Her generes listen, og vises til brugeren. Hvis brugeren trykker på et af resultaterne, sendes han videre til setScreen
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameList);
            searchList.setAdapter(adapter);

            searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final String item = (String) parent.getItemAtPosition(position);
                    Double lat = latList.get(position);
                    Double lng = lngList.get(position);

                    //Sendes til setScreen med informationer om stationens navn og koordinater.
                    Intent setScreen = new Intent(SearchResultsActivity.this, setScreen.class);
                    setScreen.putExtra("item", item);
                    setScreen.putExtra("lat", lat);
                    setScreen.putExtra("lng", lng);
                    startActivity(setScreen);
                }

            });


        }
    }
    //Denne class bruges til at kunne hente JSON fra en URL
    class RequestTask extends AsyncTask<String, String, String> {

        InputStream inputStream = null;
        public String result = "";

        @Override
        protected String doInBackground(String... uri) {
            String url_select = uri[0];

            ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

            try {//HttpClient sættes op
                HttpClient httpClient = new DefaultHttpClient();

                HttpPost httpPost = new HttpPost(url_select);
                httpPost.setEntity(new UrlEncodedFormEntity(param));
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();


                inputStream = httpEntity.getContent();
            } catch (UnsupportedEncodingException e1) {
                Log.e("UnsupportedEncodingException", e1.toString());
                e1.printStackTrace();
            } catch (ClientProtocolException e2) {
                Log.e("ClientProtocolException", e2.toString());
                e2.printStackTrace();
            } catch (IllegalStateException e3) {
                Log.e("IllegalStateException", e3.toString());
                e3.printStackTrace();
            } catch (IOException e4) {
                Log.e("IOException", e4.toString());
                e4.printStackTrace();
            }
            // Genererer en string fra URL'en, tilføjer en linje ad gangen
            try {
                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line + "\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
            }
            return result;
        }

    }

}
