package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.HashMap;
import java.util.Map;

import adapter.InvestmentAdapter;
import model.Investment;

public class ProfileActivity extends AppCompatActivity {

    EditText firstName, lastName, middleName, phone, address, email;
    EditText kfirstName, klastName, kphone, kaddress, kemail;

    private HashMap<String, String> user = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        middleName = findViewById(R.id.middle_name);
        phone = findViewById(R.id.phone_number);
        address = findViewById(R.id.address);
        email = findViewById(R.id.email);

        kfirstName = findViewById(R.id.k_first_name);
        klastName = findViewById(R.id.k_last_name);
        kphone = findViewById(R.id.k_phone_number);
        kaddress = findViewById(R.id.k_address);
        kemail = findViewById(R.id.k_email);

        DatabaseHandler db = new DatabaseHandler(ProfileActivity.this);
        user = db.getUserDetails();

        getProfile(user.get("uid"));

        findViewById(R.id.update_profile).setOnClickListener(v -> {

            String sfirstName = firstName.getText().toString();
            String slastName = lastName.getText().toString();
            String smiddleName = middleName.getText().toString();
            String sphone = phone.getText().toString();
            String saddress = address.getText().toString();
            String semail = email.getText().toString();
            String skfirstName = kfirstName.getText().toString();
            String sklastName = klastName.getText().toString();
            String skphone = kphone.getText().toString();
            String skaddress = kaddress.getText().toString();
            String skemail = kemail.getText().toString();
            updateProfile(sfirstName, slastName, smiddleName, sphone, saddress, semail, skfirstName, sklastName,
                    skphone, skaddress, skemail);
        });
    }

    private void getProfile(String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.USER_PROFILE_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONObject res = jObj.getJSONObject("user");

                firstName.setText(res.getString("fname"));
                lastName.setText(res.getString("lname"));
                middleName.setText(res.getString("middlename"));
                email.setText(res.getString("email"));
                phone.setText(res.getString("phone"));
                address.setText(res.getString("address"));
                kfirstName.setText(res.getString("nfname"));
                klastName.setText(res.getString("fname"));
                kphone.setText(res.getString("nphone"));
                kemail.setText(res.getString("nemail"));
                kaddress.setText(res.getString("naddress"));

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to erssavingsapp url
                Map<String, String> params = new HashMap<>();
                params.put("user", userID);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "profile_req");

    }

    private void updateProfile(String sfirstName, String slastName, String smiddleName, String sphone, String saddress,
                               String semail, String skfirstName, String sklastName, String skphone, String skaddress, String skemail) {

        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.USER_UPDATE_PROFILE_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(getApplicationContext(), "Profile Updated!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to erssavingsapp url
                Map<String, String> params = new HashMap<>();
                params.put("user", user.get("uid"));
                params.put("fname", sfirstName);
                params.put("middlename", smiddleName);
                params.put("lname", slastName);
                params.put("phone", sphone);
                params.put("address", saddress);
                params.put("nfname", skfirstName);
                params.put("nlname", sklastName);
                params.put("nemail", semail);
                params.put("nphone", skphone);
                params.put("naddress", skaddress);
                params.put("email", skemail);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "investment_req");

    }

    public void goBack(View view) {
        finish();
    }

    private void showDialog() {
        Functions.showProgressDialog(ProfileActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(ProfileActivity.this);
    }
}