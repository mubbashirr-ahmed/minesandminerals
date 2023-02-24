package com.example.pitbassignment1.Activities;

import static com.example.pitbassignment1.Activities.LoginActivity.PREFS_DISTRICT;
import static com.example.pitbassignment1.Activities.LoginActivity.PREFS_ID;
import static com.example.pitbassignment1.Activities.LoginActivity.PREFS_NAME;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pitbassignment1.Models.JsonData;
import com.example.pitbassignment1.R;
import com.example.pitbassignment1.databinding.ActivityAddRecordBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class AddRecordActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityAddRecordBinding binding;
    private static final int CAMERA_REQUEST_CODE = 1;
    private ImageView front, back;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inIT();

    }

    private void inIT() {
        setViews();
        getPrefs();
        getData();
        setMineralSpinner();
        clickListeners();
    }
    private void getData() {
        ArrayList<String> mines = new ArrayList<>();
        String dis = binding.etdistrict.getText().toString();
        Query query = FirebaseDatabase.getInstance().getReference().child("AllMinesData").orderByChild("district").equalTo(dis);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot data : snapshot.getChildren()) {
                        JsonData jd = data.getValue(JsonData.class);
                        assert jd != null;
                        if(jd.getMineNumber()!=null){
                            mines.addAll(jd.getMineNumber());
                        }

                    }
                    setMineSpinner(mines);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setViews() {
        binding = ActivityAddRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        front = binding.ivFront;
        back = binding.ivBacks;
    }
    private void getPrefs() {
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        binding.etLeseID.setText(preferences.getString(PREFS_ID, "null"));
        binding.etdistrict.setText(preferences.getString(PREFS_DISTRICT, "null"));
    }
    private void setMineralSpinner() {
        ArrayList<String> minerals = new ArrayList<>();
        minerals.add("Coal");
        minerals.add("Gypsum");
        minerals.add("Limestone");
        minerals.add("Ocher");
        minerals.add("Argillaceous clay");
        minerals.add("Fire Clay");
        minerals.add("Iron Ore");
        binding.spinnerMineral.setOnItemSelectedListener(AddRecordActivity.this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                minerals);
        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        binding.spinnerMineral.setAdapter(ad);
    }
    private void setMineSpinner(ArrayList<String> mines) {

        HashSet<String> set = new HashSet<>(mines);
        mines.clear();
        mines.addAll(set);

        binding.spMineNumber.setOnItemSelectedListener(AddRecordActivity.this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                mines);
        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        binding.spMineNumber.setAdapter(ad);
    }
    private void clickListeners() {
        binding.capturePhoto.setOnClickListener(v -> getPhoto());
        binding.bLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        });
        binding.bSubmit.setOnClickListener(v -> submitData());
    }
    private void submitData() {
        String mType = binding.spinnerMineral.getSelectedItem().toString();
        String mNo = binding.spMineNumber.getSelectedItem().toString();
        String vName = binding.etVehicleID.getText().toString();
        String qty = binding.etQuantity.getText().toString();

        if (mType.isEmpty() ||
                mNo.isEmpty() ||
                vName.isEmpty() ||
                qty.isEmpty()) {
            Toast.makeText(this, "Please fill all the details!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (front.getDrawable() == null || back.getDrawable() == null) {
            binding.message.setTextColor(ContextCompat.getColor(AddRecordActivity.this, R.color.red));
            return;
        }
        putData(mType, mNo, vName, qty);
    }
    private void putData(String mType, String mNo, String vName, String qty) {
        visible();
        String loc = getCurrentLocation();
        if (loc == null) {
            Toast.makeText(this, "Unable to get Location", Toast.LENGTH_SHORT).show();
            visibilityGone();
            return;
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        JsonData jsonData = new JsonData();

        jsonData.setQty(qty);
        jsonData.setmNo(mNo);
        jsonData.setMineralType(mType);
        jsonData.setVehicleNumber(vName);
        jsonData.settStamp(timeStamp);
        jsonData.setLocation(loc);

        FirebaseDatabase.getInstance().getReference().child("AllMinesData")
                .child(binding.etLeseID.getText().toString())
                .child(timeStamp).setValue(jsonData);

        uploadFile();
        visibilityGone();
        Snackbar.make(findViewById(android.R.id.content), "Data Uploaded Successfully", Snackbar.LENGTH_LONG).show();
    }
    private String getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                return latitude + ", " + longitude;
            }
        }
        return null;
    }
    private void getPhoto() {
        binding.message.setTextColor(ContextCompat.getColor(AddRecordActivity.this, R.color.black));
        dispatchTakePictureIntent();
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (binding.ivFront.getDrawable() == null) {
                binding.ivFront.setImageBitmap(imageBitmap);
            } else {
                binding.ivBacks.setImageBitmap(imageBitmap);
            }
        }
    }
    private void uploadFile() {
        front.setDrawingCacheEnabled(true);
        back.setDrawingCacheEnabled(true);
        front.buildDrawingCache();
        back.buildDrawingCache();

        ArrayList<ImageView> iv = new ArrayList<>();
        iv.add(front);
        iv.add(back);
        Bitmap fBitmap;
        for (int i = 0; i < iv.size(); i++) {
            ImageView ivU = iv.get(i);
            fBitmap = ((BitmapDrawable) ivU.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataF = baos.toByteArray();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String iName = binding.etLeseID.getText().toString() + timeStamp + ".jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("AllImages");
            StorageReference imageRef = storageRef.child(iName);
            UploadTask uploadTask = imageRef.putBytes(dataF);
            uploadTask.addOnSuccessListener(taskSnapshot -> Toast.makeText(AddRecordActivity.this, "Upload successful", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> visibilityGone());
        }
    }
    private void visibilityGone() {
        binding.progressCircular.setVisibility(View.GONE);
        binding.bSubmit.setVisibility(View.VISIBLE);
    }
    private void visible() {
        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.bSubmit.setVisibility(View.GONE);
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}