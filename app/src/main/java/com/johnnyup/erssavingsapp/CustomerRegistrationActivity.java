package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.HashMap;
import java.util.Map;

public class CustomerRegistrationActivity extends AppCompatActivity {

    TextView firstName, lastName, middleName, phone, address, email, username, bank, accountName, accountNumber;
    TextView password, kfirstName, klastName, kphone, kaddress;

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        middleName = findViewById(R.id.middle_name);
        username = findViewById(R.id.username);
        phone = findViewById(R.id.phone_number);
        address = findViewById(R.id.address);
        email = findViewById(R.id.email);
        bank = findViewById(R.id.bank);
        accountName = findViewById(R.id.account_name);
        accountNumber = findViewById(R.id.account_number);
        password = findViewById(R.id.password);

        kfirstName = findViewById(R.id.k_first_name);
        klastName = findViewById(R.id.k_last_name);
        kphone = findViewById(R.id.k_phone_number);
        kaddress = findViewById(R.id.k_address);

        DatabaseHandler db = new DatabaseHandler(CustomerRegistrationActivity.this);
        user = db.getUserDetails();

        findViewById(R.id.add_customer).setOnClickListener(v -> {

            String sfirstName = firstName.getText().toString();
            String slastName = lastName.getText().toString();
            String smiddleName = middleName.getText().toString();
            String susername = username.getText().toString();
            String sphone = phone.getText().toString();
            String saddress = address.getText().toString();
            String semail = email.getText().toString();
            String sbank = bank.getText().toString();
            String saccountName = accountName.getText().toString();
            String saccountNumber = accountNumber.getText().toString();
            String spassword = password.getText().toString();
            String skfirstName = kfirstName.getText().toString();
            String sklastName = klastName.getText().toString();
            String skphone = kphone.getText().toString();
            String skaddress = kaddress.getText().toString();
            addCustomer(sfirstName, slastName, smiddleName, susername, sphone, saddress, semail, sbank,
                    saccountName, saccountNumber, spassword, skfirstName, sklastName, skphone, skaddress);
        });
    }

    private void addCustomer(String sfirstName, String slastName, String smiddleName, String susername, String sphone, String saddress,
                               String semail, String sbank, String saccountName, String saccountNumber, String spassword,
                                String skfirstName, String sklastName, String skphone, String skaddress) {

        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.USER_ADD_CUSTOMER_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(getApplicationContext(), "Customer Added Successfully!", Toast.LENGTH_LONG).show();
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
                params.put("username", susername);
                params.put("phone", sphone);
                params.put("email", semail);
                params.put("bank", sbank);
                params.put("acc_name", saccountName);
                params.put("acc_number", saccountNumber);
                params.put("password", spassword);
                params.put("address", saddress);
                params.put("nfname", skfirstName);
                params.put("nlname", sklastName);
                params.put("nemail", semail);
                params.put("nphone", skphone);
                params.put("naddress", skaddress);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "customer_reg_req");

    }

    public void goBack(View view) {
        finish();
    }

    private void showDialog() {
        Functions.showProgressDialog(CustomerRegistrationActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(CustomerRegistrationActivity.this);
    }
}