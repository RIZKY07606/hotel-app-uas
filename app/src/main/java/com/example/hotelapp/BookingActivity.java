package com.example.hotelapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Random;

public class BookingActivity extends AppCompatActivity {

    private LinearLayout bookingContainer;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        bookingContainer = findViewById(R.id.bookingContainer);

        // Fetch bookings from Firestore
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
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchBookings() {
        FirebaseFirestore.getInstance().collection("bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasBookings = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String hotelName = document.getString("hotelName");
                            String hotelAddress = document.getString("hotelAddress");
                            String hotelPrice = document.getString("hotelPrice");
                            String hotelImageUrl = document.getString("hotelImageUrl");

                            // Create the call button and payment button for each booking
                            createBookingUI(hotelName, hotelAddress, hotelPrice, hotelImageUrl);
                            hasBookings = true;
                        }
                        if (!hasBookings) {
                            Toast.makeText(this, "No bookings found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Create UI elements for each booking including Call button, Payment button, and Delete button
    private void createBookingUI(String hotelName, String hotelAddress, String hotelPrice, String hotelImageUrl) {
        // Create a CardView container for each booking
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 16, 16, 16);  // Margin di luar card
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16);  // Rounded corners
        cardView.setCardElevation(8);  // Shadow effect

        // Create the card's inner container
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.HORIZONTAL);  // Horizontal orientation
        cardContent.setPadding(16, 16, 16, 16);  // Padding inside the card
        cardView.addView(cardContent);

        // Add booking details (TextView) inside the card
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        cardContent.addView(textContainer);

        TextView bookingDetails = new TextView(this);
        bookingDetails.setText("Hotel: " + hotelName + "\nAddress: " + hotelAddress + "\nPrice: " + hotelPrice);
        bookingDetails.setTextSize(16);
        bookingDetails.setPadding(0, 0, 0, 16);  // Padding at the bottom
        textContainer.addView(bookingDetails);

        // Create a button container to hold Call Button, Payment Button, and Delete Button
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setPadding(0, 8, 0, 0);  // Padding above buttons
        textContainer.addView(buttonContainer);

        // Call button
        Button callButton = new Button(this);
        callButton.setText("Call");
        callButton.setBackgroundColor(getResources().getColor(R.color.purple_500));
        callButton.setTextColor(getResources().getColor(R.color.white));
        LinearLayout.LayoutParams callButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        callButtonParams.setMargins(8, 0, 8, 0);  // Margin antar-tombol
        callButton.setLayoutParams(callButtonParams);
        callButton.setOnClickListener(v -> {
            String message = "Saya ingin pesan:\n"
                    + "Hotel: " + hotelName + "\n"
                    + "Alamat: " + hotelAddress + "\n"
                    + "Harga: " + hotelPrice;
            String phoneNumber = "6281554133818";
            String whatsappUrl = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)));
        });
        buttonContainer.addView(callButton);

        // Pay via QRIS button
        Button paymentButton = new Button(this);
        paymentButton.setText("Pay via QRIS");
        paymentButton.setBackgroundColor(getResources().getColor(R.color.purple_500));
        paymentButton.setTextColor(getResources().getColor(R.color.white));
        LinearLayout.LayoutParams paymentButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        paymentButtonParams.setMargins(8, 0, 8, 0);  // Margin antar-tombol
        paymentButton.setLayoutParams(paymentButtonParams);
        paymentButton.setOnClickListener(v -> {
            Bitmap qrCode = generateQRCode("Pembayaran untuk:\nHotel: " + hotelName + "\nAlamat: " + hotelAddress + "\nHarga: " + hotelPrice);
            String vaNumber = generateRandomVANumber();
            showQRCodeDialog(qrCode, vaNumber);
        });
        buttonContainer.addView(paymentButton);

        // Delete button
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        deleteButton.setTextColor(getResources().getColor(R.color.white));
        LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        deleteButtonParams.setMargins(8, 0, 8, 0);  // Margin antar-tombol
        deleteButton.setLayoutParams(deleteButtonParams);
        deleteButton.setOnClickListener(v -> deleteBooking(hotelName, cardView));
        buttonContainer.addView(deleteButton);

        // Add image from Firestore (if available)
        ImageView imageView = new ImageView(this);
        if (hotelImageUrl != null && !hotelImageUrl.isEmpty()) {
            Picasso.get().load(hotelImageUrl).into(imageView);
        }
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 200);
        imageParams.setMargins(16, 0, 0, 0);  // Margin pada gambar
        imageView.setLayoutParams(imageParams);
        cardContent.addView(imageView);

        // Add CardView to the container
        bookingContainer.addView(cardView);
    }

    private Bitmap generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 500, 500);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateRandomVANumber() {
        Random random = new Random();
        int vaNumber = random.nextInt(1000000000);
        return String.format("VA%09d", vaNumber);
    }

    private void showQRCodeDialog(Bitmap qrCode, String vaNumber) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(qrCode);
        layout.addView(imageView);

        TextView vaTextView = new TextView(this);
        vaTextView.setText("Virtual Account: " + vaNumber);
        layout.addView(vaTextView);

        builder.setView(layout)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteBooking(String hotelName, CardView cardView) {
        FirebaseFirestore.getInstance().collection("bookings")
                .whereEqualTo("hotelName", hotelName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FirebaseFirestore.getInstance().collection("bookings")
                                    .document(document.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        bookingContainer.removeView(cardView);
                                        Toast.makeText(this, "Booking deleted successfully", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "No matching booking found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to query bookings", Toast.LENGTH_SHORT).show();
                });
    }
}
