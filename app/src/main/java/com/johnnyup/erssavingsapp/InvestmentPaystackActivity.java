package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.snowcorp.login.R;

import java.util.HashMap;

public class InvestmentPaystackActivity extends AppCompatActivity {

    private HashMap<String, String> userdb = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_paystack);
        ProgressDialog progDialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        progDialog.setCancelable(false);

        Intent intent = getIntent();
        String investment = intent.getStringExtra("investment");
        String amount = intent.getStringExtra("amount");
        String duration = intent.getStringExtra("duration");
        String customer = intent.getStringExtra("customer");
        if(customer.equalsIgnoreCase("")) {
            customer = "0";
        }
        String user = intent.getStringExtra("user");

        DatabaseHandler db = new DatabaseHandler(InvestmentPaystackActivity.this);
        userdb = db.getUserDetails();

        String url = Functions.getUrl(Functions.ADD_INVESTMENT_URL, getApplicationContext()) + user + "/" + amount + "/" + duration + "/" + customer + "/" + investment + "/" +  userdb.get("token");

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                progDialog.show();
                if (url.contains("http://exitme")) {
                    goBack();  // close activity
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed(); // Ignore SSL certificate errors
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                progDialog.dismiss();
            }
        });

        webView.loadUrl(url);
    }

    public void goBack(View view) {
        finish();
    }

    public void goBack() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("reload", "reload");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}