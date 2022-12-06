package com.example.familymapserverclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import Model.DataCache;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch familyTreeLinesSwitch = findViewById(R.id.settings_switch_family_tree_lines);
        familyTreeLinesSwitch.setChecked(DataCache.getInstance().isShowFamilyTreeLines());
        
        Switch lifeStoryLinesSwitch = findViewById(R.id.settings_switch_life_story_lines);
        lifeStoryLinesSwitch.setChecked(DataCache.getInstance().isShowLifeStoryLine());
        
        Switch spouseLineSwitch = findViewById(R.id.settings_switch_spouse_line);
        spouseLineSwitch.setChecked(DataCache.getInstance().isShowSpouseLine());

        Switch showPaternalSwitch = findViewById(R.id.settings_switch_paternal_events);
        showPaternalSwitch.setChecked(DataCache.getInstance().isShowPaternalEvents());

        Switch showMaternalSwitch = findViewById(R.id.settings_switch_maternal_events);
        showMaternalSwitch.setChecked(DataCache.getInstance().isShowMaternalEvents());

        Switch showMaleSwitch = findViewById(R.id.settings_switch_male_events);
        showMaleSwitch.setChecked(DataCache.getInstance().isShowMaleEvents());
        
        Switch showFemaleSwitch = findViewById(R.id.settings_switch_female_events);
        showFemaleSwitch.setChecked(DataCache.getInstance().isShowFemaleEvents());

        Button logoutButton = findViewById(R.id.logout_button);

        familyTreeLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowFamilyTreeLines(true);
                } else {
                    DataCache.getInstance().setShowFamilyTreeLines(false);
                }
            }
        });

        lifeStoryLinesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowLifeStoryLine(true);
                } else {
                    DataCache.getInstance().setShowLifeStoryLine(false);
                }
            }
        });

        spouseLineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowSpouseLine(true);
                } else {
                    DataCache.getInstance().setShowSpouseLine(false);
                }
            }
        });

        showPaternalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowPaternalEvents(true);
                } else {
                    DataCache.getInstance().setShowPaternalEvents(false);
                }
            }
        });

        showMaternalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowMaternalEvents(true);
                } else {
                    DataCache.getInstance().setShowMaternalEvents(false);
                }
            }
        });

        showMaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowMaleEvents(true);
                } else {
                    DataCache.getInstance().setShowMaleEvents(false);
                }
            }
        });

        showFemaleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DataCache.getInstance().setShowFemaleEvents(true);
                } else {
                    DataCache.getInstance().setShowFemaleEvents(false);
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataCache.clearInstance();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }
}