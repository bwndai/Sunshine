package com.example.debra.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    //adapter for the fragment storing all the weather forcast
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //updateWeather();
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            //String loc = SettingsActivity.class.get
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, new ArrayList<String>());

        ListView listview = (ListView) rootView.findViewById(R.id.listview_forecast);
        listview.setAdapter(mForecastAdapter);
        //String query = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(),mForecastAdapter.getItem(position) ,Toast.LENGTH_LONG).show();
                //explicit intent, open another activity to display the weather detail
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

    //fetching weather info
    private void updateWeather(){
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = preference.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        Log.v("Location Settings", location);
        new FetchWeatherTask(getActivity(), mForecastAdapter).execute(location);
    }

}