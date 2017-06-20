package com.example.jirka.anatom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by jirka on 7.6.17.
 */

public class LoginActivity extends FragmentActivity {
    Button signInButton;
    EditText userNameEditText;
    EditText passwordEditText;
    SignInButton signInWithGoogleButton;
    LoginButton signInWithFacebookButton;

    private HTTPService service;
    int status;

    GoogleApiClient mGoogleApiClient;

    CallbackManager callbackManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = (Button) findViewById(R.id.signInButton);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        signInWithGoogleButton = (SignInButton) findViewById(R.id.signInWithGoogleButton);
        signInWithFacebookButton = (LoginButton) findViewById(R.id.signInWithFacebookButton);

        service = new HTTPService();

        /*---------------------------------------------*/
        /*  SIGN IN WITH GOOGLE */
        /*---------------------------------------------*/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                /*.requestServerAuthCode(getString(R.string.server_client_id))*/
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, 10);
            }
        });
        /*---------------------------------------------*/
        /*  SIGN IN WITH FACEBOOK */
        /*---------------------------------------------*/
        callbackManager = CallbackManager.Factory.create();
        signInWithFacebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String accessToken = loginResult.getAccessToken()
                        .getToken();
                Log.i("FBAccessToken", accessToken);
                new UserLoginTask(accessToken,"facebook").execute();
            }

            @Override
            public void onCancel() {
                Log.i("Facebook sign in result", "CANCEL");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("Facebook sign in result", "ERROR");
            }
        });
        /*---------------------------------------------*/
        /*  SIGN IN WITH PASSWORD */
        /*---------------------------------------------*/
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

        new UserLoginTask(username, password, this).execute();
    }


    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String username;
        private final String password;
        Context context;
        private final String token;
        String typeOfSignIn; /* values: "password", "google", or "facebook" */

        UserLoginTask(String username, String password, Context context) {
            this.username = username;
            this.password = password;
            this.context = context;
            this.typeOfSignIn = "password";
            this.token = "";
        }

        UserLoginTask(String token, String typeOfSignIn){
            this.token = token;
            this.typeOfSignIn = typeOfSignIn;
            this.username = "";
            this.password = "";
        }

        private Exception exception;

        @Override
        protected Integer doInBackground(Void... params) {
            status = 0;
            service.createSession();
            switch (typeOfSignIn) {
                case "password":
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
                    break;
                case "google":
                    Log.i("url",Constants.SERVER_NAME + "auth-by-token/google-oauth2/?access_token=" + token);
                    Log.i("Response", service.get(Constants.SERVER_NAME + "auth-by-token/google-oauth2/?access_token=" + token).toString());
                case "facebook":
                    Log.i("Response", service.get(Constants.SERVER_NAME + "auth-by-token/facebook/?access_token=" + token).toString());
                default: break;
            }
            return status;
        }
        @Override
        protected void onPostExecute(Integer status) {
            if (status == 200) {
                Intent intent = new Intent(context, MenuActivity.class);
                startActivity(intent);
            } else if (status == 401) {
                showSimpleDialog("Password or username does not match.","Please try again.");
            } else {
                showSimpleDialog("Something unexpected happened. (error " + status + ")","Please try again.");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInWithGoogleResult(result);
        }
    }

    private void handleSignInWithGoogleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.i("GoogleAccessToken", acct.getIdToken());
            new UserLoginTask(acct.getIdToken(),"google").execute();
        } else {
            showSimpleDialog("Something unexpected happened.", "Please try again.");
        }
    }

    /* Creates simple alert dialog, usually for displaying some error. */
    private void showSimpleDialog(String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /* Disables user to get back to previous activity. */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
