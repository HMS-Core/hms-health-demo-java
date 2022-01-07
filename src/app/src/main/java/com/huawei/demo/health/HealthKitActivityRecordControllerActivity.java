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

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.ActivityRecordsController;
import com.huawei.hms.hihealth.DataController;
import com.huawei.hms.hihealth.HiHealthActivities;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.ActivityRecord;
import com.huawei.hms.hihealth.data.ActivitySummary;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.PaceSummary;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.ActivityRecordDeleteOptions;
import com.huawei.hms.hihealth.options.ActivityRecordInsertOptions;
import com.huawei.hms.hihealth.options.ActivityRecordReadOptions;
import com.huawei.hms.hihealth.result.ActivityRecordReply;

import static java.text.DateFormat.getTimeInstance;

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
        dataController = HuaweiHiHealth.getDataController(context);
        activityRecordsController = HuaweiHiHealth.getActivityRecordsController(context);
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

        // Build an ActivityRecord object
        ActivityRecord activityRecord = new ActivityRecord.Builder().setId("MyBeginActivityRecordId")
            .setName("BeginActivityRecord")
            .setDesc("This is ActivityRecord begin test!")
            .setActivityTypeId(HiHealthActivities.RUNNING)
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
                logger("Begin MyActivityRecord was successful!");
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
                logger("End MyActivityRecord was successful!");
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

        ActivitySummary activitySummary = getActivitySummary();

        // Create data collectors for the step, distance, speed count data.
        DataCollector dataCollectorDistanceTotal =
            new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_DISTANCE_TOTAL)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataCollectorName("test1")
                .build();
        DataCollector dataCollectorSpeedTotal = new com.huawei.hms.hihealth.data.DataCollector.Builder()
            .setDataType(DataType.POLYMERIZE_CONTINUOUS_SPEED_STATISTICS)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataCollectorName("test1")
            .build();
        DataCollector dataCollectorStepTotal =
            new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataCollectorName("test1")
                .build();
        SamplePoint distanceTotalSamplePoint = new SamplePoint.Builder(dataCollectorDistanceTotal).build()
            .setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS);
        distanceTotalSamplePoint.getFieldValue(Field.FIELD_DISTANCE).setFloatValue(400f);

        SamplePoint speedTotalSamplePoint = new SamplePoint.Builder(dataCollectorSpeedTotal).build()
            .setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS);
        speedTotalSamplePoint.getFieldValue(Field.FIELD_AVG).setFloatValue(60.0f);
        speedTotalSamplePoint.getFieldValue(Field.FIELD_MIN).setFloatValue(40.0f);
        speedTotalSamplePoint.getFieldValue(Field.FIELD_MAX).setFloatValue(80.0f);

        SamplePoint stepTotalSamplePoint = new SamplePoint.Builder(dataCollectorStepTotal).build()
            .setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS);
        stepTotalSamplePoint.getFieldValue(Field.FIELD_STEPS).setIntValue(1024);
        activitySummary
            .setDataSummary(Arrays.asList(distanceTotalSamplePoint, speedTotalSamplePoint, stepTotalSamplePoint));

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

        // create an DataType.DT_INSTANTANEOUS_STEPS_RATE DataCollector
        DataCollector dataCollector =
                new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_INSTANTANEOUS_STEPS_RATE)
                        .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                        .setPackageName(context)
                        .setDataCollectorName("test1")
                        .build();

        // Build the sampling sampleSet based on the dataCollector
        SampleSet sampleSet = SampleSet.create(dataCollector);

        // Build the (DT_INSTANTANEOUS_STEPS_RATE) sampling data object and add it to the sampling dataSet
        SamplePoint samplePointDetail =
            sampleSet.createSamplePoint().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        samplePointDetail.getFieldValue(Field.FIELD_STEP_RATE).setFloatValue(10.0f);
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
                logger("Add MyActivityRecord was successful!");
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
                .read(DataType.DT_INSTANTANEOUS_STEPS_RATE)
                .build();

        checkConnect();

        // Call the read method of the ActivityRecordsController to obtain activity records
        // from the Health platform based on the conditions in the request body
        Task<ActivityRecordReply> getTask = activityRecordsController.getActivityRecord(readRequest);
        getTask.addOnSuccessListener(new OnSuccessListener<ActivityRecordReply>() {
            @Override
            public void onSuccess(ActivityRecordReply activityRecordReply) {
                logger("Get MyActivityRecord was successful!");

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
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();


        // Build the subDataTypeList
        List<DataType> subDataTypeList = Collections.singletonList(DataType.DT_CONTINUOUS_STEPS_DELTA);
        // Build the activityRecordIds
        List<String> activityRecordIds = Collections.singletonList("MyAddActivityRecordId");

        // Build the request body for delete activity records
        ActivityRecordDeleteOptions deleteRequest = new ActivityRecordDeleteOptions.Builder()
                .setSubDataTypeList(subDataTypeList)
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .setActivityRecordIds(activityRecordIds)
                .isDeleteSubData(true)
                .build();

        // Call the delete method of the ActivityRecordsController
        // from the Health platform based on the conditions in the request body
        Task<Void> deleteTask = activityRecordsController.deleteActivityRecord(deleteRequest);
        deleteTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("Delete MyActivityRecord was successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "deleteActivityRecord");
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
            activityRecordsController = HuaweiHiHealth.getActivityRecordsController(this);
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
     * @param api api name
     */
    private void printFailureMessage(Exception exception, String api) {
        CommonUtil.printFailureMessage(TAG, exception, api, logInfoView);
    }
}
