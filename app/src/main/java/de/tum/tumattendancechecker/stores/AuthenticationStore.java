package de.tum.tumattendancechecker.stores;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationStore extends EventsStore {
    private String jwtToken;

    private final AuthenticationAsyncTask client;

    public AuthenticationStore(String jwtToken) {
        this.jwtToken = jwtToken;
        this.client = new AuthenticationAsyncTask();
    }

    public void setJwtToken(String token) {
        this.jwtToken = token;

        notifySuccess(token);
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public AsyncTask login(final String email, final String password) {
        return this.client.execute("http://google.de", email, password);
    }

    public void logout() {
        this.setJwtToken(null);
    }

    private class AuthenticationAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            notifyProcessing();
        }

        @Override
        protected String doInBackground(String... params) {
            String token = null;

            try {
                //------------------>>
                HttpPost httppost = new HttpPost(params[0]);
                HttpClient httpclient = new DefaultHttpClient();

                List<NameValuePair> body = new ArrayList<NameValuePair>(2);
                body.add(new BasicNameValuePair("email", params[1]));
                body.add(new BasicNameValuePair("password", params[2]));
                httppost.setEntity(new UrlEncodedFormEntity(body, "UTF-8"));

                HttpResponse response = httpclient.execute(httppost);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String rawData = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(rawData);

                    token = jsono.getString("token");
                    notifySuccess(token);
                } else if (status == 400) {
                    HttpEntity entity = response.getEntity();
                    String rawData = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(rawData);

                    final String errorMessage = jsono.getString("message");

                    notifyError(new IllegalAccessException(errorMessage));
                } else {
                    Log.d(this.getClass().getName(), "Invalid status code: " + status);
                    Log.d(this.getClass().getName(), response.toString());

                    notifyError(new Exception(response.toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return token;
        }

        protected void onPostExecute(String token) {
            setJwtToken(token);
        }
    }
}
