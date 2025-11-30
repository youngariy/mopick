package com.youngariy.mopick.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youngariy.mopick.R;

import java.util.List;

public class SlotMovieTitleAdapter extends RecyclerView.Adapter<SlotMovieTitleAdapter.ViewHolder> {

    private List<String> mTitles;

    public SlotMovieTitleAdapter(List<String> titles) {
        this.mTitles = titles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_slot_movie_title, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mTitles != null && position < mTitles.size()) {
            holder.titleTextView.setText(mTitles.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mTitles != null ? mTitles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_slot_title);
        }
    }
}

