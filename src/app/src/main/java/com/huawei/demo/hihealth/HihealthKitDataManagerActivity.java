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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.demo.hihealth.broadcastreceiver.DataRegisterReceiver;
import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.DataController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HiHealthStatusCodes;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.options.DeleteOptions;
import com.huawei.hms.hihealth.options.ModifyDataMonitorOptions;
import com.huawei.hms.hihealth.options.ReadOptions;
import com.huawei.hms.hihealth.options.UpdateOptions;
import com.huawei.hms.hihealth.result.ReadReply;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sample code for managing fitness and health data
 *
 * @since 2020-03-17
 */
public class HihealthKitDataManagerActivity extends AppCompatActivity {
    private static final String TAG = "DataManager";

    // Object of controller for fitness and health data, providing APIs for read/write, batch read/write, and listening
    private DataController dataController;

    // Internal context object of the activity
    private Context context;

    // PendingIntent, required when registering or unregistering a listener within the data controller
    private PendingIntent pendingIntent;

    // TextView for displaying operation information on the UI
    private TextView logInfoView;

    // Line separators for the display on the UI
    private String tag = "*******************************" + System.lineSeparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hihealth_datamanager);
        context = this;
        logInfoView = (TextView) findViewById(R.id.data_controller_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());
        initDataController();
    }

    /**
     * Initialize a data controller object.
     */
    private void initDataController() {
        // Obtain and set the read & write permissions for DT_CONTINUOUS_STEPS_DELTA and DT_INSTANTANEOUS_HEIGHT.
        // Use the obtained permissions to obtain the data controller object.
        HiHealthOptions hiHealthOptions = HiHealthOptions.builder()
            .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_READ)
            .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_WRITE)
            .addDataType(DataType.DT_INSTANTANEOUS_HEIGHT, HiHealthOptions.ACCESS_READ)
            .addDataType(DataType.DT_INSTANTANEOUS_HEIGHT, HiHealthOptions.ACCESS_WRITE)
            .build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions);
        dataController = HuaweiHiHealth.getDataController(context, signInHuaweiId);
    }

    /**
     * Use the data controller to add a sampling dataset.
     *
     * @param view (indicating a UI object)
     * @throws ParseException (indicating a failure to parse the time string)
     */
    public void insertData(View view) throws ParseException {
        // 1. Build a DataCollector object.
        DataCollector dataCollector = new DataCollector.Builder().setPackageName(context)
            .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();

        // 2. Create a sampling dataset set based on the data collector.
        final SampleSet sampleSet = SampleSet.create(dataCollector);

        // 3. Build the start time, end time, and incremental step count for a DT_CONTINUOUS_STEPS_DELTA sampling point.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date startDate = dateFormat.parse("2020-03-17 09:00:00");
        Date endDate = dateFormat.parse("2020-03-17 09:05:00");
        int stepsDelta = 1000;

        // 4. Build a DT_CONTINUOUS_STEPS_DELTA sampling point.
        SamplePoint samplePoint = sampleSet.createSamplePoint()
            .setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
        samplePoint.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(stepsDelta);

        // 5. Save a DT_CONTINUOUS_STEPS_DELTA sampling point to the sampling dataset.
        // You can repeat steps 3 through 5 to add more sampling points to the sampling dataset.
        sampleSet.addSample(samplePoint);

        // 6. Call the data controller to insert the sampling dataset into the Health platform.
        Task<Void> insertTask = dataController.insert(sampleSet);

        // 7. Calling the data controller to insert the sampling dataset is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the data insertion is successful or not.
        insertTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("Success insert an SampleSet into HMS core");
                showSampleSet(sampleSet);
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "insert");
            }
        });
    }

    /**
     * Use the data controller to delete the sampling data by specific criteria.
     *
     * @param view (indicating a UI object)
     * @throws ParseException (indicating a failure to parse the time string)
     */
    public void deleteData(View view) throws ParseException {
        // 1. Build the condition for data deletion: a DataCollector object.
        DataCollector dataCollector = new DataCollector.Builder().setPackageName(context)
            .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();

        // 2. Build the time range for the deletion: start time and end time.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date startDate = dateFormat.parse("2020-03-17 09:00:00");
        Date endDate = dateFormat.parse("2020-03-17 09:05:00");

        // 3. Build a parameter object as the conditions for the deletion.
        DeleteOptions deleteOptions = new DeleteOptions.Builder().addDataCollector(dataCollector)
            .setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS)
            .build();

        // 4. Use the specified condition deletion object to call the data controller to delete the sampling dataset.
        Task<Void> deleteTask = dataController.delete(deleteOptions);

        // 5. Calling the data controller to delete the sampling dataset is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the data deletion is successful or not.
        deleteTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("Success delete sample data from HMS core");
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "delete");
            }
        });
    }

    /**
     * Use the data controller to modify the sampling data by specific criteria.
     *
     * @param view (indicating a UI object)
     * @throws ParseException (indicating a failure to parse the time string)
     */
    public void updateData(View view) throws ParseException {
        // 1. Build the condition for data update: a DataCollector object.
        DataCollector dataCollector = new DataCollector.Builder().setPackageName(context)
            .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();

        // 2. Build the sampling dataset for the update: create a sampling dataset
        // for the update based on the data collector.
        SampleSet sampleSet = SampleSet.create(dataCollector);

        // 3. Build the start time, end time, and incremental step count for
        // a DT_CONTINUOUS_STEPS_DELTA sampling point for the update.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date startDate = dateFormat.parse("2020-03-17 09:00:00");
        Date endDate = dateFormat.parse("2020-03-17 09:05:00");
        int stepsDelta = 2000;

        // 4. Build a DT_CONTINUOUS_STEPS_DELTA sampling point for the update.
        SamplePoint samplePoint = sampleSet.createSamplePoint()
            .setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS);
        samplePoint.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(stepsDelta);

        // 5. Add an updated DT_CONTINUOUS_STEPS_DELTA sampling point to the sampling dataset for the update.
        // You can repeat steps 3 through 5 to add more updated sampling points to the sampling dataset for the update.
        sampleSet.addSample(samplePoint);

        // 6. Build a parameter object for the update.
        // Note: (1) The start time of the modified object updateRequest cannot be greater than the minimum
        // value of the start time of all sample data points in the modified data sample set
        // (2) The end time of the modified object updateRequest cannot be less than the maximum value of the
        // end time of all sample data points in the modified data sample set
        UpdateOptions updateRequest =
            new UpdateOptions.Builder().setTimeInterval(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS)
                .setSampleSet(sampleSet)
                .build();

        // 7. Use the specified parameter object for the update to call the
        // data controller to modify the sampling dataset.
        Task<Void> updateTask = dataController.update(updateRequest);

        // 8. Calling the data controller to modify the sampling dataset is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the data update is successful or not.
        updateTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("Success update sample data from HMS core");
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "update");
            }
        });
    }

    /**
     * Use the data controller to query the sampling dataset by specific criteria.
     *
     * @param view (indicating a UI object)
     * @throws ParseException (indicating a failure to parse the time string)
     */
    public void readData(View view) throws ParseException {
        // 1. Build the condition for data query: a DataCollector object.
        DataCollector dataCollector = new DataCollector.Builder().setPackageName(context)
            .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();

        // 2. Build the time range for the query: start time and end time.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date startDate = dateFormat.parse("2020-03-17 09:00:00");
        Date endDate = dateFormat.parse("2020-03-17 09:05:00");

        // 3. Build the condition-based query objec
        ReadOptions readOptions = new ReadOptions.Builder().read(dataCollector)
            .setTimeRange(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS)
            .build();

        // 4. Use the specified condition query object to call the data controller to query the sampling dataset.
        Task<ReadReply> readReplyTask = dataController.read(readOptions);

        // 5. Calling the data controller to query the sampling dataset is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the data query is successful or not.
        readReplyTask.addOnSuccessListener(new OnSuccessListener<ReadReply>() {
            @Override
            public void onSuccess(ReadReply readReply) {
                logger("Success read an SampleSets from HMS core");
                for (SampleSet sampleSet : readReply.getSampleSets()) {
                    showSampleSet(sampleSet);
                }
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "read");
            }
        });
    }

    /**
     * Use the data controller to query the summary data of the current day by data type.
     *
     * @param view (indicating a UI object)
     */
    public void readToday(View view) {
        // 1. Use the specified data type (DT_CONTINUOUS_STEPS_DELTA) to call the data controller to query
        // the summary data of this data type of the current day.
        Task<SampleSet> todaySummationTask = dataController.readTodaySummation(DataType.DT_CONTINUOUS_STEPS_DELTA);

        // 2. Calling the data controller to query the summary data of the current day is an
        // asynchronous operation. Therefore, a listener needs to be registered to monitor whether
        // the data query is successful or not.
        // Note: In this example, the inserted data time is fixed at 2020-03-17 09:05:00.
        // When commissioning the API, you need to change the inserted data time to the current date
        // for data to be queried.
        todaySummationTask.addOnSuccessListener(new OnSuccessListener<SampleSet>() {
            @Override
            public void onSuccess(SampleSet sampleSet) {
                logger("Success read today summation from HMS core");
                if (sampleSet != null) {
                    showSampleSet(sampleSet);
                }
                logger(tag);
            }
        });
        todaySummationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "readTodaySummation");
            }
        });
    }

    /**
     * Use the data controller to query the summary data of the current day on the local device by data type.
     *
     * @param view (indicating a UI object)
     */
    public void readTodayDevice(View view) {
        // 1. Use the specified data type (DT_CONTINUOUS_STEPS_DELTA) to call the data controller to query
        // the summary data of this data type of the current day.
        Task<SampleSet> todaySummationTask =
            dataController.readTodaySummationFromDevice(DataType.DT_CONTINUOUS_STEPS_DELTA);

        // 2. Calling the data controller to query the summary data of the current day is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the data query is successful or not.
        todaySummationTask.addOnSuccessListener(new OnSuccessListener<SampleSet>() {
            @Override
            public void onSuccess(SampleSet sampleSet) {
                logger("Success read today device summation from HMS core");
                if (sampleSet != null) {
                    showSampleSet(sampleSet);
                }
                logger(tag);
            }
        });
        todaySummationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "readTodaySummationFromDevice");
            }
        });
    }

    /**
     * Use the data controller to register a listener for data changes.
     *
     * @param view (indicating a UI object)
     */
    public void registerListener(View view) {
        if (pendingIntent != null) {
            logger("There is already an listener, no need to re listener");
            logger(tag);
            return;
        }

        // 1. Build a DataController object. Listeners can be registered for updates of the following six data types.
        // Basic metabolic rate per day (unit: kcal): DataType.DT_INSTANTANEOUS_CALORIES_BMR
        // Body fat rate: DataType.DT_INSTANTANEOUS_BODY_FAT_RATE
        // Height (unit: meter): DataType.DT_INSTANTANEOUS_HEIGHT
        // Water taken over a single drink (unit: liter): DataType.DT_INSTANTANEOUS_HYDRATE
        // Nutrient intake over a meal: DataType.DT_INSTANTANEOUS_NUTRITION_FACTS
        // Weight (unit: kg): DataType.DT_INSTANTANEOUS_BODY_WEIGHT
        DataCollector dataCollector = new DataCollector.Builder().setPackageName(context)
            .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();

        // 2. Build a listening object for data changes: PendingIntent.
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, DataRegisterReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT);

        // 3. Build a parameter object for registering a listener for data changes.
        ModifyDataMonitorOptions dataMonitorOptions =
            new ModifyDataMonitorOptions.Builder().setModifyDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
                .setModifyDataCollector(dataCollector)
                .setModifyDataIntent(pendingIntent)
                .build();

        // 4. Use the specified parameter object for registering a listener
        // for data changes to call the data controller to add the listener.
        Task<Void> registerTask = dataController.registerModifyDataMonitor(dataMonitorOptions);

        // 5. Calling the data controller to register a listener is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the registration is successful or not.
        registerTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("Register Data Update Listener Success");
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "registerModifyDataMonitor");
                pendingIntent = null;
            }
        });
    }

    /**
     * Use the data controller to unregister a listener for data changes.
     *
     * @param view (indicating a UI object)
     */
    public void unregisterListener(View view) {
        if (pendingIntent == null) {
            logger("There is no listener, no need to un listener");
            logger(tag);
            return;
        }

        // 1. Use the specified parameter object for unregistering a listener
        // for data changes to call the data controller to cancel the listener.
        Task<Void> task = dataController.unregisterModifyDataMonitor(pendingIntent);

        // 2. Calling the data controller to unregister a listener is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the unregistration is successful or not.
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("Unregister Data Update Listener Success: ");
                logger(tag);
                pendingIntent = null;
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "unregisterModifyDataMonitor");
            }
        });
    }

    /**
     * Sync data between the device and cloud.
     *
     * @param view (indicating a UI object)
     */
    public void syncCloudData(View view) {
        // 1. Call the syncAll method of the data controller to sync data.
        Task<Void> syncTask = dataController.syncAll();

        // 2. Calling the data controller to sync data between the device and cloud is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the syncing is successful or not.
        syncTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("synAll success");
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "syncAll");
            }
        });
    }

    /**
     * Clear all user data from the device and cloud.
     *
     * @param view (indicating a UI object)
     */
    public void clearCloudData(View view) {
        // 1. Call the clearAll method of the data controller to delete data
        // inserted by the current app from the device and cloud.
        Task<Void> clearTask = dataController.clearAll();

        // 2. Calling the data controller to clear user data from the device and cloud is an asynchronous operation.
        // Therefore, a listener needs to be registered to monitor whether the clearance is successful or not.
        clearTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                logger("clearAll success");
                logger(tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                printFailureMessage(e, "clearAll");
            }
        });
    }

    /**
     * Trigger the listener for data changes for testing purposes by inserting a data entry.
     *
     * @param view (indicating a UI object)
     */
    public void insertListenerData(View view) {
        if (pendingIntent == null) {
            logger("There is no listener, no need to insert sample point");
            logger(tag);
            return;
        }

        DataCollector dataCollector = new DataCollector.Builder().setPackageName(context)
            .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build();
        SampleSet sampleSet = SampleSet.create(dataCollector);
        SamplePoint samplePoint = sampleSet.createSamplePoint()
            .setTimeInterval(System.currentTimeMillis(), System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        samplePoint.getFieldValue(Field.FIELD_HEIGHT).setFloatValue(1.56f);
        sampleSet.addSample(samplePoint);
        dataController.insert(sampleSet);
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param sampleSet (indicating the sampling dataset)
     */
    private void showSampleSet(SampleSet sampleSet) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (SamplePoint samplePoint : sampleSet.getSamplePoints()) {
            logger("Sample point type: " + samplePoint.getDataType().getName());
            logger("Start: " + dateFormat.format(new Date(samplePoint.getStartTime(TimeUnit.MILLISECONDS))));
            logger("End: " + dateFormat.format(new Date(samplePoint.getEndTime(TimeUnit.MILLISECONDS))));
            for (Field field : samplePoint.getDataType().getFields()) {
                logger("Field: " + field.getName() + " Value: " + samplePoint.getFieldValue(field));
            }
        }
    }

    /**
     * Printout failure exception error code and error message
     *
     * @param e Exception object
     * @param api Interface name
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
     * TextView to send the operation result logs to the logcat and to the UI
     *
     * @param string (indicating the log string)
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
