package com.youngariy.mopick.activities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.ColorStateList;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.SearchResultsAdapter;
import com.youngariy.mopick.network.search.SearchAsyncTaskLoader;
import com.youngariy.mopick.network.search.SearchResponse;
import com.youngariy.mopick.network.search.SearchResult;
import com.youngariy.mopick.adapters.MoodButtonAdapter;
import com.youngariy.mopick.utils.BriefThought;
import com.youngariy.mopick.utils.BriefThoughtsHelper;
import com.youngariy.mopick.utils.Constants;
import com.youngariy.mopick.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

public class BriefThoughtsEditActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    private static final String[] MOOD_EMOJIS = {"😊", "😢", "😍", "😮", "😂", "🤔", "😐", "😴", "😨", "🌿"};
    private static final String[] MOOD_KEYS = {"happy", "touched", "excited", "surprised", "funny", "thoughtful", "okay", "boring", "tense", "calm"};

    private EditText mContentTitleEditText;
    private TextView mContentTitleReadonlyTextView;
    private MaterialButton mSearchContentButton;
    private MaterialButton mStatusWatchingButton;
    private MaterialButton mStatusCompletedButton;
    private TextView mStatusReadonlyTextView;
    private LinearLayout mStarsLayout;
    private TextView mRatingReadonlyTextView;
    private RecyclerView mMoodsRecyclerView;
    private TextView mMoodsReadonlyTextView;
    private MaterialButton mRecommendYesButton;
    private MaterialButton mRecommendNoButton;
    private TextView mRecommendReadonlyTextView;
    private MaterialButton mSaveButton;
    private MaterialButton mEditButton;
    private MaterialButton mDeleteButton;

    private int mThoughtId = -1;
    private int mContentId = -1;
    private String mContentType = "";
    private String mSelectedStatus = "watching";
    private int mSelectedRating = 0;
    private List<String> mSelectedMoods = new ArrayList<>();
    private String mSelectedRecommend = "no";

    private TextView[] mStarTextViews = new TextView[5];
    private MoodButtonAdapter mMoodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief_thoughts_edit);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mThoughtId = intent.getIntExtra("thought_id", -1);
        mContentId = intent.getIntExtra("content_id", -1);
        mContentType = intent.getStringExtra("content_type");
        String contentTitle = intent.getStringExtra("content_title");

        initViews();
        setupStars();
        setupMoods();
        initClickListeners();
        
        if (mThoughtId > 0) {
            setTitle(R.string.edit_thought);
            loadThought();
            // After loading, set read-only mode
            setReadOnlyMode(true);
        } else {
            setTitle(R.string.new_thought);
            if (contentTitle != null && !contentTitle.isEmpty()) {
                mContentTitleEditText.setText(contentTitle);
                if (mContentId > 0 && mContentType != null) {
                    mContentTitleEditText.setEnabled(false);
                }
            }
        }
    }

    private void initViews() {
        mContentTitleEditText = findViewById(R.id.edit_text_content_title);
        mContentTitleReadonlyTextView = findViewById(R.id.text_view_content_title_readonly);
        mSearchContentButton = findViewById(R.id.button_search_content);
        mStatusWatchingButton = findViewById(R.id.button_status_watching);
        mStatusCompletedButton = findViewById(R.id.button_status_completed);
        mStatusReadonlyTextView = findViewById(R.id.text_view_status_readonly);
        mStarsLayout = findViewById(R.id.layout_stars);
        mRatingReadonlyTextView = findViewById(R.id.text_view_rating_readonly);
        mMoodsRecyclerView = findViewById(R.id.recycler_view_moods);
        mMoodsReadonlyTextView = findViewById(R.id.text_view_moods_readonly);
        mRecommendYesButton = findViewById(R.id.button_recommend_yes);
        mRecommendNoButton = findViewById(R.id.button_recommend_no);
        mRecommendReadonlyTextView = findViewById(R.id.text_view_recommend_readonly);
        mSaveButton = findViewById(R.id.button_save);
        mEditButton = findViewById(R.id.button_edit);
        mDeleteButton = findViewById(R.id.button_delete);
    }
    
    private void setReadOnlyMode(boolean readOnly) {
        // Keep question labels visible in read-only mode
        TextView contentTitleLabel = findViewById(R.id.text_view_content_title_label);
        TextView statusLabel = findViewById(R.id.text_view_status_label);
        TextView ratingLabel = findViewById(R.id.text_view_rating_label);
        TextView moodsLabel = findViewById(R.id.text_view_moods_label);
        TextView recommendLabel = findViewById(R.id.text_view_recommend_label);
        
        // Question labels should always be visible
        if (contentTitleLabel != null) contentTitleLabel.setVisibility(View.VISIBLE);
        if (statusLabel != null) statusLabel.setVisibility(View.VISIBLE);
        if (ratingLabel != null) ratingLabel.setVisibility(View.VISIBLE);
        if (moodsLabel != null) moodsLabel.setVisibility(View.VISIBLE);
        if (recommendLabel != null) recommendLabel.setVisibility(View.VISIBLE);
        
        // Content Title
        mContentTitleEditText.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mContentTitleReadonlyTextView.setVisibility(readOnly ? View.VISIBLE : View.GONE);
        if (readOnly) {
            mContentTitleReadonlyTextView.setText(mContentTitleEditText.getText());
        }
        mSearchContentButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        
        // Status
        mStatusWatchingButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mStatusCompletedButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mStatusReadonlyTextView.setVisibility(readOnly ? View.VISIBLE : View.GONE);
        if (readOnly) {
            String statusText = mSelectedStatus.equals("watching") 
                    ? getString(R.string.status_watching_text)
                    : getString(R.string.status_completed_text);
            mStatusReadonlyTextView.setText(statusText);
        }
        
        // Rating
        mStarsLayout.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mRatingReadonlyTextView.setVisibility(readOnly ? View.VISIBLE : View.GONE);
        if (readOnly) {
            String ratingText = String.format(getString(R.string.rating_text_format), mSelectedRating);
            mRatingReadonlyTextView.setText(ratingText);
        }
        
        // Disable star clicks
        for (TextView star : mStarTextViews) {
            if (star != null) {
                star.setClickable(!readOnly);
            }
        }
        
        // Moods
        mMoodsRecyclerView.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mMoodsReadonlyTextView.setVisibility(readOnly ? View.VISIBLE : View.GONE);
        if (readOnly) {
            updateMoodsReadonlyText();
        }
        
        // Disable mood selection
        if (mMoodAdapter != null) {
            mMoodAdapter.setReadOnly(readOnly);
        }
        
        // Recommend
        mRecommendYesButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mRecommendNoButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        mRecommendReadonlyTextView.setVisibility(readOnly ? View.VISIBLE : View.GONE);
        if (readOnly) {
            String recommendText = mSelectedRecommend.equals("yes")
                    ? getString(R.string.recommend_yes_text)
                    : getString(R.string.recommend_no_text);
            mRecommendReadonlyTextView.setText(recommendText);
        }
        
        // Hide save button, show edit/delete buttons at bottom when read-only
        mSaveButton.setVisibility(readOnly ? View.GONE : View.VISIBLE);
        if (readOnly) {
            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
        } else {
            mEditButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);
        }
    }
    
    private void updateMoodsReadonlyText() {
        if (mSelectedMoods == null || mSelectedMoods.isEmpty()) {
            mMoodsReadonlyTextView.setText("");
            return;
        }
        
        // Map emojis to mood adjectives (remove emoji and convert to adjective form)
        String[] moodStrings = {
            getString(R.string.mood_happy),
            getString(R.string.mood_touched),
            getString(R.string.mood_excited),
            getString(R.string.mood_surprised),
            getString(R.string.mood_funny),
            getString(R.string.mood_thoughtful),
            getString(R.string.mood_okay),
            getString(R.string.mood_boring),
            getString(R.string.mood_tense),
            getString(R.string.mood_calm)
        };
        
        // Convert mood strings to adjectives (remove emoji and convert past tense to adjective)
        String[] moodAdjectives = new String[10];
        for (int i = 0; i < moodStrings.length; i++) {
            String moodStr = moodStrings[i];
            // Remove emoji (first character or first 2 characters if emoji)
            moodStr = moodStr.replaceAll("^[😊😢😍😮😂🤔😐😴😨🌿]\\s*", "");
            
            // Convert to adjective form based on language
            String lang = LocaleHelper.getLanguageCode(this);
            if (lang.equals("ko")) {
                // Korean: convert past tense to adjective form that works with "고" conjunction
                moodStr = moodStr.replace("재밌었어요", "재밌는")
                                 .replace("감동했어요", "감동적인")
                                 .replace("설렜어요", "설레는")
                                 .replace("놀랐어요", "놀라운")
                                 .replace("웃겼어요", "웃긴")
                                 .replace("생각이 많아졌어요", "생각하게 만드는")
                                 .replace("그냥 그랬어요", "그냥 그런")
                                 .replace("지루했어요", "지루한")
                                 .replace("긴장됐어요", "긴장되는")
                                 .replace("잔잔했어요", "잔잔한");
                moodAdjectives[i] = moodStr;
            } else {
                // For other languages, use as is or convert similarly
                moodAdjectives[i] = moodStr;
            }
        }
        
        List<String> selectedMoodTexts = new ArrayList<>();
        for (String emoji : mSelectedMoods) {
            for (int i = 0; i < MOOD_EMOJIS.length; i++) {
                if (MOOD_EMOJIS[i].equals(emoji)) {
                    selectedMoodTexts.add(moodAdjectives[i]);
                    break;
                }
            }
        }
        
        if (selectedMoodTexts.isEmpty()) {
            mMoodsReadonlyTextView.setText("");
        } else {
            String lang = LocaleHelper.getLanguageCode(this);
            String moodText;
            if (lang.equals("ko")) {
                // Fix grammar: "설레는고" -> "설레고", "재밌는고" -> "재밌고", etc.
                // Remove "는" from all adjectives except the last one before joining with "고"
                // Format: ~고 ~고 ~는 영화였어요!
                List<String> fixedMoodTexts = new ArrayList<>();
                for (int i = 0; i < selectedMoodTexts.size(); i++) {
                    String mood = selectedMoodTexts.get(i);
                    // Remove trailing "는" from all except the last one
                    if (i < selectedMoodTexts.size() - 1 && mood.endsWith("는")) {
                        mood = mood.substring(0, mood.length() - 1);
                    }
                    // Keep "는" for the last adjective
                    fixedMoodTexts.add(mood);
                }
                moodText = String.join("고 ", fixedMoodTexts) + " 영화였어요!";
            } else {
                // For other languages, use comma or "and"
                moodText = String.join(", ", selectedMoodTexts) + " movie!";
            }
            mMoodsReadonlyTextView.setText(moodText);
        }
    }

    private void setupStars() {
        mStarsLayout.removeAllViews();
        for (int i = 0; i < 5; i++) {
            TextView starTextView = new TextView(this);
            starTextView.setText("⭐");
            starTextView.setTextSize(32);
            starTextView.setPadding(12, 0, 12, 0);
            starTextView.setAlpha(0.3f);
            final int rating = i + 1;
            starTextView.setOnClickListener(v -> selectRating(rating));
            mStarTextViews[i] = starTextView;
            mStarsLayout.addView(starTextView);
        }
    }

    private void selectRating(int rating) {
        mSelectedRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                mStarTextViews[i].setAlpha(1.0f);
            } else {
                mStarTextViews[i].setAlpha(0.3f);
            }
        }
    }

    private void setupMoods() {
        List<String> moodStrings = new ArrayList<>();
        moodStrings.add(getString(R.string.mood_happy));
        moodStrings.add(getString(R.string.mood_touched));
        moodStrings.add(getString(R.string.mood_excited));
        moodStrings.add(getString(R.string.mood_surprised));
        moodStrings.add(getString(R.string.mood_funny));
        moodStrings.add(getString(R.string.mood_thoughtful));
        moodStrings.add(getString(R.string.mood_okay));
        moodStrings.add(getString(R.string.mood_boring));
        moodStrings.add(getString(R.string.mood_tense));
        moodStrings.add(getString(R.string.mood_calm));

        List<String> moodEmojisList = new ArrayList<>();
        for (String emoji : MOOD_EMOJIS) {
            moodEmojisList.add(emoji);
        }

        mMoodAdapter = new MoodButtonAdapter(this, moodStrings, moodEmojisList, mSelectedMoods);
        mMoodAdapter.setOnMoodClickListener((moodEmoji, position) -> {
            if (mSelectedMoods.contains(moodEmoji)) {
                mSelectedMoods.remove(moodEmoji);
            } else {
                mSelectedMoods.add(moodEmoji);
            }
            mMoodAdapter.setSelectedMoods(mSelectedMoods);
        });

        mMoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mMoodsRecyclerView.setAdapter(mMoodAdapter);
    }

    private void initClickListeners() {
        mSearchContentButton.setOnClickListener(v -> showSearchDialog());
        
        mStatusWatchingButton.setOnClickListener(v -> {
            mSelectedStatus = "watching";
            updateStatusButtons();
        });
        
        mStatusCompletedButton.setOnClickListener(v -> {
            mSelectedStatus = "completed";
            updateStatusButtons();
        });
        
        mRecommendYesButton.setOnClickListener(v -> {
            mSelectedRecommend = "yes";
            updateRecommendButtons();
        });
        
        mRecommendNoButton.setOnClickListener(v -> {
            mSelectedRecommend = "no";
            updateRecommendButtons();
        });
        
        mSaveButton.setOnClickListener(v -> saveThought());
        mEditButton.setOnClickListener(v -> {
            // Switch to edit mode
            setReadOnlyMode(false);
        });
        mDeleteButton.setOnClickListener(v -> deleteThought());
        
        updateStatusButtons();
        updateRecommendButtons();
    }

    private void updateStatusButtons() {
        if (mSelectedStatus.equals("watching")) {
            mStatusWatchingButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            mStatusWatchingButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
            mStatusWatchingButton.setStrokeWidth(0);
            mStatusWatchingButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            mStatusCompletedButton.setBackgroundTintList(null);
            mStatusCompletedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary)));
            mStatusCompletedButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
            mStatusCompletedButton.setTextColor(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary));
        } else {
            mStatusWatchingButton.setBackgroundTintList(null);
            mStatusWatchingButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary)));
            mStatusWatchingButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
            mStatusWatchingButton.setTextColor(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary));
            mStatusCompletedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            mStatusCompletedButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
            mStatusCompletedButton.setStrokeWidth(0);
            mStatusCompletedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void updateRecommendButtons() {
        if (mSelectedRecommend.equals("yes")) {
            mRecommendYesButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            mRecommendYesButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
            mRecommendYesButton.setStrokeWidth(0);
            mRecommendYesButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            mRecommendNoButton.setBackgroundTintList(null);
            mRecommendNoButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary)));
            mRecommendNoButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
            mRecommendNoButton.setTextColor(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary));
        } else {
            mRecommendYesButton.setBackgroundTintList(null);
            mRecommendYesButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary)));
            mRecommendYesButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
            mRecommendYesButton.setTextColor(ContextCompat.getColor(this, R.color.colorMovieDetailTextPrimary));
            mRecommendNoButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            mRecommendNoButton.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary)));
            mRecommendNoButton.setStrokeWidth(0);
            mRecommendNoButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        }
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search_content, null);
        builder.setView(dialogView);
        
        EditText searchEditText = dialogView.findViewById(R.id.edit_text_search);
        RecyclerView resultsRecyclerView = dialogView.findViewById(R.id.recycler_view_search_results);
        TextView emptyTextView = dialogView.findViewById(R.id.text_view_empty_search);
        
        List<SearchResult> searchResults = new ArrayList<>();
        SearchResultsAdapter adapter = new SearchResultsAdapter(this, searchResults);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resultsRecyclerView.setAdapter(adapter);
        
        AlertDialog dialog = builder.create();
        
        MaterialButton searchButton = dialogView.findViewById(R.id.button_search);
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, getString(R.string.search_movies_tv_shows_people), Toast.LENGTH_SHORT).show();
                return;
            }
            performSearch(query, searchResults, adapter, emptyTextView);
        });
        
        adapter.setOnItemClickListener((result, position) -> {
            mContentTitleEditText.setText(result.getName());
            mContentId = result.getId();
            mContentType = result.getMediaType().equals("movie") ? "movie" : "tv_show";
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void performSearch(String query, List<SearchResult> searchResults, 
                               SearchResultsAdapter adapter, TextView emptyTextView) {
        searchResults.clear();
        adapter.notifyDataSetChanged();
        
        getLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<SearchResponse>() {
            @Override
            public Loader<SearchResponse> onCreateLoader(int id, Bundle args) {
                return new SearchAsyncTaskLoader(BriefThoughtsEditActivity.this, query, "1");
            }

            @Override
            public void onLoadFinished(Loader<SearchResponse> loader, SearchResponse data) {
                if (data != null && data.getResults() != null) {
                    for (SearchResult result : data.getResults()) {
                        if (result != null && (result.getMediaType().equals("movie") || result.getMediaType().equals("tv"))) {
                            searchResults.add(result);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (searchResults.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<SearchResponse> loader) {
            }
        }).forceLoad();
    }

    private void loadThought() {
        BriefThought thought = BriefThoughtsHelper.getBriefThoughtById(this, mThoughtId);
        if (thought != null) {
            mContentTitleEditText.setText(thought.getContentTitle());
            mContentId = thought.getContentId();
            mContentType = thought.getContentType();
            mSelectedStatus = thought.getStatus();
            mSelectedRating = thought.getRating();
            mSelectedMoods = new ArrayList<>(thought.getMoods());
            mSelectedRecommend = thought.getRecommend();
            
            selectRating(mSelectedRating);
            updateStatusButtons();
            updateRecommendButtons();
            
            if (mMoodAdapter != null) {
                mMoodAdapter.setSelectedMoods(mSelectedMoods);
            }
            
            // Update readonly views
            setReadOnlyMode(true);
        }
    }

    private void saveThought() {
        String contentTitle = mContentTitleEditText.getText().toString().trim();
        if (contentTitle.isEmpty()) {
            Toast.makeText(this, getString(R.string.content_title), Toast.LENGTH_SHORT).show();
            return;
        }

        BriefThought thought = new BriefThought();
        if (mThoughtId > 0) {
            thought.setId(mThoughtId);
        }
        thought.setContentId(mContentId);
        thought.setContentType(mContentType != null ? mContentType : "movie");
        thought.setContentTitle(contentTitle);
        thought.setStatus(mSelectedStatus);
        thought.setRating(mSelectedRating);
        thought.setMoods(mSelectedMoods);
        thought.setRecommend(mSelectedRecommend);

        if (mThoughtId > 0) {
            BriefThoughtsHelper.updateBriefThought(this, thought);
        } else {
            BriefThoughtsHelper.addBriefThought(this, thought);
        }

        Toast.makeText(this, getString(R.string.save), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deleteThought() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete))
            .setPositiveButton(getString(android.R.string.yes), (dialog, which) -> {
                BriefThoughtsHelper.deleteBriefThought(this, mThoughtId);
                Toast.makeText(this, getString(R.string.delete), Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton(getString(android.R.string.no), null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

