package com.example.matos.pso;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ImageButton b1, b2, b3, b4;
    private TelephonyManager telephonyManager;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private String IMEI;
    private boolean isFirstRun;
    private boolean isInfoSubmitted;
    private boolean isActivated;
    private boolean isActivatedConfirmed;
    DisplayDialog displayDialog;
    private AlertDialog ad;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        b1 = (ImageButton) findViewById(R.id.button1);
        b2 = (ImageButton) findViewById(R.id.button2);
        b3 = (ImageButton) findViewById(R.id.button3);
        b4 = (ImageButton) findViewById(R.id.button4);

        b1.setOnLongClickListener(new PSORequest("Police"));
        b2.setOnLongClickListener(new PSORequest("Ambulance"));
        b3.setOnLongClickListener(new PSORequest("Fire"));
        b4.setOnLongClickListener(new PSORequest("Rescue"));

        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = settings.edit();
        editor.putString("pref_ip", "psoquickresponse.herokuapp.com");
        editor.commit();
        isFirstRun = settings.getBoolean("firstrun", true);
        isInfoSubmitted = settings.getBoolean("info_submitted", true);
        isActivated = settings.getBoolean("activated", false);
        isActivatedConfirmed = settings.getBoolean("activated_confirmed", false);
        IMEI = telephonyManager.getDeviceId();

        if(isFirstRun) {
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            MainActivity.this.finish();
            return;
        }
        if (isInfoSubmitted) {
            if(!isActivated) {
                displayDialog = new DisplayDialog(MainActivity.this);
            }
        }

        checkStatus();

        builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_pincode, null))
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.setCancelable(false);
        ad = builder.create();

        if(!settings.getString("pin_code","").contentEquals("")) {
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText PINCode = (EditText) ad.findViewById(R.id.etPinCode);
                    if(PINCode.getText().toString().contentEquals(settings.getString("pin_code", ""))) {
                        ad.dismiss();
                    }
                }
            });
        }

    }

    private void checkStatus() {
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Checking Status...\n IMEI: " + telephonyManager.getDeviceId());
        final boolean[] activated = {false};
        String url = settings.getString("pref_ip", "192.168.1.110");
        url = "http://" + url + "/check/status/?imei=" + IMEI;
            Log.d("matz", url);
            JsonObjectRequest jsonRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                progressDialog.dismiss();
                                String status = response.getString("status");
                                if(status.contentEquals("activated")) {
                                    activated[0] = true;
                                    Log.d("matz", "activated");
                                    Log.d("matz", Boolean.toString(activated[0]));
                                    if(!isActivatedConfirmed) {

                                        new DialogActivated();
                                    }
                                    editor.putBoolean("activated", true);
                                    editor.commit();
                                }else {



                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });


            Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
            progressDialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ButtonListener implements View.OnClickListener {
        String message;

        ButtonListener(String message) {
            this.message = message + " was clicked!";
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private class PSORequest implements View.OnLongClickListener {
        private ProgressDialog progressDialog;
        private String requestType;

        public PSORequest(String type) {
            requestType = type;
        }

        @Override
        public boolean onLongClick(View v) {
            String url = settings.getString("pref_ip", "");
            url = "http://" + url + "/request/police/?";
            String params[] = new String[0];
            Log.d("matz", url);

            GPSTracker gpsTracker = new GPSTracker(MainActivity.this);

            // check if GPS enabled
            if(gpsTracker.canGetLocation()){

                Log.d("matz", gpsTracker.getLatitude() + " " + gpsTracker.getLongitude());
                params = new String[]{
                        "lat=" + gpsTracker.getLatitude(),
                        "lng=" + gpsTracker.getLongitude(),
                        "imei=" + telephonyManager.getDeviceId(),
                        "type=" + this.requestType
                };
                url = url + StringUtils.join(params, "&");
                Log.d("matz", url);
                JsonObjectRequest jsonRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // the response is already constructed as a JSONObject!
                                try {
                                    progressDialog.dismiss();
    //                                response = response.getJSONObject("args");
                                    String site = response.getString("response");
                                    Toast.makeText(MainActivity.this, site, Toast.LENGTH_SHORT).show();
                                    int report_id = response.getInt("report_id");
                                    Log.d("Report ID", Integer.toString(report_id));
                                    editor.putInt("report_id", report_id);
                                    editor.commit();
                                    new Notification().run();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });


                Volley.newRequestQueue(getApplicationContext()).add(jsonRequest);
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Requesting " + requestType + "!\n IMEI: " + telephonyManager.getDeviceId());
                progressDialog.show();

            }else{
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gpsTracker.showSettingsAlert();
            }
            return true;
        }
    }

    private class DialogActivated {
        public DialogActivated() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_activated, null))
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            editor.putBoolean("activated_confirmed", true);
                            editor.commit();
                            MainActivity.this.finish();
                            startActivity(new Intent(MainActivity.this, MainActivity.class));
                        }
                    });
            builder.setCancelable(false);
            builder.create();
            builder.show();
        }
    }

    class Notification {
        boolean statusClosed;

        Handler handler = new Handler();
        public Notification() {

        }

        public void run() {
            update();
        }

        public void update() {
            handler.postDelayed(GetStatus, 10000);
        }

        private Runnable GetStatus = new Runnable() {
            @Override
            public void run() {
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "http://" + settings.getString("pref_ip", "192.168.1.110");
                url += "/notify?report_id=" + settings.getInt("report_id", 0);
                Log.d("matz", url);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("matz", response);
                                if(response.contentEquals("ok")) {
                                    statusClosed = true;
                                }

                                if(statusClosed) {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Alert")
                                            .setMessage("Help is on the way please wait a moment!")
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // continue with delete
                                                }
                                            })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // do nothing
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                    statusClosed = false;
                                    handler.removeCallbacks(GetStatus);
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "fail", Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest);
//                sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
                update();
            }

        };

    }
}
