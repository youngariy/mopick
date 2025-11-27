package com.youngariy.mopick.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.youngariy.mopick.R;
import com.youngariy.mopick.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private CardView changeLanguageCardView;
    private TextView currentLanguageTextView;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.settings);

        initViews();
        loadActivity();
        initClickListeners();
    }

    private void initViews() {
        changeLanguageCardView = findViewById(R.id.card_view_change_language);
        currentLanguageTextView = findViewById(R.id.text_view_current_language);
    }

    private void loadActivity() {
        // Set current language display
        String currentLanguage = LocaleHelper.getLanguage(this);
        switch (currentLanguage) {
            case "ko":
                currentLanguageTextView.setText(getString(R.string.language_korean));
                break;
            case "zh":
                currentLanguageTextView.setText(getString(R.string.language_chinese));
                break;
            case "ja":
                currentLanguageTextView.setText(getString(R.string.language_japanese));
                break;
            default:
                currentLanguageTextView.setText(getString(R.string.language_english));
                break;
        }
    }

    private void initClickListeners() {
        changeLanguageCardView.setOnClickListener(v -> showLanguageDialog());
    }

    private void showLanguageDialog() {
        String currentLanguage = LocaleHelper.getLanguage(this);
        String[] languages = {
            getString(R.string.language_korean),
            getString(R.string.language_english),
            getString(R.string.language_chinese),
            getString(R.string.language_japanese)
        };
        
        int checkedItem = 0;
        switch (currentLanguage) {
            case "ko":
                checkedItem = 0;
                break;
            case "en":
                checkedItem = 1;
                break;
            case "zh":
                checkedItem = 2;
                break;
            case "ja":
                checkedItem = 3;
                break;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.language);
        builder.setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
            String selectedLanguage;
            switch (which) {
                case 0:
                    selectedLanguage = "ko";
                    break;
                case 1:
                    selectedLanguage = "en";
                    break;
                case 2:
                    selectedLanguage = "zh";
                    break;
                case 3:
                    selectedLanguage = "ja";
                    break;
                default:
                    selectedLanguage = "en";
                    break;
            }
            if (!selectedLanguage.equals(currentLanguage)) {
                LocaleHelper.setLocale(this, selectedLanguage);
                recreate();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}

