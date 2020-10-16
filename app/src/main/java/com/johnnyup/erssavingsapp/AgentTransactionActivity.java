package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;
import com.johnnyup.erssavingsapp.helper.SessionManager;

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

import adapter.RecentActivityAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import model.RecentActivity;

import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_PROFILE_IMG_LINK;
import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_TRANSACTION_URL;
import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_URL;

public class AgentTransactionActivity extends AppCompatActivity {

    List<RecentActivity> recentActivities = new ArrayList<>();
    private HashMap<String, String> user = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_transaction);

        DatabaseHandler db = new DatabaseHandler(AgentTransactionActivity.this);
        user = db.getUserDetails();

        getTransactions(user.get("uid"));
    }

    public void getTransactions(String userID) {
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(MARKETER_TRANSACTION_URL, getApplicationContext()), response -> {
            hideDialog();
            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray transactionss = jObj.getJSONArray("transactions");

                if (transactionss.length() > 0) {
                    for (int i = 0; i < transactionss.length(); i++) {
                        RecentActivity recentActivity = new RecentActivity();
                        JSONObject o = transactionss.getJSONObject(i);
                        recentActivity.setAmount(o.getString("amount"));
                        recentActivity.setDate(o.getString("date"));
                        recentActivity.setDescription(o.getString("description"));
                        recentActivity.setUsername(o.getString("username"));
                        recentActivities.add(recentActivity);
                    }

                    RecentActivityAdapter adapter = new RecentActivityAdapter(AgentTransactionActivity.this, recentActivities);
                    GridLayoutManager manager = new GridLayoutManager(AgentTransactionActivity.this, 1);
                    RecyclerView recyclerView = findViewById(R.id.recent_activities_recycler_view);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);
                }

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

    private void showDialog() {
        Functions.showProgressDialog(AgentTransactionActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(AgentTransactionActivity.this);
    }
}