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

import static java.text.DateFormat.getTimeInstance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

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
import com.huawei.hms.hihealth.data.ActivitySummary;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.PaceSummary;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.ActivityRecordInsertOptions;
import com.huawei.hms.hihealth.options.ActivityRecordReadOptions;
import com.huawei.hms.hihealth.options.DeleteOptions;
import com.huawei.hms.hihealth.result.ActivityRecordReply;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ActivityRecord Sample Code
 *
 * @since 2020-03-19
 */
public class HealthKitActivityRecordControllerActivity extends AppCompatActivity {
    private static final String TAG = "ActivityRecordSample";

    // Line separators for the display on the UI
    private static final String SPLIT = "*******************************" + System.lineSeparator();

    // Internal context object
    private Context context;

    // ActivityRecordsController for managing activity records
    private ActivityRecordsController activityRecordsController;

    // DataController for deleting activity records
    private DataController dataController;

    // Text view for displaying operation information on the UI
    private TextView logInfoView;

    // PendingIntent
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_activityrecord);
        init();
    }

    /**
     * Initialization
     */
    private void init() {
        context = this;
        HiHealthOptions hiHealthOptions = HiHealthOptions.builder().build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions);
        dataController = HuaweiHiHealth.getDataController(context, signInHuaweiId);
        activityRecordsController = HuaweiHiHealth.getActivityRecordsController(context, signInHuaweiId);
        logInfoView = (TextView) findViewById(R.id.activity_records_controller_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * Start an activity record
     *
     * @param view indicating a UI object
     */
    public void beginActivityRecord(View view) {
        logger(SPLIT + "this is MyActivityRecord Begin");
        long startTime = Calendar.getInstance().getTimeInMillis();

        ActivitySummary activitySummary = getActivitySummary();

        // Create a data collector for statics data
        // The numbers are generated randomly
        DataCollector dataCollector2 = new DataCollector.Builder().setDataType(DataType.DT_STATISTICS_SLEEP)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataCollectorName("test1")
            .build();
        SamplePoint samplePoint = new SamplePoint.Builder(dataCollector2).build();
        samplePoint.setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS); 
        samplePoint.getFieldValue(Field.ALL_SLEEP_TIME).setIntValue(352);
        samplePoint.getFieldValue(Field.GO_BED_TIME).setLongValue(1599580041000L);
        samplePoint.getFieldValue(Field.SLEEP_EFFICIENCY).setIntValue(4);
        samplePoint.getFieldValue(Field.DREAM_TIME).setIntValue(58);
        samplePoint.getFieldValue(Field.WAKE_UP_TIME).setLongValue(1599608520000L);
        samplePoint.getFieldValue(Field.DEEP_SLEEP_TIME).setIntValue(82);
        samplePoint.getFieldValue(Field.DEEP_SLEEP_PART).setIntValue(64);
        samplePoint.getFieldValue(Field.AWAKE_TIME).setIntValue(3);
        samplePoint.getFieldValue(Field.SLEEP_SCORE).setIntValue(73);
        samplePoint.getFieldValue(Field.LIGHT_SLEEP_TIME).setIntValue(212);
        samplePoint.getFieldValue(Field.SLEEP_LATENCY).setIntValue(7487000);
        samplePoint.getFieldValue(Field.WAKE_UP_CNT).setIntValue(2);
        samplePoint.getFieldValue(Field.FALL_ASLEEP_TIME).setLongValue(1599587220000L);

        activitySummary.setDataSummary(Arrays.asList(samplePoint));

        // Build an ActivityRecord object
        ActivityRecord activityRecord = new ActivityRecord.Builder().setId("MyBeginActivityRecordId")
            .setName("BeginActivityRecord")
            .setDesc("This is ActivityRecord begin test!")
            .setActivityTypeId(HiHealthActivities.SLEEP)
            .setStartTime(startTime, TimeUnit.MILLISECONDS)
            .setActivitySummary(activitySummary)
            .setTimeZone("+0800")
            .build();

        checkConnect();

        // Add a listener for the ActivityRecord start success
        Task<Void> beginTask = activityRecordsController.beginActivityRecord(activityRecord);

        // Add a listener for the ActivityRecord start failure
        beginTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void voidValue) {
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

    private ActivitySummary getActivitySummary() {
        ActivitySummary activitySummary = new ActivitySummary();
        PaceSummary paceSummary = new PaceSummary();
        paceSummary.setAvgPace(247.27626);
        paceSummary.setBestPace(212.0);
        Map<String, Double> britishPaceMap = new HashMap<>();
        britishPaceMap.put("50001893", 365.0);
        paceSummary.setBritishPaceMap(britishPaceMap);
        Map<String, Double> partTimeMap = new HashMap<>();
        partTimeMap.put("1.0", 456.0);
        paceSummary.setPartTimeMap(partTimeMap);
        Map<String, Double> paceMap = new HashMap<>();
        paceMap.put("1.0", 263.0);
        paceSummary.setPaceMap(paceMap);
        Map<String, Double> britishPartTimeMap = new HashMap<>();
        britishPartTimeMap.put("1.0", 263.0);
        paceSummary.setBritishPartTimeMap(britishPartTimeMap);
        Map<String, Double> sportHealthPaceMap = new HashMap<>();
        sportHealthPaceMap.put("102802480", 535.0);
        paceSummary.setSportHealthPaceMap(sportHealthPaceMap);
        activitySummary.setPaceSummary(paceSummary);
        return activitySummary;
    }

    /**
     * Stop an activity record
     *
     * @param view indicating a UI object
     */
    public void endActivityRecord(View view) {
        logger(SPLIT + "this is MyActivityRecord End");

        // Call the related method of ActivityRecordsController to stop activity records.
        // The input parameter can be the ID string of ActivityRecord or null
        // Stop an activity record of the current app by specifying the ID string as the input parameter
        // Stop activity records of the current app by specifying null as the input parameter
        Task<List<ActivityRecord>> endTask = activityRecordsController.endActivityRecord("MyBeginActivityRecordId");
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
        logger(SPLIT + "this is MyActivityRecord Add");

        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // 创建一个步数增量的数据采集器
        // ActivityRecord 内的SampleSet 用来承载明细数据
        DataCollector dataCollector =
            new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataCollectorName("test1")
                .build();

        ActivitySummary activitySummary = getActivitySummary();

        // 创建一个总步数统计的数据采集器
        // ActivitySummary 用来承载统计数据
        DataCollector dataCollector2 =
            new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataCollectorName("test1")
                .build();
        SamplePoint samplePoint = new SamplePoint.Builder(dataCollector2).build();
        samplePoint.setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS);
        samplePoint.getFieldValue(Field.FIELD_STEPS).setIntValue(1024);
        activitySummary.setDataSummary(Arrays.asList(samplePoint));

        // Build the activity record request object
        ActivityRecord activityRecord = new ActivityRecord.Builder().setName("AddActivityRecord")
            .setDesc("This is ActivityRecord add test!")
            .setId("MyAddActivityRecordId")
            .setActivityTypeId(HiHealthActivities.RUNNING)
            .setStartTime(startTime, TimeUnit.MILLISECONDS)
            .setEndTime(endTime, TimeUnit.MILLISECONDS)
            .setActivitySummary(activitySummary)
            .setTimeZone("+0800")
            .build();

        // Build the sampling sampleSet based on the dataCollector
        SampleSet sampleSet = SampleSet.create(dataCollector);

        // Build the (DT_CONTINUOUS_STEPS_DELTA) sampling data object and add it to the sampling dataSet
        SamplePoint samplePointDetail =
            sampleSet.createSamplePoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        samplePointDetail.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(1024);
        sampleSet.addSample(samplePointDetail);

        // Build the activity record addition request object
        ActivityRecordInsertOptions insertRequest =
            new ActivityRecordInsertOptions.Builder().setActivityRecord(activityRecord).addSampleSet(sampleSet).build();

        checkConnect();

        // Call the related method in the ActivityRecordsController to add activity records
        Task<Void> addTask = activityRecordsController.addActivityRecord(insertRequest);
        addTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void voidValue) {
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
        logger(SPLIT + "this is MyActivityRecord Get");

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
        Task<ActivityRecordReply> getTask = activityRecordsController.getActivityRecord(readRequest);
        getTask.addOnSuccessListener(new OnSuccessListener<ActivityRecordReply>() {
            @Override
            public void onSuccess(ActivityRecordReply activityRecordReply) {
                logger("Get ActivityRecord was successful!");
                // Print ActivityRecord and corresponding activity data in the result
                List<ActivityRecord> activityRecordList = activityRecordReply.getActivityRecords();
                for (ActivityRecord activityRecord : activityRecordList) {
                    if (activityRecord == null) {
                        continue;
                    }
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
        logger(SPLIT + "this is MyActivityRecord Delete");

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
        Task<ActivityRecordReply> getTask = activityRecordsController.getActivityRecord(readRequest);
        getTask.addOnSuccessListener(new OnSuccessListener<ActivityRecordReply>() {
            @Override
            public void onSuccess(ActivityRecordReply activityRecordReply) {
                logger("Reading ActivityRecord  response status " + activityRecordReply.getStatus());
                List<ActivityRecord> activityRecords = activityRecordReply.getActivityRecords();

                // Get ActivityRecord and corresponding activity data in the result
                for (final ActivityRecord activityRecord : activityRecords) {
                    DeleteOptions deleteOptions = new DeleteOptions.Builder().addActivityRecord(activityRecord)
                        .setTimeInterval(activityRecord.getStartTime(TimeUnit.MILLISECONDS),
                            activityRecord.getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                        .build();
                    logger("begin delete ActivitiRecord is :" + activityRecord.getId());

                    // Delete ActivityRecord
                    Task<Void> deleteTask = dataController.delete(deleteOptions);
                    deleteTask.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void voidValue) {
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
        logger("ActivityRecord Printing -------------------------------------");
        logger("Returned for ActivityRecord: " + activityRecord.getName() + "\n\tActivityRecord Identifier is "
            + activityRecord.getId() + "\n\tActivityRecord created by app is " + activityRecord.getPackageName()
            + "\n\tDescription: " + activityRecord.getDesc() + "\n\tStart: "
            + dateFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS)) + " "
            + timeFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS)) + "\n\tEnd: "
            + dateFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS)) + " "
            + timeFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS)) + "\n\tActivity:"
            + activityRecord.getActivityType());
        if (activityRecord.getActivitySummary() != null) {
            printActivitySummary(activityRecord.getActivitySummary());
        }

        logger("ActivityRecord Printing End ----------------------------------");
    }

    public void printActivitySummary(ActivitySummary activitySummary) {
        List<SamplePoint> dataSummary = activitySummary.getDataSummary();
        logger("\nActivitySummary\n\t DataSummary: ");
        for (SamplePoint samplePoint : dataSummary) {
            logger("\n\t samplePoint: \n\t DataCollector" + samplePoint.getDataCollector() + "\n\t DataType"
                + samplePoint.getDataType() + "\n\t StartTime" + samplePoint.getStartTime(TimeUnit.MILLISECONDS)
                + "\n\t EndTime" + samplePoint.getEndTime(TimeUnit.MILLISECONDS) + "\n\t SamplingTime"
                + samplePoint.getSamplingTime(TimeUnit.MILLISECONDS) + "\n\t FieldValues"
                + samplePoint.getFieldValues());
        }
        // Printing PaceSummary
        PaceSummary paceSummary = activitySummary.getPaceSummary();
        logger("\n\t PaceSummary: \n\t AvgPace" + paceSummary.getAvgPace() + "\n\t BestPace" + paceSummary.getBestPace()
            + "\n\t PaceMap" + paceSummary.getPaceMap() + "\n\t PartTimeMap" + paceSummary.getPartTimeMap()
            + "\n\t BritishPaceMap" + paceSummary.getBritishPaceMap() + "\n\t BritishPartTimeMap"
            + paceSummary.getBritishPartTimeMap() + "\n\t SportHealthPaceMap" + paceSummary.getSportHealthPaceMap());
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
        if (activityRecordsController == null) {
            HiHealthOptions hiHealthOptions = HiHealthOptions.builder().build();
            AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions);
            activityRecordsController = HuaweiHiHealth.getActivityRecordsController(this, signInHuaweiId);
        }
    }

    /**
     * Send the operation result logs to the logcat and TextView control on the UI
     *
     * @param string indicating the log string
     */
    private void logger(String string) {
        CommonUtil.logger(string, TAG, logInfoView);
    }

    /**
     * Print error code and error information for an exception.
     *
     * @param exception indicating an exception object
     * @param api       api name
     */
    private void printFailureMessage(Exception exception, String api) {
        CommonUtil.printFailureMessage(TAG, exception, api, logInfoView);
    }
}
