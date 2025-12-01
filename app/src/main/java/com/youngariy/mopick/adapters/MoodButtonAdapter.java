package com.youngariy.mopick.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.youngariy.mopick.R;
import com.youngariy.mopick.activities.BriefThoughtsEditActivity;

import java.util.ArrayList;
import java.util.List;

public class MoodButtonAdapter extends RecyclerView.Adapter<MoodButtonAdapter.ViewHolder> {

    private List<String> mMoodStrings;
    private List<String> mMoodEmojis;
    private List<String> mSelectedMoods;
    private OnMoodClickListener mListener;
    private boolean mReadOnly = false;

    public interface OnMoodClickListener {
        void onMoodClick(String moodEmoji, int position);
    }

    public void setOnMoodClickListener(OnMoodClickListener listener) {
        mListener = listener;
    }

    public MoodButtonAdapter(BriefThoughtsEditActivity activity, List<String> moodStrings, List<String> moodEmojis, List<String> selectedMoods) {
        mMoodStrings = moodStrings;
        mMoodEmojis = moodEmojis;
        mSelectedMoods = selectedMoods != null ? selectedMoods : new ArrayList<>();
    }

    public void setSelectedMoods(List<String> selectedMoods) {
        mSelectedMoods = selectedMoods != null ? selectedMoods : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setReadOnly(boolean readOnly) {
        mReadOnly = readOnly;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialButton button = new MaterialButton(parent.getContext());
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // Convert 20dp to pixels for wider spacing
        float marginDp = 20f;
        float marginPx = marginDp * parent.getContext().getResources().getDisplayMetrics().density;
        params.setMargins(0, 0, (int) marginPx, 0);
        button.setLayoutParams(params);
        button.setPadding(16, 8, 16, 8);
        button.setMinWidth(0);
        button.setMinHeight(0);
        float cornerRadiusDp = 24f;
        float cornerRadiusPx = cornerRadiusDp * parent.getContext().getResources().getDisplayMetrics().density;
        button.setCornerRadius((int) cornerRadiusPx);
        return new ViewHolder(button);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.button.setText(mMoodStrings.get(position));
        
        boolean isSelected = mSelectedMoods.contains(mMoodEmojis.get(position));
        if (isSelected) {
            holder.button.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.colorPrimary));
            holder.button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary)));
            holder.button.setStrokeWidth(0);
            holder.button.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        } else {
            holder.button.setBackgroundTintList(null);
            holder.button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorMovieDetailTextPrimary)));
            holder.button.setStrokeWidth(holder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
            holder.button.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorMovieDetailTextPrimary));
        }
        
        holder.button.setEnabled(!mReadOnly);
        holder.button.setOnClickListener(v -> {
            if (!mReadOnly && mListener != null) {
                mListener.onMoodClick(mMoodEmojis.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMoodStrings.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialButton button;

        ViewHolder(MaterialButton itemView) {
            super(itemView);
            button = itemView;
        }
    }
}

