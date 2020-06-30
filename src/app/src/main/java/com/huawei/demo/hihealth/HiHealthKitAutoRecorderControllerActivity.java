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

package com.huawei.demo.hihealth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.AutoRecorderController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Record;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import java.util.List;

/**
 * AutoRecorderController Sample code
 *
 * @since 2020-03-19
 */
public class HiHealthKitAutoRecorderControllerActivity extends AppCompatActivity {
    private static final String TAG = "AutoRecorderTest";

    private static final String SPLIT = "*******************************" + System.lineSeparator();

    private Context context;

    // HMS Health AutoRecorderController
    private AutoRecorderController autoRecorderController;

    // Text control that displays action information on the page
    private TextView logInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hihealth_autorecorder);

        context = this;

        logInfoView = (TextView) findViewById(R.id.auto_recorder_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i(TAG, "signIn onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        HiHealthOptions options = HiHealthOptions.builder().build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
        autoRecorderController =
            HuaweiHiHealth.getAutoRecorderController(HiHealthKitAutoRecorderControllerActivity.this, signInHuaweiId);
    }

    /**
     * start record By DataType, the data from sensor will be inserted into database automatically until call Stop
     * Interface
     *
     * @param view the button view
     */
    public void startRecordByType(View view) {
        logger("startRecordByType");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        // DT_CONTINUOUS_STEPS_TOTAL as sample, after startRecord this type, the total steps will be inserted into
        // database when u shake ur handset
        DataType dataType = DataType.DT_CONTINUOUS_STEPS_TOTAL;
        autoRecorderController.startRecord(dataType).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> taskResult) {
                // the interface won't always success, if u use the onComplete interface, u should add the judgement of
                // result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful()) {
                    logger("onComplete startRecordByType Successful");
                } else {
                    logger("onComplete startRecordByType Failed");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                // u could call addOnSuccessListener to print something
                logger("onSuccess startRecordByType Successful");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure startRecordByType Failed: " + e.getMessage());
                logger(SPLIT);
            }
        });
    }

    /**
     * start record By DataCollector, the data from sensor will be inserted into database automatically until call Stop
     * Interface
     *
     * @param view the button view
     */
    public void startRecordByCollector(View view) {
        logger("startRecordByCollector");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        // When record data from a data collector, you must specify the data type and collector type (for example, raw
        // data or derived data).
        // You do not need to add other information (such as device information and data stream information) for
        // data-collector-based recording.
        // The app will start data recording by assembling the data collector based on the data type , collect type and
        // packageName.
        DataCollector dataCollector = new DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
            .setPackageName(context)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();
        autoRecorderController.startRecord(dataCollector).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> taskResult) {
                // the interface won't always success, if u use the onComplete interface, u should add the judgement of
                // result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful()) {
                    logger("onComplete startRecordByCollector Successful");
                } else {
                    logger("onComplete startRecordByCollector Failed");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                // u could call addOnSuccessListener to print something
                logger("onSuccess startRecordByCollector Successful");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure startRecordByCollector Failed: " + e.getMessage());
                logger(SPLIT);
            }
        });
    }

    /**
     * stop record By DataType, the data from sensor will NOT be inserted into database automatically
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

        // DT_CONTINUOUS_STEPS_TOTAL as sample, after stopRecord this type, the total steps will NOT be inserted into
        // database when u shake ur handset
        autoRecorderController.stopRecord(DataType.DT_CONTINUOUS_STEPS_TOTAL)
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
    }

    /**
     * stop record By DataCollector, the data from sensor will NOT be inserted into database automatically
     *
     * @param view the button view
     */
    public void stopRecordByCollector(View view) {
        logger("stopRecordByCollector");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        DataCollector dataCollector = new DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
            .setPackageName(context)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();

        // if u want to stop record by DataCollector, using the collector which exits in startRecord should be better
        autoRecorderController.stopRecord(dataCollector).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> taskResult) {
                // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful()) {
                    logger("onComplete stopRecordByCollector Successful");
                } else {
                    logger("onComplete stopRecordByCollector Failed");
                }
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                // u could call addOnSuccessListener to print something
                logger("onSuccess stopRecordByCollector Successful");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure stopRecordByCollector Failed: " + e.getMessage());
                logger(SPLIT);
            }
        });
    }

    /**
     * stop record By Record, the data from sensor will NOT be inserted into database automatically
     *
     * @param view the button view
     */
    public void stopRecordByRecord(View view) {
        logger("stopRecordByRecord");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        // Although HMS's Record can be constructed directly, it is still recommended that third-party developers
        // use the getRecords interface to obtain the record, and then stop recording data through stopRecordByRecord
        autoRecorderController.getRecords().addOnCompleteListener(new OnCompleteListener<List<Record>>() {
            @Override
            public void onComplete(Task<List<Record>> task) {
                logger("stopRecordByRecord getRecords firstly");
                if (task.isSuccessful()) {
                    logger("stopRecordByRecord getRecords Successful");
                    List<Record> result = task.getResult();
                    if (result == null) {
                        return;
                    }
                    if (result.size() == 0) {
                        logger("stopRecordByRecord there is no any record exits");
                        logger(SPLIT);
                        return;
                    }
                    for (Record record : result) {
                        autoRecorderController.stopRecord(record).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    logger("stopRecordByRecord Successful");
                                    logger(SPLIT);
                                } else {
                                    logger("stopRecordByRecord Failed");
                                    logger(SPLIT);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * get all record info of this application
     *
     * @param view the button view
     */
    public void getAllRecords(View view) {
        logger("getAllRecords");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        autoRecorderController.getRecords().addOnCompleteListener(new OnCompleteListener<List<Record>>() {
            @Override
            public void onComplete(Task<List<Record>> task) {
                // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                logger("getAllRecords:onComplete");
                if (task.isSuccessful()) {
                    logger("getAllRecords Successfully");
                    List<Record> result = task.getResult();
                    if (result == null) {
                        return;
                    }
                    if (result.size() == 0) {
                        logger("getAllRecords there is no any record exits");
                        logger(SPLIT);
                        return;
                    }
                    for (Record record : result) {
                        logger("getAllRecords Record : " + record.toString());
                    }
                    logger(SPLIT);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                logger("getAllRecords Failed: " + e.getMessage());
                logger(SPLIT);
            }
        });
    }

    /**
     * get record info of this application base on the dataType
     *
     * @param view the button view
     */
    public void getRecordsByType(View view) {
        logger("getRecordsByType");
        if (autoRecorderController == null) {
            HiHealthOptions options = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId);
        }

        // Get the record information through datatype. In addition to the records started with datatype, the records
        // started with DataCollector will also be obtained (if the datatype in this DataCollector is the same as the
        // datatype in the getrecords input parameter)
        DataType dataType = DataType.DT_CONTINUOUS_STEPS_TOTAL;
        autoRecorderController.getRecords(dataType).addOnCompleteListener(new OnCompleteListener<List<Record>>() {
            @Override
            public void onComplete(Task<List<Record>> task) {
                // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                logger("getRecordsByType:onComplete");
                if (task.isSuccessful()) {
                    List<Record> result = task.getResult();
                    if (result == null) {
                        return;
                    }
                    if (result.size() == 0) {
                        logger("getRecordsByType there is no record with this type exits");
                        logger(SPLIT);
                        return;
                    }
                    for (Record record : result) {
                        logger("getRecordsByType Record : " + record.toString());
                    }
                    logger(SPLIT);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                logger("getRecordsByType Failed: " + e.getMessage());
                logger(SPLIT);
            }
        });
    }

    private void logger(String string) {
        Log.i(TAG, string);
        logInfoView.append(string + System.lineSeparator());
        int offset = logInfoView.getLineCount() * logInfoView.getLineHeight();
        if (offset > logInfoView.getHeight()) {
            logInfoView.scrollTo(0, offset - logInfoView.getHeight());
        }
    }
}
