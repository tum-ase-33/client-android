package de.tum.tumattendancechecker.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.tum.tumattendancechecker.R;
import de.tum.tumattendancechecker.models.EventListener;
import de.tum.tumattendancechecker.stores.AuthenticationStore;

import static android.R.attr.width;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class MainActivity extends Activity {
    private AuthenticationStore authenticationStore;

    private void listenToStores() {
        authenticationStore.attachListener(new EventListener() {
            @Override
            public void onProcessing(Object data) {

            }

            @Override
            public void onFinished(Object result) {
                if (result == null) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String token = intent.getStringExtra("token");
        Log.d(getClass().getName(), "My token is: " + token);

        this.authenticationStore = new AuthenticationStore(token);
        listenToStores();

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            new FetchLessonGroupAccessToken().execute().get();
        } catch(Exception e) {}
    }

    private Uri.Builder getApiURI() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("tum-attendance-checker.appspot.com");
        return builder;
    }

    private class FetchLessonGroupAccessToken extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            String token = "";
            try {
                Uri.Builder uri = getApiURI();
                uri.appendPath("user-lesson-tokens");

                HttpGet httpGet = new HttpGet(uri.build().toString());
                httpGet.addHeader("Authorization", "Bearer ".concat(authenticationStore.getJwtToken()));
                Log.d(getClass().getName(), "Bearer ".concat(authenticationStore.getJwtToken()));

                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httpGet);

                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();
                Log.d(getClass().getName(), "My status code is: " + status);

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String rawData = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(rawData);

                    JSONArray data = jsono.getJSONArray("data");
                    if (data.length() > 0) {
                        Log.d(getClass().getName(), "Tokens available");
                        token = data.getJSONObject(0).getString("token");
                    } else {
                        Log.d(getClass().getName(), "No tokens available");
                    }
                } else {
                    Log.d(this.getClass().getName(), "Invalid status code: " + status);
                    Log.d(this.getClass().getName(), response.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return token;
        }

        protected void onPostExecute(String token) {
            setImage(token);
        }
    }

    private void setImage(String token) {
        try {
            Bitmap bm = encodeAsBitmap(token);

            if (bm != null) {
                ImageView image = (ImageView) findViewById(R.id.image);
                image.setImageBitmap(bm);
            }
        } catch (WriterException e) {
        }
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {
        BitMatrix result;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, size.x, size.y, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
        return bitmap;
    }
}