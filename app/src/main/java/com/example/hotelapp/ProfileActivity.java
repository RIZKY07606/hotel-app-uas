package com.example.hotelapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView emailTextView;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Menghubungkan UI dengan elemen di layout
        profileImage = findViewById(R.id.profileImage);
        emailTextView = findViewById(R.id.emailTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Mengambil data pengguna (misalnya, email) dan menampilkan foto profil
        String userEmail = mAuth.getCurrentUser().getEmail();
        emailTextView.setText(userEmail);

        // Menangani aksi logout
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            // Redirect ke halaman login setelah logout
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Menutup ProfileActivity dan kembali ke LoginActivity

        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new HomeFragment());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                loadFragment(new HomeFragment());
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.book) {
                loadFragment(new BookFragment());
                startActivity(new Intent(ProfileActivity.this, BookingActivity.class));
                return true;
            } else if (item.getItemId() == R.id.profile) {
                loadFragment(new ProfileFragment());
                startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);  // Ganti fragment di container
        transaction.addToBackStack(null); // Menambahkan fragment ke back stack
        transaction.commit();
    }
}
