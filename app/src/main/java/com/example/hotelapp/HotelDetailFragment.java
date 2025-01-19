package com.example.hotelapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.transition.Slide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HotelDetailFragment extends DialogFragment {
    private TextView hotelName, hotelDescription, hotelAddress, hotelPrice;
    private Button bookButton;
    private ImageButton closeButton; // Close button

    private String hotelId;
    private String hotelNameString;
    private String hotelAddressString;
    private String hotelPriceString;

    public static HotelDetailFragment newInstance(String hotelId) {
        HotelDetailFragment fragment = new HotelDetailFragment();
        Bundle args = new Bundle();
        args.putString("hotelId", hotelId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hotel_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hotelName = view.findViewById(R.id.hotelName);
        hotelDescription = view.findViewById(R.id.hotelDescription);
        hotelAddress = view.findViewById(R.id.hotelAddress);
        hotelPrice = view.findViewById(R.id.hotelPrice);
        bookButton = view.findViewById(R.id.bookButton);
        closeButton = view.findViewById(R.id.closeButton); // Close button

        if (getArguments() != null) {
            hotelId = getArguments().getString("hotelId");
            fetchHotelDetails();
        }

        // Close button click listener
        closeButton.setOnClickListener(v -> dismiss());

        // Book button click listener
        bookButton.setOnClickListener(v -> saveBookingAndNavigate());

        // Set the enter transition to slide from bottom
        Slide slide = new Slide(Gravity.BOTTOM);
        slide.setDuration(500); // Set custom duration for the slide animation
        slide.setInterpolator(new AccelerateDecelerateInterpolator()); // Add an interpolator for smoother transition
        setEnterTransition(slide);
        setExitTransition(new android.transition.Fade());
    }

    private void fetchHotelDetails() {
        FirebaseFirestore.getInstance().collection("hotels").document(hotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Map<String, Object> data = task.getResult().getData();
                        if (data != null) {
                            hotelNameString = (String) data.get("name");
                            hotelAddressString = (String) data.get("address");
                            hotelPriceString = (String) data.get("price");

                            hotelName.setText(hotelNameString);
                            hotelDescription.setText((String) data.get("description"));
                            hotelAddress.setText(hotelAddressString);
                            hotelPrice.setText(hotelPriceString);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to load hotel details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveBookingAndNavigate() {
        if (hotelNameString == null || hotelAddressString == null || hotelPriceString == null) {
            Toast.makeText(getContext(), "Hotel data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save booking to Firestore
        Map<String, Object> booking = new HashMap<>();
        booking.put("hotelId", hotelId);
        booking.put("hotelName", hotelNameString);
        booking.put("hotelAddress", hotelAddressString);
        booking.put("hotelPrice", hotelPriceString);

        FirebaseFirestore.getInstance().collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Booking saved", Toast.LENGTH_SHORT).show();

                    // Navigate to BookingActivity
                    Intent bookingIntent = new Intent(getActivity(), BookingActivity.class);
                    startActivity(bookingIntent);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save booking", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getTheme() {
        return R.style.DialogFragmentStyle;
    }
}
