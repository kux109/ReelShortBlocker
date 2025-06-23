package com.example.reelshortblocker;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ReelShortBlockerService extends AccessibilityService {
    private static final String TAG = "ReelShortBlockerService";
    private static final int MAX_DEPTH = 20; // Increased depth for safety, but the logic is more efficient.

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName() == null || !event.getPackageName().equals("com.instagram.android")) {
            return;
        }

        // Get the root node of the currently active window for a complete view
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }

        // Check if both a Reel indicator and a playback indicator are present on screen
        boolean isReelVisible = findNodeWithText(rootNode, "Reel by", 0);
        boolean isPlaybackControlVisible = findNodeWithText(rootNode, "Double tap to play or pause", 0) ||
                                           findNodeWithText(rootNode, "Turn sound on", 0) ||
                                           findNodeWithText(rootNode, "Pause", 0);

        if (isReelVisible && isPlaybackControlVisible) {
            Log.d(TAG, "Detected Reel playback view. Performing back action.");
            performGlobalAction(GLOBAL_ACTION_BACK);
        }

        // Recycle the root node to free up resources
        rootNode.recycle();
    }

    /**
     * Recursively searches the node tree for any node containing the specified text in its
     * content description or text field.
     * @return true if a node is found, false otherwise.
     */
    private boolean findNodeWithText(AccessibilityNodeInfo node, String text, int depth) {
        if (node == null || depth >= MAX_DEPTH) {
            return false;
        }

        // Check the current node
        CharSequence contentDesc = node.getContentDescription();
        CharSequence nodeText = node.getText();

        if ((contentDesc != null && contentDesc.toString().contains(text)) ||
            (nodeText != null && nodeText.toString().contains(text))) {
            return true; // Found the text
        }

        // If not found, check the children recursively
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (findNodeWithText(child, text, depth + 1)) {
                // Important: Don't recycle the child node here, it's managed by the parent
                return true; // Found in a child, so we can stop searching this branch
            }
        }

        return false; // Not found in this node or any of its children
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "Accessibility service interrupted.");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected.");
    }
}
