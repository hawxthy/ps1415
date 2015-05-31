package ws1415.veranstalterapp.task;

import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ws1415.common.task.ExtendedTask;
import ws1415.common.task.ExtendedTaskDelegate;
import ws1415.common.util.LocationUtils;
import ws1415.veranstalterapp.activity.RouteEditorActivity;

/**
 * Task zum erstellen einer Route mit den im ArrayAdapter enthaltenen Waypoints als Wegpunkten.
 * Die Route wird mithilfe der Google Directions API
 * (https://developers.google.com/maps/documentation/directions/) erstellt. Es müssen mindestens
 * 2 Wegpunkte übergeben werden. Aufgrund der Einschränkungen der API dürfen neben dem Start-
 * und Endpunkt maximal 8 Wegpunkte übergeben werden. Die Anweisungen werden als JSON String
 * runtergeladen und anschließend in ein Route Objekt eingefügt.
 */
public class RouteLoaderTask extends ExtendedTask<ArrayAdapter<RouteEditorActivity.Waypoint>, Void, RouteEditorActivity.Route> {
    private static final String LOG_TAG = RouteLoaderTask.class.getSimpleName();

    public RouteLoaderTask(ExtendedTaskDelegate delegate) {
        super(delegate);
    }

    @Override
    protected RouteEditorActivity.Route doInBackground(ArrayAdapter<RouteEditorActivity.Waypoint>... params) {
        if (params.length != 1) return null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        final ArrayAdapter<RouteEditorActivity.Waypoint> waypoints = params[0];

        if (waypoints.getCount() < 2) return null;

        final String BASE_URL = "http://maps.googleapis.com/maps/api/directions/json?";

        final String ORIGIN = positionToString(waypoints.getItem(0).getMarkerOptions().getPosition());
        final String DESTINATION = positionToString(waypoints.getItem(waypoints.getCount() - 1).getMarkerOptions().getPosition());
        final String WAYPOINTS = waypointsToString(waypoints);

        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon();
        builder.appendQueryParameter("origin", ORIGIN);
        builder.appendQueryParameter("destination", DESTINATION);
        builder.appendQueryParameter("waypoints", WAYPOINTS);
        builder.appendQueryParameter("sensor", "true");

        String jsonString = null;

        try {
            URL url = new URL(builder.build().toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() <= 0) return null;
            jsonString = buffer.toString();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Error downloading directions.", e);
            jsonString = null;
            publishError(e.getMessage());
        }
        finally {
            if (urlConnection != null) urlConnection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Error closing stream.", e);
                }
            }
        }

        if (jsonString != null) {
            try {
                return parseJSONString(jsonString);
            }
            catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse JSON string.", e);
                publishError("Unable to parse JSON string.");
                return null;
            }
        }
        return null;
    }

    private String positionToString(LatLng position) {
        return position.latitude + "," + position.longitude;
    }

    private String waypointsToString(ArrayAdapter<RouteEditorActivity.Waypoint> waypoints) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < waypoints.getCount() - 1; i++) {
            builder.append(positionToString(waypoints.getItem(i).getMarkerOptions().getPosition()));
            builder.append("|");
        }
        return builder.toString();
    }

    private RouteEditorActivity.Route parseJSONString(String jsonString) throws JSONException {
        JSONObject directionsJSON = new JSONObject(jsonString);
        JSONArray routesArray = directionsJSON.getJSONArray("routes");
        if (routesArray.length() <= 0) {
            return null;
        }

        JSONObject route = routesArray.getJSONObject(0);
        JSONArray legs = route.getJSONArray("legs");

        List<LatLng> line = new ArrayList<LatLng>();
        int distance = 0;

        for (int i = 0; i < legs.length(); i++) {
            JSONObject leg = legs.getJSONObject(i);
            JSONArray steps = leg.getJSONArray("steps");
            for (int j = 0; j < steps.length(); j++) {
                JSONObject step = steps.getJSONObject(j);
                distance += step.getJSONObject("distance").getInt("value");
                try {
                    line.addAll(LocationUtils.decodePolyline(step.getJSONObject("polyline").getString("points")));
                }
                catch (ParseException e) {
                    Log.e(LOG_TAG, "Unable to parse polyline.", e);
                    publishError("Unable to parse polyline.");
                    return null;
                }
            }
        }

        RouteEditorActivity.Route out = null;
        try {
            out = new RouteEditorActivity.Route(line, distance);
        }
        catch (ParseException e) {
            Log.e(LOG_TAG, "Unable to parse polyline.", e);
            publishError("Unable to parse polyline.");
            return null;
        }
        return out;
    }
}
