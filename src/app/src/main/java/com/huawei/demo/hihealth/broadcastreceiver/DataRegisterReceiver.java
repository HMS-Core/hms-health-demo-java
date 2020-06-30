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

package com.huawei.demo.hihealth.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.huawei.hms.hihealth.data.DataModifyInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Platform data management monitor
 *
 * @since 2020-03-17
 */
public class DataRegisterReceiver extends BroadcastReceiver {
    private static final String TAG = "DataRegisterReceiver";

    private static final String SPLIT = System.lineSeparator();

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        DataModifyInfo updateNotification = DataModifyInfo.getModifyInfo(intent);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String msg = "detected a data has been modified:" + SPLIT;
        if (updateNotification != null) {
            if (updateNotification.getDataType() != null) {
                msg += "data type：" + updateNotification.getDataType().getName() + SPLIT;
            } else {
                msg += "data type：" + SPLIT;
            }
            if (updateNotification.getDataCollector() != null) {
                msg += "data collector：" + updateNotification.getDataCollector().toString() + SPLIT;
            } else {
                msg += "data collector：" + SPLIT;
            }
            msg += "start time："
                + dateFormat.format(new Date(updateNotification.getModifyStartTime(TimeUnit.MILLISECONDS))) + SPLIT;
            msg += "end time：" + dateFormat.format(new Date(updateNotification.getModifyEndTime(TimeUnit.MILLISECONDS)))
                + SPLIT;
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}
