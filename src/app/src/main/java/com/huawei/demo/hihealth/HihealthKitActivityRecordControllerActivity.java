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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.demo.hihealth.broadcastreceiver.ActivityRecordsMonitorReceiver;
import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.ActivityRecordsController;
import com.huawei.hms.hihealth.DataController;
import com.huawei.hms.hihealth.HiHealthActivities;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HiHealthStatusCodes;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.ActivityRecord;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.ActivityRecordInsertOptions;
import com.huawei.hms.hihealth.options.ActivityRecordReadOptions;
import com.huawei.hms.hihealth.options.DeleteOptions;
import com.huawei.hms.hihealth.result.ActivityRecordReply;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import static java.text.DateFormat.getTimeInstance;

/**
 * ActivityRecord Sample Code
 *
 * @since 2020-03-19
 */
public class HihealthKitActivityRecordControllerActivity extends AppCompatActivity {
    private static final String TAG = "ActivityRecordSample";

    // Internal context object
    private Context mContext;

    // ActivityRecordsController for managing activity records
    private ActivityRecordsController mActivityRecordsController;

    // DataController for deleting activity records
    private DataController mDataController;

    // Text view for displaying operation information on the UI
    private TextView logInfoView;

    // PendingIntent
    private PendingIntent pendingIntent;

    // Line separators for the display on the UI
    private String tag = "*******************************" + System.lineSeparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hihealth_activityrecord);
        init();
    }

    /**
     * Initialization
     */
    private void init() {
        mContext = this;
        HiHealthOptions fitnessOptions = HiHealthOptions.builder().build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(fitnessOptions);
        mDataController = HuaweiHiHealth.getDataController(mContext, signInHuaweiId);
        mActivityRecordsController = HuaweiHiHealth.getActivityRecordsController(mContext, signInHuaweiId);
        logInfoView = (TextView) findViewById(R.id.activity_records_controller_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * Start an activity record
     *
     * @param view indicating a UI object
     */
    public void beginActivityRecord(View view) {
        logger(tag + "this is MyActivityRecord Begin");
        long startTime = Calendar.getInstance().getTimeInMillis();

        // Build an ActivityRecord object
        ActivityRecord activityRecord = new ActivityRecord.Builder().setId("MyBeginActivityRecordId")
                .setName("BeginActivityRecord")
                .setDesc("This is ActivityRecord begin test!")
                .setActivityTypeId(HiHealthActivities.RUNNING)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .build();

        checkConnect();

        // Add a listener for the ActivityRecord start success
        Task<Void> beginTask = mActivityRecordsController.beginActivityRecord(activityRecord);

        // Add a listener for the ActivityRecord start failure
        beginTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("MyActivityRecord begin success");
            }
            // 添加启动ActivityRecord失败监听
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "beginActivityRecord");
            }
        });
    }

    /**
     * Stop an activity record
     *
     * @param view indicating a UI object
     */
    public void endActivityRecord(View view) {
        logger(tag + "this is MyActivityRecord End");

        // Call the related method of ActivityRecordsController to stop activity records.
        // The input parameter can be the ID string of ActivityRecord or null
        // Stop an activity record of the current app by specifying the ID string as the input parameter
        // Stop activity records of the current app by specifying null as the input parameter
        Task<List<ActivityRecord>> endTask = mActivityRecordsController.endActivityRecord("MyBeginActivityRecordId");
        endTask.addOnSuccessListener(new OnSuccessListener<List<ActivityRecord>>() {
            @Override
            public void onSuccess(List<ActivityRecord> activityRecords) {
                logger("MyActivityRecord End success");
                // Return the list of activity records that have stopped
                if (activityRecords.size() > 0) {
                    for (ActivityRecord activityRecord : activityRecords) {
                        dumpActivityRecord(activityRecord);
                    }
                } else {
                    // Null will be returnded if none of the activity records has stopped
                    logger("MyActivityRecord End response is null");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "endActivityRecord");
            }
        });
    }

    /**
     * Add an activity record to the Health platform
     *
     * @param view indicating a UI object
     */
    public void addActivityRecord(View view) {
        logger(tag + "this is MyActivityRecord Add");

        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // Build the activity record request object
        ActivityRecord activityRecord = new ActivityRecord.Builder().setName("AddActivityRecord")
                .setDesc("This is ActivityRecord add test!")
                .setId("MyAddActivityRecordId")
                .setActivityTypeId(HiHealthActivities.RUNNING)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .build();

        // Build the dataCollector object
        DataCollector dataSource =
                new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                        .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                        .setPackageName(mContext)
                        .setDataCollectorName("AddActivityRecord")
                        .build();

        // Build the sampling dataSet based on the dataCollector
        SampleSet dataSet = SampleSet.create(dataSource);

        // Build the (DT_CONTINUOUS_STEPS_DELTA) sampling data object and add it to the sampling dataSet
        SamplePoint dataPoint = dataSet.createSamplePoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        dataPoint.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(1024);
        dataSet.addSample(dataPoint);

        // Build the activity record addition request object
        ActivityRecordInsertOptions insertRequest =
                new ActivityRecordInsertOptions.Builder().setActivityRecord(activityRecord).addSampleSet(dataSet).build();

        checkConnect();

        // Call the related method in the ActivityRecordsController to add activity records
        Task<Void> addTask = mActivityRecordsController.addActivityRecord(insertRequest);
        addTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("ActivityRecord add was successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "addActivityRecord");
            }
        });
    }

    /**
     * Read historical activity records
     *
     * @param view indicating a UI object
     */
    public void getActivityRecord(View view) {
        logger(tag + "this is MyActivityRecord Get");

        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build the request body for reading activity records
        ActivityRecordReadOptions readRequest =
                new ActivityRecordReadOptions.Builder().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .readActivityRecordsFromAllApps()
                        .read(DataType.DT_CONTINUOUS_STEPS_DELTA)
                        .build();

        checkConnect();

        // Call the read method of the ActivityRecordsController to obtain activity records
        // from the Health platform based on the conditions in the request body
        Task<ActivityRecordReply> getTask = mActivityRecordsController.getActivityRecord(readRequest);
        getTask.addOnSuccessListener(new OnSuccessListener<ActivityRecordReply>() {
            @Override
            public void onSuccess(ActivityRecordReply activityRecordReply) {
                logger("Get ActivityRecord was successful!");
                // Print ActivityRecord and corresponding activity data in the result
                List<ActivityRecord> activityRecordList = activityRecordReply.getActivityRecords();
                for (ActivityRecord activityRecord : activityRecordList) {
                    dumpActivityRecord(activityRecord);
                    for (SampleSet sampleSet : activityRecordReply.getSampleSet(activityRecord)) {
                        dumpSampleSet(sampleSet);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "getActivityRecord");
            }
        });
    }

    /**
     * Delete activity record
     *
     * @param view indicating a UI object
     */
    public void deleteActivityRecord(View view) {
        logger(tag + "this is MyActivityRecord Delete");

        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        long startTime = cal.getTimeInMillis();

        // Build the request body for reading activity records
        ActivityRecordReadOptions readRequest =
                new ActivityRecordReadOptions.Builder().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .readActivityRecordsFromAllApps()
                        .read(DataType.DT_CONTINUOUS_STEPS_DELTA)
                        .build();

        // Call the read method of the ActivityRecordsController to obtain activity records
        // from the Health platform based on the conditions in the request body
        Task<ActivityRecordReply> getTask = mActivityRecordsController.getActivityRecord(readRequest);
        getTask.addOnSuccessListener(new OnSuccessListener<ActivityRecordReply>() {
            @Override
            public void onSuccess(ActivityRecordReply activityRecordReply) {
                Log.i(TAG, "Reading ActivityRecord  response status " + activityRecordReply.getStatus());
                List<ActivityRecord> activityRecords = activityRecordReply.getActivityRecords();

                // Get ActivityRecord and corresponding activity data in the result
                for (final ActivityRecord activityRecord : activityRecords) {
                    DeleteOptions deleteOptions = new DeleteOptions.Builder().addActivityRecord(activityRecord)
                            .setTimeInterval(activityRecord.getStartTime(TimeUnit.MILLISECONDS),
                                    activityRecord.getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                            .build();
                    logger("begin delete ActivitiRecord is :" + activityRecord.getId());

                    // Delete ActivityRecord
                    Task<Void> deleteTask = mDataController.delete(deleteOptions);
                    deleteTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            logger("delete ActivitiRecord is Success:" + activityRecord.getId());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            printFailureMessage(e, "delete");
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "delete");
            }
        });
    }

    /**
     * Register a listener for monitoring the activity record status
     *
     * @param view indicating a UI object
     */
    public void addActivityRecordsMonitor(View view) {
        logger(tag + "this is MyActivityRecord Add Monitor");
        if (pendingIntent != null) {
            logger("There is already a Monitor, no need to add Monitor");
            return;
        }
        // Build the pendingIntent request body.
        // ActivityRecordsMonitorReceiver is the broadcast receiving class created in advance
        pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(this, ActivityRecordsMonitorReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Call the related method in the ActivityRecordsController to register a listener
        Task<Void> addMonitorTask = mActivityRecordsController.addActivityRecordsMonitor(pendingIntent);
        addMonitorTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("addActivityRecordsMonitor is successful");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "addActivityRecordsMonitor");
                pendingIntent = null;
            }
        });
    }

    /**
     * Unregister a listener for monitoring the activity record status
     *
     * @param view indicating a UI object
     */
    public void removeActivityRecordsMonitor(View view) {
        logger(tag + "this is MyActivityRecord Remove Monitor");
        if (pendingIntent == null) {
            logger("There is no Monitor, no need to remove Monitor");
            return;
        }

        // Build the pendingIntent request body.
        // ActivityRecordsMonitorReceiver is the broadcast receiving class created in advance
        pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(this, ActivityRecordsMonitorReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Call the related method in the ActivityRecordsController to unregister a listener
        Task<Void> removeMonitorTask = mActivityRecordsController.removeActivityRecordsMonitor(pendingIntent);
        removeMonitorTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("removeActivityRecordsMonitor is successful");
                pendingIntent = null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "removeActivityRecordsMonitor");
                pendingIntent = null;
            }
        });
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param sampleSet indicating the sampling dataset)
     */
    private void dumpSampleSet(SampleSet sampleSet) {
        logger("Returned for SamplePoint and Data type: " + sampleSet.getDataType().getName());
        for (SamplePoint dp : sampleSet.getSamplePoints()) {
            DateFormat dateFormat = getTimeInstance();
            logger("SamplePoint:");
            logger("DataCollector:" + dp.getDataCollector().toString());
            logger("\tType: " + dp.getDataType().getName());
            logger("\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            logger("\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                logger("\tField: " + field.toString() + " Value: " + dp.getFieldValue(field));
            }
        }
    }

    /**
     * Print the ActivityRecord object as an output.
     *
     * @param activityRecord indicating an activity record
     */
    private void dumpActivityRecord(ActivityRecord activityRecord) {
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();
        logger("Returned for ActivityRecord: " + activityRecord.getName() + "\n\tActivityRecord Identifier is "
                + activityRecord.getId() + "\n\tActivityRecord created by app is " + activityRecord.getPackageName()
                + "\n\tDescription: " + activityRecord.getDesc() + "\n\tStart: "
                + dateFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS)) + " "
                + timeFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS)) + "\n\tEnd: "
                + dateFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS)) + " "
                + timeFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS)) + "\n\tActivity:"
                + activityRecord.getActivityType());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Check the object connection
     */
    private void checkConnect() {
        if (mActivityRecordsController == null) {
            HiHealthOptions fitnessOption = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(fitnessOption);
            mActivityRecordsController = HuaweiHiHealth.getActivityRecordsController(this, signInHuaweiId);
        }
    }

    /**
     * Print error code and error information for an exception.
     *
     * @param e   indicating an exception object
     * @param api api name
     */
    private void printFailureMessage(Exception e, String api) {
        String errorCode = e.getMessage();
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(errorCode);
        if (isNum.matches()) {
            String errorMsg = HiHealthStatusCodes.getStatusCodeMessage(Integer.parseInt(errorCode));
            logger(api + " failure " + errorCode + ":" + errorMsg);
        } else {
            logger(api + " failure " + errorCode);
        }
        logger(tag);
    }

    /**
     * Send the operation result logs to the logcat and TextView control on the UI
     *
     * @param string indicating the log string
     */
    private void logger(String string) {
        Log.i(TAG, string);
        logInfoView.append(string + System.lineSeparator());
        int offset = logInfoView.getLineCount() * logInfoView.getLineHeight();
        if (offset > logInfoView.getHeight()) {
            logInfoView.scrollTo(0, offset - logInfoView.getHeight());
        }
    }
}
