package com.example.reelshortblocker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView statusText;
    private Button settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Assumes you have activity_main.xml

        // Find the UI elements from your layout
        // IMPORTANT: Make sure your activity_main.xml has a TextView with id "status_text"
        // and a Button with id "enable_service_button".
        statusText = findViewById(R.id.status_text);
        settingsButton = findViewById(R.id.enable_service_button);

        // When the button is clicked, always take the user to Accessibility Settings
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Every time the app comes into the foreground, check the permission status.
        updateUI();
    }

    private void updateUI() {
        if (isAccessibilityServiceEnabled(this, ReelShortBlockerService.class)) {
            Log.d(TAG, "Service is ENABLED. Updating UI.");
            statusText.setText("Service is Active");
            settingsButton.setText("Disable Service (in Settings)");
        } else {
            Log.d(TAG, "Service is DISABLED. Updating UI.");
            statusText.setText("Service is Inactive. Please enable it.");
            settingsButton.setText("Open Accessibility Settings");
        }
    }

    /**
     * Checks if a specific Accessibility Service is enabled in the system settings.
     * @param context The application context.
     * @param serviceClass The class of the service to check (e.g., ReelShortBlockerService.class).
     * @return true if the service is enabled, false otherwise.
     */
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        final String serviceId = context.getPackageName() + "/" + serviceClass.getCanonicalName();
        try {
            int accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);

            if (accessibilityEnabled == 1) {
                TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
                String settingValue = Settings.Secure.getString(
                        context.getApplicationContext().getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

                if (settingValue != null) {
                    colonSplitter.setString(settingValue);
                    while (colonSplitter.hasNext()) {
                        String aacessibilityService = colonSplitter.next();
                        if (aacessibilityService.equalsIgnoreCase(serviceId)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding accessibility setting", e);
        }
        return false;
    }
}
