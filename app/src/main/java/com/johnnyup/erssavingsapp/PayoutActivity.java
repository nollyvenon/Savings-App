package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.InvestmentAdapter;
import adapter.PayoutAdapter;
import model.Investment;
import model.Payout;

public class PayoutActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Payout> payoutModel = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payout);
        recyclerView = findViewById(R.id.payout_recycler_view);

        DatabaseHandler db = new DatabaseHandler(PayoutActivity.this);
        HashMap<String, String> user = db.getUserDetails();
        fetchPayout(user.get("uid"));
    }

    private void fetchPayout(final String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.PAYOUT_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray res = jObj.getJSONArray("Payout");
                // Check for error node in json
                if (res.length() > 0) {
                    for (int i = 0; i < res.length(); i++) {
                        Payout payout = new Payout();
                        JSONObject o = res.getJSONObject(i);
                        payout.setAmount(o.getString("amount"));
                        payout.setDate_requested(o.getString("date_requested"));
                        payout.setInvestment(o.getString("investment"));
                        payout.setStatus(o.getString("status"));
                        payoutModel.add(payout);
                    }
                    PayoutAdapter adapter = new PayoutAdapter(PayoutActivity.this, payoutModel);
                    GridLayoutManager manager = new GridLayoutManager(PayoutActivity.this, 1);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);
                } else {
                    //String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), "You have never requested for payout", Toast.LENGTH_LONG).show();
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
        Functions.showProgressDialog(PayoutActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(PayoutActivity.this);
    }
}