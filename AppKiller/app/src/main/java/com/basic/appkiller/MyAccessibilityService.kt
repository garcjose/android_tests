package com.basic.appkiller

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
         Log.i(TAG,"onServiceConnected");
    }
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "onAccessibilityEvent: ${event.eventType}");
        Log.d(TAG, "sourcePackageName: ${event.packageName}");
        Log.d(TAG, "parcelable: ${event.text}");
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.packageName != null && event.className != null) {
                val componentName = ComponentName(
                    event.packageName.toString(),
                    event.className.toString()
                );

                Log.i(TAG, componentName.flattenToShortString());
            }
        }
        // Get the source node of the event.
//        event.source?.apply {
//
//            // Use the event and node information to determine what action to
//            // take.
//
//            // Act on behalf of the user.
//            performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
//
//            // Recycle the nodeInfo object.
//            recycle()
//        }
//        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            if (event.getPackageName() != null && event.getClassName() != null) {
//                ComponentName componentName = new ComponentName(
//                    event.getPackageName().toString(),
//                    event.getClassName().toString()
//                );
//
//                ActivityInfo activityInfo = tryGetActivity(componentName);
//                boolean isActivity = activityInfo != null;
//                if (isActivity)
//                    Log.i("CurrentActivity", componentName.flattenToShortString());
//            }
//        }
    }

    fun tryGetActivity(component: ComponentName): ActivityInfo {
        return packageManager.getActivityInfo(component, 0);
    }
}