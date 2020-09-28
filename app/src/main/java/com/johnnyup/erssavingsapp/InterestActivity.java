package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.InterestAdapter;
import adapter.InvestmentAdapter;
import model.Customer;
import model.Investment;

public class InterestActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<Investment> investmentModel = new ArrayList<>();

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);
        recyclerView = findViewById(R.id.interest_recycler_view);

        DatabaseHandler db = new DatabaseHandler(InterestActivity.this);
        user = db.getUserDetails();
        fetchInvestment(user.get("uid"));
    }

    private void fetchInvestment(final String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.INVESTMENT_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray res = jObj.getJSONArray("Investments");
                // Check for error node in json
                if (res.length() > 0) {
                    for (int i = 0; i < res.length(); i++) {
                        Investment Investment = new Investment();
                        JSONObject o = res.getJSONObject(i);
                        Investment.setAmount(o.getString("amount"));
                        Investment.setStart(o.getString("start"));
                        Investment.setDay(o.getString("day"));
                        Investment.setInterest(o.getString("interest"));
                        Investment.setInvestment(o.getString("investment"));
                        Investment.setTotal(o.getString("total"));
                        investmentModel.add(Investment);
                    }
                    InterestAdapter adapter = new InterestAdapter(InterestActivity.this, investmentModel);
                    GridLayoutManager manager = new GridLayoutManager(InterestActivity.this, 1);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);
                } else {
                    //String errorMsg = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), "You do not have any Investment", Toast.LENGTH_LONG).show();
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
        Functions.showProgressDialog(InterestActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(InterestActivity.this);
    }
}