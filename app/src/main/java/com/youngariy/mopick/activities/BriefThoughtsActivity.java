package com.youngariy.mopick.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.youngariy.mopick.R;
import com.youngariy.mopick.adapters.BriefThoughtsAdapter;
import com.youngariy.mopick.utils.BriefThought;
import com.youngariy.mopick.utils.BriefThoughtsHelper;
import com.youngariy.mopick.utils.LocaleHelper;

import java.util.List;

public class BriefThoughtsActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    private RecyclerView mRecyclerView;
    private BriefThoughtsAdapter mAdapter;
    private TextView mEmptyTextView;
    private FloatingActionButton mFabNewThought;
    private List<BriefThought> mThoughts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brief_thoughts);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.brief_thoughts);

        initViews();
        loadThoughts();
        initClickListeners();
    }

    private void initViews() {
        mRecyclerView = findViewById(R.id.recycler_view_thoughts);
        mEmptyTextView = findViewById(R.id.text_view_empty);
        mFabNewThought = findViewById(R.id.fab_new_thought);
        
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BriefThoughtsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadThoughts() {
        mThoughts = BriefThoughtsHelper.getAllBriefThoughts(this);
        mAdapter.setThoughts(mThoughts);
        
        if (mThoughts.isEmpty()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void initClickListeners() {
        mFabNewThought.setOnClickListener(v -> {
            Intent intent = new Intent(BriefThoughtsActivity.this, BriefThoughtsEditActivity.class);
            startActivity(intent);
        });
        
        mAdapter.setOnItemClickListener((thought, position) -> {
            Intent intent = new Intent(BriefThoughtsActivity.this, BriefThoughtsEditActivity.class);
            intent.putExtra("thought_id", thought.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadThoughts();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

