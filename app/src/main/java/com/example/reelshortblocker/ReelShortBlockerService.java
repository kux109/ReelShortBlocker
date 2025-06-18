package com.example.reelshortblocker;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReelShortBlockerService extends AccessibilityService {
    private static final String TAG = "ReelShortBlockerService";
    private static final Set<String> TARGET_PACKAGES = new HashSet<>(Arrays.asList(
        "com.instagram.android",      // Instagram
        "com.facebook.katana",        // Facebook
        "com.google.android.youtube"   // YouTube
    ));

    // Known view IDs and class names for Reels/Shorts (based on common patterns; may need updates)
    private static final Set<String> REELS_SHORTS_INDICATORS = new HashSet<>(Arrays.asList(
        "reel_viewer",                // Instagram Reels
        "reel_playback_container",    // Instagram Reels
        "shorts_player",              // YouTube Shorts
        "pivot_page_shorts",          // YouTube Shorts
        "com.google.android.exoplayer2.ui.PlayerView", // YouTube Shorts video player
        "reel_container",             // Facebook Reels
        "com.facebook.video.player.FacebookVideoPlayer" // Facebook Reels video player
    ));

    private long lastBackPressTime = 0;
    private static final long DEBOUNCE_INTERVAL_MS = 500; // Prevent rapid back presses

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Early exit for non-target apps or null package
        if (event.getPackageName() == null || !TARGET_PACKAGES.contains(event.getPackageName().toString())) {
            return;
        }

        // Only process relevant event types to reduce overhead
        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            return;
        }

        // Check if we're on a Reels/Shorts screen
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) {
            return;
        }

        if (isReelsOrShortsScreen(root)) {
            // Debounce to prevent multiple back presses in quick succession
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - lastBackPressTime >= DEBOUNCE_INTERVAL_MS) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                lastBackPressTime = currentTime;
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Blocked Reels/Shorts in " + event.getPackageName());
                }
            }
        }

        // Always recycle root node to prevent memory leaks
        root.recycle();
    }

    private boolean isReelsOrShortsScreen(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }

        // Check current node's view ID and class name
        CharSequence viewId = node.getViewIdResourceName();
        CharSequence className = node.getClassName();
        if (viewId != null && REELS_SHORTS_INDICATORS.contains(viewId)) {
            return true;
        }
        if (className != null && REELS_SHORTS_INDICATORS.contains(className.toString())) {
            return true;
        }

        // Recursively check child nodes (limit depth to avoid performance hit)
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (isReelsOrShortsScreen(child)) {
                    child.recycle();
                    return true;
                }
                child.recycle();
            }
        }

        return false;
    }

    @Override
    public void onInterrupt() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Accessibility Service interrupted");
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Accessibility Service connected");
        }
    }
}