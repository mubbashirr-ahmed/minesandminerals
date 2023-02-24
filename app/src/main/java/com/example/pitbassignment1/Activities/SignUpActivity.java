package com.example.pitbassignment1.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.pitbassignment1.Models.JsonData;
import com.example.pitbassignment1.databinding.ActivitySignUpBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    List<String> districts;
    ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inItViews();
    }

    private void inItViews() {
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSpinner();
        clickListeners();
    }
    private void setSpinner() {
        districts = new ArrayList<>();
        districts.add("Mianwali");
        districts.add("Khushab");
        districts.add("Lahore");
        districts.add("Rawalpindi");
        districts.add("Chakwal");
        districts.add("Wahary");
        districts.add("Sargodha");
        districts.add("Gujrat");
        districts.add("Layyah");
        districts.add("Islamabad");
        districts.add("Karachi");


        binding.spinner.setOnItemSelectedListener(SignUpActivity.this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                districts);
        ad.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item);
        binding.spinner.setAdapter(ad);
    }
    private void clickListeners() {
        binding.bSignUp.setOnClickListener(v -> verifyCreds());
    }
    private void verifyCreds() {
        if (binding.etCPassword.getText().toString().equals("") ||
                binding.etPassword.getText().toString().equals("") ||
                binding.etLID.getText().toString().equals("") ||
                binding.spinner.getSelectedItem().toString().equals("") ||
        binding.etMNO.getText().toString().equals("")) {
            Toast.makeText(this, "Please Fill all the details!", Toast.LENGTH_SHORT).show();
            return;
        }
        passwordValidation();
    }
    private void passwordValidation() {
        String password = binding.etPassword.getText().toString();
        String cPassword = binding.etCPassword.getText().toString();
        if (!password.equals(cPassword)) {
            Toast.makeText(this, "Password does not match!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (binding.etPassword.getText().length() >= 8) {
            Pattern letter = Pattern.compile("[a-zA-z]");
            Pattern digit = Pattern.compile("[0-9]");
            Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
            Matcher hasLetter = letter.matcher(password);
            Matcher hasDigit = digit.matcher(password);
            Matcher hasSpecial = special.matcher(password);
            boolean res = hasLetter.find() && hasDigit.find() && hasSpecial.find();
            if (!res) {
                binding.etPassword.setError("Password must contain at least one special character, one number, one alphabet and must be at least 8 characters long!");
                return ;
            }
            createAccount(password);
        }
        else {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();

        }
    }

    private void createAccount(String password) {
        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.bSignUp.setVisibility(View.GONE);
        String LID = binding.etLID.getText().toString();
        String mNO = binding.etMNO.getText().toString();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(mNO.split(",")));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("MinesData").child(LID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(SignUpActivity.this, "Account with this ID already exists!", Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.bSignUp.setVisibility(View.VISIBLE);
                    ref.removeEventListener(this);
                }
                else{
                    JsonData jsonData = new JsonData();
                    jsonData.setLID(LID);
                    jsonData.setPassword(password);
                    jsonData.setDistrict(binding.spinner.getSelectedItem().toString());
                    jsonData.setMineNumber(list);
                    FirebaseDatabase.getInstance().getReference().child("AllMinesData").child(LID).setValue(jsonData);
                    Toast.makeText(SignUpActivity.this, "Account Successfully Created!", Toast.LENGTH_SHORT).show();
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.bSignUp.setVisibility(View.VISIBLE);
                    finish();
                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressCircular.setVisibility(View.GONE);
                binding.bSignUp.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Select at least one district", Toast.LENGTH_SHORT).show();
    }
}