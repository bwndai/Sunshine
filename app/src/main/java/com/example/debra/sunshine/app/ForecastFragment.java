package com.example.debra.sunshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 70/46",
                "Weds - Cloudy - 72/63",
                "Thurs - Rainy - 64/51",
                "Fri - Foggy - 70/46",
                "Sat - Sunny - 76/60"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, weekForecast);
        ListView listview = (ListView) rootView.findViewById(R.id.listview_forecast);
        listview.setAdapter(mForecastAdapter);
        //String query = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";

        return rootView;
    }
        public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
            private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
            private HttpURLConnection urlConnection = null;
            private BufferedReader reader = null;

            private String forecastJsonStr = null;

            private String getReadableDateString(long time){
                SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
                return shortenedDateFormat.format(time);
            }

            @Override
            protected void onPostExecute(String[] strings) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(new ArrayList<String> (Arrays.asList(strings)) );
                super.onPostExecute(strings);
            }

            private String formatHighLows(double high, double low){
                long roundedHigh = Math.round(high);
                long roundedLow = Math.round(low);
                String highLowStr = roundedHigh + "/" + roundedLow;
                return highLowStr;
            }
            private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException{

                final String OWM_LIST = "list";
                final String OWM_WEATHER = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                Time dayTime = new Time();
                dayTime.setToNow();
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
                dayTime = new Time();
                String[] resultStrs = new String[numDays];
                for (int i = 0; i < weatherArray.length(); i++){
                    String day;
                    String description;
                    String highAndLow;
                    JSONObject dayForecast = weatherArray.getJSONObject(i);
                    long dateTime;
                    dateTime = dayTime.setJulianDay(julianStartDay + i);
                    day = getReadableDateString(dateTime);

                    JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high = temperatureObject.getDouble(OWM_MAX);
                    double low = temperatureObject.getDouble(OWM_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;
                }
                for (String s: resultStrs)
                    Log.v(LOG_TAG, "Forecast entry: " + s);

                return resultStrs;
            }

            protected String[] doInBackground(String... urls) {
                String format = "json";
                String units = "metric";
                int numDays = 7;
                try{
                    final String FORECAST_BASE_URL =
                            "http://api.openweathermap.org/data/2.5/forecast/daily?";
                    final String QUERY_PARAM = "q";
                    final String FORMAT_PARAM = "mode";
                    final String UNITS_PARAM = "units";
                    final String DAYS_PARAM = "cnt";
                    Uri uri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                            .appendQueryParameter(QUERY_PARAM, urls[0])
                            .appendQueryParameter(FORMAT_PARAM, format)
                            .appendQueryParameter(UNITS_PARAM, units)
                            .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                            .build();

                    URL url = new URL(uri.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null)
                        //return null;
                        forecastJsonStr = null;

                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");

                    }
                    if (buffer.length() == 0)
                        //return null;
                        forecastJsonStr = null;
                    forecastJsonStr = buffer.toString();
                    Log.v(LOG_TAG, "Forecast JSON string:" + forecastJsonStr);
                } catch (IOException e){
                    Log.e("PlaceholderFragment", "Error", e);
                    return null;
                    //forecastJsonStr = null;
                } finally{
                    if (urlConnection != null)
                        urlConnection.disconnect();
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }
                //return null;
                try {
                    return getWeatherDataFromJson(forecastJsonStr, numDays);
                }catch(JSONException e){
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                return null;
            }
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
            new FetchWeatherTask().execute("32792");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}