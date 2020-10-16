package com.johnnyup.erssavingsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private MaterialButton btnRegister, btnLinkToLogin;
    private TextView firstName, lastName, email, password, phoneNumber, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);
        phoneNumber = findViewById(R.id.phone_number);
        //btnRegister = findViewById(R.id.button_register);
        btnLinkToLogin = findViewById(R.id.button_login);

        // Hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();
    }

    private void init() {
        // Login button Click Event
        btnRegister.setOnClickListener(view -> {
            // Hide Keyboard
            Functions.hideSoftKeyboard(RegisterActivity.this);

            String firstNameTxt = firstName.getText().toString().trim();
            String lastNameTxt = lastName.getText().toString().trim();
            String emailTxt = email.getText().toString().trim();
            String passwordTxt = password.getText().toString().trim();
            String usernameTxt = username.getText().toString().trim();
            String phoneNumberTxt = phoneNumber.getText().toString().trim();

            // Check for empty data in the form
            if (!firstNameTxt.isEmpty() && !lastNameTxt.isEmpty() && !emailTxt.isEmpty() &&
                    !passwordTxt.isEmpty() && !usernameTxt.isEmpty() && !phoneNumberTxt.isEmpty()) {
                if (Functions.isValidEmailAddress(emailTxt)) {
                    registerUser(firstNameTxt, lastNameTxt, emailTxt, passwordTxt, usernameTxt, phoneNumberTxt);
                } else {
                    Toast.makeText(getApplicationContext(), "Email is not valid!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Please enter your details!", Toast.LENGTH_LONG).show();
            }
        });

        // Link to Register Screen
        btnLinkToLogin.setOnClickListener(view -> {
            Intent i = new Intent(RegisterActivity.this, MarketerLoginActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void registerUser(String firstNameTxt, String lastNameTxt, String emailTxt, String passwordTxt,
                              String usernameTxt, String phoneNumberTxt) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.REGISTER_URL, response -> {
                    Log.d(TAG, "Register Response: " + response);
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");
                        if (!error) {
                            Functions logout = new Functions();
                            logout.logoutUser(getApplicationContext());

                            Bundle b = new Bundle();
                            b.putString("email", emailTxt);
                            Intent i = new Intent(RegisterActivity.this, EmailVerify.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            i.putExtras(b);
                            startActivity(i);
                            finish();

                        } else {
                            // Error occurred in registration. Get the error
                            // message
                            String errorMsg = jObj.getString("message");
                            Toast.makeText(getApplicationContext(),errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> {
                    Log.e(TAG, "Registration Error: " + error.getMessage(), error);
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("fname", firstNameTxt);
                params.put("lname", lastNameTxt);
                params.put("em", emailTxt);
                params.put("password", passwordTxt);
                params.put("username", usernameTxt);
                params.put("phone", phoneNumberTxt);

                return params;
            }

        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void goBack(View view) {
        finish();
    }

    private void showDialog() {
        Functions.showProgressDialog(RegisterActivity.this, "Registering ...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(RegisterActivity.this);
    }
}
