package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.EarningAdapter;
import adapter.RecentActivityAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Earning;
import model.RecentActivity;

import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_EARNING_URL;
import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_PROFILE_IMG_LINK;
import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_URL;

public class EarningActivity extends AppCompatActivity {

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earning);

        DatabaseHandler db = new DatabaseHandler(EarningActivity.this);
        user = db.getUserDetails();
        getEarnings(user.get("uid"));
    }

    public void getEarnings(String userID) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(MARKETER_EARNING_URL, getApplicationContext()), response -> {
            try {
                NumberFormat format = new DecimalFormat("#,###");
                JSONObject jObj = new JSONObject(response);

                JSONObject marketerEarning = jObj.getJSONObject("marketer_earning");
                JSONObject savingEarning = jObj.getJSONObject("saving_earning");
                JSONObject investmentEarning = jObj.getJSONObject("investment_earning");
                JSONArray savings = jObj.getJSONArray("savings");
                JSONArray investments = jObj.getJSONArray("investments");

                TextView marketerEarningTotal = findViewById(R.id.marketer_earning);
                marketerEarningTotal.setText("N"+marketerEarning.getString("earning"));

                TextView savingsEarningsTotal = findViewById(R.id.savings_earnings);
                savingsEarningsTotal.setText("N"+savingEarning.getString("sum"));

                TextView savingsEarningsBonus = findViewById(R.id.savings_bonus);
                savingsEarningsBonus.setText("N"+savingEarning.getString("bonus"));

                TextView investmentEarningTotal = findViewById(R.id.investment_earnings);
                investmentEarningTotal.setText("N"+investmentEarning.getString("sum"));

                TextView investmentEarningBonus = findViewById(R.id.investment_bonus);
                investmentEarningBonus.setText("N"+savingEarning.getString("bonus"));


                List<Earning> earnings = new ArrayList<>();

                if (savings.length() > 0) {
                    for (int i = 0; i < savings.length(); i++) {
                        Earning earning = new Earning();
                        JSONObject o = savings.getJSONObject(i);
                        earning.setCustomer(o.getString("username"));
                        earning.setSource(o.getString("savings"));
                        earning.setType("Savings");
                        earning.setAmount(o.getString("balance"));
                        earning.setEarning(o.getString("earning"));
                        earning.setBonus(o.getString("bonus"));
                        earnings.add(earning);
                    }
                }

                if (investments.length() > 0) {
                    for (int i = 0; i < investments.length(); i++) {
                        Earning earning = new Earning();
                        JSONObject o = investments.getJSONObject(i);
                        earning.setCustomer(o.getString("username"));
                        earning.setSource(o.getString("investment"));
                        earning.setType("Investment");
                        earning.setAmount(o.getString("amount"));
                        earning.setEarning(o.getString("earning"));
                        earning.setBonus(o.getString("bonus"));
                        earnings.add(earning);
                    }
                }

                EarningAdapter adapter = new EarningAdapter(EarningActivity.this, earnings);
                GridLayoutManager manager = new GridLayoutManager(EarningActivity.this, 1);
                RecyclerView recyclerView = findViewById(R.id.earnings_recycler_view);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(adapter);

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            //Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", userID);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "customer_req");
    }

    public void goBack(View view) {
        finish();
    }

}