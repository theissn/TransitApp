package com.example.iftekprojekt;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {
/* Dette er MainActiviy, som er det første brugeren møder. Her kan brugeren søge,
hvilket sender ham videre til SearchResultsActivity, eller vælge en af sine foretrukne stationer,
som sender ham direkte videre til setScreen
 */
    String[] names, lats, lngs; // Array's med navne på stop, samt koordinater(latitude og longitude)
    ArrayList<String> latList, lngList; //Arraylists, som også skal bruges til koordinater


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Vis actionbar i toppen
		ActionBar actionBar = getActionBar();
        actionBar.show();


		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

        final ListView favoritesList = (ListView) findViewById(R.id.favoritesList); //Listviewet, som indeholder brugerens favoritter
        final ArrayList<String> list = new ArrayList<String>();//Liste med favoritter
        latList = new ArrayList<String>();//Koordinater
        lngList = new ArrayList<String>();

        //SharedPreferences bruges til at gemme data lokalt på telefonen, her hentes Navne og koordinater til foretrukne
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sharedPreferences.getString("Names", null);
        String latitude = sharedPreferences.getString("Lats", null);
        String longitude = sharedPreferences.getString("Lngs", null);


        if(name==null){ //Hvis der ingen foretrukne findes
            list.add("Du har ingen foretrukne");
        }
        else {
            if(name.contains(",")){//Hvis der er flere end én foretrukne
                names = name.split(",");
                lats = latitude.split(",");
                lngs = longitude.split(",");

                for(int i = 0; i<names.length; i++){
                    if(!names[i].equals("")) {
                        list.add(names[i]);
                        latList.add(lats[i]);
                        lngList.add(lngs[i]);
                    }
                }
            }
            else { //Hvis der kun er en foretrukken
                if(!name.equals("")) {
                    list.add(name);
                    latList.add(latitude);
                    lngList.add(longitude);
                }
                else //Her dobbelttjekkes der, for at undgå at der sendes et tomt navn til brugeren med tomme koordinater
                    list.add("Du har ingen foretrukne");
            }
        }




        if (name==null){//Hvis der ikke er nogen foretrukne, skal brugeren ikke have mulighed for at kunne trykke på "Du har ingen foretrukne"
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            favoritesList.setAdapter(adapter);
        }
        else { //Her gives der mulighed for at kunne trykke på et ListItem
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            favoritesList.setAdapter(adapter);

            favoritesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    final String item = (String) parent.getItemAtPosition(position);
                    Double lat = Double.valueOf(latList.get(position));//Tager koordinater som passer med den rigtige station
                    Double lng = Double.valueOf(lngList.get(position));

                    Intent setScreen = new Intent(MainActivity.this, setScreen.class);
                    setScreen.putExtra("item", item); //Sender data videre til næste setScreen, om hvilken station der er valgt
                    setScreen.putExtra("lat", lat);
                    setScreen.putExtra("lng", lng);
                    startActivity(setScreen);
                }

            });
        }

    }




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Tilføjer items til actionbaren
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    // Søgefunktion, som sender brugeren videre til SearchResultsActivity
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Tjekker om der bliver trykket på en knap i actionbaren
		switch (item.getItemId()) {

            case R.id.action_search:
                break;

            case R.id.action_settings:
                // Hvis der trykkes på indstillinger, sender den brugeren videre til settingsScreen
                settingScreen();
                return true;

            default:
                break;

            }
		
		return true;
	}
	
	// Start indstillinger
	private void settingScreen() {
		Intent settingsScreenWindow = new Intent(this, settingsScreen.class);
		startActivity(settingsScreenWindow);
	}



	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}


}

