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
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.health.demo.R;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hihealth.BleController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.SensorsController;
import com.huawei.hms.hihealth.data.BleDeviceInfo;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.options.BleScanCallback;
import com.huawei.hms.hihealth.options.DataCollectorsOptions;
import com.huawei.hms.hihealth.options.OnSamplePointListener;
import com.huawei.hms.hihealth.options.SensorOptions;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SensorController Sample code
 *
 * @since 2020-03-18
 */
public class HiHealthKitSensorsControllerActivity extends AppCompatActivity {
    private static final String TAG = "SensorsControllerTest";

    private static final String SPLIT = "*******************************" + System.lineSeparator();

    // Context of the app
    private Context context;

    // Create a SensorsController object to obtain data.
    private SensorsController sensorsController;

    // Create a BleController object to scan external Bluetooth devices.
    private BleController bleController;

    // Create a BleDeviceInfo object to temporarily store the scanned devices.
    private BleDeviceInfo bleDeviceInfo;

    // Create a DataCollector object to temporarily store the external Bluetooth devices.
    private DataCollector dataCollector;

    // Text view for displaying operation information on the UI
    private TextView logInfoView;

    private boolean isHasFocus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hihealth_sensor);
        logInfoView = (TextView) findViewById(R.id.sensor_controller_log_info);
        logInfoView.setMovementMethod(ScrollingMovementMethod.getInstance());

        // Obtain SensorsController and BleControllerObtain first when accessing the UI.
        HiHealthOptions options = HiHealthOptions.builder().build();

        // Sign in to the HUAWEI ID.
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);

        // Obtain the SensorsController and BleController.
        sensorsController = HuaweiHiHealth.getSensorsController(this, signInHuaweiId);
        bleController = HuaweiHiHealth.getBleController(this, signInHuaweiId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        context = this;
    }

    /**
     * Sign in to your HUAWEI ID to obtain SensorsController and BleCtronller first.
     *
     * @param view UI Object
     */
    public void getSensorController(View view) {
        HiHealthOptions options = HiHealthOptions.builder().build();

        // Sign in to the HUAWEI ID.
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);

        // Obtain the SensorsController and BleController.
        sensorsController = HuaweiHiHealth.getSensorsController(this, signInHuaweiId);
        bleController = HuaweiHiHealth.getBleController(this, signInHuaweiId);
    }

    /**
     * Create a listener object to receive step count reports.
     */
    private OnSamplePointListener onSamplePointListener = new OnSamplePointListener() {
        @Override
        public void onSamplePoint(SamplePoint samplePoint) {
            // The step count, time, and type data reported by the pedometer is called back to the app through
            // samplePoint.
            showSamplePoint(samplePoint);
        }
    };

    /**
     * Register a listener to obtain the step count from the phone.
     *
     * @param view UI Object
     */
    public void registerSteps(View view) {
        if (sensorsController == null) {
            logger("SensorsController is null");
            logger(SPLIT);
            return;
        }

        // Build a SensorsOptions object to pass the data type (which is the total step count in this case).
        SensorOptions.Builder builder = new SensorOptions.Builder();
        builder.setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL);

        // Register a listener and adding the callback for registration success and failure.
        sensorsController.register(builder.build(), onSamplePointListener)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    logger("registerSteps successed... ");
                    logger(SPLIT);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    logger("registerSteps failed... ");
                    logger(SPLIT);
                }
            });
    }

    /**
     * Unregister the listener for the step count.
     *
     * @param view UI Object
     */
    public void unregisterSteps(View view) {
        if (sensorsController == null) {
            logger("SensorsController is null");
            logger(SPLIT);
            return;
        }

        // Unregister the listener for the step count.
        sensorsController.unregister(onSamplePointListener).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                logger("unregisterSteps successed ...");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                logger("unregisterSteps failed ..." + e.getMessage());
                logger(SPLIT);
            }
        });
    }

    /**
     * Enable Bluetooth scanning to scan for external Bluetooth devices capable of monitoring the heart rate.
     *
     * @param view UI Object
     */
    public void scanHeartRateDevices(View view) {
        if (bleController == null) {
            logger("BleController is null");
            logger(SPLIT);
            return;
        }

        logger("Scanning devices started");
        logger(SPLIT);

        // Pass the heart rate data type as an array. Multiple data types can be passed at a time. The scanning time is
        // set to 15 seconds.
        bleController.beginScan(Arrays.asList(DataType.DT_INSTANTANEOUS_HEART_RATE), 15, mBleCallback);
    }

    /**
     * Forcibly stop Bluetooth scanning.
     *
     * @param view UI Object
     */
    public void stopScanning(View view) {
        if (bleController == null) {
            logger("BleController is null");
            logger(SPLIT);
            return;
        }
        bleController.endScan(mBleCallback);
    }

    /**
     * Bluetooth scanning callback object
     */
    private BleScanCallback mBleCallback = new BleScanCallback() {
        @Override
        public void onDeviceDiscover(BleDeviceInfo bleDeviceInfo) {
            // Bluetooth devices detected during the scanning will be called back to the bleDeviceInfo object
            logger("onDeviceDiscover : " + bleDeviceInfo.getDeviceName());
            logger(SPLIT);

            // Save the scanned heart rate devices to the variables for later use.
            HiHealthKitSensorsControllerActivity.this.bleDeviceInfo = bleDeviceInfo;
        }

        @Override
        public void onScanEnd() {
            logger("onScanEnd  Scan called");
            logger(SPLIT);
        }
    };

    /**
     * Save the scanned heart rate devices to the local device for the listener that will be registered later to obtain
     * data.
     *
     * @param view UI Object
     */
    public void saveHeartRateDevice(View view) {
        if (bleController == null || bleDeviceInfo == null) {
            logger("BleController or BleDeviceInfo is null");
            logger(SPLIT);
            return;
        }

        bleController.saveDevice(bleDeviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("saveHeartRateDevice successed... ");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logger("saveHeartRateDevice failed... ");
                logger(SPLIT);
            }
        });
    }

    /**
     * List all external Bluetooth devices that have been saved to the local device.
     *
     * @param view UI Object
     */
    public void listMatchedDevices(View view) {
        if (bleController == null) {
            logger("SensorsController is null");
            logger(SPLIT);
            return;
        }

        Task<List<BleDeviceInfo>> bleDeviceInfoTask = bleController.getSavedDevices();
        bleDeviceInfoTask.addOnSuccessListener(new OnSuccessListener<List<BleDeviceInfo>>() {
            @Override
            public void onSuccess(List<BleDeviceInfo> bleDeviceInfos) {
                // bleDeviceInfos contains the list of the saved devices.
                for (BleDeviceInfo bleDeviceInfo : bleDeviceInfos) {
                    logger("Matched BLE devices:" + bleDeviceInfo.getDeviceName());
                    logger(SPLIT);
                }
            }
        });
    }

    /**
     * Find available data collectors from the saved devices in the list.
     *
     * @param view UI Object
     */
    public void findDataCollectors(View view) {
        if (sensorsController == null) {
            logger("SensorsController is null");
            logger(SPLIT);
            return;
        }

        // Build a DataCollectorsOptions object and passing the type of device we are looking for (which is heart rate
        // devices in this case).
        DataCollectorsOptions dataCollectorsOptions =
            new DataCollectorsOptions.Builder().setDataTypes(DataType.DT_INSTANTANEOUS_HEART_RATE).build();

        // Use dataCollectorsOptions as a parameter to return available heart rate devices.
        sensorsController.getDataCollectors(dataCollectorsOptions)
            .addOnSuccessListener(new OnSuccessListener<List<DataCollector>>() {
                @Override
                public void onSuccess(List<DataCollector> dataCollectors) {
                    // dataCollectors contains the returned available data collectors.
                    for (DataCollector dataCollector : dataCollectors) {
                        logger("Available data collector:" + dataCollector.getDataCollectorName());
                        logger(SPLIT);

                        // Save the heart rate data collectors for later use when registering the listener.
                        HiHealthKitSensorsControllerActivity.this.dataCollector = dataCollector;
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    logger("findDataCollectors failed... ");
                    logger(SPLIT);
                }
            });
    }

    /**
     * Register a listener for the heart rate device.
     *
     * @param view UI Object
     */
    public void registerHeartRate(View view) {
        if (sensorsController == null) {
            logger("SensorsController is null");
            logger(SPLIT);
            return;
        }

        sensorsController.register(
            // Build a SensorsOptions object and passing the data type, data collectors, and sampling rate.
            // The data type is mandatory.
            new SensorOptions.Builder().setDataType(DataType.DT_INSTANTANEOUS_HEART_RATE)
                .setDataCollector(dataCollector)
                // Set the sampling rate to 1 second.
                .setCollectionRate(1, TimeUnit.SECONDS)
                .build(),
            heartrateListener).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    logger("registerHeartRate successed... ");
                    logger(SPLIT);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logger("registerHeartRate failed... ");
                    logger(SPLIT);
                }
            });
    }

    /**
     * Create a listener object for heart rate data. The received heart rate data will be called back to onSamplePoint.
     */
    private OnSamplePointListener heartrateListener = new OnSamplePointListener() {
        @Override
        public void onSamplePoint(SamplePoint samplePoint) {
            logger("Heart rate received " + samplePoint.getFieldValue(Field.FIELD_BPM).toString());
            logger(SPLIT);
        }
    };

    /**
     * Unregister the listener for the heart rate data.
     *
     * @param view UI Object
     */
    public void unregisterHeartRate(View view) {
        if (sensorsController == null) {
            logger("SensorsController is null");
            logger(SPLIT);
            return;
        }

        sensorsController.unregister(heartrateListener).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                logger("unregisterHeartRate successed... ");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logger("unregisterHeartRate failed... ");
                logger(SPLIT);
            }
        });
    }

    /**
     * Delete the heart rate device information that has been saved.
     *
     * @param view UI Object
     */
    public void removeHeartRateDevice(View view) {
        if (bleController == null || bleDeviceInfo == null) {
            logger("BleController or BleDeviceInfo is null");
            logger(SPLIT);
            return;
        }

        // Pass the saved Bluetooth device information object to delete the information.
        bleController.deleteDevice(bleDeviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logger("removeHeartRateDevice successed... ");
                logger(SPLIT);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logger("removeHeartRateDevice failed... ");
                logger(SPLIT);
            }
        });
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param samplePoint Reported data
     */
    private void showSamplePoint(SamplePoint samplePoint) {
        if (samplePoint != null) {
            logger("Sample point type: " + samplePoint.getDataType().getName());
            for (Field field : samplePoint.getDataType().getFields()) {
                logger("Field: " + field.getName() + " Value: " + samplePoint.getFieldValue(field));
            }
        } else {
            logger("samplePoint is null!! ");
            logger(SPLIT);
        }
    }

    /**
     * TextView to send the operation result logs to the logcat and to the UI
     *
     * @param string Log string
     */
    private void logger(String string) {
        Log.i(TAG, string);
        if (isHasFocus) {
            logInfoView.append(string + System.lineSeparator());
            int offset = logInfoView.getLineCount() * logInfoView.getLineHeight();
            if (offset > logInfoView.getHeight()) {
                logInfoView.scrollTo(0, offset - logInfoView.getHeight());
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isHasFocus = hasFocus;
    }
}
