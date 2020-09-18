package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddSavingActivity extends AppCompatActivity {

    EditText savingsLabel, savingsAmount;
    Button savingsStart;
    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_saving);

        savingsLabel = findViewById(R.id.label);
        savingsAmount = findViewById(R.id.amount);
        savingsStart = findViewById(R.id.start);

        savingsStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String label = savingsLabel.getText().toString();
                String amount = savingsAmount.getText().toString();
                DatabaseHandler db = new DatabaseHandler(AddSavingActivity.this);
                user = db.getUserDetails();
                submitSavings(label, amount, user.get("uid"));
            }
        });
    }

    private void submitSavings(String label, String amount, String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.ADD_SAVINGS_URL, response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Intent intent = new Intent(AddSavingActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    startActivity(intent);
                    AddSavingActivity.this.finish();
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
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("savings", label);
                params.put("user", userID);
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
        Functions.showProgressDialog(AddSavingActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(AddSavingActivity.this);
    }
}