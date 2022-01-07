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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.HealthRecordController;
import com.huawei.hms.hihealth.HiHealthStatusCodes;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.HealthDataTypes;
import com.huawei.hms.hihealth.data.HealthFields;
import com.huawei.hms.hihealth.data.HealthRecord;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.HealthRecordDeleteOptions;
import com.huawei.hms.hihealth.options.HealthRecordInsertOptions;
import com.huawei.hms.hihealth.options.HealthRecordReadOptions;
import com.huawei.hms.hihealth.options.HealthRecordUpdateOptions;
import com.huawei.hms.hihealth.result.HealthRecordReply;

import static java.text.DateFormat.getTimeInstance;

/**
 * HealthRecord Sample Code
 *
 * @since 2021-06-04
 */
public class HealthKitHealthRecordControllerActivity extends AppCompatActivity {
    private static final String TAG = "HealthRecordSample";

    // Line separators for the display on the UI
    private static final String SPLIT = "*******************************" + System.lineSeparator();

    // Internal context object
    private Context context;

    // HealthRecordController for managing healthRecord records
    private HealthRecordController healthRecordController;

    // Text view for displaying operation information on the UI
    private TextView logInfoView;

    private String healthRecordIdFromInsertResult = "defaultValueId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_healthrecord);
        init();
    }

    private void init() {
        context = this;
        healthRecordController = HuaweiHiHealth.getHealthRecordController(context);
        logInfoView = (TextView) findViewById(R.id.activity_records_controller_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * Add an health record to the Health platform
     *
     * @param view indicating a UI object
     */
    public void addHealthRecord(View view) {
        logger(SPLIT + "this is MyHealthRecord Add");
        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // 1.Create a collector that carries the heart rate detail data type and a sampleSetList that stores the detail data.
        DataCollector dataCollector =
            new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_INSTANTANEOUS_HEART_RATE)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataStreamName("such as step count")
                .build();
        SampleSet sampleSet = SampleSet.create(dataCollector);
        // The preset time span is 5 minutes, and the heart rate detail point is set to 88.
        SamplePoint samplePoint =
            sampleSet.createSamplePoint().setTimeInterval(startTime + 300000L, startTime + 300000L, TimeUnit.MILLISECONDS);
        samplePoint.getFieldValue(Field.FIELD_BPM).setDoubleValue(88);
        sampleSet.addSample(samplePoint);
        // sampleSetList is used to store health details.
        List<SampleSet> sampleSetList = new ArrayList<>();
        sampleSetList.add(sampleSet);

        // 2.Create a collector and statistics point to carry the heart rate statistics data type.
        DataCollector dataCollector1 = new com.huawei.hms.hihealth.data.DataCollector.Builder()
            .setDataType(DataType.POLYMERIZE_CONTINUOUS_HEART_RATE_STATISTICS)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataStreamName("such as step count")
            .build();
        // samplePointList is used to store statistics points.
        List<SamplePoint> samplePointList = new ArrayList<>();
        // Constructing Heart Rate Statistics Points
        SamplePoint samplePoint1 = new SamplePoint.Builder(dataCollector1).build();
        samplePoint1.getFieldValue(Field.FIELD_AVG).setDoubleValue(90);
        samplePoint1.getFieldValue(Field.FIELD_MAX).setDoubleValue(100);
        samplePoint1.getFieldValue(Field.FIELD_MIN).setDoubleValue(80);
        samplePoint1.setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS);
        samplePointList.add(samplePoint1);

        // 3.Construct a health record collector (using the bradycardia health data type as an example) and construct a health record structure.
        DataCollector dataCollector2 = new com.huawei.hms.hihealth.data.DataCollector.Builder()
            .setDataType(HealthDataTypes.DT_HEALTH_RECORD_BRADYCARDIA)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataStreamName("such as step count")
            .build();

        HealthRecord.Builder healthRecordBuilder =
            new HealthRecord.Builder(dataCollector2).setSubDataSummary(samplePointList)
                .setSubDataDetails(sampleSetList)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS);
        // Set a value for each field of the bradycardia health data type.
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_THRESHOLD, 40d);
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_AVG_HEART_RATE, 44d);
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_MAX_HEART_RATE, 48d);
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_MIN_HEART_RATE, 40d);
        HealthRecord healthRecord = healthRecordBuilder.build();

        HealthRecordInsertOptions insertOptions =
            new HealthRecordInsertOptions.Builder().setHealthRecord(healthRecord).build();

        healthRecordController.addHealthRecord(insertOptions).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String healthRecordId) {
                // Save the healthRecordId returned after the insertion is successful.
                // The healthRecordId is used to update the scenario.
                healthRecordIdFromInsertResult = healthRecordId;
                logger("Add HealthRecord was successful,please save the healthRecordId:\n" + healthRecordId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "addHealthRecord");
            }
        });
    }

    /**
     * updates health records of a specified HealthRecordID
     *
     * @param view indicating a UI object
     */
    public void updateHealthRecord(View view) {
        logger(SPLIT + "this is MyHealthRecord Update");
        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -1);
        long startTime = cal.getTimeInMillis();

        // 1.Create a collector that carries the heart rate detail data type and a sampleSetList that stores the detail data.
        DataCollector dataCollector =
            new com.huawei.hms.hihealth.data.DataCollector.Builder().setDataType(DataType.DT_INSTANTANEOUS_HEART_RATE)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataStreamName("such as step count")
                .build();
        SampleSet sampleSet = SampleSet.create(dataCollector);
        // The preset time span is 5 minutes, and the heart rate detail point is set to 90.
        SamplePoint samplePoint =
            sampleSet.createSamplePoint().setTimeInterval(startTime + 300000L, startTime + 300000L, TimeUnit.MILLISECONDS);
        samplePoint.getFieldValue(Field.FIELD_BPM).setDoubleValue(90);
        sampleSet.addSample(samplePoint);
        // sampleSetList is used to store health details.
        List<SampleSet> sampleSetList = new ArrayList<>();
        sampleSetList.add(sampleSet);

        // 2.Create a collector and statistics point to carry the heart rate statistics data type.
        DataCollector dataCollector1 = new com.huawei.hms.hihealth.data.DataCollector.Builder()
            .setDataType(DataType.POLYMERIZE_CONTINUOUS_HEART_RATE_STATISTICS)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataStreamName("such as step count")
            .build();
        // samplePointList is used to store statistics points.
        List<SamplePoint> samplePointList = new ArrayList<>();
        // Constructing Heart Rate Statistics Points
        SamplePoint samplePoint1 = new SamplePoint.Builder(dataCollector1).build();
        samplePoint1.getFieldValue(Field.FIELD_AVG).setDoubleValue(90);
        samplePoint1.getFieldValue(Field.FIELD_MAX).setDoubleValue(100);
        samplePoint1.getFieldValue(Field.FIELD_MIN).setDoubleValue(80);
        samplePoint1.setTimeInterval(startTime + 1L, startTime + 300000L, TimeUnit.MILLISECONDS);
        samplePointList.add(samplePoint1);

        // 3.Construct a health record collector (using the bradycardia health data type as an example) and construct a health record structure.
        DataCollector dataCollector2 = new com.huawei.hms.hihealth.data.DataCollector.Builder()
            .setDataType(HealthDataTypes.DT_HEALTH_RECORD_BRADYCARDIA)
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .setPackageName(context)
            .setDataStreamName("such as step count")
            .build();

        HealthRecord.Builder healthRecordBuilder =
            new HealthRecord.Builder(dataCollector2).setSubDataSummary(samplePointList)
                .setSubDataDetails(sampleSetList)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS);
        // Set a value for each field of the bradycardia health data type.
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_THRESHOLD, 42d);
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_AVG_HEART_RATE, 45d);
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_MAX_HEART_RATE, 48d);
        healthRecordBuilder.setFieldValue(HealthFields.FIELD_MIN_HEART_RATE, 42d);
        HealthRecord healthRecord = healthRecordBuilder.build();

        // 4.Construct the updateOptions to be updated and carry the healthRecordId returned after the insertion is successful.
        HealthRecordUpdateOptions updateOptions = new HealthRecordUpdateOptions.Builder().setHealthRecord(healthRecord)
            .setHealthRecordId(healthRecordIdFromInsertResult)
            .build();

        healthRecordController.updateHealthRecord(updateOptions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("Update HealthRecord was successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "updateHealthRecord");
            }
        });
    }

    /**
     * Read historical health records
     *
     * @param view indicating a UI object
     */
    public void getHealthRecord(View view) {
        logger(SPLIT + "this is MyHealthRecord Get");

        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        // Build the request body for reading HealthRecord records
        List<DataType> subDataTypeList = new ArrayList<>();
        subDataTypeList.add(DataType.DT_INSTANTANEOUS_HEART_RATE);
        HealthRecordReadOptions healthRecordReadOptions =
            new HealthRecordReadOptions.Builder().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readHealthRecordsFromAllApps()
                .readByDataType(HealthDataTypes.DT_HEALTH_RECORD_BRADYCARDIA)
                .setSubDataTypeList(subDataTypeList)
                .build();

        // Call the get method of the HealthRecordController to obtain health records
        // from the Health platform based on the conditions in the request body
        Task<HealthRecordReply> task = healthRecordController.getHealthRecord(healthRecordReadOptions);
        task.addOnSuccessListener(new OnSuccessListener<HealthRecordReply>() {
            @Override
            public void onSuccess(HealthRecordReply readResponse) {
                logger("Get HealthRecord was successful!");
                // Print HealthRecord and corresponding health data in the result
                List<HealthRecord> recordList = readResponse.getHealthRecords();
                for (HealthRecord record : recordList) {
                    if (record == null) {
                        continue;
                    }
                    dumpHealthRecord(record);
                    logger("Print detailed data points associated with health records");
                    for (SampleSet dataSet : record.getSubDataDetails()) {
                        dumpDataSet(dataSet);
                    }
                }
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "getHealthRecord");
            }
        });
    }

    /**
     * delete historical health records
     *
     * @param view indicating a UI object
     */
    public void deleteHealthRecord(View view) {
        logger(SPLIT + "this is MyHealthRecord Delete");

        // Build the time range of the request object: start time and end time
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -2);
        long startTime = cal.getTimeInMillis();

        // 1.Create a collector that carries the heart rate detail data type and a sampleSetList that stores the detail data.
        DataCollector dataCollector = new com.huawei.hms.hihealth.data.DataCollector.Builder()
                .setDataType(HealthDataTypes.DT_HEALTH_RECORD_BRADYCARDIA)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(context)
                .setDataStreamName("such as step count")
                .build();

        // Build the dataType
        DataType dataType = dataCollector.getDataType();
        // Build the subDataTypeList
        List<DataType> subDataTypeList = new ArrayList<>();
        subDataTypeList.add(DataType.DT_INSTANTANEOUS_HEART_RATE);

        // Build the request body for delete health records
        HealthRecordDeleteOptions deleteRequest = new HealthRecordDeleteOptions.Builder()
                .setHealthRecordIds(Collections.singletonList(healthRecordIdFromInsertResult))
                .isDeleteSubData(true)
                .setDataType(dataType)
                .setSubDataTypeList(subDataTypeList)
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // Call the delete method of the HealthRecordController
        // from the Health platform based on the conditions in the request body
        Task<Void> deleteTask = healthRecordController.deleteHealthRecord(deleteRequest);
        deleteTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("Delete HealthRecord was successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "deleteHealthRecord");
            }
        });
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param sampleSet indicating the sampling dataSet
     */
    private void dumpDataSet(SampleSet sampleSet) {
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
     * Print the HealthRecord object as an output.
     *
     * @param healthRecord indicating a health record
     */
    private void dumpHealthRecord(HealthRecord healthRecord) {
        logger("Print health record summary information!");
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();
        if (healthRecord != null) {
            logger("\tHealthRecordIdentifier: " + healthRecord.getHealthRecordId() + "\n\tpackageName: "
                + healthRecord.getDataCollector().getPackageName() + "\n\tStartTime: "
                + dateFormat.format(healthRecord.getStartTime(TimeUnit.MILLISECONDS)) + " "
                + timeFormat.format(healthRecord.getStartTime(TimeUnit.MILLISECONDS)) + "\n\tEndTime: "
                + dateFormat.format(healthRecord.getEndTime(TimeUnit.MILLISECONDS)) + " "
                + timeFormat.format(healthRecord.getEndTime(TimeUnit.MILLISECONDS)) + "\n\tHealthRecordDataType: "
                + healthRecord.getDataCollector().getDataType().getName() + "\n\tHealthRecordDataCollectorId: "
                + healthRecord.getDataCollector().getDataStreamId() + "\n\tmetaData: " + healthRecord.getMetadata()
                + "\n\tFileValueMap: " + healthRecord.getFieldValues());

            if (healthRecord.getSubDataSummary() != null && !healthRecord.getSubDataSummary().isEmpty()) {
                showSamplePoints(healthRecord.getSubDataSummary());
            }
        }
    }

    /**
     * Print the SamplePoint in the SamplePointList object as an output.
     *
     * @param subDataSummary Indicates the list of sample data.
     */
    private void showSamplePoints(List<SamplePoint> subDataSummary) {
        SimpleDateFormat sDateFormat;
        sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (SamplePoint dp : subDataSummary) {
            showSamplePoint(sDateFormat, dp);
        }
    }

    private void showSamplePoint(SimpleDateFormat dateFormat, SamplePoint samplePoint) {
        logger("Sample point type: " + samplePoint.getDataType().getName());
        logger("Start: " + dateFormat.format(new Date(samplePoint.getStartTime(TimeUnit.MILLISECONDS))));
        logger("End: " + dateFormat.format(new Date(samplePoint.getEndTime(TimeUnit.MILLISECONDS))));
        for (Field field : samplePoint.getDataType().getFields()) {
            logger("Field: " + field.getName() + " Value: " + samplePoint.getFieldValue(field));
        }
        logger(System.lineSeparator());
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
