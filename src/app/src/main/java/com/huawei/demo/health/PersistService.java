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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.huawei.health.demo.R;
import com.huawei.hms.hihealth.AutoRecorderController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * Defining a Frontend Service
 *
 * @since 2020-09-05
 */
public class PersistService extends Service {
    private static final String TAG = "PersistService";

    // HMS Health AutoRecorderController
    private AutoRecorderController autoRecorderController;

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initAutoRecorderController();
        Log.i(TAG, "service is create.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Invoke the real-time callback interface of the HealthKit.
        getRemoteService();
        // Binding a notification bar
        getNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * init AutoRecorderController
     */
    private void initAutoRecorderController() {
        HiHealthOptions options = HiHealthOptions.builder().build();
        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
        autoRecorderController = HuaweiHiHealth.getAutoRecorderController(context, signInHuaweiId);
    }

    /**
     * Callback Interface for Starting the Total Step Count
     */
    private void getRemoteService() {
        if (autoRecorderController == null) {
            initAutoRecorderController();
        }
        // Start recording real-time steps.
        autoRecorderController.startRecord(DataType.DT_CONTINUOUS_STEPS_TOTAL, samplePoint -> {
            // The step count, time, and type data reported by the pedometer is called back to the app through
            // samplePoint.
            Intent intent = new Intent();
            intent.putExtra("SamplePoint", samplePoint);
            intent.setAction("HealthKitService");
            // Transmits service data to activities through broadcast.
            sendBroadcast(intent);
        }).addOnSuccessListener(aVoid -> Log.i(TAG, "record steps success... "))
                .addOnFailureListener(e -> Log.i(TAG, "report steps failed... "));
    }

    /**
     * Bind the service to the notification bar so that the service can be changed to a foreground service.
     */
    private void getNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, "1").setContentTitle("Real-time step counting")
            .setContentText("Real-time step counting...")
            .setWhen(System.currentTimeMillis())
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, new Intent(this, HealthKitAutoRecorderControllerActivity.class), 0))
            .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                new NotificationChannel("1", "subscribeName", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            notificationManager.createNotificationChannel(channel);
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "PersistService is destroy.");
    }
}
