package com.kumaj.shanbay_autopunch.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.kumaj.shanbay_autopunch.mode.SettingModel;
import com.kumaj.shanbay_autopunch.utils.SettingUtil;
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

    private final static String ID_LEARNING_NUM_TODAY = PACKAGE_NAME + "learning_num_today";

    private final static String ID_LEARNING_NUM_PASSED = PACKAGE_NAME + "learning_num_passed";

    private final static int GROUP_WORDS_NUM = 7;

    private final static int EVENT_START_LEARN = 0;

    private final static int EVENT_KNOWN_OR_NEXT_GROUP = 1;

    private final static int EVENT_UNKNOWN = 2;

    private SettingModel mSettingModel;
    private final Timer timer = new Timer(true);

    private MyTimerTask mTimerTask;


    private class MyTimerTask extends TimerTask {

        private final static int START_INTERVAL_TIME = 5;
        private final int NEXT_INTERVAL_TIME; //认识/不认识选择界面的时间间隔
        private int mStartTime;
        private int mIntervalTime;


        MyTimerTask(int time) {
            NEXT_INTERVAL_TIME = calcIntervalTime();
            Log.d(TAG, "interval time = " + time);
        }


        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override public void run() {
            Message msg = Message.obtain();
            //start learn
            if (getItemTextById(ID_LEARNING_START) != null) {
                if (mStartTime == START_INTERVAL_TIME) {
                    msg.what = EVENT_START_LEARN;
                    Log.d(TAG, "start learn");
                    mStartTime = 0;
                    mTimerHandler.sendMessage(msg);
                }
                mStartTime++;
                Log.d(TAG, "start time = " + mStartTime);
            }
            //known or next group
            if (getItemTextById(ID_KNOWN) != null
                || getItemTextById(ID_NEXT_BUTTON) != null
                || getItemTextById(ID_NEXT_GROUP) != null
                || getItemTextById(ID_STATE_BUTTON) != null) {
                if (mIntervalTime == NEXT_INTERVAL_TIME) {
                    msg.what = EVENT_KNOWN_OR_NEXT_GROUP;
                    Log.d(TAG, "known or next group");
                    mIntervalTime = 0;
                    mTimerHandler.sendMessage(msg);
                }
                mIntervalTime++;
                Log.d(TAG, "interval time = " + mIntervalTime);
            }
        }
    }


    private Handler mTimerHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e(TAG, "msg.what = " + msg.what);
            switch (msg.what) {
                case EVENT_START_LEARN:
                    if (!performClickById(ID_LEARNING_START)) { //开始
                        if (!performClickById(ID_LEARNING_CHECK_IN)) {//打卡
                            performClickById(ID_LEARNING_TEXT_IMAGE);//去测试
                        }
                    }
                    break;
                case EVENT_KNOWN_OR_NEXT_GROUP:
                    if (!performClickById(ID_KNOWN)) { // 认识/不认识
                        if (!performClickById(ID_NEXT_BUTTON)) { //下一个
                            if (!performClickById(ID_NEXT_GROUP)) { //下一组
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
        mSettingModel = SettingUtil.getInstance().loadSettings();
    }


    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String className = event.getClassName().toString();
        //// TODO: 16/9/13 监听返回键暂停服务
        Log.e(TAG, "evenType = " + eventType);
        Log.e(TAG, "class name = " + className);
        if (className.equals("com.shanbay.words.home.HomeActivity")) {
            Log.e(TAG, "进入主界面");
            mTimerTask = new MyTimerTask(calcIntervalTime());
            timer.schedule(mTimerTask, 0, 1000);
        }
    }




    @Override public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
        mTimerHandler = null;
        mTimerTask.cancel();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2) private int calcIntervalTime() {
        int todayNum = Integer.parseInt(getItemTextById(ID_LEARNING_NUM_TODAY));
        int sections = todayNum * 2 + Math.round(todayNum / GROUP_WORDS_NUM);
        //getExpectedTime - 已经被过的时间
        //passedNum != 0 是否有纪录? ->有记录,减已经花过的时间;没有纪录,提示(可能不准)
        //return 小于0的情况?
        Log.d(TAG, "total time = " + mSettingModel.getExpectedTime() +
            "todayNum = " + todayNum + " sections = " + sections);
        return Math.round(mSettingModel.getExpectedTime() / sections);
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
    private String getItemTextById(String id) {
        String text = null;
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            AccessibilityNodeInfo item = getItemById(nodeInfo, id);
            try {
                if (item != null) {
                    text = item.getText().toString();
                }
            } finally {
                nodeInfo.recycle();
            }
        }
        return text;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private AccessibilityNodeInfo getItemById(AccessibilityNodeInfo rootInfo, String id) {
        List<AccessibilityNodeInfo> list = rootInfo.findAccessibilityNodeInfosByViewId(id);
        if (list != null && !list.isEmpty()) {
            if (list.size() == 1) {
                return list.get(0);
            }
        }
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
        return null;
    }

}
