package com.kumaj.shanbay_autopunch.mode;

import android.os.Parcelable;

public class SettingModel {

    private long expectedTime;

    private boolean isAutoPunch;

    private boolean isAutoShare;


    public long getExpectedTime() {
        return expectedTime / 1000 / 60;
    }


    public void setExpectedTime(long expectedTime) {
        this.expectedTime = expectedTime * 60 * 1000;
    }


    public boolean isAutoPunch() {
        return isAutoPunch;
    }


    public void setAutoPunch(boolean autoPunch) {
        isAutoPunch = autoPunch;
    }


    public boolean isAutoShare() {
        return isAutoShare;
    }


    public void setAutoShare(boolean autoShare) {
        isAutoShare = autoShare;
    }
}
