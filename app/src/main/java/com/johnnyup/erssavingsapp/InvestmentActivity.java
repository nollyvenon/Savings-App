package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import model.Savings;

public class InvestmentActivity extends AppCompatActivity {

    private SessionManager session;
    RecyclerView recyclerView;
    List<Savings> investmentModel = new ArrayList<>();

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);
        recyclerView = findViewById(R.id.investment_recycler_view);
        TextView startNewSaving = findViewById(R.id.start_new_saving);
        startNewSaving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InvestmentActivity.this, AddSavingActivity.class);
                startActivity(intent);
            }
        });

        // session manager
        session = new SessionManager(InvestmentActivity.this);

        DatabaseHandler db = new DatabaseHandler(InvestmentActivity.this);
        user = db.getUserDetails();
        fetchInvestment(user.get("uid"));
    }

    private void fetchInvestment(final String userID) {
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
                        investmentModel.add(savings);
                    }
                    SavingsAdapter adapter = new SavingsAdapter(InvestmentActivity.this, investmentModel);
                    GridLayoutManager manager = new GridLayoutManager(InvestmentActivity.this, 1);
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
        Functions.showProgressDialog(InvestmentActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(InvestmentActivity.this);
    }
}