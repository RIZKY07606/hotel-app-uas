package com.example.hotelapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView hotelRecyclerView;
    private HotelAdapter hotelAdapter;
    private BottomNavigationView bottomNavigationView;
    private List<DocumentSnapshot> hotelList = new ArrayList<>();  // All hotels
    private List<DocumentSnapshot> filteredHotelList = new ArrayList<>();  // Filtered hotels based on search query

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        hotelRecyclerView = findViewById(R.id.hotelRecyclerView);
        hotelRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch hotel data from Firestore
        FirebaseFirestore.getInstance().collection("hotels")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        hotelList = querySnapshot.getDocuments();  // Get data from Firestore
                        filteredHotelList.addAll(hotelList);  // Initialize filtered list

                        // Only initialize the adapter here, once the data is loaded
                        hotelAdapter = new HotelAdapter(filteredHotelList, this);
                        hotelRecyclerView.setAdapter(hotelAdapter);  // Set adapter to RecyclerView

                        // Notify the adapter that the data has changed
                        hotelAdapter.notifyDataSetChanged();
                    }
                });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadFragment(new HomeFragment());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                loadFragment(new HomeFragment());
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                return true;
            } else if (item.getItemId() == R.id.book) {
                loadFragment(new BookFragment());
                startActivity(new Intent(HomeActivity.this, BookingActivity.class));
                return true;
            } else if (item.getItemId() == R.id.profile) {
                loadFragment(new ProfileFragment());
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Set up search functionality
        EditText searchHotel = findViewById(R.id.searchHotel);
        searchHotel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterHotels(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // Navigate to HotelDetailFragment when a hotel is clicked
    public void navigateToHotelDetail(String hotelId) {
        HotelDetailFragment fragment = HotelDetailFragment.newInstance(hotelId);
        fragment.show(getSupportFragmentManager(), "HotelDetailFragment"); // Show fragment as dialog
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);  // Ganti fragment di container
        transaction.addToBackStack(null); // Menambahkan fragment ke back stack
        transaction.commit();
    }

    private void filterHotels(String query) {
        filteredHotelList.clear();
        if (query.isEmpty()) {
            filteredHotelList.addAll(hotelList);
        } else {
            for (DocumentSnapshot hotel : hotelList) {
                String hotelName = hotel.getString("name");
                if (hotelName != null && hotelName.toLowerCase().contains(query.toLowerCase())) {
                    filteredHotelList.add(hotel);
                }
            }
        }

        Log.d("HotelFilter", "Filtered hotel list size: " + filteredHotelList.size());  // Add log
        hotelAdapter.notifyDataSetChanged();
    }
}
