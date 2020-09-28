package com.johnnyup.erssavingsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;
import com.johnnyup.erssavingsapp.helper.SessionManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Customer;

import static com.johnnyup.erssavingsapp.helper.Functions.PROFILE_IMG_LINK;
import static com.johnnyup.erssavingsapp.helper.Functions.USER_URL;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    private TextView btnChangePass, btnLogout;
    private SessionManager session;
    ImageButton openDrawer;
    DrawerLayout drawer;
    RecyclerView recyclerView;
    LinearLayout savingsWrap, investmentWrap;
    EditText savingsLabel, savingsAmount;
    String selectedDuration;
    String selectedCustomer = "";
    List<String> customerList = new ArrayList<>();
    List<Customer> fullCustomerList = new ArrayList<>();
    List<String> durationList = new ArrayList<>();
    private HashMap<String, String> user = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView txtName = findViewById(R.id.name);
        TextView txtEmail = findViewById(R.id.email);
        btnChangePass = findViewById(R.id.change_password);
        btnLogout = findViewById(R.id.logout);
        openDrawer = findViewById(R.id.open_drawer);
        drawer = findViewById(R.id.drawer_layout);
        savingsWrap = findViewById(R.id.Savings_wrap);
        investmentWrap = findViewById(R.id.investment_wrap);
        recyclerView = findViewById(R.id.savings_recycler_view);
        TextView startNewSaving = findViewById(R.id.start_new_saving);
        TextView startNewInvestment = findViewById(R.id.start_new_investment);
        startNewSaving.setOnClickListener(v -> addSaving());
        startNewInvestment.setOnClickListener(v -> addInvestment());
        findViewById(R.id.join_ersnetwork).setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ersnets.net/"));
            startActivity(browserIntent);
        });

        DatabaseHandler db = new DatabaseHandler(HomeActivity.this);
        user = db.getUserDetails();

        // session manager
        session = new SessionManager(HomeActivity.this);

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from database
        String name = user.get("name");
        String email = user.get("email");
        String agent = user.get("agent");

        Functions.getCustomers(customerList, fullCustomerList, user.get("uid"), getApplicationContext());
        getHomeDetails(user.get("uid"));

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        openDrawer.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        findViewById(R.id.h_profile_image).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        savingsWrap.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SavingsActivity.class);
            startActivity(intent);
        });

        investmentWrap.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InvestmentActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.drawer_savings).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SavingsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.drawer_investment).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InvestmentActivity.class);
            startActivity(intent);
        });
        ;

        findViewById(R.id.drawer_payout).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.payouts_wrap).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PayoutActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.drawer_interest).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, InterestActivity.class);
            startActivity(intent);
        });

        init();
    }

    private void init() {
        btnLogout.setOnClickListener(v -> logoutUser());

        btnChangePass.setOnClickListener(v -> {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HomeActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.change_password, null);

            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle("Change Password");
            dialogBuilder.setCancelable(false);

            final TextInputLayout oldPassword = dialogView.findViewById(R.id.old_password);
            final TextInputLayout newPassword = dialogView.findViewById(R.id.new_password);

            dialogBuilder.setPositiveButton("Change", (dialog, which) -> {
                // empty
            });

            dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            final AlertDialog alertDialog = dialogBuilder.create();

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (Objects.requireNonNull(oldPassword.getEditText()).getText().length() > 0 &&
                            Objects.requireNonNull(newPassword.getEditText()).getText().length() > 0) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };

            Objects.requireNonNull(oldPassword.getEditText()).addTextChangedListener(textWatcher);
            Objects.requireNonNull(newPassword.getEditText()).addTextChangedListener(textWatcher);

            alertDialog.setOnShowListener(dialog -> {
                final Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setEnabled(false);

                b.setOnClickListener(view -> {
                    String old_pass = oldPassword.getEditText().getText().toString();
                    String new_pass = newPassword.getEditText().getText().toString();

                    if (!old_pass.isEmpty() && !new_pass.isEmpty()) {
                        changePassword(user.get("uid"), old_pass, new_pass);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(HomeActivity.this, "Fill all values!", Toast.LENGTH_SHORT).show();
                    }

                });
            });

            alertDialog.show();
        });
    }

    public void getHomeDetails(String userID) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(USER_URL, getApplicationContext()), response -> {
            try {
                NumberFormat format = new DecimalFormat("#,###");
                JSONObject jObj = new JSONObject(response);
                JSONArray customer = jObj.getJSONArray("customer");

                JSONObject c = customer.getJSONObject(0);

                TextView agent = findViewById(R.id.h_username);
                agent.setText(String.format("%s%s", c.getString("fname"), c.getString("lname")));

                TextView contact = findViewById(R.id.h_contact);
                contact.setText(c.getString("phone"));

                TextView wallet = findViewById(R.id.h_wallet);
                wallet.setText(format.format(Double.parseDouble(jObj.getString("wallet"))));

                TextView savings = findViewById(R.id.h_saving);
                savings.setText(format.format(Double.parseDouble(jObj.getString("savings"))));

                TextView investment = findViewById(R.id.h_investment);
                investment.setText(format.format(Double.parseDouble(jObj.getString("investment"))));

                TextView payout = findViewById(R.id.h_payout);
                payout.setText(format.format(Double.parseDouble(jObj.getString("payout"))));

                CircleImageView profileImg = findViewById(R.id.h_profile_image);
                Glide.with(this).load(PROFILE_IMG_LINK +
                        c.getString("cname") + "/" + c.getString("cimage")).into(profileImg);

                CircleImageView dProfileImg = findViewById(R.id.d_profile_image);
                Glide.with(this).load(PROFILE_IMG_LINK +
                        c.getString("cname") + "/" + c.getString("cimage")).into(dProfileImg);

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
                for (Customer c : fullCustomerList) {
                    if (c.getUsername().equalsIgnoreCase(selectedCustomer)) {
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
            String label = savingsLabel.getText().toString();
            String amount = savingsAmount.getText().toString();

            if (label.equalsIgnoreCase("")) {
                savingsLabel.setError("Label is empty");
            }

            if (amount.equalsIgnoreCase("")) {
                savingsAmount.setError("Label is empty");
                return;
            }

            DatabaseHandler db = new DatabaseHandler(HomeActivity.this);
            user = db.getUserDetails();
            Functions.submitSavings(HomeActivity.this, label, amount, selectedCustomer, user.get("uid"));
        })
                .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void addInvestment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View layout = inflater.inflate(R.layout.item_add_investment, findViewById(R.id.add_saving_root));

        EditText investmentLabel = layout.findViewById(R.id.label);
        EditText investmentAmount = layout.findViewById(R.id.amount);

        durationList.add("1 Month");
        Spinner durationSpinner = layout.findViewById(R.id.duration);
        ArrayAdapter<String> durationAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, durationList);
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
        ArrayAdapter<String> customerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, customerList);
        customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        customerSpinner.setAdapter(customerAdapter);

        customerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCustomer = parent.getItemAtPosition(position).toString();
                for (Customer c : fullCustomerList) {
                    if (c.getUsername().equalsIgnoreCase(selectedCustomer)) {
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

            if (label.equalsIgnoreCase("")) {
                investmentLabel.setError("Label is empty");
            }

            if (Double.parseDouble(amount) < 5000) {
                investmentAmount.setError("Amount is less than 5,000 naira");
                return;
            }

            DatabaseHandler db = new DatabaseHandler(HomeActivity.this);
            user = db.getUserDetails();
            Intent intent = new Intent(this, InvestmentPaystackActivity.class);
            intent.putExtra("investment", label);
            intent.putExtra("amount", amount);
            intent.putExtra("duration", selectedDuration);
            intent.putExtra("customer", selectedCustomer);
            intent.putExtra("user", user.get("uid"));
            startActivity(intent);
            //Functions.submitInvestment(HomeActivity.this, label, amount, selectedDuration, selectedCustomer, user.get("uid"));
        })
                .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logoutUser() {
        session.setLogin(false);
        Functions logout = new Functions();
        logout.logoutUser(HomeActivity.this);
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void changePassword(final String userID, final String old_pass, final String new_pass) {
        // Tag used to cancel the request
        String tag_string_req = "req_reset_pass";

        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.RESET_PASS_URL, response -> {

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(HomeActivity.this, "Password updated!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(HomeActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Log.e(TAG, "Reset Password Error: " + error.getMessage());
            Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            hideDialog();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to erssavingsapp url
                Map<String, String> params = new HashMap<>();
                params.put("id", userID);
                params.put("opassword", old_pass);
                params.put("password", new_pass);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

        };

        // Adding request to volley request queue
        strReq.setRetryPolicy(new DefaultRetryPolicy(5 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, 0));
        strReq.setRetryPolicy(new DefaultRetryPolicy(0, 0, 0));
        MyApplication.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        Functions.showProgressDialog(HomeActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(HomeActivity.this);
    }

}