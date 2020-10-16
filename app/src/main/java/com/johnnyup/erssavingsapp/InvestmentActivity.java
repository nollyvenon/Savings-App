package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
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
import com.johnnyup.erssavingsapp.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.InvestmentAdapter;
import adapter.SavingsAdapter;
import model.Customer;
import model.Investment;

public class InvestmentActivity extends AppCompatActivity {
    String userType;

    RecyclerView recyclerView;
    List<Investment> investmentModel = new ArrayList<>();
    String selectedDuration;
    String selectedCustomer = "";
    List<String> customerList = new ArrayList<>();
    List<Customer> fullCustomerList = new ArrayList<>();

    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);
        recyclerView = findViewById(R.id.investment_recycler_view);
        TextView startNewInvestment = findViewById(R.id.start_new_investment);
        startNewInvestment.setOnClickListener(v -> addInvestment());

        DatabaseHandler db = new DatabaseHandler(InvestmentActivity.this);
        user = db.getUserDetails();
        userType = user.get("type");
        fetchInvestment(user.get("uid"));

        Functions.getCustomers(customerList, fullCustomerList, user.get("uid"), getApplicationContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            DatabaseHandler db = new DatabaseHandler(InvestmentActivity.this);
            user = db.getUserDetails();
            fetchInvestment(user.get("uid"));
        }
    }

    private void fetchInvestment(final String userID) {
         String NEWURL = "";
        if(userType.equalsIgnoreCase("customer")) {
            NEWURL = Functions.INVESTMENT_URL;
        } else {
            NEWURL = Functions.INVESTMENT_MARKETER_URL;
        }
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(NEWURL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray res = jObj.getJSONArray("Investments");
                // Check for error node in json
                if (res.length() > 0) {
                    for (int i = 0; i < res.length(); i++) {
                        Investment Investment = new Investment();
                        JSONObject o = res.getJSONObject(i);

                        if(!userType.equalsIgnoreCase("customer")) {
                            Investment.setFirstName(o.getString("fname"));
                            Investment.setLastName(o.getString("lname"));
                            Investment.setUsername(o.getString("username"));
                        }

                        Investment.setId(Integer.parseInt(o.getString("id")));
                        Investment.setAmount(o.getString("amount"));
                        Investment.setStart(o.getString("start"));
                        Investment.setEnd(o.getString("end"));
                        Investment.setDay(o.getString("day"));
                        Investment.setInterest(o.getString("interest"));
                        Investment.setInvestment(o.getString("investment"));
                        Investment.setTotal(o.getString("total"));
                        Investment.setStatus(o.getString("status"));
                        investmentModel.add(Investment);
                    }
                    InvestmentAdapter adapter = new InvestmentAdapter(InvestmentActivity.this, investmentModel);
                    GridLayoutManager manager = new GridLayoutManager(InvestmentActivity.this, 1);
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

    public void addInvestment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View layout = inflater.inflate(R.layout.item_add_investment, findViewById(R.id.add_saving_root));

        EditText investmentLabel = layout.findViewById(R.id.label);
        EditText investmentAmount = layout.findViewById(R.id.amount);

        if(!userType.equalsIgnoreCase("customer")) {
            layout.findViewById(R.id.customer_wrap).setVisibility(View.VISIBLE);
        }

        List<String> durationList = new ArrayList<>();
        durationList.add("1 Month");
        Spinner durationSpinner = layout.findViewById(R.id.duration);
        ArrayAdapter durationAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, durationList);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durationAdapter);

        durationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDuration = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDuration = parent.getItemAtPosition(0).toString();
            }
        });

        Spinner customerSpinner = layout.findViewById(R.id.customer);
        ArrayAdapter customerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, customerList);
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

        builder.setView(layout);
        builder.setPositiveButton(R.string.start, (dialog, id) -> {
            String label = investmentLabel.getText().toString();
            String amount = investmentAmount.getText().toString();
            DatabaseHandler db = new DatabaseHandler(InvestmentActivity.this);
            user = db.getUserDetails();
            if(userType.equalsIgnoreCase("customer")) {
                Intent intent = new Intent(this, InvestmentPaystackActivity.class);
                intent.putExtra("investment", label);
                intent.putExtra("amount", amount);
                intent.putExtra("duration", selectedDuration);
                intent.putExtra("customer", selectedCustomer);
                intent.putExtra("user", user.get("uid"));
                startActivity(intent);
            } else {
                Functions.submitInvestment(InvestmentActivity.this, label, amount, selectedDuration, selectedCustomer, user.get("uid"), userType);
                fetchInvestment(user.get("uid"));
            }
        })
                .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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