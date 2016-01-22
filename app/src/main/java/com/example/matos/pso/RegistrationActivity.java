package com.example.matos.pso;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationActivity extends AppCompatActivity {
    private Button btnSubmit;
    private ProgressDialog progressDialog;
    private TelephonyManager telephonyManager;
    private EditText etFirstname;
    private EditText etMiddlename;
    private EditText etLastname;
    private EditText etAddress;
    private EditText etEmail;
    private String IMEI;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private EditText etContactNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        etFirstname = (EditText) findViewById(R.id.etFirstname);
        etMiddlename = (EditText) findViewById(R.id.etMiddlename);
        etLastname = (EditText) findViewById(R.id.etLastname);
        etFirstname = (EditText) findViewById(R.id.etFirstname);
        etAddress = (EditText) findViewById(R.id.etAddress);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etContactNo = (EditText) findViewById(R.id.etContactNo);
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);;
        IMEI = telephonyManager.getDeviceId();

        settings = PreferenceManager.getDefaultSharedPreferences(RegistrationActivity.this);
        editor = settings.edit();



        btnSubmit = (Button) findViewById(R.id.btnRegistrationSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String url = settings.getString("pref_ip", "192.168.1.110");
                url = "http://" + url + "/record/user/?";

                String[] params = new String[]{
                        "firstname=" + Uri.encode(etFirstname.getText().toString()),
                        "middlename=" + Uri.encode(etMiddlename.getText().toString()),
                        "lastname=" + Uri.encode(etLastname.getText().toString()),
                        "address=" + Uri.encode(etAddress.getText().toString()),
                        "email=" + Uri.encode(etEmail.getText().toString()),
                        "contact_no=" + Uri.encode(etContactNo.getText().toString()),
                        "imei=" + IMEI,
                        "status=deactivated"
                };

                url+= StringUtils.join(params, "&");
                Log.d("matz", IMEI);
                Log.d("matz", url);

                JsonObjectRequest jsonRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // the response is already constructed as a JSONObject!
                                try {
                                    progressDialog.dismiss();
                                    //                                response = response.getJSONObject("args");
                                    Log.d("matz", response.getString("response"));
                                    if(response.getString("response").contentEquals("success")) {
                                        new DisplayDialog(RegistrationActivity.this);
                                        Log.d("matz", "successs!");
                                    }
//                                    Toast.makeText(RegistrationActivity.this, site, Toast.LENGTH_SHORT).show();
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
                progressDialog = new ProgressDialog(RegistrationActivity.this);
                progressDialog.setMessage("Sending");
                progressDialog.show();


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
