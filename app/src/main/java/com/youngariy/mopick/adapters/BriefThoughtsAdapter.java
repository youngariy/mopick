package com.youngariy.mopick.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youngariy.mopick.R;
import com.youngariy.mopick.utils.BriefThought;

import java.util.ArrayList;
import java.util.List;

public class BriefThoughtsAdapter extends RecyclerView.Adapter<BriefThoughtsAdapter.ViewHolder> {

    private List<BriefThought> mThoughts;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(BriefThought thought, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public BriefThoughtsAdapter(android.content.Context context) {
        mThoughts = new ArrayList<>();
    }

    public void setThoughts(List<BriefThought> thoughts) {
        mThoughts = thoughts != null ? thoughts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brief_thought, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BriefThought thought = mThoughts.get(position);
        
        holder.titleTextView.setText(thought.getContentTitle() != null ? thought.getContentTitle() : "");
        
        // Status
        String status = thought.getStatus();
        if (status == null) status = "watching";
        String statusText = status.equals("watching") 
                ? holder.itemView.getContext().getString(R.string.status_watching)
                : holder.itemView.getContext().getString(R.string.status_completed);
        holder.statusTextView.setText(statusText);
        
        // Rating
        if (thought.getRating() > 0) {
            StringBuilder ratingText = new StringBuilder();
            for (int i = 0; i < thought.getRating(); i++) {
                ratingText.append("⭐");
            }
            holder.ratingTextView.setText(ratingText.toString());
        } else {
            holder.ratingTextView.setText("");
        }
        
        // Moods - show next to title
        holder.moodsTitleLayout.removeAllViews();
        if (thought.getMoods() != null && !thought.getMoods().isEmpty()) {
            for (String mood : thought.getMoods()) {
                TextView moodTextView = new TextView(holder.itemView.getContext());
                moodTextView.setText(mood);
                moodTextView.setTextSize(20);
                moodTextView.setPadding(4, 0, 0, 0);
                holder.moodsTitleLayout.addView(moodTextView);
            }
        }
        
        // Recommend
        String recommend = thought.getRecommend();
        if (recommend == null) recommend = "no";
        String recommendText = recommend.equals("yes")
                ? holder.itemView.getContext().getString(R.string.recommend_yes_message)
                : holder.itemView.getContext().getString(R.string.recommend_no_message);
        holder.recommendTextView.setText(recommendText);
        
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(thought, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mThoughts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView statusTextView;
        TextView ratingTextView;
        LinearLayout moodsTitleLayout;
        TextView recommendTextView;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_content_title);
            statusTextView = itemView.findViewById(R.id.text_view_status);
            ratingTextView = itemView.findViewById(R.id.text_view_rating);
            moodsTitleLayout = itemView.findViewById(R.id.layout_moods_title);
            recommendTextView = itemView.findViewById(R.id.text_view_recommend);
        }
    }
}

