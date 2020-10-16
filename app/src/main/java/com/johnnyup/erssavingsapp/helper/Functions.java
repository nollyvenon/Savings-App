package com.johnnyup.erssavingsapp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.johnnyup.erssavingsapp.HomeActivity;
import com.johnnyup.erssavingsapp.MyApplication;
import com.johnnyup.erssavingsapp.SavingsActivity;
import com.johnnyup.erssavingsapp.widget.ProgressBarDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.SavingTransactionsAdapter;
import adapter.SavingsAdapter;
import model.Customer;
import model.Investment;
import model.SavingsTransaction;


public class Functions {

    //Main URL
    private static String MAIN_URL = "https://www.savings.ersnets.net/mcalls/";
    public static String PROFILE_IMG_LINK = "https://www.savings.ersnets.net/profile-img/";
    public static String MARKETER_PROFILE_IMG_LINK = "https://www.savings.ersnets.net/marketers/profile-img/marketer/";

    // Login URL
    public static String LOGIN_URL = MAIN_URL + "login";
    public static String LOGIN_MARKETER_URL = MAIN_URL + "login/marketer";

    // Register URL
    public static String REGISTER_URL = MAIN_URL + "register";

    // OTP Verification
    public static String OTP_VERIFY_URL = "verification.php";

    // Forgot Password
    public static String RESET_PASS_URL = MAIN_URL + "password/updatePassword";
    public static String FORGOT_PASS_URL = MAIN_URL + "password/forgot";

    // user
    public static String USER_URL = "user";
    public static String USER_PROFILE_URL = "user/getProfile";
    public static String MARKETER_PROFILE_URL = "user/getMarketerProfile";
    public static String MARKETER_UPDATE_PROFILE_URL = "user/updateMarketer";
    public static String USER_UPDATE_PROFILE_URL = "user/updateCustomer";
    public static String USER_FETCH_CUSTOMERS_URL = "user/getCustomers";
    public static String USER_ADD_CUSTOMER_URL = "user/registerCustomer";
    public static String MARKETER_URL = "user/getHomeDataForMarketer";

    // savings
    public static String SAVINGS_URL = "savings";
    public static String ADD_SAVINGS_URL = "savings/save2";
    public static String FUND_SAVINGS_URL = "savings/fund/";
    public static String WITHDRAW_SAVINGS_URL = "savings/withdraw/";
    public static String SAVINGS_TRANSACTION_URL = "savings/savingsTransactions/";

    // investment
    public static String INVESTMENT_URL = "investment";
    public static String ADD_INVESTMENT_URL = "investment/loadPaystack/";
    public static String ADD_MARKETER_INVESTMENT_URL = "investment/saveMarketer";
    public static String WITHDRAW_INVESTMENT_URL = "investment/withdraw/";
    public static String CUSTOMER_URL = "investment/agentCustomers";
    public static String INVESTMENT_MARKETER_URL = "investment/marketerInvestments";
    public static String MARKETER_TRANSACTION_URL = "investment/marketerTransaction";
    public static String MARKETER_EARNING_URL = "investment/earnings";

    // payout
    public static String PAYOUT_URL = "payout";


    public static String getUrl(String url, Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap<String, String> user = db.getUserDetails();
        return MAIN_URL + user.get("token") + "/" + url;
    }

    /**
     * Function to logout user
     * Resets the temporary data stored in SQLite Database
     */
    public void logoutUser(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
    }

    /**
     * Email Address Validation
     */
    public static boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * Hide Keyboard
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public static void showProgressDialog(Context context, String title) {
        FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
        DialogFragment newFragment = ProgressBarDialog.newInstance(title);
        newFragment.show(fm, "dialog");
    }

    public static void hideProgressDialog(Context context) {
        FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
        Fragment prev = fm.findFragmentByTag("dialog");
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    public static void submitSavings(Context context, String label, String amount, String selectedCustomer, String userID, String userType) {
        showProgressDialog(context, "Please wait...");
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(ADD_SAVINGS_URL, context), response -> {
            hideProgressDialog(context);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(context, "Savings Added!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressDialog(context);
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

    public static void submitInvestment(Context context, String label, String amount, String duration,
                                        String customer, String userID, String userType) {
        showProgressDialog(context, "Please wait...");
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(ADD_MARKETER_INVESTMENT_URL, context), response -> {
            hideProgressDialog(context);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(context, "Investment Added!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressDialog(context);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("investment", label);
                params.put("user", userID);
                params.put("customer", customer);
                params.put("duration", duration);
                params.put("type", userType);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "investment_req");
    }

    public static void savingTransactions(Context context, String id, RecyclerView recyclerView) {
        showProgressDialog(context, "Please wait...");
        List<SavingsTransaction> savingsTransactions = new ArrayList<>();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(SAVINGS_TRANSACTION_URL, context), response -> {
            hideProgressDialog(context);

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");
                JSONArray res = jObj.getJSONArray("transactions");

                // Check for error node in json
                if (status) {
                    if (res.length() > 0) {
                        for (int i = 0; i < res.length(); i++) {
                            SavingsTransaction s = new SavingsTransaction();
                            JSONObject o = res.getJSONObject(i);
                            s.setDescription(o.getString("description"));
                            s.setType(o.getString("type"));
                            s.setAmount(o.getString("amount"));
                            s.setSource(o.getString("source"));
                            s.setDate(o.getString("date"));
                            savingsTransactions.add(s);
                        }
                    }

                    SavingTransactionsAdapter adapter = new SavingTransactionsAdapter(context, savingsTransactions);
                    GridLayoutManager manager = new GridLayoutManager(context, 1);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressDialog(context);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "savings_trx_req");
    }

    public static void savingsWithdraw(Context context, String amount, String userID, String savingsID) {
        showProgressDialog(context, "Please wait...");
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(WITHDRAW_SAVINGS_URL, context), response -> {
            hideProgressDialog(context);
            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    ((Activity) context).finish();
                    Toast.makeText(context, "Withdrawal request sent!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressDialog(context);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("amount", amount);
                params.put("user", userID);
                params.put("id", savingsID);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "withdraw_req");
    }

    public static void InvestmentWithdraw(Context context, String userID, String investmentID) {
        showProgressDialog(context, "Please wait...");
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(WITHDRAW_INVESTMENT_URL, context), response -> {
            hideProgressDialog(context);
            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");
                // Check for error node in json
                if (status) {
                    ((Activity) context).finish();
                    Toast.makeText(context, "Withdrawal request sent!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(context, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
            hideProgressDialog(context);
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", userID);
                params.put("id", investmentID);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "withdraw_inv_req");
    }

    public static void getCustomers(List<String> customers, List<Customer> fullCustomerList, String userID, Context context) {
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(CUSTOMER_URL, context), response -> {
            try {
                JSONObject jObj = new JSONObject(response);
                JSONArray res = jObj.getJSONArray("customers");
                // Check for error node in json
                if (res.length() > 0) {
                    for (int i = 0; i < res.length(); i++) {
                        Customer c = new Customer();
                        JSONObject o = res.getJSONObject(i);
                        c.setId(o.getString("id"));
                        c.setUsername(o.getString("username"));
                        customers.add(c.getUsername());
                        fullCustomerList.add(c);
                    }
                } else {
                    String errorMsg = jObj.getString("message");
                    // Toast.makeText(getApplicationContext(), "You do not have any Investment", Toast.LENGTH_LONG).show();
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
}
