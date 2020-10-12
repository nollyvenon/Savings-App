package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.johnnyup.erssavingsapp.helper.Functions.PROFILE_IMG_LINK;
import static com.johnnyup.erssavingsapp.helper.Functions.USER_FETCH_CUSTOMERS_URL;
import static com.johnnyup.erssavingsapp.helper.Functions.USER_URL;

public class CustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
    }

    public void getCustomers(String userID) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(USER_FETCH_CUSTOMERS_URL, getApplicationContext()), response -> {
            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray customers = jObj.getJSONArray("customer");

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
}