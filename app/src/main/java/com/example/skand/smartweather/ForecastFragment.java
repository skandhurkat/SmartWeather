package com.example.skand.smartweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment containing the weather forecast list view
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    /**
     * Called to do initial creation of a fragment.  This is called after
     * {@link #onAttach(Activity)} and before
     * {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}.
     * <p/>
     * <p>Note that this can be called while the fragment's activity is
     * still in the process of being created.  As such, you can not rely
     * on things like the activity's content view hierarchy being initialized
     * at this point.  If you want to do work once the activity itself is
     * created, see {@link #onActivityCreated(android.os.Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        List<String> weekForecast = new ArrayList<String>(
                Arrays.asList(forecastArray)
        );

        ListView mListView = (ListView) rootView.findViewById(
                R.id.list_view_forecast
        );

        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast
        );

        mListView.setAdapter(mArrayAdapter);

        return rootView;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * For now, we will only handle the refresh menu item.
     * Eventually, the refresh menu item will be removed,
     * and so will this code fragment
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // For the time being, we will add a handler for refresh
        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

/**
 * This class performs an asynchronous fetch from the OpenWeatherMap
 * API as we cannot have network tasks on the main thread.
 */
class FetchWeatherTask extends AsyncTask<Void, Void, Void>
{

    /**
     * Used for logging
     */
    private String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Void doInBackground(Void... params) {

        // This needs to be declared outside try-catch in order to
        // catch exceptions
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;

        /**
         * Will contain forecast JSON string
         */
        String forecastJSONString = null;

        try {
            URL url = new URL("http://api.openweathermap.org/data/2" +
                    ".5/forecast/daily?q=94043&units=metric&cnt=7");
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            // Read the input from the API request here
            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuffer stringBuffer = new StringBuffer();

            if(inputStream == null) {
                // Nothing to do here, move along
                return null;
            }
            bufferedReader = new BufferedReader(new InputStreamReader
                    (inputStream));

            // Temporary variable to concatenate results,
            // and add newlines for pretty printing. Note that while
            // this is not required, it makes debugging so much easier.
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuffer.append(line + "\n");
            }

            if(stringBuffer.length() == 0) {
                // Did not receive any data. Move along.
                return null;
            }

            // If data was received, copy it to forecastJSONString.
            forecastJSONString = stringBuffer.toString();
            Log.d(LOG_TAG, forecastJSONString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL Exception");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception");
            e.printStackTrace();
            return null;
        } finally {
            if(httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "IO Exception when closing " +
                            "bufferedReader");
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }
}