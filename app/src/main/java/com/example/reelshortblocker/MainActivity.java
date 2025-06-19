package com.example.reelshortblocker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityServiceInfo;

public class MainActivity extends AppCompatActivity {
    private Button enableButton;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enableButton = findViewById(R.id.enableButton);
        statusText = findViewById(R.id.statusText);

        updateServiceStatus();

        enableButton.setOnClickListener(v -> {
            if (!isAccessibilityServiceEnabled()) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                statusText.setText("Accessibility Service is already enabled!");
                enableButton.setEnabled(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        ComponentName expectedComponent = new ComponentName(this, ReelShortBlockerService.class);
        String expectedComponentName = expectedComponent.flattenToShortString(); // Use short string for comparison
        for (AccessibilityServiceInfo info : am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getId().contains(expectedComponentName)) {
                return true;
            }
        }
        return false;
    }

    private void updateServiceStatus() {
        if (isAccessibilityServiceEnabled()) {
            statusText.setText("Accessibility Service: Enabled");
            enableButton.setEnabled(false);
        } else {
            statusText.setText("Accessibility Service: Disabled");
            enableButton.setEnabled(true);
        }
    }
}