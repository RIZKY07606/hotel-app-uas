package com.example.hotelapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

    // Create UI elements for each booking including Call button and Payment button
    private void createBookingUI(String hotelName, String hotelAddress, String hotelPrice, String hotelImageUrl) {
        // Create a CardView container for each booking
        CardView cardView = new CardView(this);
        cardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        cardView.setRadius(16);  // Rounded corners
        cardView.setCardElevation(8);  // Shadow effect

        // Create the card's inner container
        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.HORIZONTAL);  // Change to horizontal orientation to add image on the right
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

        // Create a button container to hold Call Button and Payment Button side by side
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setPadding(0, 8, 0, 0);  // Padding above buttons
        textContainer.addView(buttonContainer);

        // Create the Call button
        Button callButton = new Button(this);
        callButton.setText("Call");
        callButton.setTextColor(getResources().getColor(android.R.color.white));  // Set text color to white
        callButton.setBackgroundColor(getResources().getColor(R.color.purple_500));  // Set background color to purple_500
        callButton.setPadding(32, 16, 32, 16);  // Add padding around the button
//        LinearLayout.LayoutParams callButtonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
//        callButtonParams.setMargins(4, 0, 8, 0);  // Left margin 4dp and right margin 8dp
//        callButton.setLayoutParams(callButtonParams);
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

        // Create the Pay via QRIS button
        Button paymentButton = new Button(this);
        paymentButton.setText("Pay via QRIS");
        paymentButton.setTextColor(getResources().getColor(android.R.color.white));  // Set text color to white
        paymentButton.setBackgroundColor(getResources().getColor(R.color.purple_500));  // Set background color to purple_500
        paymentButton.setPadding(32, 16, 32, 16);  // Add padding around the button
        LinearLayout.LayoutParams paymentButtonParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paymentButtonParams.setMargins(4, 0, 8, 0);  // Left margin 4dp and right margin 8dp
        paymentButton.setLayoutParams(paymentButtonParams);
        paymentButton.setOnClickListener(v -> {
            String paymentMessage = "Pembayaran untuk:\n"
                    + "Hotel: " + hotelName + "\n"
                    + "Alamat: " + hotelAddress + "\n"
                    + "Harga: " + hotelPrice;

            // Generate QR Code for QRIS payment
            Bitmap qrCode = generateQRCode(paymentMessage);

            // Generate random VA number
            String vaNumber = generateRandomVANumber();

            // Display QR Code and VA Number in a new dialog or activity
            showQRCodeDialog(qrCode, vaNumber);
        });

        // Add buttons to the button container
        buttonContainer.addView(callButton);
        buttonContainer.addView(paymentButton);

        // Add the CardView to the main container (bookingContainer)
        bookingContainer.addView(cardView);

        // Add image from Firestore (if available) to the right of the card
        ImageView imageView = new ImageView(this);
        if (hotelImageUrl != null && !hotelImageUrl.isEmpty()) {
            Picasso.get().load(hotelImageUrl).into(imageView);
        }

        // Set the size and margins for the image
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200); // 200x200px image size
        params.setMargins(16, 0, 0, 0);  // Add some margin between the text and image
        imageView.setLayoutParams(params);

        // Add imageView to the cardContent container
        cardContent.addView(imageView);

        // Add spacing between the cards
        LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
        cardParams.setMargins(0, 16, 0, 16); // Add margin of 16dp at top and bottom
        cardView.setLayoutParams(cardParams);
    }

    // Method to generate QR Code
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

    // Method to generate a random Virtual Account number
    private String generateRandomVANumber() {
        Random random = new Random();
        int vaNumber = random.nextInt(1000000000); // Generates a random number with 9 digits
        return String.format("VA%09d", vaNumber);  // Format to make sure the number is always 9 digits
    }

    // Method to show QR Code and VA Number in a dialog
    private void showQRCodeDialog(Bitmap qrCode, String vaNumber) {
        // You can use an ImageView and a TextView inside a dialog to display the generated QR Code and VA Number
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add the ImageView for the QR Code
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(qrCode);
        layout.addView(imageView);

        // Add the TextView for the Virtual Account number
        TextView vaTextView = new TextView(this);
        vaTextView.setText("Virtual Account: " + vaNumber);
        layout.addView(vaTextView);

        builder.setView(layout)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}
