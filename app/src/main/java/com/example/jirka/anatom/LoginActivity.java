package com.example.jirka.anatom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jirka on 7.6.17.
 */

public class LoginActivity extends FragmentActivity {
    Button signInButton;
    EditText userNameEditText;
    EditText passwordEditText;

    private HTTPService service;
    int status;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = (Button) findViewById(R.id.signInButton);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        service = new HTTPService();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    protected void login() {
        final String username = userNameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        new UserLoginTask(username,password).execute();

        //Waiting for response. Better to do somehow else.
        while (status==0){};

        if (status==200){
            finish();
        }
        else if(status==401){
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Password or username does not match.");
            alertDialog.setMessage("Please try again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else{
            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
            alertDialog.setTitle("Something unexpected happened. (error " + status + ")");
            alertDialog.setMessage("Please try again.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Void> {

        private final String username;
        private final String password;

        UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private Exception exception;

        @Override
        protected Void doInBackground(Void... params) {
            status = 0;
            service.createSession();
            try {
                JSONObject loginData = new JSONObject();
                try {
                    loginData.put("username", username);
                    loginData.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                status = service.post(Constants.SERVER_NAME + "user/login/", loginData.toString());
            } catch (Exception e) {
                this.exception = e;
            }
            return null;
        }
    }
}
