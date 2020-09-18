package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;
import com.johnnyup.erssavingsapp.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.SavingsAdapter;

public class SavingsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<model.Savings> savingsModel = new ArrayList<>();

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);

        recyclerView = findViewById(R.id.savings_recycler_view);
        TextView startNewSaving = findViewById(R.id.start_new_saving);
        startNewSaving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SavingsActivity.this, AddSavingActivity.class);
                startActivity(intent);
            }
        });

        DatabaseHandler db = new DatabaseHandler(SavingsActivity.this);
        user = db.getUserDetails();
        fetchSavings(user.get("uid"));

        // Hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void fetchSavings(final String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.SAVINGS_URL, response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray res = jObj.getJSONArray("savings");

                // Check for error node in json
                if (res.length() > 0) {
                    model.Savings savings = new model.Savings();
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject o = res.getJSONObject(i);
                        savings.setAmount(o.getString("amount"));
                        savings.setDate_end(o.getString("date_end"));
                        savings.setDate_start(o.getString("date_start"));
                        savings.setStatus(o.getString("status"));
                        savings.setSavings(o.getString("savings"));
                        savings.setBalance(o.getString("balance"));
                        savingsModel.add(savings);
                    }
                    SavingsAdapter adapter = new SavingsAdapter(SavingsActivity.this, savingsModel);
                    GridLayoutManager manager = new GridLayoutManager(SavingsActivity.this, 1);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);
                } else {
                    String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), "You do not have any savings", Toast.LENGTH_LONG).show();
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
        Functions.showProgressDialog(SavingsActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(SavingsActivity.this);
    }
}