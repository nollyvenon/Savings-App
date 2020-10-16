package com.johnnyup.erssavingsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.snowcorp.login.R;

import java.util.HashMap;

public class PaystackActivity extends AppCompatActivity {

    private HashMap<String, String> user = new HashMap<>();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paystack);
        ProgressDialog progDialog = ProgressDialog.show(this, "Loading", "Please wait...", true);
        progDialog.setCancelable(false);

        DatabaseHandler db = new DatabaseHandler(PaystackActivity.this);
        user = db.getUserDetails();
        String userID = user.get("uid");

        Intent intent = getIntent();
        String amount = intent.getStringExtra("amount");
        String date = intent.getStringExtra("date");
        String savingID = intent.getStringExtra("saving_id");

        String url = Functions.getUrl(Functions.FUND_SAVINGS_URL, getApplicationContext()) + userID + "/" + amount + "/" + date + "/" + savingID;

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
