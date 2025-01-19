package com.example.hotelapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BookingActivity extends AppCompatActivity {

    private TextView bookingInfo;
    private LinearLayout bookingContainer;  // Reference to the container layout
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        bookingInfo = findViewById(R.id.bookingInfo);
        bookingContainer = findViewById(R.id.bookingContainer);  // Initialize bookingContainer

        fetchBookings();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new BookFragment());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                loadFragment(new HomeFragment());
                startActivity(new Intent(BookingActivity.this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.book) {
                loadFragment(new BookFragment());
                startActivity(new Intent(BookingActivity.this, BookingActivity.class));
                return true;
            } else if (item.getItemId() == R.id.profile) {
                loadFragment(new ProfileFragment());
                startActivity(new Intent(BookingActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);  // Replace fragment in container
        transaction.addToBackStack(null); // Add fragment to back stack
        transaction.commit();
    }

    private void fetchBookings() {
        FirebaseFirestore.getInstance().collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String hotelName = document.getString("hotelName");
                            String hotelAddress = document.getString("hotelAddress");
                            String hotelPrice = document.getString("hotelPrice");

                            // Create the call button for each booking
                            createCallButton(hotelName, hotelAddress, hotelPrice);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Create call button for each booking
    private void createCallButton(String hotelName, String hotelAddress, String hotelPrice) {
        // Create the button
        Button callButton = new Button(this);
        callButton.setText("Call");
        callButton.setOnClickListener(v -> {
            String message = "Saya ingin pesan:\n"
                    + "Hotel: " + hotelName + "\n"
                    + "Alamat: " + hotelAddress + "\n"
                    + "Harga: " + hotelPrice;

            String phoneNumber = "6281554133818";  // Replace with the correct phone number
            String encodedMessage = Uri.encode(message);

            // Construct WhatsApp URL
            String whatsappUrl = "https://wa.me/" + phoneNumber + "?text=" + encodedMessage;

            // Directly open the URL in the browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl));
            startActivity(intent);
        });

        // Create a container to hold the TextView and Button
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add booking details TextView and call button to the container
        TextView bookingDetails = new TextView(this);
        bookingDetails.setText("Hotel: " + hotelName + "\nAddress: " + hotelAddress + "\nPrice: " + hotelPrice);
        container.addView(bookingDetails);
        container.addView(callButton);

        // Add the container to the main layout (bookingContainer)
        bookingContainer.addView(container);
    }
}
