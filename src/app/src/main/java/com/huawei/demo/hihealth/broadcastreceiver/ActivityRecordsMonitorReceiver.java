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

import com.huawei.hms.hihealth.data.ActivityRecord;

/**
 * ActivityRecord status receiving and processing class.
 *
 * @since 2020-03-19
 */
public class ActivityRecordsMonitorReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        if (intent != null) {
            String msg = "";
            ActivityRecord activityRecord = ActivityRecord.extract(intent);
            msg += "receiver monitor ActivityRecord happen " + intent.getAction();
            msg += "\tActivityRecord info is：  " + activityRecord.toString();
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}
