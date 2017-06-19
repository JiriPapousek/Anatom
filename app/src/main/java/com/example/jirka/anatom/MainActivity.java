package com.example.jirka.anatom;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity {

    Boolean returnValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        returnValue =  false;

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent,20);

        if (returnValue){
            intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (20) : {
                if (resultCode == RESULT_OK) {
                    data.getBooleanExtra("success",returnValue);
                    Log.i("Success",returnValue.toString());
                }
                break;
            }
        }
    }

}
