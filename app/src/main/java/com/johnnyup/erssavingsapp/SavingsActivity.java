package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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

import adapter.SavingsAdapter;
import model.Customer;

import static com.johnnyup.erssavingsapp.helper.Functions.ADD_SAVINGS_URL;

public class SavingsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    String selectedCustomer = "";
    List<String> customerList = new ArrayList<>();
    List<Customer> fullCustomerList = new ArrayList<>();
    List<model.Savings> savingsModel = new ArrayList<>();

    EditText savingsLabel, savingsAmount;

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings);

        recyclerView = findViewById(R.id.savings_recycler_view);
        TextView startNewSaving = findViewById(R.id.start_new_saving);
        startNewSaving.setOnClickListener(v -> addSaving());

        DatabaseHandler db = new DatabaseHandler(SavingsActivity.this);
        user = db.getUserDetails();
        fetchSavings(user.get("uid"), user.get("type"));

        Functions.getCustomers(customerList, fullCustomerList, user.get("uid"), getApplicationContext());

        // Hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            DatabaseHandler db = new DatabaseHandler(SavingsActivity.this);
            user = db.getUserDetails();
            String userType = user.get("type");
            fetchSavings(user.get("uid"), userType);
        }
    }

    private void fetchSavings(final String userID, String userType) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.SAVINGS_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray res = jObj.getJSONArray("savings");

                // Check for error node in json
                if (res.length() > 0) {
                    savingsModel.clear();
                    for (int i = 0; i < res.length(); i++) {
                        model.Savings savings = new model.Savings();
                        JSONObject o = res.getJSONObject(i);
                        if(!userType.equalsIgnoreCase("customer")) {
                            savings.setFirstName(o.getString("lname"));
                            savings.setLastName(o.getString("fname"));
                            savings.setUsername(o.getString("username"));
                        }
                        savings.setAmount(o.getString("amount"));
                        savings.setDate_end(o.getString("date_end"));
                        savings.setDate_start(o.getString("date_start"));
                        savings.setStatus(o.getString("status"));
                        savings.setSavings(o.getString("savings"));
                        savings.setBalance(o.getString("balance"));
                        savings.setId(Integer.parseInt(o.getString("id")));
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
                params.put("type", userType);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "fetch_saving_req");
    }

    public void goBack(View view) {
        finish();
    }

    public void addSaving() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View layout = inflater.inflate(R.layout.item_add_savings, findViewById(R.id.add_saving_root));

        savingsLabel = layout.findViewById(R.id.label);
        savingsAmount = layout.findViewById(R.id.amount);

        Spinner customerSpinner = layout.findViewById(R.id.customer);
        ArrayAdapter<String> customerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, customerList);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setAdapter(customerAdapter);

        customerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCustomer = parent.getItemAtPosition(position).toString();
                for(Customer c : fullCustomerList) {
                    if(c.getUsername().equalsIgnoreCase(selectedCustomer)) {
                        selectedCustomer = c.getId();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCustomer = parent.getItemAtPosition(0).toString();
            }
        });

        if (!user.get("type").equalsIgnoreCase("customer")) {
            layout.findViewById(R.id.customer_wrap).setVisibility(View.VISIBLE);
        }

        builder.setView(layout);
        builder.setPositiveButton(R.string.start, (dialog, id) -> {
            String label = savingsLabel.getText().toString();
            String amount = savingsAmount.getText().toString();
            DatabaseHandler db = new DatabaseHandler(SavingsActivity.this);
            user = db.getUserDetails();
            submitSavings(label, amount, selectedCustomer, user.get("uid"), user.get("type"));
        })
                .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void submitSavings(String label, String amount, String selectedCustomer, String userID, String userType) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(ADD_SAVINGS_URL, SavingsActivity.this), response -> {
            hideDialog();
            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(this, "Savings Added!", Toast.LENGTH_LONG).show();
                    fetchSavings(user.get("uid"), user.get("type"));
                } else {
                    Toast.makeText(SavingsActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(SavingsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("savings", label);
                params.put("user", userID);
                params.put("type", userType);
                params.put("agent", selectedCustomer);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "saving_req");
    }

    private void showDialog() {
        Functions.showProgressDialog(SavingsActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(SavingsActivity.this);
    }
}