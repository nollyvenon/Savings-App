package com.johnnyup.erssavingsapp;

import android.app.Activity;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
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

import adapter.PayoutAdapter;
import adapter.RecentActivityAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Customer;
import model.Payout;
import model.RecentActivity;

import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_PROFILE_IMG_LINK;
import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_URL;
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
    List<RecentActivity> recentActivities = new ArrayList<>();
    List<String> customerList = new ArrayList<>();
    List<Customer> fullCustomerList = new ArrayList<>();
    List<String> durationList = new ArrayList<>();
    private HashMap<String, String> user = new HashMap<>();
    final static int RELOAD_CODE = 112;

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
        String userType = user.get("type");

        Functions.getCustomers(customerList, fullCustomerList, user.get("uid"), getApplicationContext());

        if (userType.equalsIgnoreCase("customer")) {
            getHomeDetails(user.get("uid"));
            findViewById(R.id.marketer_wrap).setVisibility(View.GONE);
        } else {
            getHomeDetailsForMarketer(user.get("uid"));
        }

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        openDrawer.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        if (userType.equalsIgnoreCase("customer")) {
            savingsWrap.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SavingsActivity.class);
                startActivity(intent);
            });

            investmentWrap.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, InvestmentActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.payouts_wrap).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PayoutActivity.class);
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

            findViewById(R.id.drawer_payout).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, PayoutActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.drawer_interest).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, InterestActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.h_profile_image).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivityForResult(intent, RELOAD_CODE);
            });
            findViewById(R.id.marketer_drawer).setVisibility(View.GONE);
        } else {
            findViewById(R.id.Savings_investment_wrap).setVisibility(View.GONE);
            findViewById(R.id.payouts_wrap).setVisibility(View.GONE);
            findViewById(R.id.top_menu_wrap).setVisibility(View.GONE);
            findViewById(R.id.customer_drawer).setVisibility(View.GONE);
            findViewById(R.id.drawer_my_customers).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CustomerActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.drawer_customer_savings).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SavingsActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.drawer_customer_investments).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, InvestmentActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.drawer_transaction_history).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, AgentTransactionActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.drawer_my_earnings).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, EarningActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.h_profile_image).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MarketerProfileActivity.class);
                startActivity(intent);
            });

            findViewById(R.id.drawer_my_profile).setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MarketerProfileActivity.class);
                startActivityForResult(intent, RELOAD_CODE);
            });
        }

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
                        changePassword(user.get("uid"), old_pass, new_pass, user.get("type"));
                        dialog.dismiss();
                    } else {
                        Toast.makeText(HomeActivity.this, "Fill all values!", Toast.LENGTH_SHORT).show();
                    }

                });
            });

            alertDialog.show();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // TODO Extract the data returned from the child Activity.
            DatabaseHandler db = new DatabaseHandler(HomeActivity.this);
            user = db.getUserDetails();
            String userType = user.get("type");
            if (userType.equalsIgnoreCase("customer")) {
                getHomeDetails(user.get("uid"));
                findViewById(R.id.marketer_wrap).setVisibility(View.GONE);
            } else {
                getHomeDetailsForMarketer(user.get("uid"));
            }
        }

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
                agent.setText(String.format("%s %s", c.getString("fname"), c.getString("lname")));

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
                CircleImageView dProfileImg = findViewById(R.id.d_profile_image);

                Glide.with(this)
                        .load(PROFILE_IMG_LINK +
                                c.getString("cname") + "/" + c.getString("cimage"))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .placeholder(R.drawable.bg_grey)
                        .error(R.drawable.bg_grey)
                        .into(profileImg);

                Glide.with(this)
                        .load(PROFILE_IMG_LINK +
                                c.getString("cname") + "/" + c.getString("cimage"))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .placeholder(R.drawable.bg_grey)
                        .error(R.drawable.bg_grey)
                        .into(dProfileImg);

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

    public void getHomeDetailsForMarketer(String userID) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(MARKETER_URL, getApplicationContext()), response -> {
            try {
                NumberFormat format = new DecimalFormat("#,###");
                JSONObject jObj = new JSONObject(response);
                JSONArray recentActivityAry = jObj.getJSONArray("recent_activity");

                JSONObject user = jObj.getJSONObject("user");
                TextView agent = findViewById(R.id.h_username);
                agent.setText(String.format("%s %s", user.getString("fname"), user.getString("lname")));

                TextView contact = findViewById(R.id.h_contact);
                contact.setText(user.getString("phone"));

                CircleImageView profileImg = findViewById(R.id.h_profile_image);
                CircleImageView dProfileImg = findViewById(R.id.d_profile_image);

                Glide.with(this)
                        .load(MARKETER_PROFILE_IMG_LINK + user.getString("image"))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .placeholder(R.drawable.bg_grey)
                        .error(R.drawable.bg_grey)
                        .into(profileImg);

                Glide.with(this)
                        .load(MARKETER_PROFILE_IMG_LINK + user.getString("image"))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .placeholder(R.drawable.bg_grey)
                        .error(R.drawable.bg_grey)
                        .into(dProfileImg);

                TextView wallet = findViewById(R.id.h_wallet);
                wallet.setText("N" + format.format(Double.parseDouble(jObj.getString("wallet"))));

                TextView savingsTargetAmount = findViewById(R.id.m_sav_target_amount);
                TextView savingsMetAmount = findViewById(R.id.m_sav_target_met_amount);
                TextView savingsPending = findViewById(R.id.m_sav_target_pending_amount);
                savingsTargetAmount.setText("N" + format.format(Double.parseDouble(jObj.getString("savings_target"))));
                savingsMetAmount.setText("N" + format.format(Double.parseDouble(jObj.getString("savings_target_met"))));
                savingsPending.setText("N" + format.format(Double.parseDouble(jObj.getString("savings_target_pending"))));

                TextView investmentTargetAmount = findViewById(R.id.m_inv_target_amount);
                TextView investmentMetAmount = findViewById(R.id.m_inv_target_met_amount);
                TextView investmentPendingAmount = findViewById(R.id.m_inv_target_pending_amount);
                investmentTargetAmount.setText("N" + format.format(Double.parseDouble(jObj.getString("investment_target"))));
                investmentMetAmount.setText("N" + format.format(Double.parseDouble(jObj.getString("investment_target_met"))));
                investmentPendingAmount.setText("N" + format.format(Double.parseDouble(jObj.getString("investment_target_pending"))));

                if (recentActivityAry.length() > 0) {
                    for (int i = 0; i < recentActivityAry.length(); i++) {
                        RecentActivity recentActivity = new RecentActivity();
                        JSONObject o = recentActivityAry.getJSONObject(i);
                        recentActivity.setAmount(o.getString("amount"));
                        recentActivity.setDate(o.getString("date"));
                        recentActivity.setDescription(o.getString("description"));
                        recentActivity.setUsername(o.getString("username"));
                        recentActivities.add(recentActivity);
                    }

                    RecentActivityAdapter adapter = new RecentActivityAdapter(HomeActivity.this, recentActivities);
                    GridLayoutManager manager = new GridLayoutManager(HomeActivity.this, 1);
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
            String userType = user.get("type");
            Functions.submitSavings(HomeActivity.this, label, amount, selectedCustomer, user.get("uid"), user.get("type"));
            if (userType.equalsIgnoreCase("customer")) {
                getHomeDetails(user.get("uid"));
            } else {
                getHomeDetailsForMarketer(user.get("uid"));
            }
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
        })
                .setNegativeButton("Close", (dialog, id) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void logoutUser() {
        session.setLogin(false);
        Functions logout = new Functions();
        logout.logoutUser(HomeActivity.this);
        Intent intent = new Intent(HomeActivity.this, EntryActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (session.isLoggedIn()) {
            Intent i = new Intent(HomeActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }
        super.onBackPressed();
    }

    private void changePassword(String userID, String old_pass, String new_pass, String userType) {
        // Tag used to cancel the request
        String tag_string_req = "req_reset_pass";

        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.RESET_PASS_URL, response -> {
            hideDialog();
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
                params.put("user", userID);
                params.put("type", userType);
                params.put("opassword", old_pass);
                params.put("npassword", new_pass);

                return params;
            }
        };

        //Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "up_password_profile_req");
    }

    private void showDialog() {
        Functions.showProgressDialog(HomeActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(HomeActivity.this);
    }

}