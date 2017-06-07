package com.example.jirka.anatom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends FragmentActivity {

    Button signInButton;
    EditText userNameEditText;
    EditText passwordEditText;
    private HTTPService service = new HTTPService();
    int status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = (Button) findViewById(R.id.signInButton);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }

    protected boolean login() {
        final String username = userNameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        new UserLoginTask(username,password).execute();

        while (status==0){}

        if (status==200){
            userNameEditText.setText("Success");
        }
        else{
            userNameEditText.setText("Failure "+status);
        }

        return false;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;

        UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private Exception exception;

        @Override
        protected Boolean doInBackground(Void... params) {
            status = 0;
            try {
                JSONObject loginData = new JSONObject();
                System.out.println(password+" "+username);
                try {
                    loginData.put("username", username);
                    loginData.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(loginData.toString());
                status = service.post(Constants.SERVER_NAME + "user/login/", loginData.toString());
            } catch (Exception e) {
                this.exception = e;
            }
            System.out.println(status);
            if (status == 200) return true;
            else return false;
        }
    }
}
