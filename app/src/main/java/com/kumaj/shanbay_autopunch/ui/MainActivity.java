package com.kumaj.shanbay_autopunch.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.kumaj.shanbay_autopunch.R;
import com.kumaj.shanbay_autopunch.mode.SettingModel;
import com.kumaj.shanbay_autopunch.utils.SettingUtil;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.item_time_expected) LinearLayout mItemTimeExpected;
    @BindView(R.id.switch_auto_punch) SwitchCompat mSwitchAutoPunch;
    @BindView(R.id.switch_auto_share) SwitchCompat mSwitchAutoShare;
    @BindView(R.id.btn_punch_card) Button mBtnPunchCard;
    @BindView(R.id.text_minute) TextView mTextMinute;

    private SettingModel mSettingModel = new SettingModel();
    private final static String SETTING_KEY = "SETTINGS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                break;
            case R.id.action_about:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick({ R.id.item_time_expected, R.id.btn_punch_card })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_time_expected:
                showDialog();
                break;
            case R.id.btn_punch_card:
                saveSettings();
                startPunch();
                break;
        }
    }


    private void showDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_input_time, null);
        final EditText editText = (EditText) content.findViewById(R.id.edit_minute);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setTitle(getString(R.string.time_input_title))
            .setView(content)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    mTextMinute.setText(
                        getString(R.string.text_minute, editText.getText().toString()));
                    dialog.cancel();
                }
            })
            .setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void saveSettings() {
        SettingModel model = new SettingModel();
        String minute = mTextMinute.getText().toString();
        model.setExpectedTime(TextUtils.isEmpty(minute) ? 5 : Long.valueOf(minute));
        model.setAutoPunch(mSwitchAutoPunch.isChecked());
        model.setAutoShare(mSwitchAutoShare.isChecked());
        SettingUtil.getInstance().saveSettings(model);
    }


    private void startPunch() {
        if (!isSettingEnable()) {
            openSettings();
            return;
        }
        Intent intent = new Intent();
        intent.setClassName("com.shanbay.words", "com.shanbay.words.activity.HomeActivity");
        startActivity(intent);
    }


    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
        Toast.makeText(this, R.string.tip_open_service, Toast.LENGTH_LONG).show();
    }


    private boolean isSettingEnable() {
        int ok = 0;
        try {
            ok = Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(':');
        if (ok == 1) {
            String settingValue = Settings.Secure
                .getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                ms.setString(settingValue);
                while (ms.hasNext()) {
                    String accessibilityService = ms.next();
                    Log.e("TAG","service = " + accessibilityService);
                    if (accessibilityService.equalsIgnoreCase(
                        getString(R.string.service_package_name))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}