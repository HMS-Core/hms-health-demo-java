/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.demo.health;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.AutoRecorderController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.options.OnSamplePointListener;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * AutoRecorderController Sample code
 *
 * @since 2020-03-19
 */
public class HealthKitAutoRecorderControllerActivity extends AppCompatActivity {
    private static final String TAG = "AutoRecorderTest";

    private static final String SPLIT = "*******************************" + System.lineSeparator();

    // HMS Health AutoRecorderController
    private AutoRecorderController autoRecorderController;

    // Text control that displays action information on the page
    private TextView logInfoView;

    private Intent intent;

    // Defining a Dynamic Broadcast Receiver
    private MyReceiver receiver = null;

    // Record Start Times
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_autorecorder);
        logInfoView = (TextView) findViewById(R.id.auto_recorder_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());
        initData();
    }

    private void initData() {
        intent = new Intent();
        intent.setPackage(getPackageName());
        intent.setAction("HealthKitService");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "signIn onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        HiHealthOptions options = HiHealthOptions.builder().build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
        autoRecorderController =
            HuaweiHiHealth.getAutoRecorderController(HealthKitAutoRecorderControllerActivity.this, signInHuaweiId);
    }

    /**
     * Returns the callback data in SamplePoint mode.
     *
     * @param samplePoint Reported data
     */
    private void showSamplePoint(SamplePoint samplePoint) {
        if (samplePoint != null) {
            logger("Sample point type: " + samplePoint.getDataType().getName());
            for (Field field : samplePoint.getDataType().getFields()) {
                logger("Field: " + field.getName() + " Value: " + samplePoint.getFieldValue(field));
                logger(stampToData(String.valueOf(System.currentTimeMillis())));
            }
        } else {
            logger("samplePoint is null!! ");
            logger(SPLIT);
        }
    }

    /**
     * Timestamp conversion function
     *
     * @param timeStr Timestamp
     * @return Time in date format
     */
    private String stampToData(String timeStr) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long it = Long.parseLong(timeStr);
        Date date = new Date(it);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * start record By DataType
     *
     * @param view the button view
     */
    public void startRecordByType(View view) {
        if (count < 1) {
            startService(intent);
            // Registering a Broadcast Receiver
            receiver = new MyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("HealthKitService");
            this.registerReceiver(receiver, filter);
            count++;
        } else {
            this.unregisterReceiver(receiver);
            count--;
            startService(intent);
            // Registering a Broadcast Receiver
            receiver = new MyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("HealthKitService");
            this.registerReceiver(receiver, filter);
            count++;
        }
    }

    /**
     * dynamic broadcast receiver
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SamplePoint samplePoint = (SamplePoint) bundle.get("SamplePoint");
            showSamplePoint(samplePoint);
        }
    }

    /**
     * stop record By DataType
     *
     * @param view the button view
     */
    public void stopRecordByType(View view) {
        logger("stopRecordByType");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        autoRecorderController.stopRecord(DataType.DT_CONTINUOUS_STEPS_TOTAL, onSamplePointListener)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> taskResult) {
                    // the interface won't always success, if u use the onComplete interface, u should add the judgement
                    // of result is successful or not. the fail reason include:
                    // 1.the app hasn't been granted the scropes
                    // 2.this type is not supported so far
                    if (taskResult.isSuccessful()) {
                        logger("onComplete stopRecordByType Successful");
                    } else {
                        logger("onComplete stopRecordByType Failed");
                    }
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // u could call addOnSuccessListener to print something
                    logger("onSuccess stopRecordByType Successful");
                    logger(SPLIT);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // otherwise u could call addOnFailureListener to catch the fail result
                    logger("onFailure stopRecordByType Failed: " + e.getMessage());
                    logger(SPLIT);
                }
            });
        if (count > 0) {
            stopService(intent);
            this.unregisterReceiver(receiver);
            count--;
        }
    }

    private void logger(String string) {
        Log.i(TAG, string);
        logInfoView.append(string + System.lineSeparator());
        int offset = logInfoView.getLineCount() * logInfoView.getLineHeight();
        if (offset > logInfoView.getHeight()) {
            logInfoView.scrollTo(0, offset - logInfoView.getHeight());
        }
    }

    /**
     * construct OnSamplePointListener
     */
    private OnSamplePointListener onSamplePointListener = new OnSamplePointListener() {
        @Override
        public void onSamplePoint(SamplePoint samplePoint) {
        }
    };

    /**
     * start record By DataType
     *
     * @param view the button view
     */
    public void startRecordHeartRate(View view) {
        logger("starRecordByType ······");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        // 使用Client Task调用方式
        Task<Void> task =
            autoRecorderController.startRecord(DataType.DT_INSTANTANEOUS_HEART_RATE, new OnSamplePointListener() {
                @Override
                public void onSamplePoint(SamplePoint samplePoint) {
                    showDataMessage(samplePoint);
                }

                @Override
                public void onException(int i, String s) {
                    showException(i, s);
                }
            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> taskResult) {
                    // the interface won't always success, if u use the onComplete interface, u should add the judgement
                    // of result is successful or not. the fail reason include:
                    // 1.the app hasn't been granted the scropes
                    // 2.this type is not supported so far
                    if (taskResult.isSuccessful()) {
                        logger("onComplete starRecordHeartRate Successful");
                    } else {
                        logger("onComplete starRecordHeartRate Failed");
                    }
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // u could call addOnSuccessListener to print something
                    logger("onSuccess starRecordHeartRate Successful");
                    logger(SPLIT);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // otherwise u could call addOnFailureListener to catch the fail result
                    logger("onFailure starRecordHeartRate Failed: " + e.getMessage());
                    logger(SPLIT);
                }
            });
    }

    private void showDataMessage(SamplePoint samplePoint) {
        if (samplePoint != null) {
            logger("Sample point type: " + samplePoint.getDataType().getName());
            for (Field field : samplePoint.getDataType().getFields()) {
                logger("Field: " + field.getName() + " Value: " + samplePoint.getFieldValue(field));
                logger(stampToData(String.valueOf(System.currentTimeMillis())));
            }
        } else {
            logger("samplePoint is null!! ");
            logger(SPLIT);
        }
    }

    private void showException(int i, String s) {
        logger("Exception occurs, exception id: " + i + " msg is : " + s + System.lineSeparator());
    }

    /**
     * stop record By DataType
     *
     * @param view the button view
     */
    public void stopRecordHeartRate(View view) {
        logger("stopRecordHeartRate doing ······");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        autoRecorderController.stopRecord(DataType.DT_INSTANTANEOUS_HEART_RATE, new OnSamplePointListener() {
            @Override
            public void onSamplePoint(SamplePoint samplePoint) {
            }

            @Override
            public void onException(int i, String s) {
                showException(i, s);
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> taskResult) {
                // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful()) {
                    logger("onComplete stopRecordHeartRate Successful");
                } else {
                    logger("onComplete stopRecordHeartRate Failed");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                // u could call addOnSuccessListener to print something
                logger("onSuccess stopRecordHeartRate Successful");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure stopRecordHeartRate Failed: " + e.getMessage());
                logger(SPLIT);
            }
        });
    }
}
