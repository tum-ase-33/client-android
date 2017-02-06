package de.tum.tumattendancechecker.stores;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.tumattendancechecker.models.EventListener;
import de.tum.tumattendancechecker.models.ListItem;

public abstract class AsyncListItemsStore<S extends ListItem> extends EventsStore {
    private String jwtToken;

    private List<S> itemList;

    private List<EventListener> listeners;
    private final FetchLessonsAsyncTask client;

    public AsyncListItemsStore(String jwtToken) {
        this.jwtToken = jwtToken;
        this.client = new FetchLessonsAsyncTask();
        this.listeners = new ArrayList<>();
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;
    }

    abstract Uri.Builder getApiURI();

    Map<String, String> getQuery() {
        Map<String, String> queryData = new HashMap<>();
        return queryData;
    }

    Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer ".concat(jwtToken));
        return headers;
    }

    public List<S> get() {
        if (itemList == null) {
            for (int i = 0; i < listeners.size(); i += 1) {
                listeners.get(i).onProcessing(null);
            }
            try {
                itemList = this.client.execute().get();
                for (int i = 0; i < listeners.size(); i += 1) {
                    listeners.get(i).onFinished(itemList);
                }
            } catch(Exception e) {
                for (int i = 0; i < listeners.size(); i += 1) {
                    listeners.get(i).onError(e);
                }
            }
        }
        return itemList;
    }

    public void attachListener(EventListener listener) {
        listeners.add(listener);
    }

    private class FetchLessonsAsyncTask extends AsyncTask<String, Void, List<S>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<S> doInBackground(String... urls) {
            try {
                Uri.Builder uri = getApiURI();
                Map<String, String> queryData = getQuery();
                for (Map.Entry<String, String> query : queryData.entrySet()) {
                    uri.appendQueryParameter(query.getKey(), query.getValue());
                }

                HttpGet httpGet = new HttpGet(uri.build().toString());
                Map<String, String> headers = getHeaders();
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpGet.addHeader(header.getKey(), header.getValue());
                }

                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httpGet);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String rawData = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(rawData);

                    List<S> list = new ArrayList<>();
                    JSONArray data = jsono.getJSONArray("data");
                    for (int i = 0; i < data.length(); i += 1) {
                        list.add(
                                (S) S.getInstance(
                                        data.getJSONObject(i).getLong("_id"),
                                        data.getJSONObject(i).getString("name")
                                )
                        );
                    }
                    return list;
                } else {
                    Log.d(this.getClass().getName(), "Invalid status code: " + status);
                    Log.d(this.getClass().getName(), response.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<S>();
        }

        protected void onPostExecute(List<S> result) {
        }
    }

}
