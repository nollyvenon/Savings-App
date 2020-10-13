package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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

import adapter.CustomerAdapter;
import model.Customer;

import static com.johnnyup.erssavingsapp.helper.Functions.USER_FETCH_CUSTOMERS_URL;

public class CustomerActivity extends AppCompatActivity {

    List<Customer> fullCustomerList = new ArrayList<>();
    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        DatabaseHandler db = new DatabaseHandler(CustomerActivity.this);
        user = db.getUserDetails();
        getCustomers(user.get("uid"));
        findViewById(R.id.add_customer_btn).setOnClickListener(v -> {
            Intent intent = new Intent(CustomerActivity.this, CustomerRegistrationActivity.class);
            startActivity(intent);
        });
    }

    public void getCustomers(String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(USER_FETCH_CUSTOMERS_URL, getApplicationContext()), response -> {
            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray customers = jObj.getJSONArray("customers");

                if (customers.length() > 0) {
                    for (int i = 0; i < customers.length(); i++) {
                        Customer customer = new Customer();
                        JSONObject o = customers.getJSONObject(i);
                        customer.setFname(o.getString("fname"));
                        customer.setLname(o.getString("lname"));
                        customer.setUsername(o.getString("username"));
                        customer.setEmail(o.getString("email"));
                        customer.setPhone(o.getString("phone"));
                        fullCustomerList.add(customer);
                    }

                    CustomerAdapter adapter = new CustomerAdapter(CustomerActivity.this, fullCustomerList);
                    GridLayoutManager manager = new GridLayoutManager(CustomerActivity.this, 1);
                    RecyclerView recyclerView = findViewById(R.id.customer_recycler_view);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);

                    hideDialog();
                }

            } catch (JSONException e) {
                hideDialog();
                Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        Functions.showProgressDialog(CustomerActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(CustomerActivity.this);
    }
}