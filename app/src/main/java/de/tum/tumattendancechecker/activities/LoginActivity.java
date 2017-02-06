package de.tum.tumattendancechecker.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import de.tum.tumattendancechecker.R;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    private Button submitButton;
    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        submitButton = (Button) findViewById(R.id.email_sign_in_button);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // spinner.setVisibility(View.VISIBLE);
                try {
                    Log.d(getClass().getName(), getApiURI().build().toString());
                    StrictMode.ThreadPolicy policy = new
                            StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    new AuthenticationAsyncTask().execute(
                            getApiURI().build().toString(),
                            email.getText().toString(),
                            password.getText().toString()
                    ).get();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                }
                // spinner.setVisibility(View.GONE);
            }
        });
    }

    private void setJwtToken(final String token) {
        if (token != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("token", token);
            startActivity(intent);
        }
    }

    private Uri.Builder getApiURI() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("tum-attendance-checker.appspot.com")
                .appendPath("auth")
                .appendPath("local");
        return builder;
    }

    private class AuthenticationAsyncTask extends AsyncTask<String, Void, HttpResponse> {
        private HttpResponse response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HttpResponse doInBackground(String... params) {
            String token = null;
            try {
                //------------------>>
                HttpPost httppost = new HttpPost(params[0]);
                httppost.setHeader("Accept", "application/json");
                HttpClient httpclient = new DefaultHttpClient();

                List<NameValuePair> body = new ArrayList<NameValuePair>(2);
                body.add(new BasicNameValuePair("email", params[1]));
                body.add(new BasicNameValuePair("password", params[2]));
                httppost.setEntity(new UrlEncodedFormEntity(body, "UTF-8"));

                response = httpclient.execute(httppost);
                Log.d(getClass().getName(), "Dummy: ");
                Log.d(getClass().getName(), response.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(HttpResponse r) {
            Log.d(getClass().getName(), "Doing stuff");
            if (response == null) {
                return;
            }
            Log.d(getClass().getName(), "Doing stuff");
            try {
                // StatusLine stat = response.getStatusLine();
                int status = response.getStatusLine().getStatusCode();
                String token = null;

                if (status == 201) {
                    HttpEntity entity = response.getEntity();
                    String rawData = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(rawData);

                    token = jsono.getString("token");
                    Log.d(getClass().getName(), "Login successful");
                    showToast("Login successful");
                } else if (status == 401) {
                    HttpEntity entity = response.getEntity();
                    String rawData = EntityUtils.toString(entity);


                    JSONObject jsono = new JSONObject(rawData);

                    final String errorMessage = jsono.getString("message");
                    Log.d(getClass().getName(), "Error message: ".concat(errorMessage));

                    showToast("Invalid credentials");
                } else {
                    Log.d(this.getClass().getName(), "Invalid status code: " + status);
                    Log.d(this.getClass().getName(), response.toString());

                    showToast("Unknown error with status " + status);
                }

                setJwtToken(token);
            } catch (Exception e) {
                Log.d(getClass().getName(), e.toString());
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

