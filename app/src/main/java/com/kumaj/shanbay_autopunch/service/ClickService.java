package com.kumaj.shanbay_autopunch.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ClickService extends AccessibilityService {

    private final static String TAG = "ClickService";

    private final static String PACKAGE_NAME = "com.shanbay.words:id/";

    private final static String ID_LEARNING_START = PACKAGE_NAME + "learning_start";

    private final static String ID_LEARNING_CHECK_IN = PACKAGE_NAME + "learning_checkin";

    private final static String ID_LEARNING_TEXT_IMAGE = PACKAGE_NAME + "learning_test_image";

    private final static String ID_KNOWN = PACKAGE_NAME + "known";

    private final static String ID_UNKNOWN = PACKAGE_NAME + "unknown";

    private final static String ID_NEXT_BUTTON = PACKAGE_NAME + "next_button";

    private final static String ID_NEXT_GROUP = PACKAGE_NAME + "button_next_group";

    private final static String ID_STATE_BUTTON = PACKAGE_NAME + "state_btn";

    private final static String STR_PUNCH_CARD = "去打卡";

    private final static int EVENT_START_LEARN = 0;

    private final static int EVENT_KNOWN_OR_NEXT_GROUP = 1;

    private final static int EVENT_UNKNOWN = 2;

    private final Timer timer = new Timer(true);

    private final TimerTask timerTask = new TimerTask() {
        private long time = 0;

        private final static int START_INTERVAL_TIME = 3;
        private final static int KNOWN_INTERVAL_TIME = 3; //认识/不认识选择界面的时间间隔
        private final static int NEXT_TIME = 5;//"下一个"界面的时间间隔;


        @Override public void run() {
            Log.e(TAG, "time = " + time);
            Message msg = Message.obtain();
            if (time == START_INTERVAL_TIME) {
                msg.what = EVENT_START_LEARN;
            }
            if ((time - START_INTERVAL_TIME) % KNOWN_INTERVAL_TIME == 0) {
                msg.what = EVENT_KNOWN_OR_NEXT_GROUP;
            }
            mTimerHandler.sendMessage(msg);
            time++;
        }
    };

    private Handler mTimerHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case EVENT_START_LEARN:
                    if(!performClickById(ID_LEARNING_START)){ //开始
                        if(!performClickById(ID_LEARNING_CHECK_IN)){//打卡
                            performClickById(ID_LEARNING_TEXT_IMAGE);//去测试
                        }
                    }
                    break;
                case EVENT_KNOWN_OR_NEXT_GROUP:
                    if (!performClickById(ID_KNOWN)) { // 认识/不认识
                        if (!performClickById(ID_NEXT_BUTTON)) { //下一个
                           if(!performClickById(ID_NEXT_GROUP)){ //下一组
                               performClickById(ID_STATE_BUTTON);//去打卡
                           }
                        }
                    }
                    break;
            }
        }
    };


    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e(TAG, "onServiceConnected" + " thread_id = " + Thread.currentThread().getId());
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.packageNames = new String[] { "com.shanbay.words" };
        serviceInfo.notificationTimeout = 100;
        setServiceInfo(serviceInfo);
    }


    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String className = event.getClassName().toString();
        Log.e(TAG, "evenType = " + eventType);
        Log.e(TAG, "class name = " + className);
        if (className.equals("com.shanbay.words.activity.HomeActivity")) {
            Log.e(TAG, "进入主界面");
            timer.schedule(timerTask, 0, 1000);
        }
    }


    @Override public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
        mTimerHandler = null;
        timerTask.cancel();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean performClickById(String id) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            AccessibilityNodeInfo item = getItemById(nodeInfo, id);
            try {
                if (item != null) {
                    item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                } else {
                    return false;
                }
            } finally {
                nodeInfo.recycle();
            }
        }
        return false;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean performClickByText(String text) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            AccessibilityNodeInfo item = getItemById(nodeInfo, text);
            try {
                if (item != null) {
                    item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                } else {
                    return false;
                }
            } finally {
                nodeInfo.recycle();
            }
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo getItemById(AccessibilityNodeInfo rootInfo, String id) {
        List<AccessibilityNodeInfo> list = rootInfo.findAccessibilityNodeInfosByViewId(id);
        if (list != null && !list.isEmpty()) {
            if (list.size() == 1) {
                return list.get(0);
            }
        }
        Log.e(TAG, "NodeInfo error");
        return null;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo getItemByText(AccessibilityNodeInfo rootInfo, String text) {
        List<AccessibilityNodeInfo> list = rootInfo.findAccessibilityNodeInfosByText(text);
        if (list != null && !list.isEmpty()) {
            if (list.size() == 1) {
                return list.get(0);
            }
        }
        Log.e(TAG, "NodeInfo error");
        return null;
    }

}
