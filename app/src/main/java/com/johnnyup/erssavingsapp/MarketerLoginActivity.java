package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;
import com.johnnyup.erssavingsapp.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MarketerLoginActivity extends AppCompatActivity {

    private static final String TAG = MarketerLoginActivity.class.getSimpleName();

    private static String KEY_UID = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_TYPE = "type";
    private static String KEY_TOKEN = "token";
    private static String KEY_CREATED_AT = "created_at";

    private MaterialButton btnLogin, btnLinkToRegister, btnForgotPass;
    private TextInputLayout inputEmail, inputPassword;

    private SessionManager session;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketer_login);

        inputEmail = findViewById(R.id.edit_email);
        inputPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.button_login);
        //btnLinkToRegister = findViewById(R.id.button_register);
        btnForgotPass = findViewById(R.id.button_reset);

        // create sqlite database
        db = new DatabaseHandler(this);

        // session manager
        session = new SessionManager(this);

        // check user is already logged in
        if (session.isLoggedIn()) {
            Intent i = new Intent(MarketerLoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }

        // Hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();
    }

    private void init() {
        // Login button Click Event
        btnLogin.setOnClickListener(view -> {
            // Hide Keyboard
            // Functions.hideSoftKeyboard(LoginActivity.this);

            String email = Objects.requireNonNull(inputEmail.getEditText()).getText().toString().trim();
            String password = Objects.requireNonNull(inputPassword.getEditText()).getText().toString().trim();

            // Check for empty data in the form
            if (!email.isEmpty() && !password.isEmpty()) {
                // erssavingsapp user
                loginProcess(email, password);
            } else {
                // Prompt user to enter credentials
                Toast.makeText(getApplicationContext(), "Please enter the credentials!", Toast.LENGTH_LONG).show();
            }
        });

        // Link to Register Screen
//        btnLinkToRegister.setOnClickListener(view -> {
//            Intent i = new Intent(MarketerLoginActivity.this, RegisterActivity.class);
//            startActivity(i);
//        });

        // Forgot Password Dialog
        //btnForgotPass.setOnClickListener(v -> forgotPasswordDialog());
    }

    private void forgotPasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.reset_password, null);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Forgot Password")
                .setCancelable(false)
                .setPositiveButton("Reset", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();

        TextInputLayout mEditEmail = dialogView.findViewById(R.id.edit_email);

        Objects.requireNonNull(mEditEmail.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEditEmail.getEditText().getText().length() > 0) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        alertDialog.setOnShowListener(dialog -> {
            final Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setEnabled(false);

            b.setOnClickListener(view -> {
                String email = mEditEmail.getEditText().getText().toString();

                if (!email.isEmpty()) {
                    if (Functions.isValidEmailAddress(email)) {
                        resetPassword(email);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Email is not valid!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Fill all values!", Toast.LENGTH_SHORT).show();
                }

            });
        });

        alertDialog.show();
    }

    private void loginProcess(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";
        showDialog("Logging in ...");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.LOGIN_MARKETER_URL, response -> {
            Log.d(TAG, "Login Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    // user successfully logged in
                    JSONObject json_user = jObj.getJSONObject("user");

                    Functions logout = new Functions();
                    logout.logoutUser(getApplicationContext());

                    if (Integer.parseInt(json_user.getString("verified")) == 1) {
                        db.addUser(json_user.getString(KEY_UID), json_user.getString(KEY_NAME),
                                json_user.getString(KEY_TYPE), json_user.getString(KEY_EMAIL),
                                json_user.getString(KEY_CREATED_AT), jObj.getString(KEY_TOKEN));

                        Intent upanel = new Intent(MarketerLoginActivity.this, HomeActivity.class);
                        upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(upanel);

                        session.setLogin(true);
                    } else {
                        Bundle b = new Bundle();
                        b.putString("email", email);

                        Intent upanel = new Intent(MarketerLoginActivity.this, EmailVerify.class);
                        upanel.putExtras(b);
                        upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(upanel);
                    }
                    finish();

                } else {
                    // Error in erssavingsapp. Get the error message
                    String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Log.e(TAG, "Login Error: " + error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to erssavingsapp url
                Map<String, String> params = new HashMap<>();
                params.put("username", email);
                params.put("password", password);

                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void resetPassword(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_reset_pass";

        showDialog("Please wait...");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.FORGOT_PASS_URL, response -> {
            Log.d(TAG, "Reset Password Response: " + response);
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);

                Toast.makeText(getApplicationContext(), jObj.getString("message"), Toast.LENGTH_LONG).show();

            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

        }, error -> {
            Log.e(TAG, "Reset Password Error: " + error.getMessage());
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to erssavingsapp url
                Map<String, String> params = new HashMap<>();
                params.put("tag", "forgot_pass");
                params.put("username", email);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };

        // Adding request to volley request queue
        strReq.setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
        strReq.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void goBack(View view) {
        finish();
    }

    private void showDialog(String title) {
        Functions.showProgressDialog(MarketerLoginActivity.this, title);
    }

    private void hideDialog() {
        Functions.hideProgressDialog(MarketerLoginActivity.this);
    }
}
