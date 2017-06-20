package com.example.jirka.anatom;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MenuActivity extends FragmentActivity {

    private HTTPService service;
    LinearLayout buttonPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        service = new HTTPService();
        new GetDataTask().execute();
    }

    public class GetDataTask extends AsyncTask<Object, Object, JSONObject> {

        @Override
        protected JSONObject doInBackground(Object... params) {
            service.createSession();
            return service.get(Constants.SERVER_NAME+"/flashcards/categorys?debug=true&all=True&db_orderby=identifier");
        }
        @Override
        protected void onPostExecute(JSONObject data) {
            Log.i("Data", data.toString());
            buttonPanel = (LinearLayout)findViewById(R.id.buttonPanel);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int)buttonPanel.getWidth()/2,(int)buttonPanel.getWidth()/2);
        }
    }
    /* Disables user to get back to previous activity. */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}

