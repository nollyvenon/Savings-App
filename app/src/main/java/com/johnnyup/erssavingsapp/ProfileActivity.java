package com.johnnyup.erssavingsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import adapter.InvestmentAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import model.Investment;

import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_PROFILE_IMG_LINK;
import static com.johnnyup.erssavingsapp.helper.Functions.PROFILE_IMG_LINK;

public class ProfileActivity extends AppCompatActivity {

    Bitmap bitmap;
    private static final int REQUEST_CODE_GALLERY = 999;
    Button uploadImage;
    CircleImageView profileImage;
    EditText firstName, lastName, middleName, phone, address, email;
    EditText kfirstName, klastName, kphone, kaddress, kemail;
    String userType, username;

    private HashMap<String, String> user = new HashMap<>();
    String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        middleName = findViewById(R.id.middle_name);
        phone = findViewById(R.id.phone_number);
        address = findViewById(R.id.address);
        email = findViewById(R.id.email);

        kfirstName = findViewById(R.id.k_first_name);
        klastName = findViewById(R.id.k_last_name);
        kphone = findViewById(R.id.k_phone_number);
        kaddress = findViewById(R.id.k_address);
        kemail = findViewById(R.id.k_email);
        uploadImage = findViewById(R.id.upload_btn);
        profileImage = findViewById(R.id.profile_image);

        uploadImage.setOnClickListener(v -> ActivityCompat.requestPermissions(ProfileActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_GALLERY));

        DatabaseHandler db = new DatabaseHandler(ProfileActivity.this);
        user = db.getUserDetails();
        userType = user.get("type");

        userID = user.get("uid");

        Intent intent = getIntent();
        if(intent.getStringExtra("userID") != null) {
            userID = intent.getStringExtra("userID");
            userType = "customer";
        }

        getProfile(userID);

        findViewById(R.id.update_profile).setOnClickListener(v -> {
            String sfirstName = firstName.getText().toString();
            String slastName = lastName.getText().toString();
            String smiddleName = middleName.getText().toString();
            String sphone = phone.getText().toString();
            String saddress = address.getText().toString();
            String semail = email.getText().toString();
            String skfirstName = kfirstName.getText().toString();
            String sklastName = klastName.getText().toString();
            String skphone = kphone.getText().toString();
            String skaddress = kaddress.getText().toString();
            String skemail = kemail.getText().toString();
            updateProfile(sfirstName, slastName, smiddleName, sphone, saddress, semail, skfirstName, sklastName,
                    skphone, skaddress, skemail, imageToString());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_GALLERY) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            } else {
                Toast.makeText(this, "Permission to access file storage not granted", Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                assert uri != null;
                InputStream inputStream = getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String imageToString() {
        if (bitmap == null) return "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] bytes = stream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void getProfile(String userID) {
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.USER_PROFILE_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONObject res = jObj.getJSONObject("user");

                firstName.setText(res.getString("fname"));
                lastName.setText(res.getString("lname"));
                middleName.setText(res.getString("middlename"));
                email.setText(res.getString("email"));
                phone.setText(res.getString("phone"));
                address.setText(res.getString("address"));
                kfirstName.setText(res.getString("nfname"));
                klastName.setText(res.getString("fname"));
                kphone.setText(res.getString("nphone"));
                kemail.setText(res.getString("nemail"));
                kaddress.setText(res.getString("naddress"));
                username = res.getString("username");

                Glide.with(this)
                        .load(PROFILE_IMG_LINK +
                                res.getString("username") + "/" + res.getString("image"))
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .placeholder(R.drawable.bg_grey)
                        .error(R.drawable.bg_grey)
                        .into(profileImage);

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
        MyApplication.getInstance().addToRequestQueue(strReq, "profile_req");

    }

    private void updateProfile(String sfirstName, String slastName, String smiddleName, String sphone, String saddress,
                               String semail, String skfirstName, String sklastName, String skphone, String skaddress, String skemail, String image) {

        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.USER_UPDATE_PROFILE_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean status = jObj.getBoolean("status");

                // Check for error node in json
                if (status) {
                    Toast.makeText(getApplicationContext(), "Profile Updated!", Toast.LENGTH_LONG).show();
                    goBack();
                } else {
                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
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
                params.put("fname", sfirstName);
                params.put("middlename", smiddleName);
                params.put("lname", slastName);
                params.put("phone", sphone);
                params.put("address", saddress);
                params.put("nfname", skfirstName);
                params.put("nlname", sklastName);
                params.put("nemail", semail);
                params.put("nphone", skphone);
                params.put("naddress", skaddress);
                params.put("email", skemail);
                params.put("image", image);
                params.put("username", username);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "investment_req");

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

    private void showDialog() {
        Functions.showProgressDialog(ProfileActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(ProfileActivity.this);
    }
}