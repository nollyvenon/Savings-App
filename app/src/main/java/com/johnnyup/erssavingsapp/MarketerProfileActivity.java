package com.johnnyup.erssavingsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.johnnyup.erssavingsapp.helper.DatabaseHandler;
import com.johnnyup.erssavingsapp.helper.Functions;

import org.json.JSONException;
import org.json.JSONObject;
import org.snowcorp.login.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.johnnyup.erssavingsapp.helper.Functions.MARKETER_PROFILE_IMG_LINK;

public class MarketerProfileActivity extends AppCompatActivity {

    Bitmap bitmap;
    private static final int REQUEST_CODE_GALLERY = 999;
    Button uploadImage;
    CircleImageView profileImage;
    EditText firstName, lastName, username, phone, email;
    private HashMap<String, String> user = new HashMap<>();
    String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketer_profile);

        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        username = findViewById(R.id.username);
        phone = findViewById(R.id.phone_number);
        email = findViewById(R.id.email);
        uploadImage = findViewById(R.id.upload_btn);
        profileImage = findViewById(R.id.profile_image);

        uploadImage.setOnClickListener(v -> ActivityCompat.requestPermissions(MarketerProfileActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_GALLERY));

        DatabaseHandler db = new DatabaseHandler(MarketerProfileActivity.this);
        user = db.getUserDetails();
        userType = user.get("type");

        getProfile(user.get("uid"));

        findViewById(R.id.update_profile).setOnClickListener(v -> {

            String sfirstName = firstName.getText().toString();
            String slastName = lastName.getText().toString();
            String susername = username.getText().toString();
            String sphone = phone.getText().toString();
            String semail = email.getText().toString();
            updateProfile(sfirstName, slastName, susername, sphone, semail, imageToString());
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
                Functions.getUrl(Functions.MARKETER_PROFILE_URL, getApplicationContext()), response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                JSONObject res = jObj.getJSONObject("marketer");

                firstName.setText(res.getString("fname"));
                lastName.setText(res.getString("lname"));
                username.setText(res.getString("username"));
                email.setText(res.getString("em"));
                phone.setText(res.getString("phone"));

                Glide.with(this)
                        .load(MARKETER_PROFILE_IMG_LINK + res.getString("image"))
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
        MyApplication.getInstance().addToRequestQueue(strReq, "marketer_profile_req");

    }

    private void updateProfile(String sfirstName, String slastName, String susername, String sphone, String semail, String image) {

        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.getUrl(Functions.MARKETER_UPDATE_PROFILE_URL, getApplicationContext()), response -> {
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
                params.put("user", user.get("uid"));
                params.put("fname", sfirstName);
                params.put("username", susername);
                params.put("lname", slastName);
                params.put("phone", sphone);
                params.put("email", semail);
                params.put("image", image);
                return params;
            }
        };

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, "up_marketer_profile_req");

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
        Functions.showProgressDialog(MarketerProfileActivity.this, "Please wait...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(MarketerProfileActivity.this);
    }
}