package com.example.hotelapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private List<DocumentSnapshot> hotelList;
    private Context context;

    public HotelAdapter(List<DocumentSnapshot> hotelList, Context context) {
        this.hotelList = hotelList;
        this.context = context;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.hotel_item, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        DocumentSnapshot document = hotelList.get(position);

        String hotelId = document.getId(); // Get hotel ID for detail
        String hotelName = document.getString("name");
        String hotelAddress = document.getString("address");
        String hotelPrice = document.getString("price");
        String hotelImage = document.getString("imageUrl");
        float hotelRating = document.getDouble("rating") != null ? document.getDouble("rating").floatValue() : 0f;

        holder.hotelName.setText(hotelName);
        holder.hotelAddress.setText(hotelAddress);
        holder.hotelPrice.setText(hotelPrice);
        holder.hotelRating.setRating(hotelRating);

        Glide.with(context).load(hotelImage).into(holder.hotelImage);

        // Set click listener to show hotel details
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof HomeActivity) {
                ((HomeActivity) context).navigateToHotelDetail(hotelId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        TextView hotelName, hotelAddress, hotelPrice;
        RatingBar hotelRating;
        ImageView hotelImage;

        public HotelViewHolder(View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotelImage);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelAddress = itemView.findViewById(R.id.hotelAddress);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            hotelRating = itemView.findViewById(R.id.hotelRating);
        }
    }
}
