package com.example.reelshortblocker;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ReelShortBlockerService extends AccessibilityService {
    private static final String TAG = "ReelShortBlockerService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Accessibility Event: " + event.getEventType() + " - " + event.getPackageName());

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                checkForReelsOrShorts(rootNode, event);
            }
        }
    }

    private void checkForReelsOrShorts(AccessibilityNodeInfo node, AccessibilityEvent event) {
        if (node == null) return;

        String className = node.getClassName() != null ? node.getClassName().toString() : "";
        String contentDesc = node.getContentDescription() != null ? node.getContentDescription().toString() : "";
        String text = node.getText() != null ? node.getText().toString() : "";

        Log.d(TAG, "Checking node: Class=" + className + ", ContentDesc=" + contentDesc + ", Text=" + text);

        // Look for keywords or UI elements related to Reels/Shorts
        if ((className.contains("Reel") || text.contains("Reels") || contentDesc.contains("Reels") ||
            className.contains("Short") || text.contains("Shorts") || contentDesc.contains("Shorts")) &&
            (event.getPackageName() != null && (
                event.getPackageName().toString().contains("com.instagram") ||
                event.getPackageName().toString().contains("com.facebook.katana") ||
                event.getPackageName().toString().contains("com.youtube")))) {
            Log.d(TAG, "Detected Reels/Shorts in " + event.getPackageName());
            performGlobalAction(GLOBAL_ACTION_BACK); // Exit or go back
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            checkForReelsOrShorts(child, event);
        }
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "Accessibility Service connected");
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service interrupted");
    }
}