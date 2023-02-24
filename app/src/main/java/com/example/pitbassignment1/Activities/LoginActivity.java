package com.example.pitbassignment1.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pitbassignment1.Models.JsonData;
import com.example.pitbassignment1.R;
import com.example.pitbassignment1.databinding.ActivityLoginBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inIt();
    }

    private void inIt() {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = getSharedPreferences("MYPREFS", MODE_PRIVATE);
        boolean key = preferences.getBoolean("loginKey", false);

        if (key) {
            finish();
            startActivity(new Intent(this, AddRecordActivity.class));
            return;
        }
        clickListners();

    }

    private void clickListners() {
        binding.tvRegister.setOnClickListener(c -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
        binding.bLogin.setOnClickListener(c -> {
            verifyUser();
        });
    }

    private void verifyUser() {
        String Id = binding.etUserName.getText().toString();
        String password = binding.etPassword.getText().toString().trim();

        if (Id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all the details!", Toast.LENGTH_SHORT).show();
            return;
        }
        getdata(Id, password);
    }

    private void getdata(String Id, String password) {
        binding.bLogin.setVisibility(View.GONE);
        binding.progressCircular.setVisibility(View.VISIBLE);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AllMinesData").child(Id);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    JsonData data = snapshot.getValue(JsonData.class);
                    String pass = data.getPassword();
                    if (!pass.equals(password)) {
                        Toast.makeText(LoginActivity.this, "In correct password", Toast.LENGTH_SHORT).show();
                        removeVisible();
                        return;
                    }
                    savePrefs(data);

                } else {
                    Toast.makeText(LoginActivity.this, "No such user found", Toast.LENGTH_SHORT).show();
                    removeVisible();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Some Internal Errors. Please Try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeVisible() {
        binding.bLogin.setVisibility(View.VISIBLE);
        binding.progressCircular.setVisibility(View.GONE);
    }

    private void savePrefs(JsonData data) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("loginKey", true);
        editor.putString("ID", data.getLID());
        editor.putString("district", data.getDistrict());
        editor.putString("password", data.getPassword());
        editor.apply();
        binding.bLogin.setVisibility(View.VISIBLE);
        binding.progressCircular.setVisibility(View.GONE);
        finish();
        startActivity(new Intent(this, AddRecordActivity.class));
    }
}